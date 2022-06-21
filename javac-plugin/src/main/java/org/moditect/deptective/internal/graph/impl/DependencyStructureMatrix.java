/*
 *  Copyright 2019-2022 The ModiTect authors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.moditect.deptective.internal.graph.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.graph.IDependencyStructureMatrix;
import org.moditect.deptective.internal.graph.INodeSorter;
import org.moditect.deptective.internal.graph.INodeSorter.SortResult;
import org.moditect.deptective.internal.graph.Node;

public class DependencyStructureMatrix<T extends Node<T>> implements IDependencyStructureMatrix<T> {

    private List<List<T>> cycles;

    private List<T> nodes;

    private List<Dependency<T>> upwardDependencies;

    public DependencyStructureMatrix(Collection<T> nodes) {
        initialize(nodes);
    }

    @Override
    public List<Dependency<T>> getUpwardDependencies() {
        return upwardDependencies;
    }

    @Override
    public int getWeight(int i, int j) {

        if (i < 0 || i >= nodes.size() || j < 0 || j >= nodes.size()) {
            return -1;
        }

        Dependency<?> dependency = nodes.get(i).getOutgoingDependencyTo(nodes.get(j));

        return dependency != null ? dependency.getAggregatedWeight() : 0;
    }

    @Override
    public List<T> getOrderedNodes() {
        return nodes;
    }

    @Override
    public boolean isRowInCycle(int i) {
        return isCellInCycle(i, i);
    }

    @Override
    public boolean isCellInCycle(int i, int j) {

        if (i < 0 || i >= nodes.size() || j < 0 || j >= nodes.size()) {
            return false;
        }

        for (List<T> cycle : cycles) {
            if (cycle.size() > 1 && cycle.contains(nodes.get(i)) && cycle.contains(nodes.get(j))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<List<T>> getCycles() {
        return cycles;
    }

    private void initialize(Collection<T> unorderedArtifacts) {
        Objects.requireNonNull(unorderedArtifacts);

        upwardDependencies = new ArrayList<>();

        List<List<T>> c = GraphUtils.detectStronglyConnectedComponents(unorderedArtifacts);
        INodeSorter artifactSorter = new FastFasSorter();
        for (List<T> cycle : c) {
            SortResult<T> sortResult = artifactSorter.sort(cycle);
            cycle.clear();
            cycle.addAll(sortResult.getOrderedNodes());
            upwardDependencies.addAll(sortResult.getUpwardsDependencies());
        }

        List<T> orderedArtifacts = new ArrayList<>();

        // optimize: un-cycled artifacts without dependencies first
        for (List<T> artifactList : c) {
            if (artifactList.size() == 1 && !artifactList.get(0).hasOutgoingDependencies()) {
                orderedArtifacts.add(artifactList.get(0));
            }
        }

        for (List<T> cycle : c) {
            for (T node : cycle) {
                if (!orderedArtifacts.contains(node)) {
                    orderedArtifacts.add(node);
                }
            }
        }
        Collections.reverse(orderedArtifacts);
        nodes = orderedArtifacts;

        //
        cycles = c.stream().filter(nodeList -> nodeList.size() > 1).collect(Collectors.toList());
    }
}

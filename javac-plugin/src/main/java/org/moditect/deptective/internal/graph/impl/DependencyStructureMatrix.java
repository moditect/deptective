/**
 *  Copyright 2019 The ModiTect authors
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.graph.IDependencyStructureMatrix;
import org.moditect.deptective.internal.graph.INodeSorter;
import org.moditect.deptective.internal.graph.INodeSorter.SortResult;
import org.moditect.deptective.internal.graph.Node;

public class DependencyStructureMatrix implements IDependencyStructureMatrix {

    private List<List<Node>> cycles;

    private List<Node> nodes;

    private List<Dependency> upwardDependencies;

    public DependencyStructureMatrix(Collection<Node> nodes) {
        initialize(nodes);
    }

    @Override
    public List<Dependency> getUpwardDependencies() {
        return upwardDependencies;
    }

    @Override
    public int getWeight(int i, int j) {

        if (i < 0 || i >= nodes.size() || j < 0 || j >= nodes.size()) {
            return -1;
        }

        Dependency dependency = nodes.get(i).getOutgoingDependencyTo(nodes.get(j));

        return dependency != null ? dependency.getAggregatedWeight() : 0;
    }

    @Override
    public List<Node> getOrderedNodes() {
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

        for (List<Node> cycle : cycles) {
            if (cycle.size() > 1 && cycle.contains(nodes.get(i)) && cycle.contains(nodes.get(j))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<List<Node>> getCycles() {
        return cycles;
    }

    private void initialize(Collection<Node> unorderedArtifacts) {

        checkNotNull(unorderedArtifacts);

        upwardDependencies = new ArrayList<>();

        List<List<Node>> c = GraphUtils.detectStronglyConnectedComponents(unorderedArtifacts);
        INodeSorter artifactSorter = new FastFasSorter();
        for (List<Node> cycle : c) {
            SortResult sortResult = artifactSorter.sort(cycle);
            cycle.clear();
            cycle.addAll(sortResult.getOrderedNodes());
            upwardDependencies.addAll(sortResult.getUpwardsDependencies());
        }

        List<Node> orderedArtifacts = new ArrayList<>();

        // optimize: un-cycled artifacts without dependencies first
        for (List<Node> artifactList : c) {
            if (artifactList.size() == 1 && !artifactList.get(0).hasOutgoingDependencies()) {
                orderedArtifacts.add(artifactList.get(0));
            }
        }

        for (List<Node> cycle : c) {
            for (Node node : cycle) {
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

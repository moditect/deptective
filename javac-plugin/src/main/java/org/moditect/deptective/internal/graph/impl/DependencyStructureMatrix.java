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
import java.util.LinkedList;
import java.util.List;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.IDependencyStructureMatrix;
import org.moditect.deptective.internal.graph.INodeSorter.SortResult;
import org.moditect.deptective.internal.graph.Node;
import org.moditect.deptective.internal.graph.INodeSorter;

public class DependencyStructureMatrix implements IDependencyStructureMatrix {

	private List<List<Node>> cycles;

	private int[][] cycleArray;

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
	public int[][] getCycleArray() {
		return cycleArray;
	}

	private void initialize(Collection<Node> unorderedArtifacts) {

		checkNotNull(unorderedArtifacts);
		
		upwardDependencies = new ArrayList<>();

		cycles = new Tarjan<Node>().detectStronglyConnectedComponents(unorderedArtifacts);
		INodeSorter artifactSorter = new FastFasSorter();
		for (List<Node> cycle : cycles) {
			if (cycle.size() > 1) {
				SortResult sortResult = artifactSorter.sort(cycle);
				cycle = sortResult.getOrderedNodes();
				upwardDependencies.addAll(sortResult.getUpwardsDependencies());
			}
		}

		List<Node> orderedArtifacts = new ArrayList<>();

		// optimize: un-cycled artifacts without dependencies first
		for (List<Node> artifactList : cycles) {
			if (artifactList.size() == 1 && !artifactList.get(0).hasOutgoingDependencies()) {
				orderedArtifacts.add(artifactList.get(0));
			}
		}

		for (List<Node> cycle : cycles) {
			for (Node node : cycle) {
				if (!orderedArtifacts.contains(node)) {
					orderedArtifacts.add(node);
				}
			}
		}
		Collections.reverse(orderedArtifacts);
		nodes = orderedArtifacts;

		List<int[]> cyc = new LinkedList<int[]>();
		for (List<Node> artifactList : cycles) {
			if (artifactList.size() > 1) {
				int[] cycle = new int[artifactList.size()];
				for (int i = 0; i < cycle.length; i++) {
					cycle[cycle.length - (i + 1)] = orderedArtifacts.indexOf(artifactList.get(i));
				}
				cyc.add(cycle);
			}
		}

		cycleArray = cyc.toArray(new int[0][0]);
	}
}
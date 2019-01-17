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

import org.moditect.deptective.internal.graph.IDependency;
import org.moditect.deptective.internal.graph.IDependencyStructureMatrix;
import org.moditect.deptective.internal.graph.INode;
import org.moditect.deptective.internal.graph.INodeSorter;
import org.moditect.deptective.internal.graph.INodeSorter.SortResult;

/**
 * <p>
 * </p>
 * 
 * @author Gerd W&uuml;therich (gerd@gerd-wuetherich.de)
 */
public class DependencyStructureMatrix<N extends INode, D extends IDependency> implements IDependencyStructureMatrix<N, D> {

	/** - */
	private List<List<N>> _cycles;

	/** - */
	private int[][] _cycleArray;

	/** - */
	private List<N> _nodes;
	
	/** - */
	private List<D> _upwardDependencies;

	/**
	 * <p>
	 * Creates a new instance of type {@link DependencyStructureMatrix}.
	 * </p>
	 * 
	 * @param unorderedArtifacts
	 */
	public DependencyStructureMatrix(Collection<N> nodes) {
		initialize(nodes);
	}
	
	@Override
	public List<D> getUpwardDependencies() {
		return _upwardDependencies;
	}

	@Override
	public int getWeight(int i, int j) {

		//
		if (i < 0 || i >= _nodes.size() || j < 0 || j >= _nodes.size()) {
			return -1;
		}

		//
		IDependency dependency = _nodes.get(i).getOutgoingDependencyTo(_nodes.get(j));

		//
		return dependency != null ? dependency.getAggregatedWeight() : 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<N> getOrderedNodes() {
		return _nodes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRowInCycle(int i) {
		return isCellInCycle(i, i);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCellInCycle(int i, int j) {

		//
		if (i < 0 || i >= _nodes.size() || j < 0 || j >= _nodes.size()) {
			return false;
		}

		//
		for (List<N> cycle : _cycles) {
			if (cycle.size() > 1 && cycle.contains(_nodes.get(i)) && cycle.contains(_nodes.get(j))) {
				return true;
			}
		}

		//
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int[][] getCycleArray() {
		return _cycleArray;
	}

	/**
	 * 
	 * @param unorderedArtifacts
	 */
	private void initialize(Collection<N> unorderedArtifacts) {

		checkNotNull(unorderedArtifacts);
		
		_upwardDependencies = new ArrayList<>();

		_cycles = new Tarjan<N>().detectStronglyConnectedComponents(unorderedArtifacts);
		INodeSorter<N, D> artifactSorter = new FastFasSorter<N, D>();
		for (List<N> cycle : _cycles) {
			if (cycle.size() > 1) {
				SortResult<N, D> sortResult = artifactSorter.sort(cycle);
				cycle = sortResult.getOrderedNodes();
				_upwardDependencies.addAll(sortResult.getUpwardsDependencies());
			}
		}

		//
		List<N> orderedArtifacts = new ArrayList<>();

		// optimize: un-cycled artifacts without dependencies first
		for (List<N> artifactList : _cycles) {
			if (artifactList.size() == 1 && !artifactList.get(0).hasOutgoingDependencies()) {
				orderedArtifacts.add(artifactList.get(0));
			}
		}

		//
		for (List<N> cycle : _cycles) {
			for (N node : cycle) {
				if (!orderedArtifacts.contains(node)) {
					orderedArtifacts.add(node);
				}
			}
		}
		Collections.reverse(orderedArtifacts);
		_nodes = orderedArtifacts;

		//
		List<int[]> cycles = new LinkedList<int[]>();
		for (List<N> artifactList : _cycles) {
			if (artifactList.size() > 1) {
				int[] cycle = new int[artifactList.size()];
				for (int i = 0; i < cycle.length; i++) {
					cycle[cycle.length - (i + 1)] = orderedArtifacts.indexOf(artifactList.get(i));
				}
				cycles.add(cycle);
			}
		}

		_cycleArray = cycles.toArray(new int[0][0]);
	}
}
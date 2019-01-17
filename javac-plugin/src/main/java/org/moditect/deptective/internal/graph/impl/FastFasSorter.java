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

import java.util.ArrayList;
import java.util.List;

import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.graph.IDependency;
import org.moditect.deptective.internal.graph.INode;
import org.moditect.deptective.internal.graph.INodeSorter;

public class FastFasSorter<T extends INode, D extends IDependency> implements INodeSorter<T, D> {

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public SortResult<T, D> sort(List<T> artifacts) {

		// we have to compute the adjacency matrix first
		int[][] adjacencyMatrix = GraphUtils.computeAdjacencyMatrix(artifacts);

		// the ordered sequence (highest first!)
		FastFAS fastFAS = new FastFAS(adjacencyMatrix);
		int[] ordered = fastFAS.getOrderedSequence();

		// Bubbles
		for (int outerIndex = 1; outerIndex < ordered.length; outerIndex++) {
			for (int index = outerIndex; index >= 1; index--) {

				//
				if (adjacencyMatrix[ordered[index]][ordered[index
						- 1]] > adjacencyMatrix[ordered[index - 1]][ordered[index]]) {

					// swap...
					int temp = ordered[index];
					ordered[index] = ordered[index - 1];
					ordered[index - 1] = temp;

				} else {

					// stop bubbling...
					break;
				}
			}
		}

		// reverse it
		ordered = FastFAS.reverse(ordered);

		// create the result list
		List<T> resultNodes = new ArrayList<T>(artifacts.size());
		for (int index : ordered) {
			resultNodes.add(artifacts.get(index));
		}

		//
		List<D> upwardsDependencies = new ArrayList<>();
		for (Integer[] values : fastFAS.getSkippedEdge()) {
			INode source = artifacts.get(values[0]);
			INode target = artifacts.get(values[1]);
			upwardsDependencies.add((D) source.getOutgoingDependencyTo(target));
		}

		//
		return new SortResult() {

			@Override
			public List getOrderedNodes() {
				return resultNodes;
			}

			@Override
			public List getUpwardsDependencies() {
				return upwardsDependencies;
			}
		};
	}
}

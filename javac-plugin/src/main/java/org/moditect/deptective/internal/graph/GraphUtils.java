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
package org.moditect.deptective.internal.graph;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.impl.DependencyStructureMatrix;
import org.moditect.deptective.internal.graph.impl.FastFasSorter;
import org.moditect.deptective.internal.graph.impl.Tarjan;

/**
 * 
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class GraphUtils {

	/**
	 * A directed graph is called strongly connected if there is a path in each direction between each pair of vertices of the graph.
	 * A strongly connected component (SCC) of a directed graph is a maximal strongly connected subgraph. 
	 * 
	 * @param nodes the collection of nodes (the directed graph)
	 * @return a list of strongly connected components (SCCs). Note that the result also contains components that contain only a single node.
	 */
	public static <N extends INode> List<List<N>> detectStronglyConnectedComponents(Collection<? extends N> nodes) {

		return new Tarjan<N>().detectStronglyConnectedComponents(checkNotNull(nodes));
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public static <N extends INode> List<List<N>> detectCycles(Collection<N> nodes) {
		return new Tarjan<N>().detectStronglyConnectedComponents(nodes).stream().filter(cycle -> cycle.size() > 1)
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public static <N extends INode, D extends IDependency> IDependencyStructureMatrix<N, D> createDependencyStructureMatrix(Collection<N> nodes) {
		return new DependencyStructureMatrix<N, D>(nodes);
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param artifacts
	 * @return
	 */
	public static int[][] computeAdjacencyMatrix(Collection<? extends INode> artifacts) {

		//
		checkNotNull(artifacts);

		//
		return computeAdjacencyMatrix((INode[]) artifacts.toArray(new INode[artifacts.size()]));
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param monitor
	 * @param artifacts
	 * @return
	 */
	public static int[][] computeAdjacencyMatrix(INode... artifacts) {

		//
		int[][] result = new int[artifacts.length][artifacts.length];

		//
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {

				// get the dependency
				IDependency dependency = artifacts[i].getOutgoingDependencyTo(artifacts[j]);
				result[i][j] = dependency != null ? dependency.getAggregatedWeight() : 0;
			}
		}

		// return the matrix
		return result;
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param artifacts
	 * @return
	 */
	public static int[][] computeAdjacencyList(Collection<INode> artifacts) {
		checkNotNull(artifacts);

		return computeAdjacencyList((INode[]) artifacts.toArray(new INode[artifacts.size()]));
	}

	/**
	 * @param nodes
	 */
	public static int[][] computeAdjacencyList(INode... nodes) {

		//
		int[][] matrix;

		// prepare
		int i = 0;
		Map<INode, Integer> map = new HashMap<INode, Integer>();
		for (INode iArtifact : nodes) {
			map.put(iArtifact, i);
			i++;
		}

		matrix = new int[nodes.length][];

		//
		for (INode node : nodes) {

			// get the referenced artifacts
			Collection<IDependency> dependencies = node.getOutgoingDependenciesTo(Arrays.asList(nodes));

			if (dependencies == null) {
				dependencies = Collections.emptyList();
			}

			//
			int index = map.get(node);
			matrix[index] = new int[dependencies.size()];

			//
			int count = 0;
			for (IDependency dependency : dependencies) {
				matrix[index][count] = map.get(dependency.getTo());
				count++;
			}
		}

		//
		return matrix;
	}
	
	/**
	 * 
	 * @return
	 */
	public static <N extends INode, D extends IDependency> INodeSorter<N, D> createFasNodeSorter() {

		return new FastFasSorter<>();
	}
}

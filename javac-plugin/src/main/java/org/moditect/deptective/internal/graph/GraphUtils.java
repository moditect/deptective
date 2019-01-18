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

public class GraphUtils {

	/**
	 * A directed graph is called strongly connected if there is a path in each direction between each pair of vertices 
	 * of the graph. A strongly connected component (SCC) of a directed graph is a maximal strongly connected subgraph. 
	 * 
	 * @param nodes the collection of nodes (the directed graph)
	 * @return a list of strongly connected components (SCCs). Note that the result also contains components that 
	 * contain only a single node.
	 */
	public static  List<List<Node>> detectStronglyConnectedComponents(Collection<Node> nodes) {

		return new Tarjan<Node>().detectStronglyConnectedComponents(checkNotNull(nodes));
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public static  List<List<Node>> detectCycles(Collection<Node> nodes) {
		return new Tarjan<Node>().detectStronglyConnectedComponents(nodes).stream().filter(cycle -> cycle.size() > 1)
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param nodes
	 * @return
	 */
	public static <N extends Node, D extends Dependency> DependencyStructureMatrix createDependencyStructureMatrix(Collection<Node> nodes) {
		return new DependencyStructureMatrix(nodes);
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param artifacts
	 * @return
	 */
	public static int[][] computeAdjacencyMatrix(Collection<Node> artifacts) {

		//
		checkNotNull(artifacts);

		//
		return computeAdjacencyMatrix((Node[]) artifacts.toArray(new Node[artifacts.size()]));
	}

	/**
	 * <p>
	 * </p>
	 * 
	 * @param monitor
	 * @param artifacts
	 * @return
	 */
	public static int[][] computeAdjacencyMatrix(Node... artifacts) {

		//
		int[][] result = new int[artifacts.length][artifacts.length];

		//
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result.length; j++) {

				// get the dependency
				Dependency dependency = artifacts[i].getOutgoingDependencyTo(artifacts[j]);
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
	public static int[][] computeAdjacencyList(Collection<Node> artifacts) {
		checkNotNull(artifacts);

		return computeAdjacencyList((Node[]) artifacts.toArray(new Node[artifacts.size()]));
	}

	/**
	 * @param nodes
	 */
	public static int[][] computeAdjacencyList(Node... nodes) {

		//
		int[][] matrix;

		// prepare
		int i = 0;
		Map<Node, Integer> map = new HashMap<Node, Integer>();
		for (Node iArtifact : nodes) {
			map.put(iArtifact, i);
			i++;
		}

		matrix = new int[nodes.length][];

		//
		for (Node node : nodes) {

			// get the referenced artifacts
			Collection<Dependency> dependencies = node.getOutgoingDependenciesTo(Arrays.asList(nodes));

			if (dependencies == null) {
				dependencies = Collections.emptyList();
			}

			//
			int index = map.get(node);
			matrix[index] = new int[dependencies.size()];

			//
			int count = 0;
			for (Dependency dependency : dependencies) {
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
	public static INodeSorter createFasNodeSorter() {
		return new FastFasSorter();
	}
}

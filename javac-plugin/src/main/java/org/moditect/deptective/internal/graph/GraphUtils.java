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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.impl.DependencyStructureMatrix;
import org.moditect.deptective.internal.graph.impl.FastFasSorter;
import org.moditect.deptective.internal.graph.impl.Tarjan;

/**
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class GraphUtils {

    /**
     * A directed graph is called strongly connected if there is a path in each direction between each pair of vertices
     * of the graph. A strongly connected component (SCC) of a directed graph is a maximal strongly connected subgraph.
     *
     * @param nodes the collection of nodes (the directed graph)
     * @return a list of strongly connected components (SCCs). Note that the result also contains components that
     *         contain just a single node. If you want to detect 'real' cycle (size > 1) please use
     *         {@link GraphUtils#detectCycles(Collection)}.
     */
    public static <T extends Node<T>> List<List<T>> detectStronglyConnectedComponents(Collection<T> nodes) {
        return new Tarjan<T>().detectStronglyConnectedComponents(Objects.requireNonNull(nodes));
    }

    /**
     * Returns all strongly connected subgraphs (size > 1) of the specified graph.
     *
     * @param nodes
     * @return a list of strongly connected components (SCCs) with a size > 1.
     */
    public static <T extends Node<T>> List<Cycle<T>> detectCycles(Iterable<T> nodes) {
        return new Tarjan<T>().detectStronglyConnectedComponents(nodes)
                .stream()
                .filter(cycle -> cycle.size() > 1)
                .map(Cycle::new)
                .collect(Collectors.toList());
    }

    /**
     * Creates a dependency structure matrix (DSM) for the given graph nodes.
     *
     * @param nodes the collection of nodes
     * @return
     */
    public static <T extends Node<T>> IDependencyStructureMatrix<T> createDependencyStructureMatrix(
            Collection<T> nodes) {
        return new DependencyStructureMatrix<T>(nodes);
    }

    /**
     * An adjacency matrix is a square matrix used to represent a finite graph. The elements of the matrix
     * indicate whether pairs of vertices are connected (adjacent) or not in the graph.
     *
     * @param nodes the collection of nodes
     * @return the adjacency matrix for the given list of nodes
     */
    public static <T extends Node<T>> int[][] computeAdjacencyMatrix(List<T> nodes) {
        Objects.requireNonNull(nodes);
        int[][] result = new int[nodes.size()][nodes.size()];
        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result.length; j++) {
                Dependency<T> dependency = nodes.get(i).getOutgoingDependencyTo(nodes.get(j));
                result[i][j] = dependency != null ? dependency.getAggregatedWeight() : 0;
            }
        }
        return result;
    }

    /**
     * An adjacency list is a collection of (unordered) lists used to represent a finite graph. Each list
     * describes the set of neighbors of a node.
     *
     * @param nodes the array of nodes
     * @return the adjacency list for the given list of nodes
     */
    public static <T extends Node<T>> int[][] computeAdjacencyList(Iterable<T> nodes) {

        int[][] matrix;

        // prepare
        int i = 0;
        Map<T, Integer> map = new HashMap<T, Integer>();
        for (T iArtifact : nodes) {
            map.put(iArtifact, i);
            i++;
        }
        matrix = new int[map.size()][];

        for (T node : nodes) {
            Collection<Dependency<T>> dependencies = node.getOutgoingDependenciesTo(nodes);
            if (dependencies == null) {
                dependencies = Collections.emptyList();
            }
            int index = map.get(node);
            matrix[index] = new int[dependencies.size()];
            int count = 0;
            for (Dependency<?> dependency : dependencies) {
                matrix[index][count] = map.get(dependency.getTo());
                count++;
            }
        }
        return matrix;
    }

    /**
     * Creates a FastFAS based {@link INodeSorter}.
     *
     * @return a FastFAS based {@link INodeSorter}.
     */
    public static INodeSorter createFasNodeSorter() {
        return new FastFasSorter();
    }
}

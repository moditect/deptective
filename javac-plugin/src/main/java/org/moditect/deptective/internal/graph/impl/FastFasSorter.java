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
import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.Node;
import org.moditect.deptective.internal.graph.INodeSorter;

public class FastFasSorter implements INodeSorter {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public SortResult sort(List<Node> artifacts) {

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

                }
                else {

                    // stop bubbling...
                    break;
                }
            }
        }

        // reverse it
        ordered = FastFAS.reverse(ordered);

        // create the result nodes list
        List<Node> resultNodes = new ArrayList<>(artifacts.size());
        for (int index : ordered) {
            resultNodes.add(artifacts.get(index));
        }

        // create the list of upwards dependencies
        List<Dependency> upwardsDependencies = new ArrayList<>();
        for (Integer[] values : fastFAS.getSkippedEdge()) {
            Node source = artifacts.get(values[0]);
            Node target = artifacts.get(values[1]);
            upwardsDependencies.add(source.getOutgoingDependencyTo(target));
        }

        // return the result
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

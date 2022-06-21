/**
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
package org.moditect.deptective.internal.graph;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A cycle between multiple nodes.
 *
 * @author Gunnar Morling
 * @param <T> the specific node type
 */
public class Cycle<T extends Node<T>> {

    private static final String STRING_DELIMITER = ", ";
    private final List<T> nodes;

    public Cycle(List<T> nodes) {
        this.nodes = Collections.unmodifiableList(nodes);
    }

    public List<T> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return nodes.stream()
                .map(Node::asShortString)
                .sorted()
                .collect(Collectors.joining(STRING_DELIMITER));
    }
}

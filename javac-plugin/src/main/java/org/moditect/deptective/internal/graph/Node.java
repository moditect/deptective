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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class Node {

    private Map<Node, Dependency> outgoingDependencies;
    private final String id;

    public Node(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String getId() {
        return id;
    }

    public Dependency getOutgoingDependencyTo(Node node) {
        if (!hasOutgoingDependencies()) {
            return null;
        }

        return outgoingDependencies.get(node);
    }

    public Set<Dependency> getOutgoingDependenciesTo(Collection<Node> nodes) {
        return Objects.requireNonNull(nodes).stream()
                .map(node -> getOutgoingDependencyTo(node))
                .filter(dep -> dep != null)
                .collect(Collectors.toSet());
    }

    public boolean hasOutgoingDependencies() {
        return outgoingDependencies != null && !outgoingDependencies.isEmpty();
    }

    public void addOutgoingDependency(Node to, int aggregatedWeight) {
        outgoingDependencies().put(to, new Dependency(to, aggregatedWeight));
    }

    @Override
    public String toString() {
        return "Node [id=" + id + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        }
        else if (!id.equals(other.id))
            return false;
        return true;
    }

    private Map<Node, Dependency> outgoingDependencies() {
        if (outgoingDependencies == null) {
            outgoingDependencies = new HashMap<>();
        }
        return outgoingDependencies;
    }
}

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
package org.moditect.deptective.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.Node;

/**
 * Describes a component, a set of packages identified by one more naming patterns.
 * <p>
 * A component may be configured to reference one or more other components. A special kind of
 * component is the whitelist component, which implicitly may be referenced by all other components.
 *
 * @author Gunnar Morling
 */
public class Component implements Node<Component> {

    public static class Builder {

        private final String name;
        private final Set<PackagePattern> contained;
        private final Map<String, ReadKind> reads;

        public Builder(String name) {
            this.name = name;
            this.contained = new HashSet<>();
            this.reads = new HashMap<>();
        }

        public Builder addReads(Iterable<String> reads) {
            for (String read : reads) {
                addRead(read, ReadKind.ALLOWED);
            }
            return this;
        }

        public Builder addRead(String read, ReadKind readKind) {
            if (!read.isEmpty() && !read.equals(name) && !read.equals("java.lang")) {
                reads.put(read, readKind);
            }

            return this;
        }

        public Builder addContains(Collection<PackagePattern> contains) {
            contained.addAll(contains);
            return this;
        }

        public Builder addContains(PackagePattern contains) {
            contained.add(contains);
            return this;
        }

        public Component build() {
            return new Component(name, contained, reads);
        }

        public Map<String, ReadKind> getReads() {
            return reads;
        }

        public String getName() {
            return name;
        }
    }

    private final String name;
    private final Set<PackagePattern> contained;
    private final Map<String, ReadKind> reads;

    public Component(String name, Set<PackagePattern> contained, Map<String, ReadKind> reads) {
        this.name = name;
        this.contained = Collections.unmodifiableSet(contained);
        this.reads = Collections.unmodifiableMap(reads);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public boolean containsPackage(String qualifiedName) {
        for (PackagePattern packagePattern : contained) {
            if (packagePattern.matches(qualifiedName)) {
                return true;
            }
        }

        return false;
    }

    public boolean allowedToRead(Component other) {
        return name.equals(other.name) || reads.get(other.getName()) == ReadKind.ALLOWED;
    }

    public String getName() {
        return name;
    }

    public Set<PackagePattern> getContained() {
        return contained;
    }

    public Map<String, ReadKind> getReads() {
        return reads;
    }

    @Override
    public String toString() {
        return name + " { contained=" + contained + ", reads=" + reads + "] }";
    }

    @Override
    public String asShortString() {
        return name;
    }

    @Override
    public Dependency<Component> getOutgoingDependencyTo(Component node) {
        return reads.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(node.getName()))
                .map(e -> new Dependency<Component>(Component.builder(e.getKey()).build(), 1))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean hasOutgoingDependencies() {
        return !reads.isEmpty();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        Component other = (Component) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        }
        else if (!name.equals(other.name))
            return false;
        return true;
    }

}

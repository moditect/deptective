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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Describes a component, a set of packages identified by one more naming patterns.
 * <p>
 * A component may be configured to reference one or more other components. A special kind of
 * component is the whitelist component, which implicitly may be referenced by all other components.
 *
 * @author Gunnar Morling
 */
public class Component {

    public static class Builder {

        private final String name;
        private final List<PackagePattern> contained;
        private final Map<String, ReadKind> reads;

        public Builder(String name) {
            this.name = name;
            this.contained = new ArrayList<>();
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

        public Builder addContains(List<PackagePattern> contains) {
            contained.addAll(contains);
            return this;
        }

        public Component build() {
            return new Component(name, contained, reads);
        }

        public Map<String, ReadKind> getReads() {
            return reads;
        }
    }

    private final String name;
    private final List<PackagePattern> contained;
    private final Map<String, ReadKind> reads;

    public Component(String name, List<PackagePattern> contained, Map<String, ReadKind> reads) {
        this.name = name;
        this.contained = contained;
        this.reads = reads;
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

    public List<PackagePattern> getContained() {
        return contained;
    }

    public Map<String, ReadKind> getReads() {
        return reads;
    }

    @Override
    public String toString() {
        return name + " { contained=" + contained + ", reads=" + reads + "] }";
    }
}

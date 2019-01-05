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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Describes a Java package and its intended dependences to other packages.
 *
 * @author Gunnar Morling
 */
public class Package {

    public static class Builder {

        private final String name;
        private final Set<String> reads;

        private Builder(String name) {
            this.name = name;
            this.reads = new HashSet<>();
        }

        public Builder addReads(String read, String... furtherReads) {
            addRead(read);
            if(furtherReads != null) {
                addReads(Arrays.asList(furtherReads));
            }
            return this;
        }

        public Builder addReads(Iterable<String> reads) {
            for (String read : reads) {
                addRead(read);
            }
            return this;
        }

        private void addRead(String read) {
            if (!read.isEmpty() && !read.equals(name) && !read.equals("java.lang")) {
                reads.add(read);
            }
        }

        public Package build() {
            return new Package(name, reads);
        }

        public Set<String> getReads() {
            return reads;
        }
    }

    public static final Package UNCONFIGURED = new Package("__unconfigured__", Collections.emptySet(), false);

    private final String name;
    private final Set<String> reads;
    private final boolean configured;

    private Package(String name, Set<String> reads) {
        this(name, reads, true);
    }

    private Package(String name, Set<String> reads, boolean configured) {
        this.name = name;
        this.reads = Collections.unmodifiableSet(reads);
        this.configured = configured;
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String getName() {
        return name;
    }

    public Set<String> getReads() {
        return reads;
    }

    /**
     * Whether this package reads the given other package.
     */
    public boolean reads(String qualifiedName) {
        return reads.contains(qualifiedName);
    }

    public boolean isConfigured() {
        return configured;
    }

    @Override
    public String toString() {
        return name;
    }
}

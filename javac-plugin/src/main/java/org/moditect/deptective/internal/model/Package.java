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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes a Java package and its intended dependences to other packages.
 *
 * @author Gunnar Morling
 */
public class Package {

    public static class Builder {

        private final String name;
        private final boolean configured;
        private final Map<String, ReadKind> reads;

        private Builder(String name, boolean configured) {
            this.name = name;
            this.configured = configured;
            this.reads = new HashMap<>();
        }

        public Builder addReads(Iterable<String> reads) {
            for (String read : reads) {
                addRead(read, ReadKind.ALLOWED);
            }
            return this;
        }

        public void addRead(String read, ReadKind readKind) {
            if (!read.isEmpty() && !read.equals(name) && !read.equals("java.lang")) {
                reads.put(read, readKind);
            }
        }

        public Package build() {
            return new Package(name, reads, configured);
        }

        public Map<String, ReadKind> getReads() {
            return reads;
        }
    }

    public enum ReadKind {
        ALLOWED,
        DISALLOWED,
        UKNOWN;
    }

    private final String name;
    private final Map<String, ReadKind> reads;
    private final boolean configured;

    private Package(String name, Map<String, ReadKind> reads) {
        this(name, reads, true);
    }

    private Package(String name, Map<String, ReadKind> reads, boolean configured) {
        this.name = name;
        this.reads = Collections.unmodifiableMap(reads);
        this.configured = configured;
    }

    public static Builder builder(String name, boolean configured) {
        return new Builder(name, configured);
    }

    public String getName() {
        return name;
    }

    public Map<String, ReadKind> getReads() {
        return reads;
    }

    /**
     * Whether this package is allowed to read the given other package.
     */
    public boolean allowedToRead(String qualifiedName) {
        return reads.get(qualifiedName) == ReadKind.ALLOWED;
    }

    public boolean isConfigured() {
        return configured;
    }

    @Override
    public String toString() {
        return name;
    }
}

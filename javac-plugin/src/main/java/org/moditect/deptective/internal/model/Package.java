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
import java.util.List;

/**
 * Describes a Java package and its intended dependences to other packages.
 *
 * @author Gunnar Morling
 */
public class Package {

    public static final Package UNCONFIGURED = new Package("__unconfigured__", Collections.emptyList(), false);

    private final String name;
    private final List<String> reads;
    private final boolean configured;

    Package(String name, List<String> reads) {
        this(name, reads, true);
    }

    private Package(String name, List<String> reads, boolean configured) {
        this.name = name;
        this.reads = Collections.unmodifiableList(reads);
        this.configured = configured;
    }

    public String getName() {
        return name;
    }

    public List<String> getReads() {
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

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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PackageDependencies {

    public static class Builder {

        private final Map<String, Package> packagesByName = new HashMap<>();

        public PackageDependencies build() {
            return new PackageDependencies(packagesByName);
        }

        public void addPackage(String name, List<String> reads) {
            if (packagesByName.containsKey(name)) {
                throw new IllegalArgumentException("Package " + name + " may not be configured more than once.");
            }

            packagesByName.put(name, new Package(name, reads));
        }
    }

    private final Map<String, Package> packagesByName;

    private PackageDependencies(Map<String, Package> packagesByName) {
        this.packagesByName = Collections.unmodifiableMap(packagesByName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Package getPackage(String qualifiedName) {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return null;
        }

        return packagesByName.get(qualifiedName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Entry<String, Package> pakkage : packagesByName.entrySet()) {
            sb.append(pakkage.getKey() + "=" + pakkage.getValue().getReads() + System.lineSeparator());
        }

        return sb.toString();
    }
}

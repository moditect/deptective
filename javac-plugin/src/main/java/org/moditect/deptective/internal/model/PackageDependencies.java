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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PackageDependencies {

    public static class Builder {

        private final Map<String, Package> packagesByName = new HashMap<>();
        private final Set<WhitelistedPackagePattern> whitelisted = new HashSet<>();

        public PackageDependencies build() {
            return new PackageDependencies(packagesByName, whitelisted);
        }

        public void addPackage(String name, List<String> reads) {
            if (packagesByName.containsKey(name)) {
                throw new IllegalArgumentException("Package " + name + " may not be configured more than once.");
            }

            packagesByName.put(name, new Package(name, reads));
        }

        public void addWhitelistedPackage(String pattern) {
            this.whitelisted.add(new WhitelistedPackagePattern(pattern));
        }
    }

    private final Map<String, Package> packagesByName;
    private final Set<WhitelistedPackagePattern> whitelisted;

    private PackageDependencies(Map<String, Package> packagesByName, Set<WhitelistedPackagePattern> whitelisted) {
        this.packagesByName = Collections.unmodifiableMap(packagesByName);
        this.whitelisted = Collections.unmodifiableSet(whitelisted);
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
        StringBuilder sb = new StringBuilder("packages {");
        sb.append(System.lineSeparator());

        for (Entry<String, Package> pakkage : packagesByName.entrySet()) {
            sb.append("  ");
            sb.append(pakkage.getKey());
            sb.append("=");
            sb.append(pakkage.getValue().getReads());
            sb.append(System.lineSeparator());
        }

        sb.append("}");
        sb.append(System.lineSeparator());
        sb.append("whitelisted {").append(System.lineSeparator());
        for (WhitelistedPackagePattern whitelistedPackagePattern : whitelisted) {
            sb.append("  ");
            sb.append(whitelistedPackagePattern);
            sb.append(System.lineSeparator());
        }
        sb.append("}");
        return sb.toString();
    }

    public boolean isWhitelisted(String packageName) {
        return whitelisted.stream()
            .filter(w -> w.matches(packageName))
            .findFirst()
            .isPresent();
    }
}

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
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PackageDependencies {

    public static class Builder {

        private final Map<String, Package.Builder> packagesByName = new HashMap<>();
        private final Set<WhitelistedPackagePattern> whitelisted = new HashSet<>();

        public PackageDependencies build() {
            return new PackageDependencies(
                    packagesByName.values()
                        .stream()
                        .map(Package.Builder::build)
                        .collect(Collectors.toMap(Package::getName, Function.identity())),
                    whitelisted);
        }

        public void addPackage(String name, List<String> reads) {
            if (packagesByName.containsKey(name)) {
                throw new IllegalArgumentException("Package " + name + " may not be configured more than once.");
            }

            packagesByName.put(name, Package.builder(name).addReads(reads));
        }

        public void addRead(String name, String readPackage) {
            Package.Builder builder = packagesByName.computeIfAbsent(name, n -> Package.builder(n));
            builder.addReads(readPackage);
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

        Package pakkage = packagesByName.get(qualifiedName);

        return pakkage != null ? pakkage : Package.UNCONFIGURED;
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

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectNode root = mapper.createObjectNode();
        ArrayNode packages = root.putArray("packages");

        packagesByName.values()
            .stream()
            .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
            .forEach(p -> packages.add(toJsonNode(p, mapper)));

        ArrayNode whitelisted = root.putArray("whitelisted");

        this.whitelisted.stream()
            .map(WhitelistedPackagePattern::toString)
            .sorted()
            .forEach(whitelisted::add);

        try {
            return mapper.writeValueAsString(root);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonNode toJsonNode(Package pakkage, ObjectMapper mapper) {
        ObjectNode node = mapper.createObjectNode();

        node.put("name", pakkage.getName());
        ArrayNode reads = node.putArray("reads");
        pakkage.getReads()
            .stream()
            .sorted()
            .forEach(r -> reads.add(r));

        return node;
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph \"package dependencies\"\n");
        sb.append("{\n");

        for (Package pakkage : packagesByName.values()) {
            for (String referencedPackage : pakkage.getReads()) {
                sb.append("    \"").append(pakkage.getName()).append("\" -> \"").append(referencedPackage).append("\";\n");
            }
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

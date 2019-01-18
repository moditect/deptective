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
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.graph.Dependency;
import org.moditect.deptective.internal.graph.Node;
import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.graph.INode;
import org.moditect.deptective.internal.model.Package.ReadKind;

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

            packagesByName.put(name, Package.builder(name, true).addReads(reads));
        }

        public void addRead(String name, String readPackage, ReadKind readKind) {
            Package.Builder builder = packagesByName.computeIfAbsent(name, n -> Package.builder(n, true));
            builder.addRead(readPackage, readKind);
        }

        public void addWhitelistedPackage(WhitelistedPackagePattern pattern) {
            if (pattern == null || pattern.toString().isEmpty()) {
                return;
            }

            this.whitelisted.add(pattern);

            for (Package.Builder pakkage : packagesByName.values()) {
                pakkage.getReads()
                    .entrySet()
                    .removeIf(r -> pattern.matches(r.getKey()));
            }
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

        return pakkage != null ? pakkage : Package.builder(qualifiedName, false).build();
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

        if (!pakkage.getReads().isEmpty()) {
            ArrayNode reads = node.putArray("reads");
            pakkage.getReads()
                .keySet()
                .stream()
                .sorted()
                .forEach(r -> reads.add(r));
        }

        return node;
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph \"package dependencies\"\n");
        sb.append("{\n");

        SortedSet<String> allPackages = new TreeSet<>();
        SortedMap<String, SortedSet<String>> allowedReads = new TreeMap<>();
        SortedMap<String, SortedSet<String>> disallowedReads = new TreeMap<>();
        SortedMap<String, SortedSet<String>> unknownReads = new TreeMap<>();

        for (Package pakkage : packagesByName.values()) {
            allPackages.add(pakkage.getName());

            SortedSet<String> allowed = new TreeSet<>();
            allowedReads.put(pakkage.getName(), allowed);

            SortedSet<String> disallowed = new TreeSet<>();
            disallowedReads.put(pakkage.getName(), disallowed);

            SortedSet<String> unknown = new TreeSet<>();
            unknownReads.put(pakkage.getName(), unknown);

            for (Entry<String, ReadKind> referencedPackage : pakkage.getReads().entrySet()) {
                String referencedPackageName = referencedPackage.getKey();
                allPackages.add(referencedPackageName);

                if (referencedPackage.getValue() == ReadKind.ALLOWED) {
                    allowed.add(referencedPackageName);
                }
                else if (referencedPackage.getValue() == ReadKind.DISALLOWED) {
                    disallowed.add(referencedPackageName);
                }
                else {
                    unknown.add(referencedPackageName);
                }
            }
        }

        for (String pakkage : allPackages) {
            sb.append("  \"").append(pakkage).append("\";").append(System.lineSeparator());
        }

        addSubGraph(sb, allowedReads, "Allowed", null);
        addSubGraph(sb, disallowedReads, "Disallowed", "red");
        addSubGraph(sb, unknownReads, "Unknown", "yellow");

        sb.append("}");

        return sb.toString();
    }

    private void addSubGraph(StringBuilder sb, SortedMap<String, SortedSet<String>> readsOfKind, String kind, String color) {
        sb.append("  subgraph " + kind + " {").append(System.lineSeparator());
        if (color != null) {
            sb.append("    edge [color=" + color + "]").append(System.lineSeparator());
        }
        for (Entry<String, SortedSet<String>> reads : readsOfKind.entrySet()) {
            for (String read : reads.getValue()) {
                sb.append("    \"").append(reads.getKey()).append("\" -> \"").append(read).append("\";\n");
            }
        }

        sb.append("  }").append(System.lineSeparator());
    }

    public boolean isWhitelisted(String packageName) {
        return whitelisted.stream()
            .filter(w -> w.matches(packageName))
            .findFirst()
            .isPresent();
    }
    
    public String toCycleReport() {
    	
    	//
    	Map<String, Node> graphNodeMap = new HashMap<>();
    	
    	// create the nodes
    	packagesByName.values().forEach(p -> graphNodeMap.put(p.getName(), new Node(p.getName())));
    	
    	// create the dependencies
    	packagesByName.values().forEach(p -> {
    		Node sourceNode = graphNodeMap.get(p.getName());
    		p.getReads().keySet().forEach(target -> {
    			Node targetNode = graphNodeMap.get(p.getName());
    			// TODO: WEIGHT
    			Dependency defaultDependency = new Dependency(sourceNode, targetNode, 1);
    			sourceNode.addOutgoingDependency(defaultDependency);
    		});
    	});
    	
    	//
    	List<List<Node>> cycles = GraphUtils.detectCycles(graphNodeMap.values());
    	
    	//
    	if (!cycles.isEmpty()) {
    		
    		// FAIL!
    	}
    	
    	return "TODO";
    }
}

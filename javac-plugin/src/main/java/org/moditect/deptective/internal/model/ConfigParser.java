/*
 *  Copyright 2019-2022 The ModiTect authors
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.moditect.deptective.internal.model.PackageDependencies.Builder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class ConfigParser {

    private PackageDependencies packageDependencies;

    public ConfigParser(InputStream config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }

        try {
            this.packageDependencies = parseConfig(config);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ConfigParser(String config) {
        if (config == null) {
            throw new IllegalArgumentException("Config must not be null");
        }

        try {
            this.packageDependencies = parseConfig(config);
        }
        catch (RuntimeException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PackageDependencies getPackageDependencies() {
        return packageDependencies;
    }

    private PackageDependencies parseConfig(InputStream config) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return parseComponents(objectMapper.readTree(config));
    }

    private PackageDependencies parseConfig(String config) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        return parseComponents(objectMapper.readTree(config));
    }

    private PackageDependencies parseComponents(JsonNode config) throws IOException {
        PackageDependencies.Builder builder = PackageDependencies.builder();

        ArrayNode components = (ArrayNode) config.get("components");

        if (components != null) {
            Iterator<JsonNode> it = components.iterator();
            while (it.hasNext()) {
                parseComponent(it.next(), builder);
            }
        }

        ArrayNode whitelisted = (ArrayNode) config.get("whitelisted");

        if (whitelisted != null) {
            Iterator<JsonNode> it = whitelisted.iterator();
            while (it.hasNext()) {
                PackagePattern pattern = PackagePattern.getPattern(it.next().asText());
                builder.addWhitelistedPackage(pattern);
            }
        }

        return builder.build();
    }

    private void parseComponent(JsonNode component, Builder builder) {
        String name = component.get("name").asText();
        List<PackagePattern> contains = parseContains((ArrayNode) (component.get("contains")));
        List<String> reads = parseReads((ArrayNode) (component.get("reads")));

        for (String read : reads) {
            builder.addRead(name, read, ReadKind.ALLOWED);
        }

        for (PackagePattern pattern : contains) {
            builder.addContains(name, pattern);
        }
    }

    private List<String> parseReads(ArrayNode arrayNode) {
        if (arrayNode == null) {
            return Collections.emptyList();
        }

        Iterator<JsonNode> it = arrayNode.iterator();
        List<String> packages = new ArrayList<>();

        while (it.hasNext()) {
            packages.add(it.next().asText());
        }

        return packages;
    }

    private List<PackagePattern> parseContains(ArrayNode arrayNode) {
        if (arrayNode == null) {
            return Collections.emptyList();
        }

        Iterator<JsonNode> it = arrayNode.iterator();
        List<PackagePattern> components = new ArrayList<>();

        while (it.hasNext()) {
            components.add(PackagePattern.getPattern(it.next().asText()));
        }

        return components;
    }
}

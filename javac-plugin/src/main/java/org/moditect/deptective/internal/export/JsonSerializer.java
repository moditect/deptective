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
package org.moditect.deptective.internal.export;

import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.WhitelistedPackagePattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Serializes models to the {@code deptective.json} descriptor format.
 *
 * @author Gunnar Morling
 */
public class JsonSerializer implements ModelSerializer {

    private final ObjectMapper mapper;

    private final ObjectNode root;
    private final ArrayNode packages;
    private final ArrayNode whitelisted;

    public JsonSerializer() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        root = mapper.createObjectNode();
        packages = root.putArray("packages");
        whitelisted = root.putArray("whitelisted");
    }

    @Override
    public void addPackage(Package pakkage) {
        packages.add(toJsonNode(pakkage, mapper));
    }

    @Override
    public void addWhitelistedPackagePattern(WhitelistedPackagePattern pattern) {
        whitelisted.add(pattern.toString());
    }

    @Override
    public String serialize() {
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
}

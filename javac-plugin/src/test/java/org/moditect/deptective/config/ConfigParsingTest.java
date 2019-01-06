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
package org.moditect.deptective.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.moditect.deptective.internal.model.ConfigParser;
import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.PackageDependencies;

public class ConfigParsingTest {

    @Test
    public void shouldLoadConfig() throws Exception {
        PackageDependencies dependencies = new ConfigParser(
                "{\n" +
                "    \"packages\" : [\n" +
                "        {\n" +
                "            \"name\" : \"com.example.ui\",\n" +
                "            \"reads\" : [\n" +
                "                \"com.example.service\",\n" +
                "                \"com.example.persistence\"\n" +
                "            ]\n" +
                "        },\n" +
                "        {\n" +
                "            \"name\" : \"com.example.service\",\n" +
                "            \"reads\" : [\n" +
                "                \"com.example.persistence\"\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"whitelisted\" : [\n" +
                "        \"java.awt*\", \"java.util*\"\n" +
                "    ]\n" +
                "}\n")
               .getPackageDependencies();

        assertThat(dependencies).isNotNull();

        Package ui = dependencies.getPackage("com.example.ui");
        assertThat(ui).isNotNull();
        assertThat(ui.allowedToRead("com.example.service")).isTrue();
        assertThat(ui.allowedToRead("com.example.persistence")).isTrue();

        Package service = dependencies.getPackage("com.example.service");
        assertThat(service).isNotNull();
        assertThat(service.allowedToRead("com.example.ui")).isFalse();
        assertThat(service.allowedToRead("com.example.persistence")).isTrue();

        assertThat(dependencies.isWhitelisted("java.awt")).isTrue();
        assertThat(dependencies.isWhitelisted("java.awt.color")).isTrue();
        assertThat(dependencies.isWhitelisted("java.util.concurrent")).isTrue();
        assertThat(dependencies.isWhitelisted("java.io")).isFalse();
    }
}

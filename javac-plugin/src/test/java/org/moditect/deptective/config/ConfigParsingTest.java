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
import static org.moditect.deptective.internal.util.Strings.lines;

import org.junit.Test;
import org.moditect.deptective.internal.model.Component;
import org.moditect.deptective.internal.model.ConfigParser;
import org.moditect.deptective.internal.model.PackageDependencies;

public class ConfigParsingTest {

    @Test
    public void shouldLoadConfig() throws Exception {
        PackageDependencies dependencies = new ConfigParser(
                lines(
                        "{",
                        "    \"components\" : [",
                        "        {",
                        "            \"name\" : \"ui\",",
                        "            \"contains\" : [ \"com.example.ui\" ],",
                        "            \"reads\" : [",
                        "                \"service\",",
                        "                \"persistence\"",
                        "            ]",
                        "        },",
                        "        {",
                        "            \"name\" : \"service\",",
                        "            \"contains\" : [ \"com.example.service\" ],",
                        "            \"reads\" : [",
                        "                \"persistence\"",
                        "            ]",
                        "        },",
                        "        {",
                        "            \"name\" : \"persistence\",",
                        "            \"contains\" : [ \"com.example.persistence\" ]",
                        "        }",
                        "    ],",
                        "    \"whitelisted\" : [",
                        "        \"java.awt*\", \"java.util*\"",
                        "    ]",
                        "}"
                )
        ).getPackageDependencies();

        assertThat(dependencies).isNotNull();

        Component ui = dependencies.getComponentByPackage("com.example.ui");
        assertThat(ui).isNotNull();
        assertThat(ui.getName()).isEqualTo("ui");
        assertThat(ui.containsPackage("com.example.ui")).isTrue();
        assertThat(ui.allowedToRead(component("service"))).isTrue();
        assertThat(ui.allowedToRead(component("persistence"))).isTrue();

        Component service = dependencies.getComponentByPackage("com.example.service");
        assertThat(service).isNotNull();
        assertThat(service.getName()).isEqualTo("service");
        assertThat(service.containsPackage("com.example.service")).isTrue();
        assertThat(service.allowedToRead(component("ui"))).isFalse();
        assertThat(service.allowedToRead(component("persistence"))).isTrue();

        assertThat(dependencies.isWhitelisted("java.awt")).isTrue();
        assertThat(dependencies.isWhitelisted("java.awt.color")).isTrue();
        assertThat(dependencies.isWhitelisted("java.util.concurrent")).isTrue();
        assertThat(dependencies.isWhitelisted("java.io")).isFalse();
    }

    private Component component(String name) {
        return Component.builder(name).build();
    }
}

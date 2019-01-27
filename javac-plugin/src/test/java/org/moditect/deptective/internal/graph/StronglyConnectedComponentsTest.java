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
package org.moditect.deptective.internal.graph;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;

/**
 * 
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class StronglyConnectedComponentsTest {

    @Test
    public void detectCycle() {

        //
        List<Node> nodes = TestModelCreator.createDummyModel();

        //
        List<List<Node>> stronglyConnectedComponents = GraphUtils.detectStronglyConnectedComponents(nodes);

        //
        assertThat(stronglyConnectedComponents).hasSize(3);

        //
        for (List<Node> scc : stronglyConnectedComponents) {
            if (scc.size() == 2) {
                assertThat(scc).contains(nodes.get(2)).contains(nodes.get(3));
            }
        }
    }
}

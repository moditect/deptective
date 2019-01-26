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
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class DependencyStructureMatrixTest {

    @Test
    public void detectCycle() {
        List<SimpleNode> nodes = TestModelCreator.createDummyModel();

        IDependencyStructureMatrix<SimpleNode> dsm = GraphUtils.createDependencyStructureMatrix(nodes);

        // assert ordered nodes
        assertThat(dsm.getOrderedNodes()).hasSize(4).containsExactly(
                nodes.get(0), nodes.get(1), nodes.get(2),
                nodes.get(3)
        );

        // assert upward dependencies
        assertThat(dsm.getUpwardDependencies()).hasSize(1)
                .containsExactly(nodes.get(3).getOutgoingDependencyTo(nodes.get(2)));

        // assert cycles
        assertThat(dsm.getCycles()).hasSize(1);
        assertThat(dsm.getCycles().get(0)).containsExactly(nodes.get(3), nodes.get(2));
    }
}

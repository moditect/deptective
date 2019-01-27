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
import org.moditect.deptective.internal.graph.INodeSorter.SortResult;

public class FasNodeSorterTest {

    @Test
    public void sortNodes() {
        List<Node> nodes = TestModelCreator.createDummyModel();

        INodeSorter nodeSorter = GraphUtils.createFasNodeSorter();
        SortResult sortResult = nodeSorter.sort(nodes);
        assertThat(sortResult.getUpwardsDependencies()).hasSize(1);
    }
}

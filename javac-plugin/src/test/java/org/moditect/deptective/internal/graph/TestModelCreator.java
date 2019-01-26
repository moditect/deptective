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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public class TestModelCreator {

    /**
     * Returns a test graph in with this structure:
     * <p>
     * P1 -> P2 -> P3 <-> P4
     */
    public static List<Node> createDummyModel() {

        Node p1 = new Node("p1");
        Node p2 = new Node("p2");
        Node p3 = new Node("p3");
        Node p4 = new Node("p4");

        p1.addOutgoingDependency(p2, 13);
        p2.addOutgoingDependency(p3, 57);
        p3.addOutgoingDependency(p4, 45);
        p4.addOutgoingDependency(p3, 3);

        return new ArrayList<>(Arrays.asList(p1, p2, p3, p4));
    }
}

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

import java.util.List;

/**
 * @author Gerd W&uuml;therich (gw@code-kontor.io)
 */
public interface INodeSorter {

    <T extends Node<T>> SortResult<T> sort(List<T> node);

    public interface SortResult<T extends Node<T>> {

        List<T> getOrderedNodes();

        List<Dependency<T>> getUpwardsDependencies();
    }
}

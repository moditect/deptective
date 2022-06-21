/**
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

import org.moditect.deptective.internal.graph.Node;

/**
 * A component in a software system, e.g. a package or a group of packages.
 * <p>
 * Identity and equality are solely based on the component's name.
 *
 * @author Gunnar Morling
 */
public abstract class IdentifiableComponent implements Node<IdentifiableComponent> {

    protected final String name;

    public IdentifiableComponent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public final int hashCode() {
        return name.hashCode();
    }

    @Override
    public final boolean equals(Object other) {
        if (other instanceof IdentifiableComponent) {
            return name.equals(((IdentifiableComponent) other).name);
        }

        return false;
    }
}

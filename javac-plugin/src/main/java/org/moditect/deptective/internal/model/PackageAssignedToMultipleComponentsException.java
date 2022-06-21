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

import java.util.Set;

/**
 * Indicates that one and the same package has been matched by the filter expressions of multiple components.
 *
 * @author Gunnar Morling
 */
public class PackageAssignedToMultipleComponentsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Set<Component> matchingComponents;

    public PackageAssignedToMultipleComponentsException(Set<Component> matchingComponents) {
        this.matchingComponents = matchingComponents;
    }

    public Set<Component> getMatchingComponents() {
        return matchingComponents;
    }
}

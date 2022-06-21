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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A set of {@link Component}s.
 *
 * @author Gunnar Morling
 */
public class Components implements Iterable<Component> {

    private final Set<Component> contained;
    private final Map<String, Component> componentsByPackage;

    public Components(Set<Component> contained) {
        this.contained = Collections.unmodifiableSet(contained);
        this.componentsByPackage = new HashMap<>();
    }

    @Override
    public Iterator<Component> iterator() {
        return contained.iterator();
    }

    public Stream<Component> stream() {
        return contained.stream();
    }

    /**
     * Returns the component containing the given package or {@code null} if no such component exists.
     *
     * @throws PackageAssignedToMultipleComponentsException In case more than one component was found whose filter
     *         expressions match the given package.
     */
    public Component getComponentByPackage(String qualifiedName) throws PackageAssignedToMultipleComponentsException {
        if (qualifiedName == null || qualifiedName.isEmpty()) {
            return null;
        }

        return componentsByPackage.computeIfAbsent(
                qualifiedName,
                p -> {
                    Set<Component> candidates = contained.stream()
                            .filter(c -> c.containsPackage(p))
                            .collect(Collectors.toSet());

                    if (candidates.isEmpty()) {
                        return null;
                    }
                    else if (candidates.size() == 1) {
                        return candidates.iterator().next();
                    }
                    else {
                        throw new PackageAssignedToMultipleComponentsException(candidates);
                    }
                }
        );
    }
}

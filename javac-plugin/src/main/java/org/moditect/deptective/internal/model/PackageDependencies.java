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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.export.ModelSerializer;
import org.moditect.deptective.internal.graph.Cycle;

public class PackageDependencies {

    public static class Builder {

        private final Map<String, Component.Builder> componentsByName = new HashMap<>();
        private final Set<PackagePattern> whitelisted = new HashSet<>();

        public PackageDependencies build() {
            Set<Component> components = componentsByName.values()
                    .stream()
                    .map(Component.Builder::build)
                    .collect(Collectors.toSet());

            return new PackageDependencies(new Components(components), whitelisted);
        }

        public void addContains(String componentName, PackagePattern contained) {
            Component.Builder builder = componentsByName.computeIfAbsent(componentName, n -> Component.builder(n));
            builder.addContains(contained);
        }

        public void addRead(String name, String readComponent, ReadKind readKind) {
            Component.Builder builder = componentsByName.computeIfAbsent(name, n -> Component.builder(n));
            builder.addRead(readComponent, readKind);
        }

        public void addWhitelistedPackage(PackagePattern pattern) {
            if (pattern == null || pattern.toString().isEmpty()) {
                return;
            }

            this.whitelisted.add(pattern);

            for (Component.Builder component : componentsByName.values()) {
                component.getReads()
                        .entrySet()
                        .removeIf(r -> pattern.matches(r.getKey()));
            }
        }

        public Iterable<Component.Builder> getComponents() {
            return componentsByName.values();
        }

        public void updateFromCycles(List<Cycle<IdentifiableComponent>> cycles) {
            for (Cycle<IdentifiableComponent> cycle : cycles) {
                for (IdentifiableComponent nodeInCycle : cycle.getNodes()) {
                    Component.Builder builder = componentsByName.get(nodeInCycle.name);

                    if (builder == null) {
                        continue;
                    }

                    for (IdentifiableComponent otherNodeInCycle : cycle.getNodes()) {
                        if (builder.getReads().containsKey(otherNodeInCycle.getName())) {
                            builder.addRead(otherNodeInCycle.getName(), ReadKind.CYCLE);
                        }
                    }
                }
            }
        }
    }

    private final Components components;
    private final Set<PackagePattern> whitelisted;

    private PackageDependencies(Components components, Set<PackagePattern> whitelisted) {
        this.components = components;
        this.whitelisted = Collections.unmodifiableSet(whitelisted);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Returns the component containing the given package or {@code null} if no such component exists.
     *
     * @throws PackageAssignedToMultipleComponentsException In case more than one component was found whose filter
     *         expressions match the given package.
     */
    public Component getComponentByPackage(String qualifiedName) throws PackageAssignedToMultipleComponentsException {
        return components.getComponentByPackage(qualifiedName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("components {");
        sb.append(System.lineSeparator());

        for (Component component : components) {
            sb.append("  ");
            sb.append(component);
            sb.append(System.lineSeparator());
        }

        sb.append("}");
        sb.append(System.lineSeparator());
        sb.append("whitelisted {").append(System.lineSeparator());
        for (PackagePattern whitelistedPackagePattern : whitelisted) {
            sb.append("  ");
            sb.append(whitelistedPackagePattern);
            sb.append(System.lineSeparator());
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Serializes this model through the given {@link ModelSerializer}. In alphabetical order, first all components will
     * be serialized, then all whitelist patterns.
     */
    public void serialize(ModelSerializer serializer) {
        components.stream()
                .sorted((c1, c2) -> c1.getName().compareTo(c2.getName()))
                .forEach(c -> serializer.addComponent((c)));

        whitelisted.stream()
                .sorted()
                .forEach(serializer::addWhitelistedPackagePattern);
    }

    public boolean isWhitelisted(String packageName) {
        return whitelisted.stream()
                .filter(w -> w.matches(packageName))
                .findFirst()
                .isPresent();
    }

    public Iterable<Component> getComponents() {
        return components;
    }
}

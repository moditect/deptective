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
package org.moditect.deptective.internal.export;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.moditect.deptective.internal.model.Component;
import org.moditect.deptective.internal.model.PackagePattern;
import org.moditect.deptective.internal.model.ReadKind;

/**
 * Serializes models to GraphViz format ("DOT files").
 *
 * @author Gunnar Morling
 */
public class DotSerializer implements ModelSerializer {

    private final StringBuilder sb;
    private final SortedSet<String> allPackages;
    private final SortedMap<String, SortedSet<String>> allowedReads;
    private final SortedMap<String, SortedSet<String>> disallowedReads;
    private final SortedMap<String, SortedSet<String>> cycleReads;
    private final SortedMap<String, SortedSet<String>> unknownReads;

    public DotSerializer() {
        sb = new StringBuilder();
        sb.append("digraph \"package dependencies\"\n");
        sb.append("{\n");

        allPackages = new TreeSet<>();
        allowedReads = new TreeMap<>();
        disallowedReads = new TreeMap<>();
        cycleReads = new TreeMap<>();
        unknownReads = new TreeMap<>();
    }

    @Override
    public void addComponent(Component component) {
        allPackages.add(component.getName());

        SortedSet<String> allowed = new TreeSet<>();
        allowedReads.put(component.getName(), allowed);

        SortedSet<String> disallowed = new TreeSet<>();
        disallowedReads.put(component.getName(), disallowed);

        SortedSet<String> cycle = new TreeSet<>();
        cycleReads.put(component.getName(), cycle);

        SortedSet<String> unknown = new TreeSet<>();
        unknownReads.put(component.getName(), unknown);

        for (Entry<String, ReadKind> referencedPackage : component.getReads().entrySet()) {
            String referencedPackageName = referencedPackage.getKey();
            allPackages.add(referencedPackageName);

            if (referencedPackage.getValue() == ReadKind.ALLOWED) {
                allowed.add(referencedPackageName);
            }
            else if (referencedPackage.getValue() == ReadKind.DISALLOWED) {
                disallowed.add(referencedPackageName);
            }
            else if (referencedPackage.getValue() == ReadKind.CYCLE) {
                cycle.add(referencedPackageName);
            }
            else {
                unknown.add(referencedPackageName);
            }
        }
    }

    @Override
    public void addWhitelistedPackagePattern(PackagePattern pattern) {
    }

    @Override
    public String serialize() {
        for (String pakkage : allPackages) {
            sb.append("  \"").append(pakkage).append("\";").append(System.lineSeparator());
        }

        addSubGraph(sb, allowedReads, "Allowed", null);
        addSubGraph(sb, disallowedReads, "Disallowed", "red");
        addSubGraph(sb, cycleReads, "Cycle", "purple");
        addSubGraph(sb, unknownReads, "Unknown", "yellow");

        sb.append("}");

        return sb.toString();
    }

    private void addSubGraph(StringBuilder sb, SortedMap<String, SortedSet<String>> readsOfKind, String kind,
            String color) {

        StringBuilder subGraphBuilder = new StringBuilder();
        boolean atLeastOneEdge = false;

        subGraphBuilder.append("  subgraph " + kind + " {").append(System.lineSeparator());
        if (color != null) {
            subGraphBuilder.append("    edge [color=" + color + ", penwidth=2]").append(System.lineSeparator());
        }
        for (Entry<String, SortedSet<String>> reads : readsOfKind.entrySet()) {
            for (String read : reads.getValue()) {
                subGraphBuilder.append("    \"").append(reads.getKey()).append("\" -> \"").append(read).append("\";\n");
                atLeastOneEdge = true;
            }
        }

        subGraphBuilder.append("  }").append(System.lineSeparator());

        if (atLeastOneEdge) {
            sb.append(subGraphBuilder);
        }
    }
}

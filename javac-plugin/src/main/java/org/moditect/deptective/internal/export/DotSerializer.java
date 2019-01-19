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
package org.moditect.deptective.internal.export;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.Package.ReadKind;
import org.moditect.deptective.internal.model.WhitelistedPackagePattern;

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
    private final SortedMap<String, SortedSet<String>> unknownReads;

    public DotSerializer() {
        sb = new StringBuilder();
        sb.append("digraph \"package dependencies\"\n");
        sb.append("{\n");

        allPackages = new TreeSet<>();
        allowedReads = new TreeMap<>();
        disallowedReads = new TreeMap<>();
        unknownReads = new TreeMap<>();
    }

    @Override
    public void addPackage(Package pakkage) {
        allPackages.add(pakkage.getName());

        SortedSet<String> allowed = new TreeSet<>();
        allowedReads.put(pakkage.getName(), allowed);

        SortedSet<String> disallowed = new TreeSet<>();
        disallowedReads.put(pakkage.getName(), disallowed);

        SortedSet<String> unknown = new TreeSet<>();
        unknownReads.put(pakkage.getName(), unknown);

        for (Entry<String, ReadKind> referencedPackage : pakkage.getReads().entrySet()) {
            String referencedPackageName = referencedPackage.getKey();
            allPackages.add(referencedPackageName);

            if (referencedPackage.getValue() == ReadKind.ALLOWED) {
                allowed.add(referencedPackageName);
            }
            else if (referencedPackage.getValue() == ReadKind.DISALLOWED) {
                disallowed.add(referencedPackageName);
            }
            else {
                unknown.add(referencedPackageName);
            }
        }
    }

    @Override
    public void addWhitelistedPackagePattern(WhitelistedPackagePattern pattern) {
    }

    @Override
    public String serialize() {
        for (String pakkage : allPackages) {
            sb.append("  \"").append(pakkage).append("\";").append(System.lineSeparator());
        }

        addSubGraph(sb, allowedReads, "Allowed", null);
        addSubGraph(sb, disallowedReads, "Disallowed", "red");
        addSubGraph(sb, unknownReads, "Unknown", "yellow");

        sb.append("}");

        return sb.toString();
    }

    private void addSubGraph(StringBuilder sb, SortedMap<String, SortedSet<String>> readsOfKind, String kind,
            String color) {
        sb.append("  subgraph " + kind + " {").append(System.lineSeparator());
        if (color != null) {
            sb.append("    edge [color=" + color + "]").append(System.lineSeparator());
        }
        for (Entry<String, SortedSet<String>> reads : readsOfKind.entrySet()) {
            for (String read : reads.getValue()) {
                sb.append("    \"").append(reads.getKey()).append("\" -> \"").append(read).append("\";\n");
            }
        }

        sb.append("  }").append(System.lineSeparator());
    }
}

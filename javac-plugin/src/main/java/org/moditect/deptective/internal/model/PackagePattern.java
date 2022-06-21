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

import java.util.regex.Pattern;

/**
 * An expression matching one or more whitelisted Java packages, i.e. packages that other packages are always allowed to
 * depend on.
 *
 * @author Gunnar Morling
 */
public class PackagePattern implements Comparable<PackagePattern> {

    private static final String ALL_EXTERNAL_PATTERN = "*ALL_EXTERNAL*";
    public static final PackagePattern ALL_EXTERNAL = new PackagePattern(ALL_EXTERNAL_PATTERN);

    private final String pattern;
    private final Pattern regex;

    private PackagePattern(String pattern) {
        this.pattern = pattern;
        this.regex = Pattern.compile(pattern.replace("*", ".*"));
    }

    public static PackagePattern getPattern(String pattern) {
        if (pattern.equals(ALL_EXTERNAL_PATTERN)) {
            return ALL_EXTERNAL;
        }
        else {
            return new PackagePattern(pattern);
        }
    }

    public boolean matches(String packageName) {
        return regex.matcher(packageName).matches();
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public int compareTo(PackagePattern o) {
        return pattern.compareTo(o.pattern);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pattern.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PackagePattern other = (PackagePattern) obj;

        return pattern.equals(other.pattern);
    }
}

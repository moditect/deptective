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
package org.moditect.deptective.internal.model;

import java.util.regex.Pattern;

/**
 * An expression matching one or more whitelisted Java packages, i.e. packages that other packages are always allowed to
 * depend on.
 *
 * @author Gunnar Morling
 */
public class WhitelistedPackagePattern {

    private static final String ALL_EXTERNAL_PATTERN = "*ALL_EXTERNAL*";
    public static final WhitelistedPackagePattern ALL_EXTERNAL = new WhitelistedPackagePattern(ALL_EXTERNAL_PATTERN);
    private final Pattern pattern;

    private WhitelistedPackagePattern(String pattern) {
        this.pattern = Pattern.compile(pattern.replace("*", ".*"));
    }

    public static WhitelistedPackagePattern getPattern(String pattern) {
        if (pattern.equals(ALL_EXTERNAL_PATTERN)) {
            return ALL_EXTERNAL;
        }
        else {
            return new WhitelistedPackagePattern(pattern);
        }
    }

    public boolean matches(String packageName) {
        return pattern.matcher(packageName).matches();
    }

    @Override
    public String toString() {
        return pattern.pattern();
    }
}

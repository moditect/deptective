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
package org.moditect.deptective.internal;

import java.util.ListResourceBundle;

public class DeptectiveMessages extends ListResourceBundle {

    private static final String NOTE_PREFIX = "compiler.note.";
    private static final String ERROR_PREFIX = "compiler.err.";
    private static final String WARNING_PREFIX = "compiler.warn.";

    public static final String ILLEGAL_PACKAGE_DEPENDENCY = "deptective.illegalpackagedependency";
    public static final String NO_DEPTECTIVE_CONFIG_FOUND = "deptective.nodeptectiveconfigfound";
    public static final String PACKAGE_NOT_CONFIGURED = "deptective.packagenotconfigured";
    public static final String GENERATED_CONFIG = "deptective.generatedconfig";
    public static final String DOT_REPRESENTATION = "deptective.dotrepresentation";

    @Override
    protected final Object[][] getContents() {
        return new Object[][] {
            { ERROR_PREFIX + ILLEGAL_PACKAGE_DEPENDENCY, "package {0} must not access {1}" },
            { WARNING_PREFIX + ILLEGAL_PACKAGE_DEPENDENCY, "package {0} must not access {1}" },
            { ERROR_PREFIX + PACKAGE_NOT_CONFIGURED, "no Deptective configuration found for package {0}" },
            { WARNING_PREFIX + PACKAGE_NOT_CONFIGURED, "no Deptective configuration found for package {0}" },
            { ERROR_PREFIX + NO_DEPTECTIVE_CONFIG_FOUND, "Config file deptective.json was not found" },
            { NOTE_PREFIX + GENERATED_CONFIG, "Generated Deptective configuration:{0}{1}" },
            { NOTE_PREFIX + DOT_REPRESENTATION, "Converted Deptective configuration to Dot graph:{0}{1}" }
        };
    }
}

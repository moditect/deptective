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
package org.moditect.deptective.internal.log;

import java.util.ListResourceBundle;

public class DeptectiveMessages extends ListResourceBundle {

    public static final String NOTE_PREFIX = "compiler.note.";
    public static final String ERROR_PREFIX = "compiler.err.";
    public static final String WARNING_PREFIX = "compiler.warn.";

    public static final String ILLEGAL_PACKAGE_DEPENDENCY = "deptective.illegalpackagedependency";
    public static final String NO_DEPTECTIVE_CONFIG_FOUND = "deptective.nodeptectiveconfigfound";
    public static final String PACKAGE_NOT_CONFIGURED = "deptective.packagenotconfigured";
    public static final String GENERATED_CONFIG = "deptective.generatedconfig";
    public static final String GENERATED_DOT_REPRESENTATION = "deptective.dotrepresentation";
    public static final String PACKAGE_CONTAINED_IN_MULTIPLE_COMPONENTS = "deptective.packageinmultiplecomponents";
    public static final String CYCLE_IN_ARCHITECTURE = "deptective.cycleinarchitecture";
    public static final String CYCLE_IN_CODE_BASE = "deptective.cycleincodebase";

    @Override
    protected final Object[][] getContents() {
        return new Object[][] {
                { ERROR_PREFIX + ILLEGAL_PACKAGE_DEPENDENCY, "package {0} must not access {1}" },
                { WARNING_PREFIX + ILLEGAL_PACKAGE_DEPENDENCY, "package {0} must not access {1}" },
                { ERROR_PREFIX + PACKAGE_NOT_CONFIGURED, "no Deptective configuration found for package {0}" },
                { WARNING_PREFIX + PACKAGE_NOT_CONFIGURED, "no Deptective configuration found for package {0}" },
                { ERROR_PREFIX + NO_DEPTECTIVE_CONFIG_FOUND, "Config file deptective.json was not found" },
                { NOTE_PREFIX + GENERATED_CONFIG, "Generated Deptective configuration template at {0}" },
                { NOTE_PREFIX + GENERATED_DOT_REPRESENTATION,
                        "Created DOT file representing the Deptective configuration at {0}" },
                { ERROR_PREFIX + PACKAGE_CONTAINED_IN_MULTIPLE_COMPONENTS,
                        "Multiple components match package {1}: {0}" },
                { ERROR_PREFIX + CYCLE_IN_ARCHITECTURE,
                        "Architecture model contains cycle(s) between these components: " + System.lineSeparator()
                                + "{0}" },
                { WARNING_PREFIX + CYCLE_IN_ARCHITECTURE,
                        "Architecture model contains cycle(s) between these components: " + System.lineSeparator()
                                + "{0}" },
                { ERROR_PREFIX + CYCLE_IN_CODE_BASE,
                        "Analysed code base contains cycle(s) between these components: " + System.lineSeparator()
                                + "{0}" },
                { WARNING_PREFIX + CYCLE_IN_CODE_BASE,
                        "Analysed code base contains cycle(s) between these components: " + System.lineSeparator()
                                + "{0}" },
        };
    }

    public String getFormat(String prefix, String key) {
        for (Object[] message : getContents()) {
            if (message[0].equals(prefix + key)) {
                return (String) message[1];
            }
        }

        throw new IllegalArgumentException("No message found for prefix " + prefix + " and key " + key);
    }
}

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
package org.moditect.deptective.testutil;

import java.text.MessageFormat;

import org.moditect.deptective.internal.options.DeptectiveOptions;

public class TestOptions {

    public static String deptectiveOptions(DeptectiveOptions.Options option, String value) {
        return MessageFormat.format("-Xplugin:Deptective {0}={1}", option.getName(), value);
    }

    public static String deptectiveOptions(DeptectiveOptions.Options option1, String value1,
            DeptectiveOptions.Options option2, String value2) {
        return MessageFormat
                .format("-Xplugin:Deptective {0}={1} {2}={3}", option1.getName(), value1, option2.getName(), value2);
    }

    public static String deptectiveOptions(DeptectiveOptions.Options option1, String value1,
            DeptectiveOptions.Options option2, String value2, DeptectiveOptions.Options option3, String value3) {
        return MessageFormat
                .format(
                        "-Xplugin:Deptective {0}={1} {2}={3} {4}={5}", option1.getName(), value1, option2.getName(),
                        value2, option3.getName(), value3
                );
    }

    public static String deptectiveOptions(DeptectiveOptions.Options option1, String value1,
            DeptectiveOptions.Options option2, String value2, DeptectiveOptions.Options option3, String value3,
            DeptectiveOptions.Options option4, String value4) {
        return MessageFormat
                .format(
                        "-Xplugin:Deptective {0}={1} {2}={3} {4}={5} {6}={7}", option1.getName(), value1,
                        option2.getName(), value2, option3.getName(), value3, option4.getName(), value4
                );
    }
}

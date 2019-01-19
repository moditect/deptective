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
package org.moditect.deptective.internal.util;

import java.util.Scanner;

public class Strings {

    public static String readToString(java.io.InputStream is) {
        try (Scanner s = new Scanner(is, "UTF-8")) {
            s.useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }
    }

    public static String lines(String line, String... furtherLines) {
        StringBuilder sb = new StringBuilder(line);
        sb.append(System.lineSeparator());

        if (furtherLines != null) {
            sb.append(String.join(System.lineSeparator(), furtherLines));
        }

        return sb.toString();
    }
}

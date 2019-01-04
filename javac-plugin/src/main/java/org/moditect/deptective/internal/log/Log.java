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
package org.moditect.deptective.internal.log;

import javax.tools.JavaFileObject;

import org.moditect.deptective.internal.options.ReportingPolicy;

import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.JCDiagnostic.Note;
import com.sun.tools.javac.util.Position;

public class Log {

    private final com.sun.tools.javac.util.Log log;

    private Log(com.sun.tools.javac.util.Log log) {
        this.log = log;
    }

    public static Log getInstance(com.sun.tools.javac.util.Log log) {
        return new Log(log);
    }

    public void report(ReportingPolicy reportingPolicy, String key, Object... params) {
        if (reportingPolicy == ReportingPolicy.ERROR) {
            log.error(Position.NOPOS, key, params);
        }
        else {
            log.strictWarning(null, key, params);
        }
    }

    public void report(ReportingPolicy reportingPolicy, DiagnosticPosition pos, String key, Object... params) {
        if (reportingPolicy == ReportingPolicy.ERROR) {
            log.error(pos.getPreferredPosition(), key, params);
        }
        else {
            log.strictWarning(pos, key, params);
        }
    }

    public void note(String key, Object... params) {
        log.note(new Note("compiler", key, params));
    }

    public void useSource(JavaFileObject file) {
        log.useSource(file);
    }
}

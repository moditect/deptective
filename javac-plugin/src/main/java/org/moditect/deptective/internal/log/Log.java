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

import java.text.MessageFormat;
import java.util.ResourceBundle;

import javax.tools.JavaFileObject;

import org.moditect.deptective.internal.options.ReportingPolicy;

import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.JCDiagnostic.Note;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.Position;

public class Log {

    private final com.sun.tools.javac.util.Log log;

    private final DeptectiveMessages messages;

    /**
     * The resource bundle cannot be registered in Java 8; this flag triggers fallback to emitting raw messages in this
     * case
     */
    private boolean registeredResourceBundle;

    private Log(com.sun.tools.javac.util.Log log, JavacMessages messages) {
        this.log = log;
        registeredResourceBundle = true;
        this.messages = new DeptectiveMessages();
        registerResourceBundle(messages);
    }

    public static Log getInstance(com.sun.tools.javac.util.Log log, JavacMessages messages) {
        return new Log(log, messages);
    }

    public void report(ReportingPolicy reportingPolicy, String key, Object... params) {
        if (reportingPolicy == ReportingPolicy.ERROR) {
            if (registeredResourceBundle) {
                log.error(Position.NOPOS, key, params);
            }
            else {
                MessageFormat format = new MessageFormat(messages.getFormat(DeptectiveMessages.ERROR_PREFIX, key));
                log.rawError(Position.NOPOS, format.format(params));
            }
        }
        else {
            if (registeredResourceBundle) {
                log.strictWarning(null, key, params);
            }
            else {
                MessageFormat format = new MessageFormat(messages.getFormat(DeptectiveMessages.WARNING_PREFIX, key));
                log.rawWarning(Position.NOPOS, format.format(params));
            }
        }
    }

    public void report(ReportingPolicy reportingPolicy, DiagnosticPosition pos, String key, Object... params) {
        if (reportingPolicy == ReportingPolicy.ERROR) {
            if (registeredResourceBundle) {
                log.error(pos.getPreferredPosition(), key, params);
            }
            else {
                MessageFormat format = new MessageFormat(messages.getFormat(DeptectiveMessages.ERROR_PREFIX, key));
                log.rawError(pos.getPreferredPosition(), format.format(params));
            }
        }
        else {
            if (registeredResourceBundle) {
                log.strictWarning(pos, key, params);
            }
            else {
                MessageFormat format = new MessageFormat(messages.getFormat(DeptectiveMessages.WARNING_PREFIX, key));
                log.rawWarning(pos.getPreferredPosition(), format.format(params));
            }
        }
    }

    public void note(String key, Object... params) {
        // no "raw" API for producing notes; so omitting them on Java 8
        if (registeredResourceBundle) {
            log.note(new Note("compiler", key, params));
        }
    }

    public void useSource(JavaFileObject file) {
        log.useSource(file);
    }

    private void registerResourceBundle(JavacMessages messages) {
        // Without touch the class here, I'm getting a weird classloading error
        // when using Maven and not having <fork>true</fork> :(
        DeptectiveMessages.class.getName();

        try {
            messages.add(l -> ResourceBundle.getBundle(DeptectiveMessages.class.getName(), l));
        }
        // add(ResourceBundleHelper) doesn't exist in Java 8
        catch (Throwable t) {
            registeredResourceBundle = false;
        }
    }
}

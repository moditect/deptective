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
package org.moditect.deptective;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.DeptectiveMessages;
import org.moditect.deptective.internal.DeptectiveTreeVisitor;
import org.moditect.deptective.internal.ReportingPolicy;
import org.moditect.deptective.internal.model.ConfigParser;
import org.moditect.deptective.internal.model.PackageDependencies;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacMessages;

public class DeptectivePlugin implements Plugin {

    @Override
    public String getName() {
        return "Deptective";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Context context = ((BasicJavacTask) task).getContext();

        // Without touch the class here, I'm getting a weird classloading error
        // when using Maven and not having <fork>true</fork> :(
        DeptectiveMessages.class.getName();

        JavacMessages messages = context.get(JavacMessages.messagesKey);
        messages.add(l -> ResourceBundle.getBundle(DeptectiveMessages.class.getName(), l));

        DeptectiveOptions options = new DeptectiveOptions(JavacProcessingEnvironment.instance(context).getOptions());

        PackageDependencies config = getConfig(context, options);
        ReportingPolicy reportingPolicy = options.getReportingPolicy();

        task.addTaskListener(new TaskListener() {

            @Override
            public void started(TaskEvent e) {
            }

            @Override
            public void finished(TaskEvent e) {
                if(e.getKind().equals(TaskEvent.Kind.ANALYZE)) {
                    CompilationUnitTree compilationUnit = e.getCompilationUnit();
                    new DeptectiveTreeVisitor(config, reportingPolicy, task).scan(compilationUnit, null);
                }
            }
        });
    }

    private PackageDependencies getConfig(Context context, DeptectiveOptions options) {
        try {
            try (InputStream is = getConfig(options.getConfigFilePath(), context)) {
                return new ConfigParser(is).getPackageDependencies();
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getConfig(Optional<Path> configFile, Context context) {
        try {
            if (configFile.isPresent()) {
                    return Files.newInputStream(configFile.get());
            }
            else {
                JavaFileManager jfm = context.get(JavaFileManager.class);
                return jfm.getFileForInput(StandardLocation.SOURCE_PATH, "", "deptective.json").openInputStream();

            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load Deptective configuration file", e);
        }
    }
}

/*
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
package org.moditect.deptective;

import javax.tools.JavaFileManager;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import org.moditect.deptective.internal.DeptectiveTreeVisitor;
import org.moditect.deptective.internal.handler.PackageReferenceHandler;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.ConfigLoader;
import org.moditect.deptective.internal.options.DeptectiveOptions;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskEvent.Kind;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JavacMessages;

public class DeptectivePlugin implements Plugin {

    /**
     * {@link TaskEvent.Kind} doesn't support COMPILATION on JDK 8, hence it's resembled here.
     */
    private enum TaskEventKind {
        PARSE,
        ANALYZE,
        LAST_ANALYZE,
        COMPILATION,
        OTHER;
    }

    private static final boolean HAS_KIND_COMPILATION = hasKindCompilation();

    @Override
    public String getName() {
        return "Deptective";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Context context = ((BasicJavacTask) task).getContext();

        DeptectiveOptions options = new DeptectiveOptions(args);

        Log log = Log.getInstance(
                context.get(JavacProcessingEnvironment.class),
                context.get(JavacMessages.messagesKey)
        );

        PackageReferenceHandler handler = options.getPluginTask()
                .getPackageReferenceHandler(
                        context.get(JavaFileManager.class),
                        options,
                        () -> new ConfigLoader()
                                .getConfig(options.getConfigFilePath(), context.get(JavaFileManager.class)),
                        log
                );

        if (handler.configIsValid()) {
            task.addTaskListener(new TaskListener() {

                private int sourceFileCount = 0;
                private int analyzed = 0;

                @Override
                public void started(TaskEvent e) {
                }

                @Override
                public void finished(TaskEvent e) {
                    TaskEventKind kind = getTaskEventKind(e.getKind(), sourceFileCount, analyzed);

                    if (kind == TaskEventKind.PARSE) {
                        sourceFileCount++;
                    }
                    else if (kind == TaskEventKind.ANALYZE || kind == TaskEventKind.LAST_ANALYZE) {
                        analyzed++;

                        CompilationUnitTree compilationUnit = e.getCompilationUnit();
                        new DeptectiveTreeVisitor(task, log, handler).scan(compilationUnit, null);

                        // On JDK 8 there's no callback for the completion of the compilation,
                        // so this handler is invoked after analyzing the last source file
                        if (kind == TaskEventKind.LAST_ANALYZE) {
                            handler.onCompletingCompilation();
                        }
                    }
                    else if (kind == TaskEventKind.COMPILATION) {
                        handler.onCompletingCompilation();
                    }
                }
            });
        }
    }

    private TaskEventKind getTaskEventKind(TaskEvent.Kind kind, int totalSourceFiles, int analyzedSourceFiles) {
        if (kind == Kind.PARSE) {
            return TaskEventKind.PARSE;
        }
        else if (kind == Kind.ANALYZE) {
            if (!HAS_KIND_COMPILATION && analyzedSourceFiles >= totalSourceFiles - 1) {
                return TaskEventKind.LAST_ANALYZE;
            }
            else {
                return TaskEventKind.ANALYZE;
            }

        }
        else if (kind.name().equals("COMPILATION")) {
            return TaskEventKind.COMPILATION;
        }
        else {
            return TaskEventKind.OTHER;
        }
    }

    private static boolean hasKindCompilation() {
        for (TaskEvent.Kind kind : TaskEvent.Kind.values()) {
            if (kind.name().equals("COMPILATION")) {
                return true;
            }
        }

        return false;
    }
}

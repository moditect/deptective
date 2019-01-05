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
package org.moditect.deptective.internal.handler;

import java.io.IOException;
import java.io.Writer;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.options.ReportingPolicy;

/**
 * Emits the given {@code deptective.json} as Dot file (GraphViz).
 *
 * @author Gunnar Morling
 */
public class PackageReferenceVisualizer implements PackageReferenceHandler {

    private final Log log;
    private final PackageDependencies packageDependencies;
    private final JavaFileManager jfm;

    public PackageReferenceVisualizer( JavaFileManager jfm, PackageDependencies packageDependencies, Log log) {
        this.jfm = jfm;
        this.log = log;
        this.packageDependencies = packageDependencies;
    }

    @Override
    public boolean configIsValid() {
        if (packageDependencies == null) {
            log.report(ReportingPolicy.ERROR, DeptectiveMessages.NO_DEPTECTIVE_CONFIG_FOUND);
            return false;
        }

        return true;
    }

    @Override
    public void onCompletingCompilation() {
        log.useSource(null);

        try {
            FileObject output = jfm.getFileForOutput(StandardLocation.CLASS_OUTPUT, "", "deptective.dot", null);
            log.note(DeptectiveMessages.GENERATED_DOT_REPRESENTATION, output.toUri());
            Writer writer = output.openWriter();
            writer.append(packageDependencies.toDot());
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write deptective.dot file", e);
        }
    }
}

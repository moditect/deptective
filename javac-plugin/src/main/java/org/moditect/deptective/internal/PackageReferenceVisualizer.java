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

import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.PackageDependencies;

/**
 * Emits the given {@code deptective.json} as Dot file (GraphViz).
 *
 * @author Gunnar Morling
 */
public class PackageReferenceVisualizer implements PackageReferenceHandler {

    private final Log log;
    private final PackageDependencies packageDependencies;

    public PackageReferenceVisualizer(PackageDependencies packageDependencies, Log log) {
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
        log.note(DeptectiveMessages.GENERATED_DOT_REPRESENTATION, System.lineSeparator(), packageDependencies.toDot());
    }
}

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

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.tools.JavaFileManager;

import org.moditect.deptective.internal.handler.PackageReferenceCollector;
import org.moditect.deptective.internal.handler.PackageReferenceHandler;
import org.moditect.deptective.internal.handler.PackageReferenceValidator;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.Component;
import org.moditect.deptective.internal.model.Components;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.options.DeptectiveOptions;

/**
 * Describes the {@link PackageReferenceHandler} to be invoked when traversing the ASTs of the project under
 * compilation.
 *
 * @author Gunnar Morling
 */
public enum PluginTask {

    VALIDATE {

        @Override
        public PackageReferenceHandler getPackageReferenceHandler(JavaFileManager jfm, DeptectiveOptions options,
                Supplier<PackageDependencies> configSupplier, Log log) {
            return new PackageReferenceValidator(
                    jfm,
                    configSupplier.get(),
                    options.getReportingPolicy(),
                    options.getUnconfiguredPackageReportingPolicy(),
                    options.getCycleReportingPolicy(),
                    options.createDotFile(),
                    log
            );
        }
    },
    ANALYZE {

        @Override
        public PackageReferenceHandler getPackageReferenceHandler(JavaFileManager jfm, DeptectiveOptions options,
                Supplier<PackageDependencies> configSupplier, Log log) {

            Set<Component> components = options.getComponentPackagePatterns()
                    .entrySet()
                    .stream()
                    .map(e -> new Component.Builder(e.getKey())
                            .addContains(e.getValue())
                            .build()
                    )
                    .collect(Collectors.toSet());

            return new PackageReferenceCollector(
                    jfm,
                    log,
                    options.getWhitelistedPackagePatterns(),
                    new Components(components),
                    options.createDotFile()
            );
        }
    };

    public abstract PackageReferenceHandler getPackageReferenceHandler(JavaFileManager jfm, DeptectiveOptions options,
            Supplier<PackageDependencies> configSupplier, Log log);
}

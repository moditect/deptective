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

import org.moditect.deptective.internal.log.Log;

import com.sun.tools.javac.util.Context;

/**
 * Describes the {@link PackageReferenceHandler} to be invoked when traversing
 * the ASTs of the project under compilation.
 *
 * @author Gunnar Morling
 */
public enum PluginTask {

    VALIDATE {
        @Override
        public PackageReferenceHandler getPackageReferenceHandler(DeptectiveOptions options, Context context, Log log) {
            return new PackageReferenceValidator(
                    context,
                    options.getConfigFilePath(),
                    options.getReportingPolicy(),
                    options.getUnconfiguredPackageReportingPolicy(),
                    log
          );
        }
    },
    VISUALIZE {
        @Override
        public PackageReferenceHandler getPackageReferenceHandler(DeptectiveOptions options, Context context, Log log) {
            return new PackageReferenceVisualizer(
                    context,
                    options.getConfigFilePath(),
                    log
          );
        }
    },
    ANALYZE {
        @Override
        public PackageReferenceHandler getPackageReferenceHandler(DeptectiveOptions options, Context context, Log log) {
            return new PackageReferenceCollector(log);
        }
    };

    public abstract PackageReferenceHandler getPackageReferenceHandler(DeptectiveOptions options, Context context, Log log);
}

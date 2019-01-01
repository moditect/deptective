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

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.moditect.deptective.internal.ReportingPolicy;

class DeptectiveOptions {

    private final Map<String, String> options;

    public DeptectiveOptions(Map<String, String> options) {
        this.options = Collections.unmodifiableMap(options);
    }

    public Optional<Path> getConfigFilePath() {
        String path = options.get("deptective.configfile");

        if (path != null) {
            return Optional.of(new File(path).toPath());
        }
        else {
            return Optional.empty();
        }
    }

    public ReportingPolicy getReportingPolicy() {
        String policy = options.get("deptective.reportingpolicy");

        if (policy != null) {
            return ReportingPolicy.valueOf(policy.trim().toUpperCase());
        }
        else {
            return ReportingPolicy.ERROR;
        }
    }
}
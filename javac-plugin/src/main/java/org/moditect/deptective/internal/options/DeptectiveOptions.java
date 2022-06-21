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
package org.moditect.deptective.internal.options;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.moditect.deptective.internal.PluginTask;
import org.moditect.deptective.internal.model.PackagePattern;

/**
 * The options supported by the Deptective plug-in. To be given as javac plugin arguments through the
 * command line.
 *
 * <pre>
 *     -Xplugin:Deptective arg1=val1 ... argN=valN
 * </pre>
 *
 * @author Gunnar Morling
 */
public class DeptectiveOptions {

    public enum Options {
        COMPONENTS("components"),
        WHITELISTED("whitelisted"),
        VISUALIZE("visualize"),
        MODE("mode"),
        CYCLE_REPORTING_POLICY("cycle_reporting_policy"),
        UNCONFIGURED_PACKAGE_REPORTING_POLICY("unconfigured_package_reporting_policy"),
        REPORTING_POLICY("reporting_policy"),
        CONFIG_FILE("config_file");

        private final String name;

        Options(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public String getFrom(Map<String, String> options) {
            return options.get(name);
        }
    }

    private final Map<String, String> options;

    public DeptectiveOptions(String... args) {
        if (args != null) {
            this.options = Collections.unmodifiableMap(
                    Arrays.stream(args)
                            .map(o -> o.split("="))
                            .collect(Collectors.toMap(o -> o[0], o -> o[1]))
            );
        }
        else {
            this.options = Collections.emptyMap();
        }
    }

    public Optional<Path> getConfigFilePath() {
        String path = Options.CONFIG_FILE.getFrom(options);

        if (path != null) {
            return Optional.of(new File(path).toPath());
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns the policy for reporting illegal package references.
     */
    public ReportingPolicy getReportingPolicy() {
        String policy = Options.REPORTING_POLICY.getFrom(options);

        if (policy != null) {
            return ReportingPolicy.valueOf(policy.trim().toUpperCase());
        }
        else {
            return ReportingPolicy.ERROR;
        }
    }

    /**
     * Returns the policy for reporting unconfigured packages.
     */
    public ReportingPolicy getUnconfiguredPackageReportingPolicy() {
        String policy = Options.UNCONFIGURED_PACKAGE_REPORTING_POLICY.getFrom(options);

        if (policy != null) {
            return ReportingPolicy.valueOf(policy.trim().toUpperCase());
        }
        else {
            return ReportingPolicy.WARN;
        }
    }

    /**
     * Returns the policy for reporting cycles between components.
     */
    public ReportingPolicy getCycleReportingPolicy(ReportingPolicy defaultPolicy) {
        String policy = Options.CYCLE_REPORTING_POLICY.getFrom(options);

        if (policy != null) {
            return ReportingPolicy.valueOf(policy.trim().toUpperCase());
        }
        else {
            return defaultPolicy;
        }
    }

    /**
     * Returns the task to be performed by the plug-in.
     */
    public PluginTask getPluginTask() {
        String mode = Options.MODE.getFrom(options);

        if (mode != null) {
            return PluginTask.valueOf(mode.trim().toUpperCase());
        }
        else {
            return PluginTask.VALIDATE;
        }
    }

    public boolean createDotFile() {
        String visualize = Options.VISUALIZE.getFrom(options);

        return visualize != null && Boolean.parseBoolean(visualize.trim());
    }

    public List<PackagePattern> getWhitelistedPackagePatterns() {
        String whitelisted = Options.WHITELISTED.getFrom(options);

        if (whitelisted != null) {
            String[] patterns = whitelisted.split(",");
            return Arrays.stream(patterns)
                    .map(String::trim)
                    .map(PackagePattern::getPattern)
                    .collect(Collectors.toList());
        }
        else {
            return Collections.emptyList();
        }
    }

    public Map<String, List<PackagePattern>> getComponentPackagePatterns() {
        String components = Options.COMPONENTS.getFrom(options);

        if (components != null) {
            Map<String, List<PackagePattern>> componentPatterns = new HashMap<String, List<PackagePattern>>();
            String[] patterns = components.split(";");

            for (String patternsForComponent : patterns) {
                String[] componentAndPatterns = patternsForComponent.split(":");
                String component = componentAndPatterns[0];
                List<PackagePattern> patternsOfComponent = Arrays.stream(componentAndPatterns[1].split(","))
                        .map(String::trim)
                        .map(PackagePattern::getPattern)
                        .collect(Collectors.toList());

                componentPatterns.put(component, patternsOfComponent);
            }

            return componentPatterns;
        }
        else {
            return Collections.emptyMap();
        }
    }
}

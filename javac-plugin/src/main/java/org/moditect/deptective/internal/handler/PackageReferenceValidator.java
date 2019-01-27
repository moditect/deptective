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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.export.DotSerializer;
import org.moditect.deptective.internal.graph.Cycle;
import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.Component;
import org.moditect.deptective.internal.model.PackageAssignedToMultipleComponentsException;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.model.ReadKind;
import org.moditect.deptective.internal.options.ReportingPolicy;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

/**
 * Validates a project's package relationships against a given description of allowed references in
 * {@code deptective.json}.
 *
 * @author Gunnar Morling
 */
public class PackageReferenceValidator implements PackageReferenceHandler {

    private final Log log;
    private final PackageDependencies allowedPackageDependencies;
    private final JavaFileManager jfm;
    private final ReportingPolicy reportingPolicy;
    private final ReportingPolicy unconfiguredPackageReportingPolicy;
    private final ReportingPolicy cycleReportingPolicy;
    private final Map<String, Boolean> reportedUnconfiguredPackages;
    private final PackageDependencies.Builder actualPackageDependencies;

    private boolean createDotFile;
    private String currentPackageName;
    private Component currentComponent;

    public PackageReferenceValidator(JavaFileManager jfm, PackageDependencies packageDependencies,
            ReportingPolicy reportingPolicy, ReportingPolicy unconfiguredPackageReportingPolicy,
            ReportingPolicy cycleReportingPolicy, boolean createDotFile,
            Log log) {
        this.log = log;
        this.allowedPackageDependencies = packageDependencies;
        this.jfm = jfm;
        this.reportingPolicy = reportingPolicy;
        this.unconfiguredPackageReportingPolicy = unconfiguredPackageReportingPolicy;
        this.cycleReportingPolicy = cycleReportingPolicy;
        this.reportedUnconfiguredPackages = new HashMap<>();
        this.actualPackageDependencies = PackageDependencies.builder();
        this.createDotFile = createDotFile;
    }

    @Override
    public boolean configIsValid() {
        if (allowedPackageDependencies == null) {
            log.report(ReportingPolicy.ERROR, DeptectiveMessages.NO_DEPTECTIVE_CONFIG_FOUND);
            return false;
        }

        return true;
    }

    @Override
    public boolean onEnteringCompilationUnit(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return false;
        }

        String packageName = packageNameTree.toString();
        currentPackageName = packageName;

        try {
            currentComponent = allowedPackageDependencies.getComponentByPackage(packageName);

            if (currentComponent == null) {
                reportUnconfiguredPackageIfNeeded(tree, packageName);
            }
        }
        catch (PackageAssignedToMultipleComponentsException e) {
            log.report(
                    ReportingPolicy.ERROR,
                    DeptectiveMessages.PACKAGE_CONTAINED_IN_MULTIPLE_COMPONENTS,
                    String.join(
                            ", ",
                            e.getMatchingComponents()
                                    .stream()
                                    .map(Component::getName)
                                    .sorted()
                                    .collect(Collectors.toList())
                    ),
                    packageName
            );

            createDotFile = false;
            return false;
        }

        return true;
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        if (isIgnoredDependency(referencedPackageName)) {
            return;
        }

        Component referencedComponent = allowedPackageDependencies.getComponentByPackage(referencedPackageName);

        if (referencedComponent == null) {
            referencedComponent = Component.builder(referencedPackageName).build();
        }

        if (currentComponent == null) {
            actualPackageDependencies.addRead(
                    Component.builder(currentPackageName).build().getName(),
                    referencedComponent.getName(),
                    ReadKind.UKNOWN
            );
        }
        else if (currentComponent.allowedToRead(referencedComponent)) {
            actualPackageDependencies.addRead(
                    currentComponent.getName(),
                    referencedComponent.getName(),
                    ReadKind.ALLOWED
            );
        }
        else {
            actualPackageDependencies.addRead(
                    currentComponent.getName(),
                    referencedComponent.getName(),
                    ReadKind.DISALLOWED
            );

            log.report(
                    reportingPolicy,
                    (com.sun.tools.javac.tree.JCTree) referencingNode,
                    DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY,
                    currentComponent.getName(),
                    referencedPackageName
            );
        }
    }

    @Override
    public void onCompletingCompilation() {
        log.useSource(null);

        List<Cycle<Component>> cycles = GraphUtils.detectCycles(allowedPackageDependencies.getComponents());

        if (!cycles.isEmpty()) {
            String cyclesAsString = "- " + cycles.stream()
                    .map(Cycle::toString)
                    .collect(Collectors.joining("," + System.lineSeparator() + "- "));

            log.report(cycleReportingPolicy, DeptectiveMessages.CYCLE_IN_ARCHITECTURE, cyclesAsString);
        }

        if (!createDotFile) {
            return;
        }

        if (!cycles.isEmpty()) {
            for (Component.Builder component : actualPackageDependencies.getComponents()) {
                for (Cycle<Component> cycle : cycles) {
                    if (contains(cycle, component.getName())) {
                        for (Component nodeInCycle : cycle.getNodes()) {
                            if (component.getReads().containsKey(nodeInCycle.getName())) {
                                component.addRead(nodeInCycle.getName(), ReadKind.CYCLE);
                            }
                        }
                    }
                }
            }
        }

        DotSerializer serializer = new DotSerializer();
        actualPackageDependencies.build().serialize(serializer);

        try {
            FileObject output = jfm.getFileForOutput(StandardLocation.CLASS_OUTPUT, "", "deptective.dot", null);
            log.note(DeptectiveMessages.GENERATED_DOT_REPRESENTATION, output.toUri());
            Writer writer = output.openWriter();
            writer.append(serializer.serialize());
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write deptective.dot file", e);
        }
    }

    private boolean contains(Cycle<Component> cycle, String name) {
        for (Component component : cycle.getNodes()) {
            if (component.getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private boolean isIgnoredDependency(String referencedPackageName) {
        return "java.lang".equals(referencedPackageName) ||
                allowedPackageDependencies.isWhitelisted(referencedPackageName) ||
                currentPackageName.equals(referencedPackageName) ||
                referencedPackageName.isEmpty();
    }

    private void reportUnconfiguredPackageIfNeeded(CompilationUnitTree tree, String packageName) {
        boolean reportedBefore = Boolean.TRUE.equals(reportedUnconfiguredPackages.get(packageName));

        if (!reportedBefore) {
            log.report(
                    unconfiguredPackageReportingPolicy,
                    (com.sun.tools.javac.tree.JCTree) tree,
                    DeptectiveMessages.PACKAGE_NOT_CONFIGURED, packageName
            );

            reportedUnconfiguredPackages.put(packageName, true);
        }
    }
}

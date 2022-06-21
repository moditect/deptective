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
package org.moditect.deptective.internal.handler;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.export.DotSerializer;
import org.moditect.deptective.internal.export.JsonSerializer;
import org.moditect.deptective.internal.export.ModelSerializer;
import org.moditect.deptective.internal.graph.Cycle;
import org.moditect.deptective.internal.graph.GraphUtils;
import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.Component;
import org.moditect.deptective.internal.model.Components;
import org.moditect.deptective.internal.model.IdentifiableComponent;
import org.moditect.deptective.internal.model.PackageAssignedToMultipleComponentsException;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.model.PackagePattern;
import org.moditect.deptective.internal.model.ReadKind;
import org.moditect.deptective.internal.options.ReportingPolicy;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

/**
 * A handler that produces a candidate {@code deptective.json} file based on the actual package relationships of the
 * project under compilation.
 *
 * @author Gunnar Morling
 */
public class PackageReferenceCollector implements PackageReferenceHandler {

    private final Log log;
    private final boolean createDotFile;
    private final PackageDependencies.Builder builder;

    private final JavaFileManager jfm;
    private final List<PackagePattern> whitelistPatterns;
    private final ReportingPolicy cycleReportingPolicy;

    /**
     * Any components that were declared externally.
     */
    private final Components declaredComponents;
    private final Set<String> packagesOfCurrentCompilation;
    private final Set<String> referencedPackages;

    private String currentPackageName;
    private Component currentComponent;
    private boolean createOutputFile = true;

    public PackageReferenceCollector(JavaFileManager jfm, Log log, List<PackagePattern> whitelistPatterns,
            ReportingPolicy cycleReportingPolicy, Components declaredComponents, boolean createDotFile) {
        this.log = log;
        this.jfm = jfm;
        this.whitelistPatterns = Collections.unmodifiableList(whitelistPatterns);
        this.cycleReportingPolicy = cycleReportingPolicy;
        this.declaredComponents = declaredComponents;
        this.createDotFile = createDotFile;

        this.packagesOfCurrentCompilation = new HashSet<String>();
        this.referencedPackages = new HashSet<String>();

        builder = PackageDependencies.builder();

        for (Component component : declaredComponents) {
            for (PackagePattern contained : component.getContained()) {
                builder.addContains(component.getName(), contained);
            }
        }
    }

    @Override
    public boolean onEnteringCompilationUnit(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return false;
        }

        currentPackageName = packageNameTree.toString();
        packagesOfCurrentCompilation.add(currentPackageName);

        try {
            currentComponent = declaredComponents.getComponentByPackage(currentPackageName);
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
                    currentPackageName
            );

            createOutputFile = false;
            return false;
        }

        if (currentComponent == null) {
            builder.addContains(currentPackageName, PackagePattern.getPattern(currentPackageName));
        }

        return true;
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        referencedPackages.add(referencedPackageName);

        Component referencedComponent = declaredComponents.getComponentByPackage(referencedPackageName);

        builder.addRead(
                currentComponent != null ? currentComponent.getName() : currentPackageName,
                referencedComponent != null ? referencedComponent.getName() : referencedPackageName,
                ReadKind.ALLOWED
        );
    }

    @Override
    public void onCompletingCompilation() {
        if (!createOutputFile) {
            return;
        }

        List<PackagePattern> effectiveWhitelistPatterns;

        if (isWhitelistAllExternal()) {
            Set<String> externalPackages = new HashSet<>(referencedPackages);
            externalPackages.removeAll(packagesOfCurrentCompilation);
            effectiveWhitelistPatterns = externalPackages.stream()
                    .filter(p -> !p.equals("java.lang"))
                    .map(PackagePattern::getPattern)
                    .collect(Collectors.toList());
        }
        else {
            effectiveWhitelistPatterns = whitelistPatterns;
        }

        for (PackagePattern whitelistedPackagePattern : effectiveWhitelistPatterns) {
            // removes any explicit read to that package
            builder.addWhitelistedPackage(whitelistedPackagePattern);
        }

        PackageDependencies packageDependencies = builder.build();

        log.useSource(null);

        ModelSerializer serializer = new JsonSerializer();
        packageDependencies.serialize(serializer);

        try {
            FileObject output = jfm.getFileForOutput(StandardLocation.SOURCE_OUTPUT, "", "deptective.json", null);
            log.note(DeptectiveMessages.GENERATED_CONFIG, output.toUri());
            Writer writer = output.openWriter();
            writer.append(serializer.serialize());
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write deptective.json file", e);
        }

        List<Cycle<IdentifiableComponent>> cycles = GraphUtils.detectCycles(packageDependencies.getComponents());

        if (!cycles.isEmpty()) {
            String cyclesAsString = "- " + cycles.stream()
                    .map(Cycle::toString)
                    .collect(Collectors.joining("," + System.lineSeparator() + "- "));

            log.report(cycleReportingPolicy, DeptectiveMessages.CYCLE_IN_CODE_BASE, cyclesAsString);
        }

        if (createDotFile) {
            builder.updateFromCycles(cycles);
            packageDependencies = builder.build();

            serializer = new DotSerializer();
            packageDependencies.serialize(serializer);

            try {
                FileObject output = jfm.getFileForOutput(StandardLocation.SOURCE_OUTPUT, "", "deptective.dot", null);
                log.note(DeptectiveMessages.GENERATED_DOT_REPRESENTATION, output.toUri());
                Writer writer = output.openWriter();
                writer.append(serializer.serialize());
                writer.close();
            }
            catch (IOException e) {
                throw new RuntimeException("Failed to write deptective.dot file", e);
            }
        }
    }

    private boolean isWhitelistAllExternal() {
        return whitelistPatterns.contains(PackagePattern.ALL_EXTERNAL);
    }
}

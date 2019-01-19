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
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.export.DotSerializer;
import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.Package.ReadKind;
import org.moditect.deptective.internal.model.PackageDependencies;
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
    private final boolean createDotFile;
    private final ReportingPolicy unconfiguredPackageReportingPolicy;
    private final Map<String, Boolean> reportedUnconfiguredPackages;

    private final PackageDependencies.Builder actualPackageDependencies;

    private Package currentPackage;

    public PackageReferenceValidator(JavaFileManager jfm, PackageDependencies packageDependencies,
            ReportingPolicy reportingPolicy, ReportingPolicy unconfiguredPackageReportingPolicy, boolean createDotFile,
            Log log) {
        this.log = log;
        this.allowedPackageDependencies = packageDependencies;
        this.jfm = jfm;
        this.reportingPolicy = reportingPolicy;
        this.unconfiguredPackageReportingPolicy = unconfiguredPackageReportingPolicy;
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
    public void onEnteringCompilationUnit(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return;
        }

        String packageName = packageNameTree.toString();
        currentPackage = allowedPackageDependencies.getPackage(packageName);

        if (!currentPackage.isConfigured()) {
            reportUnconfiguredPackageIfNeeded(tree, packageName);
        }
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        if (isIgnoredDependency(referencedPackageName)) {
            return;
        }

        ReadKind readKind;

        if (!currentPackage.isConfigured()) {
            readKind = ReadKind.UKNOWN;
        }
        else if (currentPackage.allowedToRead(referencedPackageName)) {
            readKind = ReadKind.ALLOWED;
        }
        else {
            readKind = ReadKind.DISALLOWED;
        }

        actualPackageDependencies.addRead(currentPackage.getName(), referencedPackageName, readKind);

        if (!currentPackage.isConfigured()) {
            return;
        }

        if (!currentPackage.allowedToRead(referencedPackageName)) {
            log.report(
                    reportingPolicy,
                    (com.sun.tools.javac.tree.JCTree) referencingNode,
                    DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY,
                    currentPackage,
                    referencedPackageName
            );
        }
    }

    @Override
    public void onCompletingCompilation() {
        log.useSource(null);

        if (!createDotFile) {
            return;
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

    private boolean isIgnoredDependency(String referencedPackageName) {
        return "java.lang".equals(referencedPackageName) ||
                allowedPackageDependencies.isWhitelisted(referencedPackageName) ||
                currentPackage.getName().equals(referencedPackageName) ||
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

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

import java.util.HashMap;
import java.util.Map;

import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.options.ReportingPolicy;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

/**
 * Validates a project's package relationships against a given description of
 * allowed references in {@code deptective.json}.
 *
 * @author Gunnar Morling
 */
public class PackageReferenceValidator implements PackageReferenceHandler {

    private final Log log;
    private final PackageDependencies packageDependencies;
    private final ReportingPolicy reportingPolicy;
    private final ReportingPolicy unconfiguredPackageReportingPolicy;
    private final Map<String, Boolean> reportedUnconfiguredPackages;

    private Package currentPackage;

    public PackageReferenceValidator(PackageDependencies packageDependencies,
            ReportingPolicy reportingPolicy, ReportingPolicy unconfiguredPackageReportingPolicy, Log log) {
        this.log = log;
        this.packageDependencies = packageDependencies;
        this.reportingPolicy = reportingPolicy;
        this.unconfiguredPackageReportingPolicy = unconfiguredPackageReportingPolicy;
        this.reportedUnconfiguredPackages = new HashMap<>();
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
    public void onEnteringCompilationUnit(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return;
        }

        String packageName = packageNameTree.toString();
        currentPackage = packageDependencies.getPackage(packageName);

        if (!currentPackage.isConfigured()) {
            reportUnconfiguredPackageIfNeeded(tree, packageName);
        }
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        if ("java.lang".equals(referencedPackageName)) {
            return;
        }

        if (packageDependencies.isWhitelisted(referencedPackageName)) {
            return;
        }

        if (!currentPackage.isConfigured()) {
            return;
        }

        if (currentPackage.getName().equals(referencedPackageName)) {
            return;
        }

        if (referencedPackageName.isEmpty() || currentPackage.reads(referencedPackageName)) {
            return;
        }

        log.report(
                reportingPolicy,
                (com.sun.tools.javac.tree.JCTree)referencingNode,
                DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY,
                currentPackage,
                referencedPackageName
        );
    }

    private void reportUnconfiguredPackageIfNeeded(CompilationUnitTree tree, String packageName) {
        boolean reportedBefore = Boolean.TRUE.equals(reportedUnconfiguredPackages.get(packageName));

        if (!reportedBefore) {
            log.report(
                    unconfiguredPackageReportingPolicy,
                    (com.sun.tools.javac.tree.JCTree)tree,
                    DeptectiveMessages.PACKAGE_NOT_CONFIGURED, packageName
            );

            reportedUnconfiguredPackages.put(packageName, true);
        }
    }
}

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

import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.PackageDependencies;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

public class DeptectiveTreeVisitor extends TreePathScanner<Void, Void> {

    private final Log log;
    private final Elements elements;
    private final PackageDependencies packageDependencies;

    private Package packageOfCurrentCompilationUnit;
    private final ReportingPolicy reportingPolicy;

    public  DeptectiveTreeVisitor(PackageDependencies packageDependencies, ReportingPolicy reportingPolicy, JavacTask task) {
        elements = task.getElements();

        Context context = ((BasicJavacTask) task).getContext();
        this.log = Log.instance(context);
        this.packageDependencies = packageDependencies;
        this.reportingPolicy = reportingPolicy;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
        log.useSource(tree.getSourceFile());

        ExpressionTree packageName = tree.getPackageName();

        if (packageName != null) {
            packageOfCurrentCompilationUnit = packageDependencies.getPackage(packageName.toString());
        }

        if (packageOfCurrentCompilationUnit == null) {
            throw new IllegalArgumentException("Package " +packageName + " is not configured.");
        }

        return super.visitCompilationUnit(tree, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        com.sun.tools.javac.tree.JCTree jcTree = (com.sun.tools.javac.tree.JCTree)node;

        PackageElement pakkage = elements.getPackageOf(jcTree.type.asElement());
        String qualifiedName = pakkage.getQualifiedName().toString();

        if (!qualifiedName.isEmpty() && !packageOfCurrentCompilationUnit.reads(qualifiedName)) {
            if (reportingPolicy == ReportingPolicy.ERROR) {
                log.error(jcTree.pos, DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY, packageOfCurrentCompilationUnit, qualifiedName);
            }
            else {
                log.strictWarning(jcTree, DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY, packageOfCurrentCompilationUnit, qualifiedName);
            }
        }

        return super.visitVariable(node, p);
    }
}

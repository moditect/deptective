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
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

public class CodePatternTreeVisitor extends TreePathScanner<Void, Void> {

    private final Log log;
    private final Elements elements;
    private final PackageDependencies packageDependencies;

    private Package packageOfCurrentCompilationUnit;

    public  CodePatternTreeVisitor(PackageDependencies packageDependencies, JavacTask task) {
        elements = task.getElements();

        Context context = ((BasicJavacTask) task).getContext();
        this.log = Log.instance(context);
        this.packageDependencies = packageDependencies;
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
        Type type = ((com.sun.tools.javac.tree.JCTree)node).type;
        PackageElement pakkage = elements.getPackageOf(type.asElement());

        String qualifiedName = pakkage.getQualifiedName().toString();
        if (!qualifiedName.isEmpty() && !packageOfCurrentCompilationUnit.reads(qualifiedName)) {
            // log.rawError((int)sourcePositions.getStartPosition(currCompUnit, node), "Package: " + packageOfCurrentCompilationUnit + " doesn't read " + qualifiedName);
//            log.rawError(((com.sun.tools.javac.tree.JCTree)node).pos, "error: Package " + packageOfCurrentCompilationUnit + " doesn't read " + qualifiedName);
            log.error(((com.sun.tools.javac.tree.JCTree)node).pos, DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY, packageOfCurrentCompilationUnit, qualifiedName);
        }

        return super.visitVariable(node, p);
    }
}
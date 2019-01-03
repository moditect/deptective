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

import java.util.Map;

import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;

import org.moditect.deptective.internal.model.Package;
import org.moditect.deptective.internal.model.PackageDependencies;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Log;

public class DeptectiveTreeVisitor extends TreePathScanner<Void, Void> {

    private final Log log;
    private final Elements elements;
    private final PackageDependencies packageDependencies;
    private final ReportingPolicy reportingPolicy;
    private final ReportingPolicy unconfiguredPackageReportingPolicy;
    private final Map<String, Boolean> reportedUnconfiguredPackages;

    private Package packageOfCurrentCompilationUnit;

    public  DeptectiveTreeVisitor(PackageDependencies packageDependencies, DeptectiveOptions options, JavacTask task, Map<String, Boolean> reportedUnconfiguredPackages) {
        elements = task.getElements();

        Context context = ((BasicJavacTask) task).getContext();
        this.log = Log.instance(context);
        this.packageDependencies = packageDependencies;
        this.reportingPolicy = options.getReportingPolicy();
        this.unconfiguredPackageReportingPolicy = options.getUnconfiguredPackageReportingPolicy();
        this.reportedUnconfiguredPackages = reportedUnconfiguredPackages;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
        log.useSource(tree.getSourceFile());

        resetCurrentPackage(tree);

        return super.visitCompilationUnit(tree, p);
    }

    private void resetCurrentPackage(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return;
        }

        String packageName = packageNameTree.toString();
        packageOfCurrentCompilationUnit = packageDependencies.getPackage(packageName);

        if (!packageOfCurrentCompilationUnit.isConfigured()) {
            reportUnconfiguredPackageIfNeeded(tree, packageName);
        }
    }

    private void reportUnconfiguredPackageIfNeeded(CompilationUnitTree tree, String packageName) {
        boolean reportedBefore = Boolean.TRUE.equals(reportedUnconfiguredPackages.get(packageName));

        if (!reportedBefore) {
            com.sun.tools.javac.tree.JCTree jcTree = (com.sun.tools.javac.tree.JCTree)tree;

            if (unconfiguredPackageReportingPolicy == ReportingPolicy.ERROR) {
                log.error(jcTree.pos, DeptectiveMessages.PACKAGE_NOT_CONFIGURED, packageName);
            }
            else {
                log.strictWarning(jcTree, DeptectiveMessages.PACKAGE_NOT_CONFIGURED, packageName);
            }

            reportedUnconfiguredPackages.put(packageName, true);
        }
    }

    //    @Override
    //    public Void visitImport(ImportTree node, Void p) {
    // TODO: Deal with "on-demand-imports" (com.foo.*)
    //        node.getQualifiedIdentifier().accept(new TreeScanner<Void, Void>() {
    //            @Override
    //            public Void visitMemberSelect(MemberSelectTree n, Void p) {
    //                return super.visitMemberSelect(n, p);
    //            }
    //        }, null);
    //
    //        checkPackageAccess(node, getQualifiedName(node.getQualifiedIdentifier()));
    //        return super.visitImport(node, p);
    //    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        Tree extendsClause = node.getExtendsClause();
        if (extendsClause != null) {
            checkPackageAccess(extendsClause, getQualifiedName(extendsClause));
        }

        node.getImplementsClause().forEach(implementsClause -> checkPackageAccess(implementsClause, getQualifiedName(implementsClause)));

        return super.visitClass(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        com.sun.tools.javac.tree.JCTree jcTree = (com.sun.tools.javac.tree.JCTree)node;

        PackageElement pakkage = elements.getPackageOf(jcTree.type.asElement());
        String qualifiedName = pakkage.getQualifiedName().toString();
        checkPackageAccess(node, qualifiedName);

        return super.visitVariable(node, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Void p) {
        node.getBounds().forEach(s -> {
            checkPackageAccess(s, getQualifiedName(s));
        });

        return super.visitTypeParameter(node, p);
    }

    @Override
    public Void visitParameterizedType(ParameterizedTypeTree node, Void p) {
        node.getTypeArguments().forEach(s -> {
            checkPackageAccess(s, getQualifiedName(s));
        });
        return super.visitParameterizedType(node, p);
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        checkPackageAccess(node.getAnnotationType(), getQualifiedName(node));

        // TODO: find Types that are references from Annotation Arguments
        //        node.getArguments().forEach(expr -> {
        //
        //            System.out.println("expr" + expr + "(" + expr.getClass().getName() + ")");
        //            if (expr instanceof AssignmentTree) {
        //                AssignmentTree assignmentTree = (AssignmentTree)expr;
        //                System.out.println("expr" + expr);
        //                System.out.println("qn => " + getQualifiedName(assignmentTree.getExpression()));
        //                checkPackageAccess(assignmentTree.getExpression(), getQualifiedName(assignmentTree.getExpression()));
        //            }
        //        });
        return super.visitAnnotation(node, p);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        checkPackageAccess(node, getQualifiedName(node));
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        Tree returnType = node.getReturnType();
        if (returnType != null) {
            checkPackageAccess(returnType, getQualifiedName(returnType));
        }
        return super.visitMethod(node, p);
    }

    protected String getQualifiedName(Tree tree) {
        com.sun.tools.javac.tree.JCTree jcTree = (com.sun.tools.javac.tree.JCTree)tree;
        Type type = jcTree.type;
        if (type == null) {
            throw new IllegalArgumentException("Could not determine type for tree object " + tree + " (" + tree.getClass()+")");
        }
        PackageElement pakkage = elements.getPackageOf(type.asElement());
        return pakkage.getQualifiedName().toString();
    }

    protected void checkPackageAccess(Tree node, String qualifiedName) {
        com.sun.tools.javac.tree.JCTree jcTree = (com.sun.tools.javac.tree.JCTree)node;

        if ("java.lang".equals(qualifiedName)) {
            return;
        }

        if (packageDependencies.isWhitelisted(qualifiedName)) {
            return;
        }

        if (!packageOfCurrentCompilationUnit.isConfigured()) {
            return;
        }

        if (packageOfCurrentCompilationUnit.getName().equals(qualifiedName)) {
            return;
        }

        if (qualifiedName.isEmpty() || packageOfCurrentCompilationUnit.reads(qualifiedName)) {
            return;
        }

        if (reportingPolicy == ReportingPolicy.ERROR) {
            log.error(jcTree.pos, DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY, packageOfCurrentCompilationUnit, qualifiedName);
        }
        else {
            log.strictWarning(jcTree, DeptectiveMessages.ILLEGAL_PACKAGE_DEPENDENCY, packageOfCurrentCompilationUnit, qualifiedName);
        }
    }
}

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
package org.moditect.deptective.internal;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.moditect.deptective.internal.handler.PackageReferenceHandler;
import org.moditect.deptective.internal.log.Log;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

public class DeptectiveTreeVisitor extends TreePathScanner<Void, Void> {

    private final Log log;
    private final Elements elements;
    private final PackageReferenceHandler packageReferenceHandler;
    private final Trees trees;
    private final Types types;

    public DeptectiveTreeVisitor(JavacTask task, Log log, PackageReferenceHandler packageReferenceHandler) {
        elements = task.getElements();
        types = task.getTypes();
        trees = Trees.instance(task);
        this.log = log;
        this.packageReferenceHandler = packageReferenceHandler;
    }

    @Override
    public Void visitCompilationUnit(CompilationUnitTree tree, Void p) {
        log.useSource(tree.getSourceFile());
        boolean proceed = packageReferenceHandler.onEnteringCompilationUnit(tree);

        return proceed ? super.visitCompilationUnit(tree, p) : null;
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        Tree extendsClause = node.getExtendsClause();
        if (extendsClause != null) {
            checkPackageAccess(extendsClause, getQualifiedPackageName(extendsClause));
        }

        node.getImplementsClause().forEach(
                implementsClause -> checkPackageAccess(implementsClause, getQualifiedPackageName(implementsClause))
        );

        return super.visitClass(node, p);
    }

    @Override
    public Void visitMemberSelect(MemberSelectTree node, Void p) {
        checkPackageAccess(node, getQualifiedPackageName(node));
        return super.visitMemberSelect(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        checkPackageAccess(node, getQualifiedPackageName(node));
        return super.visitVariable(node, p);
    }

    @Override
    public Void visitTypeParameter(TypeParameterTree node, Void p) {
        node.getBounds().forEach(s -> {
            checkPackageAccess(s, getQualifiedPackageName(s));
        });

        return super.visitTypeParameter(node, p);
    }

    @Override
    public Void visitParameterizedType(ParameterizedTypeTree node, Void p) {
        node.getTypeArguments().forEach(s -> {
            checkPackageAccess(s, getQualifiedPackageName(s));
        });
        return super.visitParameterizedType(node, p);
    }

    @Override
    public Void visitAnnotation(AnnotationTree node, Void p) {
        checkPackageAccess(node.getAnnotationType(), getQualifiedPackageName(node));
        return super.visitAnnotation(node, p);
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        checkPackageAccess(node, getQualifiedPackageName(node));
        return super.visitNewClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        Tree returnType = node.getReturnType();
        if (returnType != null) {
            checkPackageAccess(returnType, getQualifiedPackageName(returnType));
        }
        return super.visitMethod(node, p);
    }

    /**
     * Returns the qualified Package Name of the given Tree object or null if the package could not be determined
     */
    protected String getQualifiedPackageName(Tree tree) {
        TypeMirror typeMirror = trees.getTypeMirror(getCurrentPath());
        if (typeMirror == null) {
            return null;
        }

        if (typeMirror.getKind() != TypeKind.DECLARED && typeMirror.getKind() != TypeKind.TYPEVAR) {
            return null;
        }

        Element typeMirrorElement = types.asElement(typeMirror);
        if (typeMirrorElement == null) {
            throw new IllegalStateException("Could not get Element for type '" + typeMirror + "'");
        }
        PackageElement pakkage = elements.getPackageOf(typeMirrorElement);
        return pakkage.getQualifiedName().toString();
    }

    protected void checkPackageAccess(Tree node, String qualifiedName) {
        if (qualifiedName != null) {
            packageReferenceHandler.onPackageReference(node, qualifiedName);
        }
    }
}

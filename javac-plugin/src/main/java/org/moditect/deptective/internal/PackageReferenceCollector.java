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

import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.PackageDependencies;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

/**
 * A handler that produces a candidate {@code deptective.json} file based on the
 * actual package relationships of the project under compilation.
 *
 * @author Gunnar Morling
 */
public class PackageReferenceCollector implements PackageReferenceHandler {

    private final Log log;
    private final PackageDependencies.Builder builder;

    private String currentPackageName;

    public PackageReferenceCollector(Log log) {
        this.log = log;
        builder = PackageDependencies.builder();
    }

    @Override
    public void onEnteringCompilationUnit(CompilationUnitTree tree) {
        ExpressionTree packageNameTree = tree.getPackageName();

        // TODO deal with default package
        if (packageNameTree == null) {
            return;
        }

        currentPackageName = packageNameTree.toString();
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        builder.addRead(currentPackageName, referencedPackageName);
    }

    @Override
    public void onCompletingCompilation() {
        log.useSource(null);
        log.note(DeptectiveMessages.GENERATED_CONFIG, System.lineSeparator(), builder.build().toJson());
    }
}

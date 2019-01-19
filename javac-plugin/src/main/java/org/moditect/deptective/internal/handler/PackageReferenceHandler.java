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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;

/**
 * Implementations are invoked when traversing the ASTs of the project under compilation.
 *
 * @author Gunnar Morling
 */
public interface PackageReferenceHandler {

    /**
     * Whether the plug-ins configuration is valid for the given handler. If that's not the case, ASTs won't be
     * traversed.
     */
    default public boolean configIsValid() {
        return true;
    }

    /**
     * Invoked when entering a new compilation unit.
     */
    default void onEnteringCompilationUnit(CompilationUnitTree tree) {
    }

    /**
     * Invoked when referencing a package.
     *
     * @param referencingNode the node referencing the other package, e.g. a variable or field.
     * @param referencedPackageName the name of the referenced package
     */
    default void onPackageReference(Tree referencingNode, String referencedPackageName) {
    }

    /**
     * Invoked when the compilation is done.
     */
    default void onCompletingCompilation() {
    }
}

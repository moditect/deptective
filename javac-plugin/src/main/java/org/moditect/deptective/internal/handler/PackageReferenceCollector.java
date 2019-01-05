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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

import org.moditect.deptective.internal.log.DeptectiveMessages;
import org.moditect.deptective.internal.log.Log;
import org.moditect.deptective.internal.model.PackageDependencies;
import org.moditect.deptective.internal.model.WhitelistedPackagePattern;

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

    private final JavaFileManager jfm;
    private final List<WhitelistedPackagePattern> whitelistPatterns;
    private final Set<String> packagesOfCurrentCompilation;
    private final Set<String> referencedPackages;

    private String currentPackageName;


    public PackageReferenceCollector(JavaFileManager jfm, Log log, List<WhitelistedPackagePattern> whitelistPatterns) {
        this.log = log;

        this.jfm = jfm;
        this.whitelistPatterns = Collections.unmodifiableList(whitelistPatterns);
        this.packagesOfCurrentCompilation = new HashSet<String>();
        this.referencedPackages = new HashSet<String>();

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
        packagesOfCurrentCompilation.add(currentPackageName);
    }

    @Override
    public void onPackageReference(Tree referencingNode, String referencedPackageName) {
        referencedPackages.add(referencedPackageName);
        builder.addRead(currentPackageName, referencedPackageName);
    }

    @Override
    public void onCompletingCompilation() {
        List<WhitelistedPackagePattern> effectiveWhitelistPatterns;

        if (isWhitelistAllExternal()) {
            Set<String> externalPackages = new HashSet<>(referencedPackages);
            externalPackages.removeAll(packagesOfCurrentCompilation);
            effectiveWhitelistPatterns = externalPackages.stream()
                    .filter(p -> !p.equals("java.lang"))
                    .map(WhitelistedPackagePattern::getPattern)
                    .collect(Collectors.toList());
        }
        else {
            effectiveWhitelistPatterns = whitelistPatterns;
        }

        for (WhitelistedPackagePattern whitelistedPackagePattern : effectiveWhitelistPatterns) {
            // removes any explicit read to that package
            builder.addWhitelistedPackage(whitelistedPackagePattern);
        }

        log.useSource(null);

        try {
            FileObject output = jfm.getFileForOutput(StandardLocation.CLASS_OUTPUT, "", "deptective.json", null);
            log.note(DeptectiveMessages.GENERATED_CONFIG, output.toUri());
            Writer writer = output.openWriter();
            writer.append(builder.build().toJson());
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to write deptective.json file", e);
        }
    }

    private boolean isWhitelistAllExternal() {
        return whitelistPatterns.contains(WhitelistedPackagePattern.ALL_EXTERNAL);
    }
}

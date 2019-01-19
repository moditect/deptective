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
package org.moditect.deptective.plugintest.unconfiguredpackage;

import static com.google.testing.compile.CompilationSubject.assertThat;

import org.junit.Test;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.unconfiguredpackage.foo.Foo;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class UnconfiguredPackageTest extends PluginTestBase {

    @Test
    public void shouldWarnWhenEncounteringUnconfiguredPackage() {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        getConfigFileOption()
                )
                .compile(
                        forTestClass(Foo.class)
                );

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining(
                "no Deptective configuration found for package org.moditect.deptective.plugintest.unconfiguredpackage.foo"
        );
    }

    @Test
    public void shouldFailWhenEncounteringUnconfiguredPackage() {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        getConfigFileOption(),
                        "-Adeptective.unconfigured_package_reporting_policy=ERROR"
                )
                .compile(
                        forTestClass(Foo.class)
                );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(
                "no Deptective configuration found for package org.moditect.deptective.plugintest.unconfiguredpackage.foo"
        );
    }
}

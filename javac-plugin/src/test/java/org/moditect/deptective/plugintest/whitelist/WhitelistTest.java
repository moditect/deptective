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
package org.moditect.deptective.plugintest.whitelist;

import static com.google.testing.compile.CompilationSubject.assertThat;

import org.junit.Test;
import org.moditect.deptective.internal.options.DeptectiveOptions.Options;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.whitelist.bar.Bar;
import org.moditect.deptective.plugintest.whitelist.foo.Foo;
import org.moditect.deptective.testutil.TestOptions;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class WhitelistTest extends PluginTestBase {

    @Test
    public void shouldApplyWhitelistForJavaLangType() {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        TestOptions.deptectiveOptions(
                                Options.CONFIG_FILE, getConfigFileOption()
                        )
                )
                .compile(
                        forTestClass(Bar.class),
                        forTestClass(Foo.class)
                );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.whitelist.foo must not access org.moditect.deptective.plugintest.whitelist.bar"
        );
    }
}

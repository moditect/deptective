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
package org.moditect.deptective.plugintest.basic;

import static com.google.testing.compile.CompilationSubject.assertThat;

import org.junit.Test;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.basic.barctorcall.BarCtorCall;
import org.moditect.deptective.plugintest.basic.barfield.BarField;
import org.moditect.deptective.plugintest.basic.barlocalvar.BarLocalVar;
import org.moditect.deptective.plugintest.basic.barloopvar.BarLoopVar;
import org.moditect.deptective.plugintest.basic.barparameter.BarParameter;
import org.moditect.deptective.plugintest.basic.barretval.BarRetVal;
import org.moditect.deptective.plugintest.basic.bartypearg.BarTypeArg;
import org.moditect.deptective.plugintest.basic.foo.Foo;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class BasicPluginTest extends PluginTestBase {

    @Test
    public void shouldDetectDisallowedPackageDependence() {
        Compilation compilation = Compiler.javac()
            .withOptions(
                    "-Xplugin:Deptective",
                    getConfigFileOption()
            )
            .compile(
                    forTestClass(BarCtorCall.class),
                    forTestClass(BarField.class),
                    forTestClass(BarLocalVar.class),
                    forTestClass(BarLoopVar.class),
                    forTestClass(BarParameter.class),
                    forTestClass(BarRetVal.class),
                    forTestClass(BarTypeArg.class),
                    forTestClass(Foo.class)
            );

        assertThat(compilation).failed();

        // TODO https://github.com/moditect/deptective/issues/7
//        assertThat(compilation).hadErrorContaining(
//                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barctorcall"
//        );
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barfield"
        );
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barlocalvar"
        );
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barloopvar"
        );
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barparameter"
        );

        // TODO https://github.com/moditect/deptective/issues/7
//        assertThat(compilation).hadErrorContaining(
//                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barretval"
//        );
//        assertThat(compilation).hadErrorContaining(
//                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.bartypearg"
//        );
    }

    @Test
    public void shouldUseWarnReportingPolicy() {
        Compilation compilation = Compiler.javac()
            .withOptions(
                    "-Xplugin:Deptective",
                    getConfigFileOption(),
                    "-Adeptective.reportingpolicy=WARN"
            )
            .compile(
                    forTestClass(BarCtorCall.class),
                    forTestClass(BarField.class),
                    forTestClass(BarLocalVar.class),
                    forTestClass(BarLoopVar.class),
                    forTestClass(BarParameter.class),
                    forTestClass(BarRetVal.class),
                    forTestClass(BarTypeArg.class),
                    forTestClass(Foo.class)
            );

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barfield"
        );
    }
}

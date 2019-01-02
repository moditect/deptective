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

    private Compilation compile() {
        Compilation compilation = Compiler.javac()
                .withOptions("-Xplugin:Deptective", getConfigFileOption())
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

        return compilation;
    }

    @Test
    public void shouldDetectInvalidSuperClass() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barsuper");

        // inner class
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barinnersuper");
    }

    @Test
    public void shouldDetectInvalidImplementedInterface() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barinter");

        // inner interface
        assertThat(compilation).hadErrorContaining("package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barinnerinner");
    }


    @Test
    public void shouldDetectInvalidConstructorParameters() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barctorparam");
    }

    @Test
    public void shouldDetectInvalidConstructorCalls() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barctorcall");
    }

    @Test
    public void shouldDetectInvalidFieldReferences() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barfield");
    }

    @Test
    public void shouldDetectInvalidLocalVariableReferences() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barlocalvar");
    }

    @Test
    public void shouldDetectInvalidLoopVariableReferences() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barloopvar");
    }

    @Test
    public void shouldDetectInvalidMethodParameterReferences() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barparameter");
    }

    @Test
    public void shouldDetectInvalidAnnotationReferences() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barclazzan");
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barfieldan");
    }

    @Test
    public void shouldDetectInvalidReturnValueReferences() {
    	Compilation compilation = compile();
    	assertThat(compilation).failed();

    	assertThat(compilation).hadErrorContaining(
    			"package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barretval"
    	);

    	// Invalid Reference in Type Parameter
    	assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barretvalgen"
        );
    }

    @Test
    public void shouldDetectInvalidTypeArguments() {
        Compilation compilation = compile();
        assertThat(compilation).failed();

        // in type argument
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.bartypearg");

        // in class definition type argument bound
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.bargen");

        // in 'extends' class definition type argument
        assertThat(compilation).hadErrorContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.bargentype");
    }

    @Test
    public void shouldUseWarnReportingPolicy() {
        Compilation compilation = Compiler.javac()
                .withOptions("-Xplugin:Deptective", getConfigFileOption(), "-Adeptective.reportingpolicy=WARN")
                .compile(forTestClass(BarCtorCall.class), forTestClass(BarField.class), forTestClass(BarLocalVar.class),
                        forTestClass(BarLoopVar.class), forTestClass(BarParameter.class), forTestClass(BarRetVal.class),
                        forTestClass(BarTypeArg.class), forTestClass(Foo.class));

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining(
                "package org.moditect.deptective.plugintest.basic.foo does not read org.moditect.deptective.plugintest.basic.barfield");
    }
}

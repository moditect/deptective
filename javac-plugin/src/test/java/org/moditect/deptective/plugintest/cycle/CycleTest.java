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
package org.moditect.deptective.plugintest.cycle;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.Test;
import org.moditect.deptective.internal.util.Strings;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.cycle.abc.Abc;
import org.moditect.deptective.plugintest.cycle.bar.Bar;
import org.moditect.deptective.plugintest.cycle.baz.Baz;
import org.moditect.deptective.plugintest.cycle.def.Def;
import org.moditect.deptective.plugintest.cycle.foo.Foo;
import org.moditect.deptective.plugintest.cycle.qux.Qux;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class CycleTest extends PluginTestBase {

    @Test
    public void shouldDetectCyclesInArchitectureModel() {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        getConfigFileOption()
                )
                .compile(
                        forTestClass(Foo.class),
                        forTestClass(Bar.class),
                        forTestClass(Baz.class),
                        forTestClass(Qux.class),
                        forTestClass(Abc.class),
                        forTestClass(Def.class)

                );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("Architecture model contains cycle(s) between these components:");
        assertThat(compilation).hadErrorContaining("  - bar, baz, foo, qux");
        assertThat(compilation).hadErrorContaining("  - abc, def");
    }

    @Test
    public void shouldVisualizeCyclesInArchitectureModel() throws Exception {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        "-Adeptective.visualize=true",
                        "-Adeptective.cycle_reporting_policy=WARN",
                        getConfigFileOption()
                )
                .compile(
                        forTestClass(Foo.class),
                        forTestClass(Bar.class),
                        forTestClass(Baz.class),
                        forTestClass(Qux.class),
                        forTestClass(Abc.class),
                        forTestClass(Def.class)
                );

        assertThat(compilation).succeeded();
        assertThat(compilation).hadWarningContaining("Architecture model contains cycle(s) between these components:");
        assertThat(compilation).hadWarningContaining("  - bar, baz, foo, qux");
        assertThat(compilation).hadWarningContaining("  - abc, def");

        assertThat(compilation).hadNoteContaining(
                "Created DOT file representing the Deptective configuration at mem:///SOURCE_OUTPUT/deptective.dot"
        );
        assertThat(compilation).hadNoteCount(1);

        String expectedConfig = Strings.lines(
                "digraph \"package dependencies\"",
                "{",
                "  \"abc\";",
                "  \"bar\";",
                "  \"baz\";",
                "  \"def\";",
                "  \"foo\";",
                "  \"qux\";",
                "  subgraph Cycle {",
                "    edge [color=purple, penwidth=2]",
                "    \"abc\" -> \"def\";",
                "    \"bar\" -> \"baz\";",
                "    \"bar\" -> \"qux\";",
                "    \"baz\" -> \"foo\";",
                "    \"def\" -> \"abc\";",
                "    \"foo\" -> \"bar\";",
                "    \"qux\" -> \"bar\";",
                "  }",
                "}"
        );

        Optional<JavaFileObject> deptectiveFile = compilation
                .generatedFile(StandardLocation.SOURCE_OUTPUT, "deptective.dot");
        assertThat(deptectiveFile.isPresent()).isTrue();
        String generatedConfig = Strings.readToString(deptectiveFile.get().openInputStream());

        assertThat(generatedConfig).isEqualTo(expectedConfig);
    }
}

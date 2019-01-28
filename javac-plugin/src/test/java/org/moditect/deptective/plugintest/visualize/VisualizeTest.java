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
package org.moditect.deptective.plugintest.visualize;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.Test;
import org.moditect.deptective.internal.util.Strings;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.visualize.bar.Bar;
import org.moditect.deptective.plugintest.visualize.foo.Foo;
import org.moditect.deptective.plugintest.visualize.qux.Qux;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class VisualizeTest extends PluginTestBase {

    @Test
    public void shouldGenerateDotFileForAnalyse() throws Exception {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        "-Adeptective.mode=ANALYZE",
                        "-Adeptective.visualize=true",
                        "-Adeptective.whitelisted=java.math",
                        getConfigFileOption()
                )
                .compile(
                        forTestClass(Bar.class),
                        forTestClass(Foo.class),
                        forTestClass(Qux.class)
                );

        assertThat(compilation).succeeded();

        assertThat(compilation).hadNoteContaining(
                "Created DOT file representing the Deptective configuration at mem:///CLASS_OUTPUT/deptective.dot"
        );
        assertThat(compilation).hadNoteCount(2);

        assertThat(compilation).hadWarningContaining("Analysed code base contains cycle(s) between these components:");
        assertThat(compilation).hadWarningContaining(
                "  - org.moditect.deptective.plugintest.visualize.bar, org.moditect.deptective.plugintest.visualize.qux"
        );

        String expectedConfig = Strings.lines(
                "digraph \"package dependencies\"",
                "{",
                "  \"org.moditect.deptective.plugintest.visualize.bar\";",
                "  \"org.moditect.deptective.plugintest.visualize.foo\";",
                "  \"org.moditect.deptective.plugintest.visualize.qux\";",
                "  subgraph Allowed {",
                "    \"org.moditect.deptective.plugintest.visualize.foo\" -> \"org.moditect.deptective.plugintest.visualize.bar\";",
                "    \"org.moditect.deptective.plugintest.visualize.foo\" -> \"org.moditect.deptective.plugintest.visualize.qux\";",
                "  }",
                "  subgraph Cycle {",
                "    edge [color=purple, penwidth=2]",
                "    \"org.moditect.deptective.plugintest.visualize.bar\" -> \"org.moditect.deptective.plugintest.visualize.qux\";",
                "    \"org.moditect.deptective.plugintest.visualize.qux\" -> \"org.moditect.deptective.plugintest.visualize.bar\";",
                "  }",
                "}"
        );

        Optional<JavaFileObject> deptectiveFile = compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, "deptective.dot");
        assertThat(deptectiveFile.isPresent()).isTrue();
        String generatedConfig = Strings.readToString(deptectiveFile.get().openInputStream());

        assertThat(generatedConfig).isEqualTo(expectedConfig);
    }

    @Test
    public void shouldGenerateDotFileForValidate() throws Exception {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        "-Adeptective.visualize=true",
                        "-Adeptective.whitelisted=java.math",
                        "-Adeptective.reporting_policy=WARN",
                        getConfigFileOption()
                )
                .compile(
                        forTestClass(Bar.class),
                        forTestClass(Foo.class),
                        forTestClass(Qux.class)
                );

        assertThat(compilation).succeeded();

        assertThat(compilation).hadNoteContaining(
                "Created DOT file representing the Deptective configuration at mem:///CLASS_OUTPUT/deptective.dot"
        );
        assertThat(compilation).hadNoteCount(1);

        String expectedConfig = Strings.lines(
                "digraph \"package dependencies\"",
                "{",
                "  \"org.moditect.deptective.plugintest.visualize.bar\";",
                "  \"org.moditect.deptective.plugintest.visualize.foo\";",
                "  \"org.moditect.deptective.plugintest.visualize.qux\";",
                "  subgraph Allowed {",
                "    \"org.moditect.deptective.plugintest.visualize.bar\" -> \"org.moditect.deptective.plugintest.visualize.qux\";",
                "    \"org.moditect.deptective.plugintest.visualize.foo\" -> \"org.moditect.deptective.plugintest.visualize.qux\";",
                "  }",
                "  subgraph Disallowed {",
                "    edge [color=red, penwidth=2]",
                "    \"org.moditect.deptective.plugintest.visualize.foo\" -> \"org.moditect.deptective.plugintest.visualize.bar\";",
                "  }",
                "  subgraph Unknown {",
                "    edge [color=yellow, penwidth=2]",
                "    \"org.moditect.deptective.plugintest.visualize.qux\" -> \"org.moditect.deptective.plugintest.visualize.bar\";",
                "  }",
                "}"
        );

        Optional<JavaFileObject> deptectiveFile = compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, "deptective.dot");
        assertThat(deptectiveFile.isPresent()).isTrue();
        String generatedConfig = Strings.readToString(deptectiveFile.get().openInputStream());

        assertThat(generatedConfig).isEqualTo(expectedConfig);
    }
}

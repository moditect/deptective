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
package org.moditect.deptective.plugintest.analyze;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.moditect.deptective.internal.util.Strings.lines;

import java.util.Optional;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.junit.Test;
import org.moditect.deptective.internal.util.Strings;
import org.moditect.deptective.plugintest.PluginTestBase;
import org.moditect.deptective.plugintest.analyze.bar.Bar;
import org.moditect.deptective.plugintest.analyze.foo.Foo;
import org.moditect.deptective.plugintest.analyze.qux.Qux;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

public class AnalyzeTest extends PluginTestBase {

    @Test
    public void shouldGenerateConfig() throws Exception {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        "-Adeptective.mode=ANALYZE",
                        "-Adeptective.whitelisted=java.math"
                )
                .compile(
                        forTestClass(Bar.class),
                        forTestClass(Foo.class),
                        forTestClass(Qux.class)
                );

        assertThat(compilation).succeeded();

        assertThat(compilation).hadNoteContaining(
                "Generated Deptective configuration template at mem:///CLASS_OUTPUT/deptective.json"
        );
        assertThat(compilation).hadNoteCount(1);

        String expectedConfig = lines(
                "{",
                "    \"components\" : [ {",
                "      \"name\" : \"org.moditect.deptective.plugintest.analyze.bar\",",
                "      \"contains\" : [ \"org.moditect.deptective.plugintest.analyze.bar\" ],",
                "      \"reads\" : [ \"org.moditect.deptective.plugintest.analyze.qux\" ]",
                "    }, {",
                "      \"name\" : \"org.moditect.deptective.plugintest.analyze.foo\",",
                "      \"contains\" : [ \"org.moditect.deptective.plugintest.analyze.foo\" ],",
                "      \"reads\" : [",
                "          \"org.moditect.deptective.plugintest.analyze.qux\",",
                "          \"org.moditect.deptective.plugintest.analyze.bar\" ]",
                "    }, {",
                "      \"name\" : \"org.moditect.deptective.plugintest.analyze.qux\",",
                "      \"contains\" : [ \"org.moditect.deptective.plugintest.analyze.qux\" ]",
                "    } ]",
                "}"
        );

        Optional<JavaFileObject> deptectiveFile = compilation
                .generatedFile(StandardLocation.CLASS_OUTPUT, "deptective.json");
        assertThat(deptectiveFile.isPresent()).isTrue();
        String generatedConfig = Strings.readToString(deptectiveFile.get().openInputStream());

        JSONAssert.assertEquals(expectedConfig, generatedConfig, JSONCompareMode.LENIENT);
    }
}

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
package org.moditect.deptective;

import static com.google.testing.compile.CompilationSubject.assertThat;

import org.junit.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

public class DeptectivePluginTest {

    @Test
    public void shouldDetectDisallowedPackageDependence() {
        Compilation compilation = Compiler.javac()
            .withOptions(
                    "-Xplugin:Deptective",
                    "-Adeptective.configfile=" + DeptectivePluginTest.class.getResource("deptective.json").getPath())
            .compile(
                    JavaFileObjects.forSourceString(
                            "com.example.bar.Bar",
                            "package com.example.bar;" +
                            "public class Bar {" +
                            "}"
                    ),
                    JavaFileObjects.forSourceString(
                            "com.example.foo.Foo",
                            "package com.example.foo;\n" +
                            "import com.example.bar.Bar;\n" +
                            "public class Foo {\n" +
                            "    private Bar bar = new Bar();\n" +
                            "}\n"
                    )
            );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("package com.example.foo does not read com.example.bar");
    }

    @Test
    public void shouldProduceCorrectJavacMessages() {
        Compilation compilation = Compiler.javac()
            .withOptions(
                    "-Xplugin:Deptective",
                    "-Adeptective.configfile=" + DeptectivePluginTest.class.getResource("deptective.json").getPath())
            .compile(
                    JavaFileObjects.forSourceString(
                            "com.example.foo.Foo",
                            "package com.example.foo;\n" +
                            "public class Foo {\n" +
                            "    private final String s;\n" +
                            "}\n"
                    )
            );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("variable s not initialized in the default constructor");
    }
}

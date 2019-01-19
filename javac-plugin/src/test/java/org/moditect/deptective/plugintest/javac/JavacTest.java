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
package org.moditect.deptective.plugintest.javac;

import static com.google.testing.compile.CompilationSubject.assertThat;

import org.junit.Test;
import org.moditect.deptective.plugintest.PluginTestBase;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

public class JavacTest extends PluginTestBase {

    @Test
    public void shouldProduceCorrectJavacMessages() {
        Compilation compilation = Compiler.javac()
                .withOptions(
                        "-Xplugin:Deptective",
                        getConfigFileOption()
                )
                .compile(
                        JavaFileObjects.forSourceLines(
                                "com.example.foo.Foo",
                                "package com.example.foo;",
                                "public class Foo {",
                                "    private final String s;",
                                "}"
                        )
                );

        assertThat(compilation).failed();
        assertThat(compilation).hadErrorContaining("variable s not initialized in the default constructor");
    }
}

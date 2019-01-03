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
package org.moditect.deptective.plugintest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.tools.JavaFileObject;

import com.google.testing.compile.JavaFileObjects;

public class PluginTestBase {

    protected String getConfigFileOption() {
        URL resource = getClass().getResource("deptective.json");

        if (resource == null) {
            throw new IllegalStateException("No config file found");
        }

        return "-Adeptective.config_file=" + resource.getPath();
    }

    protected JavaFileObject forTestClass(Class<?> clazz) {
        try {
            Path projectDir = Paths.get(getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    )
                    .getParent()
                    .getParent();

            URL resource = projectDir
                    .resolve("src/test/java")
                    .resolve(clazz.getName().replace(".", File.separator) + ".java")
                    .toUri()
                    .toURL();

            return JavaFileObjects.forResource(resource);
        }
        catch(URISyntaxException | MalformedURLException e) {
            throw new RuntimeException("Couldn't retrieve source for class " + clazz, e);
        }
    }
}

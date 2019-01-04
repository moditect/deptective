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
package org.moditect.deptective.internal.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;

public class ConfigLoader {

    public PackageDependencies getConfig(Optional<Path> configFile, JavaFileManager jfm) {
        try {
            InputStream inputStream = getConfigStream(configFile, jfm);

            if (inputStream == null) {
                return null;
            }

            try (InputStream is = inputStream) {
                return new ConfigParser(is).getPackageDependencies();
            }
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getConfigStream(Optional<Path> configFile, JavaFileManager jfm) {
        try {
            if (configFile.isPresent()) {
                    return Files.newInputStream(configFile.get());
            }
            else {
                FileObject file = jfm.getFileForInput(StandardLocation.SOURCE_PATH, "", "deptective.json");

                if (file != null) {
                    return file.openInputStream();
                }

                file = jfm.getFileForInput(StandardLocation.CLASS_PATH, "", "META-INF/deptective.json");

                if (file != null) {
                    return file.openInputStream();
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to load Deptective configuration file", e);
        }

        return null;
    }
}

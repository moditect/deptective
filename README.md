# Deptective

🕵️ Deptective is a plug-in for the Java compiler (_javac_) that validates the dependencies
amongst a project's packages against a description of allowed package dependences
and fail the project compilation when detecting any unintentional dependencies.

* [Deptective](#deptective)
  * [Requirements](#requirements)
  * [Usage](#usage)
  * [Configuration Options](#configuration-options)
  * [Contributing and Development](#contributing-and-development)
     * [IDE Set-Up](#ide-set-up)
  * [License](#license)

## Requirements

🕵 JDK 11 is needed to run Deptective.
Support for JDK 8 may be added later on.

The plug-in is specific to _javac_, i.e. the compiler coming with the JDK, it does not work with other compilers such as Eclipse's _ejc_ compiler.
Support for _ejc_ may be added later on.

## Usage

🕵 Define a file _deptective.json_ which describes the allowed dependencies amongst the project's packages like so:

```
{
    "packages" : [
        {
            "name" : "com.example.foo",
            "reads" : [
                "com.example.bar",
                "com.example.baz"
            ]
        },
        {
            "name" : "com.example.bar",
            "reads" : [
                "com.example.baz"
            ]
        }
    ]
}
```

Place the file in the root of your source directory (e.g. _src/main/java_ for Maven projects).
Alternatively you can specify the location of the config file using the `-Adeptective.configfile` option (see below).

Add _deptective-javac-plugin-1.0-SNAPSHOT.jar_ to your project's annotation processor path
and specify the option `-Xplugin:Deptective` when invoking _javac_.
When using Maven, the following configuration can be used:

```
...
<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-Xplugin:Deptective</arg>
                    <!-- specify options like so -->
                    <!-- <arg>-Adeptective.reportingpolicy=WARN</arg> -->
                </compilerArgs>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.moditect.deptective</groupId>
                        <artifactId>deptective-javac-plugin</artifactId>
                        <version>1.0-SNAPSHOT</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
...
```

See _integration-test/pom.xml_ for a complete example.

## Configuration Options

🕵 The following options can be provided when running the plug-in:

* `-Adeptective.configfile=path/to/deptective.json`: Path of the configuration file in the file system
* `-Adeptective.reportingpolicy=(ERROR|WARN)`: Whether to fail the build or just raise a warning when spotting any illegal package dependencies (defaults to `ERROR`)

## Contributing and Development

🕵 In order to build Deptective, [OpenJDK 11](https://openjdk.java.net/projects/jdk/11/) or later and Apache Maven 3.x must be installed.
Then obtain the source code from GitHub and build it like so:

```
git clone https://github.com/moditect/deptective.git
cd deptective
mvn clean install
```

Your contributions to Deptective in form of [pull requests](https://help.github.com/articles/about-pull-requests/) are very welcomed.
Before working on larger changes, it's recommended to get in touch first to make sure there's agreement on the feature and design.

### IDE Set-Up

🕵 To work on the code base in Eclipse, register an OpenJDK 11 instance ("Preferences" -> "Java" -> "Installed JREs").
Then run "File" -> "Import..." -> "Maven" -> "Existing Maven Projects".
After importing the project, make sure that Java 11 is on the build path of the _javac-plugin_ module
(right-click on that module, then "Properties" -> "Java Build Path" -> "Libraries").

## License

🕵 Deptective is licensed under the Apache License version 2.0.

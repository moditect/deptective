# Deptective

🕵️ Deptective is a plug-in for the Java compiler (_javac_) that validates the dependencies
amongst a project's packages against a description of allowed package dependences
and fail the project compilation when detecting any unintentional dependencies.

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

## License

🕵 Deptective is licensed under the Apache License version 2.0.

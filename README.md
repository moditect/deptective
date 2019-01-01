# Deptective

🕵️ Deptective is a plug-in for the Java compiler (_javac_) that validates the dependencies
amongst a project's packages against a description of allowed package dependences
and fail the project compilation when detecting any unintentional dependencies.

## Usage

Define a file _deptective.json_ which describes the allowed dependencies amongst the project's packages like so:

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

Add _deptective-javac-plugin-1.0-SNAPSHOT.jar_ to your project's classpath
and specify the option `-Xplugin:Deptective` when invoking _javac_.
When using Maven, the following configuration can be used:

```
<dependencies>
    <dependency>
        <groupId>org.moditect.deptective</groupId>
        <artifactId>deptective-javac-plugin</artifactId>
        <scope>provided</scope>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <compilerArgs>
                    <arg>-Xplugin:Deptective</arg>
                </compilerArgs>
            </configuration>
        </plugin>
    </plugins>
</build>
```

## Configuration Options

The following options can be provided when running the plug-in:

* `-Adeptective.configfile=path/to/deptective.json`: Path of the configuration file in the file system
* `-Adeptective.reportingpolicy=(ERROR|WARN)`: Whether to fail the build or just raise a warning when spotting any illegal package dependencies (defaults to `ERROR`)

## License

Deptective is licensed under the Apache License version 2.0.

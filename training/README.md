# Mandrel Training


## GraalVM


### Introduction

TODO


### Benefits of Native Image

TODO


#### Fast Startup

Important for short-lived applications, e.g. Function As A Service (FaaS).


#### Low Memory Consumption

Gives you higher density of application.

In an orchestration env, e.g. OpenShift, you are dealing with multiple pods.
You have multiple call points between pods.
You have many images, small images preferably.

Interesting even for long-running apps, e.g. Infinispan / Data Grid.


### Setup

Download latest
[GraalVM CE](https://www.graalvm.org/downloads/).

Add `bin` directory to the `PATH`.

Install `native-image` executable:

```bash
gu install native-image
```


### First Example

Create the example code:

```bash
cd first
make hello.java
```

Load it into an IDE of choice, e.g. IntelliJ IDEA:

```bash
make intellij-idea
```

Build a jar for it:

```bash
make hello.jar
```

Run the jar with `java`:

```bash
java -jar hello.jar
```

Build the native executable:

```bash
native-image -jar hello.jar hello
```

Run the native executable:

```bash
./hello
```

Getting `native-image` info while building executables:

```bash
native-image --native-image-info -jar hello.jar hello
```

Use `--verbose` to get a better understanding of how `native-image` works.
It's a 2-step process.
The first is a short java process, executed as native code.
The second is where the real stuff happens.
A normal java process for which you can see all the parameter invocations.
You can take that and run it manually.

```bash
native-image --verbose -jar hello.jar hello
```

Find out shared library dependencies of an executable:

```bash
ldd ./hello
```

Find the printed message inside the executable:

```bash
strings hello | grep Hello
```

Find out some interesting strings inside the binary,
e.g. GraalVM version, static libraries...etc:

```bash
strings hello | grep core.VM
```

Inspect sections of the binary.
SVM heap and text sections contributing the most to the size of the binary:

```bash
readelf -SW ./hello
```


### Reports

Create the example code:

```bash
cd reporting
make reporting.java
```

Load it into an IDE of choice, e.g. IntelliJ IDEA:

```bash
make intellij-idea
```

Build a jar for it:

```bash
make reporting.jar
```

Run the jar with `java`:

```bash
java -jar reporting.jar
```

Build the native executable:

```bash
native-image -jar reporting.jar reporting
```

Some warnings appearing:

```bash
Warning: Reflection method java.lang.Class.forName invoked at picocli.CommandLine$BuiltIn$ClassConverter.convert(CommandLine.java:13819)
```

Not obvious from the java code where these calls are coming from.
Use reporting flags to gather more information, e.g. `-H:+PrintAnalysisCallTree`:

```bash
native-image -H:+PrintAnalysisCallTree -jar reporting.jar reporting
```

`used_packages`, `used_classes` and `used_methods` when comparing different versions of the application.
Example: why does the image take longer to build? why is the image bigger now?

`call_tree` report useful for getting an approximation on why something is included.
Open the `call_tree` report and look for `Class.forName` invocations.

There are other reporting flags,
but not used as much as `-H:+PrintAnalysisCallTree`.
Find all available flags calling:

```bash
native-image --expert-options-all
```


### Bonus: configuration with native image agent

Go back to the reporting example and generate a native image as default:

```bash
native-image -jar reporting.jar reporting
```

Try running fallback image:

```bash
$ ./reporting
Hello World!
```

Hmmm, it seems to work.
The fallback image appears to require a JDK for execution.
What happens if we try running it a shell with no JDK in the path?

```bash
$ which java
/usr/bin/which: no java in (...)

$ /path-to/reporting
Hello World!
```

Hmmm, it still seems to work.
Why does it manage to run even if there's no JDK in the path?
The fallback image captures the Java home directory used for image generation.

Copy the executable to a different folder and try running it:

```bash
$ cd /tmp

$ cp /path-to/reporting .

$ ./reporting
Error: Could not find or load main class reporting
Caused by: java.lang.ClassNotFoundException: reporting
```

Try doing the same thing with the binary from the first example:

```bash
$ cp /path-to/first/hello .

$ ./hello
Hello World
```

Try generating an executable passing in `--no-fallback` option,
because we want a true binary:

```bash
native-image --no-fallback -jar reporting.jar reporting
```

No the image does not fail (hmmm?).
But does the executable work?
Try running it:

```bash
$ ./reporting
Exception in thread "main" picocli.CommandLine$InitializationException: picocli.CommandLine$AutoHelpMixin@3a1e2070 is not a command: it has no @Command, @Option, @Parameters or @Unmatched annotations
	at picocli.CommandLine$Model$CommandReflection.validateCommandSpec(CommandLine.java:10905)
	at picocli.CommandLine$Model$CommandReflection.extractCommandSpec(CommandLine.java:10741)
	at picocli.CommandLine$Model$CommandSpec.forAnnotatedObject(CommandLine.java:5863)
	at picocli.CommandLine$Model$CommandSpec.mixinStandardHelpOptions(CommandLine.java:6718)
	at picocli.CommandLine$Model$CommandReflection.extractCommandSpec(CommandLine.java:10736)
	at picocli.CommandLine$Model$CommandSpec.forAnnotatedObject(CommandLine.java:5863)
	at picocli.CommandLine.<init>(CommandLine.java:223)
	at picocli.CommandLine.<init>(CommandLine.java:196)
	at reporting.main(reporting.java:18)
```

It doesn't work.
Most likely because the lack of reflection configuration is stopping annotations being found.

If you are prototyping,
or you are building a reproducer,
you can quickly fix this by using the native image agent to generate the missing native configuration.
This configuration applies to reflection, serialization, resources, JNI and proxies.
To do that, first create a directory where the configuration will be generated:

```bash
mkdir -p META-INF/native-image
```

Run the java program with the native image agent configuration pointing to the newly created folder:

```bash
java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar reporting.jar
```

Inspect the configuration files created inside `META-INF/native-image` folder.

Next up, add generated the `META-INF` folder into the jar:

```bash
jar -uf reporting.jar META-INF
```

You can verify the files have been added via:

```bash
jar tf reporting.jar
```

Now that the jar contains the native image configuration,
re-execute the native-image command:

```bash
native-image --no-fallback -jar reporting.jar reporting
```

Finally, the binary created should work as expected:

```bash
./reporting
```

For more details, 
see [GraalVM documentation on native image build configuration](https://www.graalvm.org/reference-manual/native-image/BuildConfiguration/).

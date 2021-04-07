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

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
make init
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

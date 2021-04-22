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


### Initialization

This section focuses on the initialization aspects of native executables.

Load it into an IDE of choice, e.g. IntelliJ IDEA:

```bash
make intellij-idea
```

Build a jar for it:

```bash
make init.jar
```

Run it with java:

```bash
java -jar init.jar
```

Generate a native executable:

```bash
native-image -jar init.jar init
```

Try running it:

```bash
$ ./init
Caused by: java.lang.ClassNotFoundException: sun.security.rsa.RSAKeyPairGenerator$Legacy
```

Look for security options:

```bash
native-image --help | grep securiy
```

Create native executable with security services enabled: 

```bash
native-image --enable-all-security-services -jar init.jar init
```

Run it:

```bash
./init
```

The static block initialization,
when is it running?
At build time or at runtime?
Add a print statement to the static block, 
rebuild and execute:

```bash
$ make
$ native-image --enable-all-security-services -jar init.jar init
$ ./init
(message)
```

It shows up at runtime.
GraalVM initializes user static variables and blocks at runtime by default.
It can be beneficial though to push some initialization steps to the build phase.
For example: loading classes, reading configuration...etc.
That can both speed up startup and reduce memory consumption.

Instruct native image to initialize encryption example at build time:

```bash
native-image --initialize-at-build-time --enable-all-security-services -jar init.jar init
```

The static block message appears during build time,
but now the code hits some unsupported feature.
Request native image to not use fallback:

```bash
$ native-image --no-fallback --initialize-at-build-time --enable-all-security-services -jar init.jar init
Error: No instances of sun.security.provider.NativePRNG are allowed in the image heap as this class should be initialized at image runtime. To see how this object got instantiated use --trace-object-instantiation=sun.security.provider.NativePRNG.
Detailed message:
Trace: Object was reached by
	reading field java.security.SecureRandom.secureRandomSpi of
		constant java.security.SecureRandom@7b5c3cf3 reached by
	reading field java.security.KeyPairGenerator$Delegate.initRandom of
		constant java.security.KeyPairGenerator$Delegate@492ceba1 reached by
	scanning method init$AsymmetricEncryption.main(init.java:44)
Call path from entry point to init$AsymmetricEncryption.main():
	at init$AsymmetricEncryption.main(init.java:44)
	at init.main(init.java:18)
	at com.oracle.svm.core.JavaMainWrapper.runCore(JavaMainWrapper.java:146)
	at com.oracle.svm.core.JavaMainWrapper.run(JavaMainWrapper.java:182)
	at com.oracle.svm.core.code.IsolateEnterStub.JavaMainWrapper_run_5087f5482cc9a6abc971913ece43acb471d2631b(generated:0)
```

So, what the message above is telling us is that the `KeyPairGenerator`
instance in the main method contains a `SecureRandom` instance,
which references `NativePRNG`.
This is not desirable because something that's supposed to be random is not longer so,
because the seed is baked in the image.
As a next step, we'd like to know what is causing such instance to be left in the heap image.

We could try again adding option to track object instantiation:

```bash
$ native-image --trace-object-instantiation=sun.security.provider.NativePRNG --no-fallback --initialize-at-build-time --enable-all-security-services -jar init.jar init
Error: No instances of sun.security.provider.NativePRNG are allowed in the image heap as this class should be initialized at image runtime. Object has been initialized by the com.sun.jndi.dns.DnsClient class initializer with a trace:
 	at sun.security.provider.NativePRNG.<init>(NativePRNG.java:205)
	at jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance0(Unknown Source)
	at jdk.internal.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at jdk.internal.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:490)
	at java.security.Provider.newInstanceUtil(Provider.java:176)
	at java.security.Provider$Service.newInstance(Provider.java:1894)
	at java.security.SecureRandom.getDefaultPRNG(SecureRandom.java:290)
	at java.security.SecureRandom.<init>(SecureRandom.java:219)
	at sun.security.jca.JCAUtil$CachedSecureRandomHolder.<clinit>(JCAUtil.java:59)
	at sun.security.jca.JCAUtil.getSecureRandom(JCAUtil.java:69)
	at com.sun.jndi.dns.DnsClient.<clinit>(DnsClient.java:82)
```

What does `DnsClient` have to do with our example?
Run it again with `-H:+PrintAnalysisCallTree`:

```bash
native-image -H:+PrintAnalysisCallTree --trace-object-instantiation=sun.security.provider.NativePRNG --no-fallback --initialize-at-build-time --enable-all-security-services -jar init.jar init
```

Open `call_tree` report.
The report won't show you anything meaningful here.
Try grepping or looking for `AsymmetricEncryption`, or `clinit`.
We don't see anything related to our static block.

To potentially gather more clues,
try running the reports with the class being initialized at runtime.
See if it gives us some clues:

```bash
native-image -H:+PrintAnalysisCallTree --trace-object-instantiation=sun.security.provider.NativePRNG --no-fallback --enable-all-security-services -jar init.jar init
```

If you open the report,
you should now see an entry point for the static block:

```bash
├── entry init$AsymmetricEncryption.<clinit>():void id=644 
...
│   ├── virtually calls java.security.KeyPairGenerator.initialize(int):void @bci=22
│   │   └── is overridden by java.security.KeyPairGenerator.initialize(int):void id=1108 
│   │       ├── virtually calls java.security.KeyPairGenerator.initialize(int, java.security.SecureRandom):void @bci=5
```

We know get some clues.
The `initialize` methods ends up calling another method with a `SecureRandom`.
Let's now look at the source code to see what is going on there.

It turns out that the `initialize` method that takes `int`,
it uses the `JCAUtil.getSecureRandom()`,
which returns a cached `SecureRandom` instance.
However, this is also happens to be used by the `DnsClient`.
When you try trace initialization,
unfortunately GraalVM is showing you the path through `DnsClient`,
which has nothing to do with our application.

So, trace initialization flags can be useful,
but they can seem to be lying to you at a first glance.
This is something that can be applied to some of the exceptions you get when building native images.
Often they can show misleading or imprecise information.

So, how do we fix the example?
It's an easy fix, move the `initialize` call to the main method.
By doing that, the initialization happens at runtime and it's all fine.
Let's try it:

```bash
native-image --no-fallback --initialize-at-build-time --enable-all-security-services -jar init.jar init
```

### Runtime Profiling

This section focuses on learning how to profile native image generated executables.
It includes 2 different examples,
the first covers a single threaded program,
and the second is a multi-threaded one.


#### Profiling single thread program

Load it into an IDE of choice, e.g. IntelliJ IDEA:

```bash
cd exec-profile-single
make intellij-idea
```

Build a jar for it:

```bash
make stringbuilders.jar
```

Run it with java:

```bash
java -jar stringbuilders.jar
```

Generate a native executable:

```bash
native-image -jar stringbuilders.jar stringbuilders
```

Run it:

```bash
./stringbuilders
```

It never completes... what is happening?
Let's assume we can't profile the java version.
How do we profile it?
Since we're dealing with a linux native executable,
we can use tools like `perf` directly:

```bash
perf record -F 1009 -g -a -- ./stringbuilders
```

We could use `perf report` to inspect the perf data,
but you can often get a better picture showing that data as a 
[flame graph](https://github.com/brendangregg/FlameGraph):

```bash
make flamegraph
```

The flamegraph is an `svg` file that a web browser,
such as Google Chrome,
can easily display it:

```bash
open flamegraph.svg
```

Hmmmm, we see a big majority of time spent in what is supposed to be our main,
but we see no trace of our class,
nor the `StringBuilder` class we're calling.
We should look at the symbol table of the binary:
can we find symbols for our class and the `StringBuilder`?
We need those in order to get meaningful data:

```bash
objdump -t stringbuilders | grep stringbuilders
objdump -t stringbuilders | grep StringBuilder
```

None of those really show anything.
This is why we don't see any call graphs in the flame graphs.
This is deliberate decision that native image makes.
By default, it removes symbols from the binary.

We can regain those back in several ways,
one of which is generating a native executable with debug info:

```bash
native-image -g -jar stringbuilders.jar stringbuilders
```

Inspect the native executable with `objdump`,
and see how the symbols are now present:

```bash
objdump -t stringbuilders | grep StringBuilder
```

Then, run the executable through `perf`,
indicating that the call graph is `dwarf`:

```bash
perf record -F 1009 --call-graph dwarf -a -- ./stringbuilders
```

Make and open a flamegraph:

```bash
make flamegraph
open flamegraph.svg
```

The flamegraph now shows where the bottleneck is.
It's when `StringBuilder.delete` is called,
and `System.arraycopy` is called as part of `AbstractStringBuilder.shift`.
The issue is that 1 million characters need to be shifted in very small increments.

Technically, the same flamegraph could have been achieved in other ways that didn't require DWARF debug info.
However, the advantage of that debug info is that,
`perf` can show us the relevant source code lines.
To do that, simply call `perf report` with an extra parameter to shwow source code lines: 

```bash
$ perf report --stdio -F+srcline
...
    99.08%     0.01%  AbstractStringBuilder.java:1025                   stringbuilders  stringbuilders      [.] AbstractStringBuilder_delete_58681f709e2653ae2d27c3a178d8de9a64d94ff7
            |
            ---AbstractStringBuilder_delete_58681f709e2653ae2d27c3a178d8de9a64d94ff7
```


#### Profile multi thread program

<TODO>


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


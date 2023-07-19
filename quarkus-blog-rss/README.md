# Increased RSS debugging blog post

Start by verifying you can run both apps in JVM mode:

```bash
$ cd before
$ make run-jvm
...
$ cd after
$ make run-jvm
```

Then, check if you can run in native mode before the RSS fix,
and check what the RSS consumption is:

```bash
$ cd before
$ make
...
$ make rss
ps -p $(pidof getting-started-1.0.0-SNAPSHOT-runner) -o rss=
33024
```

Do the same after the RSS fix and compare values:

```bash
$ cd after
$ make
...
$ make rss
ps -p $(pidof getting-started-1.0.0-SNAPSHOT-runner) -o rss=
36608
```

Next, clean, regenerate the native image with debug info and run it through `perf record`:

```bash
$ cd before
$ make clean
$ make record NATIVE_BUILD_ARGS=-H:-DeleteLocalSymbols MAVEN_ARGS=-Dquarkus.native.debug.enabled
```

Once shutdown the application,
generate a flamegraph with the data and open the generated `.svg` file with the browser:

```bash
$ cd before
$ make flame
```

Repeat same process after the fixes and compare the `mmap` calls in main in the `.svg` files.

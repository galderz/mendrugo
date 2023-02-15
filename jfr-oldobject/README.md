= JFR Old Object JVM and GraalVM examples

== Plain object leak

```bash
$ TEST_CLASS=PlainObjectLeak make jvm
$ JFR_FILE=target/jvm.jfr make print
```

```bash
$ TEST_CLASS=PlainObjectLeak make native
$ JFR_FILE=target/native.jfr make print
```

== Array leak

```bash
$ TEST_CLASS=ArrayLeak make jvm
$ JFR_FILE=target/jvm.jfr make print
```

```bash
$ TEST_CLASS=ArrayLeak make native
$ JFR_FILE=target/native.jfr make print
```

== Path to GC roots

```bash
$ PATH_TO_GC_ROOTS=true \
  TEST_CLASS=PlainObjectLeak make jvm
$ JFR_FILE=target/jvm.jfr make print
```

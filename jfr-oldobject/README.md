= JFR Old Object JVM and GraalVM examples

== Plain object leak

```bash
$ make run-jvm TEST_CLASS=PlainObjectLeak
$ make print JFR_FILE=target/jvm.jfr
```

```bash
$ make run-native TEST_CLASS=PlainObjectLeak
$ make print JFR_FILE=target/native.jfr
```

== Array leak

```bash
$ make run-jvm TEST_CLASS=ArrayLeak
$ make print JFR_FILE=target/jvm.jfr
```

```bash
$ make run-native TEST_CLASS=ArrayLeak
$ make print JFR_FILE=target/native.jfr
```

== Path to GC roots

```bash
$ make run-jvm TEST_CLASS=PlainObjectLeak PATH_TO_GC_ROOTS=true
$ make print JFR_FILE=target/jvm.jfr
```

# Crema + Quarkus libjvm.so Optimization Progress

## Goal

Improve Quarkus startup time when running inside Crema by pre-compiling
Quarkus dependencies into `$GRAALVM_HOME/lib/svm/libjvm.so`.

Dependencies are added via:
```
-cp dependency.jar -H:Preserve=path=dependency.jar
```
appended to the base `native-image --macro:jvm-library` command.

## Baseline

```bash
$GRAALVM_HOME/bin/native-image --macro:jvm-library -H:Preserve=package=sun.misc
```

**Startup time: 1.023s**

Run with:
```bash
$GRAALVM_HOME/bin/java -svm -jar getting-started/target/quarkus-app/quarkus-run.jar
```

Verified via `curl http://localhost:8080/hello` → `"hello"`.

---

## Current Working Configuration

See [`build-libjvm.sh`](build-libjvm.sh) for the full script.

### Preserved Jars (18 jars, AOT compiled into libjvm.so)

**Boot jars (15 of 16):**

| Jar | Status |
|-----|--------|
| `jboss-logging-3.6.3.Final.jar` | ✅ Preserved |
| `jboss-logmanager-3.2.2.Final.jar` | ✅ Preserved |
| `smallrye-common-constraint-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-cpu-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-expression-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-function-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-io-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-net-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-os-2.20.0.jar` | ✅ Preserved |
| `smallrye-common-ref-2.20.0.jar` | ✅ Preserved |
| `crac-1.5.0.jar` | ✅ Preserved |
| `quarkus-classloader-commons-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-development-mode-spi-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-value-registry-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-vertx-latebound-mdc-provider-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-bootstrap-runner-999-SNAPSHOT.jar` | ❌ Excluded (see below) |

**Main jars (3, as transitive deps):**

| Jar | Why needed |
|-----|-----------|
| `jakarta.json-api-2.1.3.jar` | Required by `jboss-logmanager` (`JsonFormatter`) |
| `slf4j-api-2.0.19.jar` | Required by `jboss-logging` (`Slf4jLogger`) |
| `smallrye-common-search-2.20.0.jar` | Required by `smallrye-common-io` (`Archive`) |

### CP-Only Jars (3 optional deps, NOT Preserved)

These are downloaded by the build script and placed on the classpath to satisfy
`--link-at-build-time` linking requirements. They are never called at runtime
(Quarkus uses jboss-logmanager, not log4j).

| Jar | Why needed |
|-----|-----------|
| `log4j-1.2.17.jar` | `jboss-logging` → `Log4jLogger` references `org.apache.log4j.Logger` |
| `log4j-api-2.24.3.jar` | `jboss-logging` → `Log4j2Logger` references `org.apache.logging.log4j.spi.AbstractLogger` |
| `jboss-modules-2.1.5.Final.jar` | `jboss-logmanager` → `ObjectBuilder$ModuleFinder$Holder` references `org.jboss.modules.ModuleLoader` |

### Excluded: `quarkus-bootstrap-runner`

This jar uses `getProtectionDomain().getCodeSource().getLocation()` in
`QuarkusEntryPoint.resolveJarPath()` to locate `quarkus-application.dat`
at runtime. When the class is AOT-compiled into `libjvm.so`, the code source
points to the native-image build directory instead of the jar location,
causing `NoSuchFileException`.

### Init Flags

**Build-time init (packages):**
- `io.smallrye.common` — smallrye utility classes
- `io.quarkus` — quarkus bootstrap/logging classes
- `org.crac` — checkpoint/restore API
- `org.jboss.logging` — logging facade
- `org.jboss.logmanager` — JBoss log manager
- `org.jboss.modules` — module system (optional dep)
- `org.apache.log4j` — log4j 1.x (optional dep)
- `org.apache.logging.log4j` — log4j 2.x API (optional dep)
- `org.slf4j` — SLF4J API
- `jakarta.json` — Jakarta JSON API

**Run-time init (deferred):**
- `io.smallrye.common.os.Process` — calls native Process APIs
- `io.smallrye.common.net` — `CidrAddress` creates `Inet6Address` objects (JDK run-time init conflict)
- `io.smallrye.common.io.Files2` — bakes CWD path into image heap
- `io.smallrye.common.io.Files2$PrivateAttrs` — bakes user info into image heap
- `io.smallrye.common.function.FunctionsLogging` — creates a jboss Logger at `<clinit>`
- `org.jboss.logmanager.handlers.ConsoleHandler$ConsoleHolder` — console native access
- `org.jboss.logmanager.handlers.SyslogHandler` — syslog native access
- `org.jboss.logmanager.configuration.ObjectBuilder$ModuleFinder$Holder` — references `org.jboss.modules`
- `org.jboss.logmanager.formatters.JsonFormatter` — references `jakarta.json.stream`
- `org.apache.log4j.net` — networking (sockets)
- `org.apache.log4j.jdbc` — JDBC
- `org.apache.log4j.nt` — Windows NT native (`NTEventLogAppender`)
- `org.apache.log4j.or.jms` — JMS (`javax.jms.JMSException` not on CP)
- `org.apache.log4j.chainsaw` — Swing GUI
- `org.apache.log4j.lf5` — Swing GUI
- `org.apache.log4j.jmx` — JMX

### Startup Time

**1.032s – 1.143s** across measurements (within variance of baseline 1.023s).

---

## Key Technical Discoveries

### 1. CP-Only Jars Crash at Runtime

Jars placed on `-cp` without `-H:Preserve` cause **runtime crashes**:
```
Fatal error: Unable to call AOT method: ... name=getMessageLogger
Consider using '-H:Preserve=package=org.jboss.logging'
```

The `jvm-library` macro's `--link-at-build-time` registers all CP classes for
AOT compilation. If a class is registered but NOT compiled (no Preserve), the
runtime aborts when that method is called.

**Rule: Every jar on `-cp` MUST also be in `-H:Preserve=path=`**

**Exception:** Jars whose classes are never actually invoked at runtime can be
on CP without Preserve. This is how we handle the optional deps (log4j,
jboss-modules) — they satisfy linking but are never called because Quarkus
uses jboss-logmanager, not log4j.

### 2. Optional Dependencies Solved with Real Jars

The main blocker was `jboss-logging` referencing `log4j`, `log4j2`, and `slf4j`,
and `jboss-logmanager` referencing `org.jboss.modules`. These are optional
dependencies — the classes exist in jboss-logging/logmanager but are only used
if the corresponding logging backend is present at runtime.

**Solution:** Download the actual dependency jars and place them on the classpath
(without Preserve). This satisfies the linker without bloating `libjvm.so`.
Since Quarkus never calls log4j at runtime, the "CP without Preserve crashes"
issue doesn't apply.

### 3. `getCodeSource()` Breaks Under Preserve

`QuarkusEntryPoint.resolveJarPath()` uses `getProtectionDomain().getCodeSource().getLocation()`
to find its own jar location at runtime. When the class is AOT-compiled into
`libjvm.so`, the code source points to the native-image build directory, not
the original jar. This is a fundamental incompatibility — classes that locate
resources relative to their own jar path cannot be Preserved.

### 4. `--link-at-build-time` (from `jvm-library` macro)

The `jvm-library` macro (`$GRAALVM_HOME/lib/svm/macros/jvm-library/native-image.properties`)
includes `--link-at-build-time` which requires ALL classes on the classpath to be
fully linkable. If any class references a type not on the classpath, the build
fails — even if that class is set for `--initialize-at-run-time`.

The macro also sets:
- `-H:+AssertInitializationSpecifiedForAllClasses` — every class on CP needs explicit init policy
- `-H:+GuaranteeSubstrateTypesLinked` — all substrate types must be linkable

### 5. `-H:-DetectUserDirectoriesInImageHeap` Not Needed

Earlier iterations used this flag to work around `Files2.CWD` baking the build-time
working directory into the image heap. The proper fix is
`--initialize-at-run-time=io.smallrye.common.io.Files2` which defers the class
init (including `CWD` capture) to runtime. The detection flag is then unnecessary.

### 6. `Inet6Address` Cannot Be Build-Time Initialized

`CidrAddress` static initializer creates `Inet6Address` objects. The image builder
demands `--initialize-at-build-time=java.net.Inet6Address` but this conflicts with
JDK requirements (`InetAddress` must be run-time initialized for JNI native code
support). The solution is `--initialize-at-run-time=io.smallrye.common.net` to
defer the entire net package.

### 7. log4j 1.x Has Many Problematic Subpackages

The full `log4j-1.2.17.jar` contains classes with native deps (`nt`), external
deps (`jms`, `jdbc`), and Swing GUI components (`chainsaw`, `lf5`). Since we
init `org.apache.log4j` at build time for linking, each problematic subpackage
must be explicitly deferred with `--initialize-at-run-time`.

---

## Main Jars Experiment (In Progress)

Attempted adding ALL main jars (100 jars) at once with ALL boot jars.
This required downloading many additional optional dependency jars.

### Optional Dependencies Discovered

When adding main jars (especially netty, vertx, smallrye-config), many
additional optional dependencies surface that need to be on the classpath
for linking:

| Optional Dep | Needed By | Reason |
|-------------|-----------|--------|
| `javax.jms-api` | log4j | `or.jms.MessageRenderer` |
| `javax.mail-api` | log4j | `net.SMTPAppender` |
| `jboss-marshalling-osgi` | netty | `codec-marshalling.LimitingByteInput` |
| `protobuf-java` | netty | `codec-protobuf` |
| `protobuf-javanano` | netty | `codec-protobuf` (nano) |
| `lz4-java` | netty | `codec-compression.Lz4FrameEncoder` |
| `zstd-jni` | netty | `codec-compression.ZstdConstants` |
| `compress-lzf` | netty | `codec-compression.LzfDecompressor` |
| `lzma-java` | netty | `codec-compression` (LZMA) |
| `jzlib` | netty | `codec-compression` (JZlib) |
| `asm` | smallrye-config | `ConfigMappingGenerator` |
| `jackson-core-2.x` | vertx-core | `json.jackson.JacksonCodec` (backward compat) |
| `jackson-databind-2.x` | vertx-core | `json.jackson` |
| `conscrypt-openjdk-uber` | netty | `handler.ssl` |
| `bcpkix-jdk18on` | smallrye-certs | X509 cert builder |
| `bcprov-jdk18on` | BouncyCastle | provider |
| `bcutil-jdk18on` | BouncyCastle | utility |

### Additional Run-Time Init Classes Discovered

- `org.eclipse.parsson.JsonMessages` — loads `PropertyResourceBundle` at `<clinit>`
- `jakarta.activation.MailcapCommandMap` — bakes user home dir into image heap
- `jakarta.activation.MimetypesFileTypeMap` — similar path issue

### Current Blocker for Full Main Jar Build

`io.smallrye.common.net.CidrAddress.<clinit>` creates `Inet6Address` objects.
`java.net.Inet6Address` cannot be build-time initialized (conflicts with JDK's
`InetAddress` run-time init requirement for JNI). With all main jars on CP,
more code paths reach `CidrAddress`, and deferring it to run-time doesn't prevent
the `<clinit>` from being parsed by the analysis.

Possible solutions:
1. GraalVM substitution to replace `CidrAddress` static fields
2. Exclude `smallrye-common-net` from Preserve (but other jars reference it)
3. Patch `CidrAddress` to lazy-init the `Inet6Address` objects

### `quarkus-bootstrap-runner` Must Be Included

When adding main jars, `quarkus-bootstrap-runner` MUST be on CP + Preserve
because other jars reference its classes (e.g., `InitialConfigurator`,
`QuarkusDelayedHandler`). Without it, the build fails:
```
java.lang.IllegalArgumentException: io.quarkus.bootstrap.logging.InitialConfigurator.class
/* Warning: type not present! */
```

The `getCodeSource()` path issue from `QuarkusEntryPoint.resolveJarPath()`
caused `NoSuchFileException` when we Preserved it. This needs a workaround
(GraalVM substitution or Quarkus patch).

---

## Next Steps

### 1. Resolve `CidrAddress` / `Inet6Address` blocker

This is the main blocker for adding all main jars. Options:
- Create a GraalVM substitution for `CidrAddress` static fields
- Patch `CidrAddress` upstream to lazy-init
- Exclude `smallrye-common-net` from Preserve and handle downstream

### 2. Resolve `quarkus-bootstrap-runner` `getCodeSource()` issue

Needed to include all boot + main jars. Options:
- GraalVM substitution for `QuarkusEntryPoint.resolveJarPath()`
- Quarkus patch to support system property fallback

### 3. Add main jars incrementally

Once blockers are resolved, add main jars in dependency order.
The dependency tree analysis showed 25 leaf jars that can be added first,
then work up through the dependency layers.

---

## Startup Time Tracking

| Configuration | Startup Time | Jars Preserved |
|--------------|-------------|----------------|
| Baseline (no extra jars) | 1.023s | 0 |
| +cpu,os only | 1.156s | 2 |
| All boot jars + 3 main deps | 1.032s – 1.143s | 18 |

Note: Startup time differences are within measurement variance at this stage.
With only boot jars (~small utility classes), the impact is minimal. Significant
improvement is expected when adding the larger framework jars (netty, vert.x,
quarkus-core) which have much more code to JIT compile.

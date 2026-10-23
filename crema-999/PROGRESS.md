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

### Preserved Jars (19 jars, AOT compiled into libjvm.so)

**All 16 boot jars:**

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
| `quarkus-bootstrap-runner-999-SNAPSHOT.jar` | ✅ Preserved (fixed) |
| `quarkus-classloader-commons-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-development-mode-spi-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-value-registry-999-SNAPSHOT.jar` | ✅ Preserved |
| `quarkus-vertx-latebound-mdc-provider-999-SNAPSHOT.jar` | ✅ Preserved |

**3 main jars (transitive deps):**

| Jar | Why needed |
|-----|-----------|
| `jakarta.json-api-2.1.3.jar` | Required by `jboss-logmanager` (`JsonFormatter`) |
| `slf4j-api-2.0.19.jar` | Required by `jboss-logging` (`Slf4jLogger`) |
| `smallrye-common-search-2.20.0.jar` | Required by `smallrye-common-io` (`Archive`) |

### CP-Only Jars (3 optional deps, NOT Preserved)

Downloaded by the build script. On classpath to satisfy `--link-at-build-time`
linking requirements. Never called at runtime.

| Jar | Why needed |
|-----|-----------|
| `log4j-1.2.17.jar` | `jboss-logging` → `Log4jLogger` references `org.apache.log4j.Logger` |
| `log4j-api-2.24.3.jar` | `jboss-logging` → `Log4j2Logger` references `org.apache.logging.log4j.spi.AbstractLogger` |
| `jboss-modules-2.1.5.Final.jar` | `jboss-logmanager` → `ObjectBuilder$ModuleFinder$Holder` references `org.jboss.modules.ModuleLoader` |

### Init Flags

**Build-time init (packages):**
`io.smallrye.common`, `io.quarkus`, `org.crac`, `org.jboss.logging`,
`org.jboss.logmanager`, `org.jboss.modules`, `org.apache.log4j`,
`org.apache.logging.log4j`, `org.slf4j`, `jakarta.json`

**Run-time init (deferred):**
- `io.smallrye.common.os.Process` — native Process APIs
- `io.smallrye.common.net.HostName` — captures hostname
- `io.smallrye.common.net.CidrAddress` — creates `Inet6Address` (JDK init conflict)
- `io.smallrye.common.net.CidrAddressTable` — depends on `CidrAddress`
- `io.smallrye.common.io.Files2` — bakes CWD path
- `io.smallrye.common.io.Files2$PrivateAttrs` — bakes user info
- `io.smallrye.common.function.FunctionsLogging` — creates Logger at `<clinit>`
- `org.jboss.logmanager.handlers.ConsoleHandler$ConsoleHolder` — console native
- `org.jboss.logmanager.handlers.SyslogHandler` — syslog native
- `org.jboss.logmanager.configuration.ObjectBuilder$ModuleFinder$Holder` — jboss.modules ref
- `org.jboss.logmanager.formatters.JsonFormatter` — jakarta.json.stream ref
- `org.apache.log4j.{net,jdbc,nt,or.jms,chainsaw,lf5,jmx}` — log4j subpackages with native/external deps

### Startup Time

**0.984s** — first sub-1s result, **3.8% improvement** over baseline.

---

## Quarkus Fix: `resolveJarPath` for AOT/Crema

**Problem:** `QuarkusEntryPoint.resolveJarPath()` uses
`getProtectionDomain().getCodeSource().getLocation()` to find the jar path.
When AOT-compiled into `libjvm.so`, `getCodeSource()` returns the `java`
binary path (via `ProcessProperties.getExecutableName()`), not the jar.
The method returned a non-null path, so the `getResource()` fallback was
never reached.

**Fix:** Added a `.jar` suffix check before returning the code source path:
```java
if (location != null) {
    Path path = Path.of(location.toURI());
    if (path.toString().endsWith(".jar")) {
        return path;
    }
}
// fallback: find class resource in jar on classpath
return resolveJarPathFromClassResource(
        entryPointClass.getResource(entryPointClass.getSimpleName() + ".class"));
```

When the path is not a `.jar` (AOT case), it falls through to `getResource()`
which finds the `.class` file in the boot jar on the classpath and extracts
the jar path from the `jar:file:///...!/...` URL.

**Committed in:** `~/src/quarkus` on branch `topic.layers.oct26`

---

## Main Jars Experiment (In Progress)

Attempted adding ALL 100 main jars at once with all boot jars.

### Optional Dependencies Discovered

When adding main jars, many additional optional dependencies surface that
need to be on the classpath for linking:

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

### Current Blocker: `CidrAddress` / `Inet6Address`

`io.smallrye.common.net.CidrAddress.<clinit>` creates `Inet6Address` objects.
`java.net.Inet6Address` cannot be build-time initialized because it conflicts
with JDK requirements — `InetAddress` must be run-time initialized for JNI
native code support:

```
Error: Incompatible change of initialization policy for java.net.InetAddress:
trying to change BUILD_TIME super type of java.net.Inet6Address to RUN_TIME
for JDK native code support via JNI
```

Setting `--initialize-at-run-time=io.smallrye.common.net.CidrAddress` defers
the class init, but with all main jars on the classpath, more code paths reach
`CidrAddress` and the analysis still parses its `<clinit>`, discovering the
`Inet6Address` constants.

**This is the sole remaining blocker for adding all main jars.**

Possible solutions:
1. **Patch `CidrAddress`** upstream to lazy-init the `Inet6Address` objects
   (move them out of `<clinit>` into a holder class or lazy field)
2. **GraalVM substitution** to replace `CidrAddress` static fields
3. **Exclude `smallrye-common-net` from Preserve** and handle downstream
   (but other boot/main jars reference it)

---

## Key Technical Discoveries

### 1. CP-Only Jars Crash at Runtime (with exceptions)

Jars on `-cp` without `-H:Preserve` cause runtime crashes IF their classes
are actually called. The `--link-at-build-time` macro registers them for AOT
but they're not compiled.

**Exception:** Jars whose classes are never invoked at runtime can be on CP
without Preserve. This is how we handle optional deps (log4j, jboss-modules).

### 2. Optional Dependencies Solved with Real Jars

The main blocker was `jboss-logging` referencing `log4j`/`log4j2`/`slf4j`,
and `jboss-logmanager` referencing `org.jboss.modules`. These are optional
deps — classes exist but are only used if the backend is present.

**Solution:** Download the actual dependency jars and place them on the
classpath without Preserve. This satisfies the linker without bloating
`libjvm.so`.

### 3. `getCodeSource()` Returns Executable Path in Crema

For AOT-compiled classes in `libjvm.so`, `getCodeSource().getLocation()`
returns the executable path (via `ProcessProperties.getExecutableName()`),
not the original jar path. Fixed in Quarkus by checking for `.jar` suffix
and falling back to `getResource()`.

### 4. `--link-at-build-time` from `jvm-library` Macro

The macro includes blanket `--link-at-build-time` requiring ALL classes to
be fully linkable. Also sets `-H:+AssertInitializationSpecifiedForAllClasses`
and `-H:+GuaranteeSubstrateTypesLinked`.

### 5. `Inet6Address` Cannot Be Build-Time Initialized

JDK constraint: `InetAddress` (superclass) must be run-time initialized for
JNI. Any class whose `<clinit>` creates `Inet*Address` objects forces the
image builder to request build-time init for `Inet6Address`, which conflicts.

### 6. log4j 1.x Has Many Problematic Subpackages

The full jar contains classes with native deps (`nt`), external deps (`jms`,
`jdbc`), and Swing GUI (`chainsaw`, `lf5`). Each must be explicitly deferred.

---

## Startup Time Tracking

| Configuration | Startup Time | Jars Preserved |
|--------------|-------------|----------------|
| Baseline (no extra jars) | 1.023s | 0 |
| +cpu,os only | 1.156s | 2 |
| 15 boot jars + 3 main deps | 1.032s – 1.143s | 18 |
| **All 16 boot jars + 3 main deps** | **0.984s** | **19** |

---

## Next Steps

### 1. Resolve `CidrAddress` / `Inet6Address` blocker (MAIN BLOCKER)

This is the sole remaining blocker for adding all ~100 main jars.
`CidrAddress.<clinit>` creates `Inet6Address` objects which can't be
build-time initialized. With all main jars on CP, more analysis paths
reach this class.

Options:
- Patch `CidrAddress` to lazy-init (move `Inet6Address` fields to a
  holder class initialized on first access)
- GraalVM substitution for the static fields
- Investigate if `-H:+UnlockExperimentalVMOptions` can relax the
  `InetAddress` JNI constraint

### 2. Add main jars incrementally after blocker is resolved

Dependency tree analysis identified 25 leaf jars (no deps on other main
jars). Add those first, then work up through the dependency layers.
Use `--initialize-at-run-time` flags from `layers-999/build-layer-base.sh`
for netty and vert.x.

### 3. Update `build-libjvm.sh` for main jars

The build script lists all needed optional deps in comments. Once the
blocker is resolved, uncomment and enable them.

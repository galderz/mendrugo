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

### Preserved Jars (2 jars, AOT compiled into libjvm.so)

| Jar | Package |
|-----|---------|
| `smallrye-common-cpu-2.20.0.jar` | `io.smallrye.common.cpu` |
| `smallrye-common-os-2.20.0.jar` | `io.smallrye.common.os` |

### Init Flags

**Build-time init:**
- `io.smallrye.common.cpu`
- `io.smallrye.common.os`

**Run-time init (deferred):**
- `io.smallrye.common.os.Process` — calls native Process APIs

### Startup Time

**~1.156s** (within measurement variance of baseline)

### Important Discovery: CP-Only Jars Crash at Runtime

Putting jars on the classpath WITHOUT Preserve causes runtime crashes:
```
Fatal error: Unable to call AOT method ... org/jboss/logging/Logger
Consider using '-H:Preserve=package=org.jboss.logging'
```
This means **every jar on `-cp` MUST also be in `-H:Preserve`**. Jars cannot
be added as "compile-time only" dependencies.

---

## Boot Jars Not Yet Added

### `org.jboss.logging.jboss-logging-3.6.3.Final.jar`

**Status: On classpath only, cannot be Preserved**

**Blocker:** The jar contains `Log4jLogger`, `Log4j2Logger`, and `Slf4jLocationAwareLogger`
which reference optional logging backends (`org.apache.log4j`, `org.apache.logging.log4j`,
`org.slf4j`) that are not on the classpath. The `--link-at-build-time` flag from
the `jvm-library` macro requires ALL types in Preserved jars to be fully linkable.
Setting `--initialize-at-run-time` for these classes does NOT prevent the linker from
resolving their type references.

When we tried adding it to Preserve without also having log4j on the CP, the app
crashed at runtime with:
```
Fatal error: Unable to call AOT method ... org/jboss/logging/Logger; name=getMessageLogger
Consider using '-H:Preserve=package=org.jboss.logging'
```

### `org.jboss.logmanager.jboss-logmanager-3.2.2.Final.jar`

**Status: Not on classpath or Preserve**

**Blocker:** Contains `ObjectBuilder$ModuleFinder$Holder` which extends
`org.jboss.modules.ModuleClassLoader` (not available). Also needs:
- `io.smallrye.common.expression` (from boot)
- `io.smallrye.common.net` (from boot)
- `io.smallrye.common.os` (from boot)
- `jakarta.json` (from main)
- `io.smallrye.common.search` (from main)

Even when all these are provided, the `ModuleClassLoader` reference causes
linking failure with no workaround found.

Additionally, the logmanager is registered as the system `java.util.logging.LogManager`
via SPI, so it gets pulled into the image heap automatically, requiring
`--initialize-at-build-time` for `LogManager`, `Level`, `JBossLoggerFinder`, etc.

### `io.smallrye.common.smallrye-common-net-2.20.0.jar`

**Status: Not added**

**Blocker:** `CidrAddress` static initializer creates `Inet4Address` and `Inet6Address`
objects. The image builder demands `--initialize-at-build-time=java.net.Inet6Address`
which is a JDK class and may conflict with other expectations. Setting
`--initialize-at-run-time=io.smallrye.common.net.CidrAddress` was tried but the
static initializer is still parsed and the embedded constants are discovered.

### `io.smallrye.common.smallrye-common-io-2.20.0.jar`

**Status: Not added**

**Blocker:** 
- `Files2.CWD` bakes the current working directory path into the image heap, triggering
  `-H:+DetectUserDirectoriesInImageHeap` error
- `Files2$PrivateAttrs.MY_USER` embeds user info
- Even with `-H:-DetectUserDirectoriesInImageHeap`, `Files2$PrivateAttrs` needs
  `--initialize-at-build-time=sun.nio.fs.UnixUserPrincipals$User`
- Needs `smallrye-common-search` from main lib

### Remaining boot jars (not yet attempted)

| Jar | Expected Dependencies |
|-----|----------------------|
| `quarkus-bootstrap-runner-999-SNAPSHOT.jar` | `jboss-logging`, `jboss-logmanager` |
| `quarkus-classloader-commons-999-SNAPSHOT.jar` | Unknown |
| `quarkus-development-mode-spi-999-SNAPSHOT.jar` | `jboss-logging` |
| `quarkus-value-registry-999-SNAPSHOT.jar` | Unknown |
| `quarkus-vertx-latebound-mdc-provider-999-SNAPSHOT.jar` | Unknown |
| `org.crac.crac-1.5.0.jar` | Unknown (likely self-contained) |

### Main jars (not yet started)

~100 jars in `lib/main/` covering netty, vert.x, quarkus-core, Jakarta APIs,
smallrye-config, Jackson, etc. The `../layers-999/build-layer-base.sh` script has
extensive `--initialize-at-run-time` flags for many of these.

---

## Key Technical Challenges

### 1. `--link-at-build-time` (from `jvm-library` macro)

The `jvm-library` macro includes `--link-at-build-time` which requires ALL classes
in Preserved jars to be fully linkable. If any class references a type not on the
classpath, the build fails — even if that class is set for `--initialize-at-run-time`.

The macro also sets:
- `-H:+AssertInitializationSpecifiedForAllClasses` — every class on CP needs explicit init policy
- `-H:+GuaranteeSubstrateTypesLinked` — all substrate types must be linkable

These can be overridden with `-H:+UnlockExperimentalVMOptions` but doing so
didn't resolve the fundamental linking issues.

### 2. Optional dependencies in logging jars

`jboss-logging` is a logging facade with optional backends (log4j, log4j2, slf4j).
`jboss-logmanager` has an optional dependency on `org.jboss.modules`. These optional
deps are loaded via class references in concrete classes, not just reflection, so
the linker tries to resolve them.

### 3. Static initializers with host-specific data

Several classes (`Files2`, `CidrAddress`, `HostName`, `Process`) capture
host-specific data (CWD, IP addresses, hostname, PID) in static fields. These
need `--initialize-at-run-time` to avoid baking build-time values into the image,
but some also create objects of types that themselves need init policy specified.

### 4. Circular dependency chains

Boot jars have significant interdependencies:
```
constraint → jboss-logging → logmanager → constraint, ref, net, os, expression
function   → jboss-logging → logmanager → ...
```

---

## Next Steps to Try

### 1. Try remaining self-contained boot jars

Try `org.crac.crac-1.5.0.jar` and the quarkus utility jars that may not
have deep dependency chains:
- `quarkus-value-registry`
- `quarkus-classloader-commons`
- `quarkus-development-mode-spi`
- `quarkus-vertx-latebound-mdc-provider`

### 2. Try `smallrye-common-net` with Inet address init

The blocker was `--initialize-at-build-time=java.net.Inet6Address`. This might
actually work — try it and test if the app still functions correctly.

### 3. Start adding main jars

Many main jars (Jakarta API jars, smallrye jars) are likely self-contained or
have resolvable deps. Good candidates to try first:
- `jakarta.inject.jakarta.inject-api-2.0.1.jar` (likely self-contained)
- `jakarta.annotation.jakarta.annotation-api-3.0.0.jar`
- `jakarta.json.jakarta.json-api-2.1.3.jar`
- `org.reactivestreams.reactive-streams-1.0.4.jar`
- `org.jspecify.jspecify-1.0.0.jar`

### 4. Use init flags from `layers-999/build-layer-base.sh`

The layers script has proven `--initialize-at-run-time` flags for netty, vert.x,
and other frameworks. Use those when adding the corresponding main jars:
```
io.netty.buffer, io.netty.util.*, io.netty.handler.ssl, ...
io.vertx.core.buffer.impl.*, io.vertx.core.json.Json, ...
```

### 5. Investigate providing stub jars for log4j (HIGH PRIORITY)

Create minimal stub jars containing just the classes referenced by jboss-logging
(`org.apache.log4j.Priority`, `org.apache.logging.log4j.message.Message`,
`org.slf4j.LoggerFactory`, `org.slf4j.spi.LocationAwareLogger`, etc.)
so that linking succeeds. This would unblock the ENTIRE dependency chain:
jboss-logging → constraint → ref → function → expression → logmanager →
bootstrap-runner → and most main jars.

This is likely the single highest-impact next step.

### 6. Investigate Crema-specific Preserve options

Check if there are finer-grained Preserve options (e.g., exclude specific classes)
or if the `--link-at-build-time` requirement can be relaxed per-jar rather than
globally.

---

## Startup Time Tracking

| Configuration | Startup Time | Delta |
|--------------|-------------|-------|
| Baseline (no extra jars) | 1.023s | — |
| +cpu | 1.166s | +0.143s (likely variance) |
| +cpu,os,ref,constraint (broken — crashed at runtime) | 1.082s | N/A |
| +cpu,os (current working) | 1.156s | +0.133s (variance) |

Note: Startup time differences at this stage are within measurement variance.
The benefit should become more visible as more jars are added, especially
the larger framework jars (netty, vert.x, quarkus-core).

### Key Discovery

Jars placed on `-cp` without `-H:Preserve` cause **runtime crashes** because
the jvm-library macro's `--link-at-build-time` registers them for AOT but
they are not compiled. When the app calls a method from such a jar, the
runtime aborts with "Unable to call AOT method". This means:
- Every jar on `-cp` **must** also be in `-H:Preserve=path=`
- A jar's entire transitive dependency chain must be resolvable at link time
- The `jboss-logging` → `log4j` dependency chain is the main blocker

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
| `netty-pkitesting` | netty | `SelfSignedCertificate` (`SecureRandom` in heap) |
| `netty-transport-classes-kqueue` | vertx-core | `KQueueTransport` (macOS) |
| `netty-transport-classes-io_uring` | vertx-core | `IoUringTransport` (Linux) |
| `netty-transport-classes-epoll` | vertx-core | `EpollTransport` (Linux) |
| `vertx-auth-jwt` | vertx-web | `JWTAuth` |
| `vertx-auth-htdigest` | vertx-web | `HtdigestAuth` |
| `vertx-auth-oauth2` | vertx-web | `OAuth2Auth` |
| `vertx-auth-webauthn4j` | vertx-web | `WebAuthn4J` |
| `vertx-auth-{htpasswd,ldap,otp,properties,sql-client}` | vertx-web | various auth providers |
| `vertx-sql-client` | vertx-auth-sql-client | SQL client API |
| `webauthn4j-core{,-async}` | vertx-auth-webauthn4j | WebAuthn impl |
| `webauthn4j-metadata{,-async}` | vertx-auth-webauthn4j | FIDO metadata |
| `blockhound` | vertx-core | `BlockHoundIntegration` |
| `jackson-annotations-2.x` | jackson-databind-2.x | annotations |
| `smallrye-mutiny-vertx-auth-*` | vertx-web | mutiny auth wrappers (**SPLIT PACKAGE**) |
| `smallrye-mutiny-vertx-sql-client` | vertx-auth-sql | mutiny SQL wrapper (**SPLIT PACKAGE**) |

### Additional Run-Time Init Classes Discovered

- `org.eclipse.parsson.JsonMessages` — loads `PropertyResourceBundle` at `<clinit>`
- `jakarta.activation.MailcapCommandMap` — bakes user home dir into image heap
- `jakarta.activation.MimetypesFileTypeMap` — similar path issue

### Resolved: `CidrAddress` / `Inet6Address`

**Fixed by including `quarkus-core` jar** which contains
`Target_io_smallrye_common_net_CidrAddress` — a GraalVM `@TargetClass`
substitution that uses `@InjectAccessors` to replace the `INET4_ANY_CIDR`
and `INET6_ANY_CIDR` static fields with lazy accessors. This avoids creating
`Inet6Address` objects during `<clinit>` while still allowing `CidrAddress`
to be build-time initialized.

### Resolved: Netty Buffer Allocator Conflicts

**Root cause was a wrong package name** in the run-time init list:
- Was: `io.vertx.core.buffer.impl.VertxByteBufAllocator`
- Fix: `io.vertx.core.impl.buffer.VertxByteBufAllocator`

`VertxByteBufAllocator.<clinit>` creates an `AdaptiveByteBufAllocator` and
stores it in `POOLED_ALLOCATOR`. With the correct package deferred to run-time,
the allocator instance is never created at build time and doesn't end up in
the image heap.

Also added `io.netty.channel.kqueue` and `io.netty.channel.uring` to run-time
init (platform-specific transports with native deps).

### False Success Retracted: Broken Line Continuation

A previous claim that "all boot + main jars build succeeds at 1.066s" was
incorrect. The build script (`build-libjvm-all.sh` at commit `0d09495`) had
a commented-out line `#-H:-AssertInitializationSpecifiedForAllClasses \`
after a `\` continuation. This broke the shell command in two, causing
native-image to run WITHOUT `-cp`, `--initialize-at-build-time`, or
`--initialize-at-run-time` flags — essentially building a minimal libjvm.so
with no Quarkus jars. The "success" was a build of the wrong thing.

A reference copy of the broken script is saved as
`build-libjvm-all-false-success-reference.sh`.

### Current Blocker: Multi-Release JAR Jackson 2.x References

`vertx-core-5.1.8.jar` is a Multi-Release JAR:
- **Root-level** `io.vertx.core.json.jackson.*` and `io.vertx.core.parsetools.impl.*`
  reference `com.fasterxml.jackson.core` (Jackson 2.x)
- **`META-INF/versions/21/`** has Jackson 3.x (`tools.jackson.core`) variants

On JDK 25, the versions/21 classes should be used. But GraalVM's
`--link-at-build-time` (from the `jvm-library` macro) processes ALL classes
in the jar including root-level, causing:
```
NoClassDefFoundError: com/fasterxml/jackson/core/JsonFactory
```

Jackson 2.x core/databind jars should NOT be on the classpath (they are
not Quarkus dependencies). The `jackson-annotations-2.21.jar` in main/
is used by Jackson 3.x for backward compat but the core/databind are not.

**This is a GraalVM Multi-Release JAR handling issue** — root-level classes
are being linked even when JDK 21+ variants exist.

---

## Key Technical Discoveries

### 1. CP-Only Jars Crash at Runtime (with exceptions)

Jars on `-cp` without `-H:Preserve` cause runtime crashes IF their classes
are actually called. The `--link-at-build-time` macro registers them for AOT
but they're not compiled.

**Exception:** Jars whose classes are never invoked at runtime can be on CP
without Preserve. This is how we handle optional deps (log4j, jboss-modules).

### 9. Preserve Triggers Reflection Registration for ALL Classes

**Root cause of why optional deps are needed in the boot-only build.**

The `-H:Preserve=path=` mechanism calls `PreserveOptionsSupport.registerPreservedClass()`
for EVERY class in the jar. This registers each class for reflection via
`RuntimeReflectionSupport.register()`, which triggers:

1. `ReflectionDataBuilder.registerClass()` → `registerTypesForTypeQuery()`
2. `registerAllMethodsQuery()` → `Class.getDeclaredMethods()` / `getMethods()`
3. The JVM resolves all method parameter/return types
4. `linkType()` → `type.link()` resolves all referenced classes
5. With `--link-at-build-time` (from jvm-library macro), any unresolvable class is fatal

So when `jboss-logging.jar` is Preserved, ALL its classes get reflected,
including `Log4jLoggerProvider` which has methods referencing `org.apache.log4j.Priority`.
The linker tries to resolve `Priority`, can't find it, and since `--link-at-build-time`
is set, the build fails.

**Confirmed by debug tracing** (with `-Dsvm.debug.reflectTrace=Log4j`):
```
REFLECT_REG: register(org.jboss.logging.Log4j2Logger, preserved=true)
  at ReflectionDataBuilder.register(ReflectionDataBuilder.java:254)
  at PreserveOptionsSupport.registerType(PreserveOptionsSupport.java:341)
  at PreserveOptionsSupport.registerPreservedClass(PreserveOptionsSupport.java:277)
  at PreserveOptionsSupport.registerPreservedClasses(PreserveOptionsSupport.java:229)
  at NativeImageGenerator.setupNativeImage(NativeImageGenerator.java:1269)
```

**Source:** `PreserveOptionsSupport.java` lines 228-270 in
`~/src/graal/substratevm/src/com.oracle.svm.hosted/src/com/oracle/svm/hosted/image/`

**Debug tracing committed** to `~/src/graal` — enable with
`-Dsvm.debug.reflectTrace=<substring>` on the native-image command.

**This explains why optional dependency jars (log4j, jboss-modules) must be on
the classpath even though they're never called at runtime.**

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

### 5. `Inet6Address` Cannot Be Build-Time Initialized (RESOLVED)

JDK constraint: `InetAddress` (superclass) must be run-time initialized for
JNI. Any class whose `<clinit>` creates `Inet*Address` objects forces the
image builder to request build-time init for `Inet6Address`, which conflicts.
**Resolved** by including `quarkus-core` which provides a GraalVM substitution
that uses `@InjectAccessors` for lazy initialization.

### 8. NativeImageClassLoader Drops Split Package Jars

When multiple jars on the classpath define classes in the same package
hierarchy, `NativeImageClassLoader` silently drops the "extra" jars.
This prevents optional dependency jars from being loaded when they share
package prefixes with jars already on the classpath. There is no warning
or error — the jars simply don't appear in the classpath.

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
| All boot + all main jars | ❌ build fails (MR-JAR Jackson 2.x) | 119 (attempted) |
| All boot + all main jars (false success) | 1.066s (broken script) | N/A |
| **All 16 boot + 97 main jars** | **0.47s (Quarkus) / 950ms (TTFR)** | **113** |

---

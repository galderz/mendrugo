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

## All Framework Jars Experiment (Nov 2026)

See [`build-libjvm.sh`](build-libjvm.sh) for the full script.
See [`benchmark.sh`](benchmark.sh) for the benchmarking script.

### Context

After GraalVM PR https://github.com/oracle/graal/pull/14901 was integrated,
`--link-at-build-time` triggered issues were resolved. Previously, when
`-H:Preserve=path=` was used, `PreserveOptionsSupport.registerPreservedClass()`
registered every class in the jar for reflection, which triggered
`Class.getDeclaredMethods()` → `linkType()` → class resolution. With
`--link-at-build-time` (from the `jvm-library` macro), any unresolvable
optional dependency class was fatal. This forced us to download and add
optional dependency jars (log4j, jboss-modules, osgi.core, etc.) to the
classpath just for linking.

With PR #14901, these optional deps are no longer needed, allowing a much
cleaner build that includes only the actual Quarkus framework jars.

### Updated Dependency Versions

Quarkus was updated to latest `main` (from the Oct 2026 `topic.crema.oct26`
branch base). Key version changes:
- netty: 4.2.18 → 4.2.20
- vertx: 5.1.8 → 5.2.3
- smallrye-mutiny-vertx: 4.0.2 → 4.1.1
- Jackson: `tools.jackson.core:jackson-core:3.1.8` (no Jackson 2.x at all)
- slf4j: 2.0.19 → 2.0.20
- Main jar count: 100 → 97 (some jars merged/removed upstream)

### Quarkus Changes

Created new `topic.crema.new` branch (based on latest `main` at `c64e3b71dc4`).

Ported fixes from `topic.crema.oct26`:

1. **resolveJarPath .jar suffix check** — cherry-picked from `8d1ef7795af`.
   When AOT-compiled into libjvm.so, `getCodeSource().getLocation()` returns
   the executable path (e.g., `/path/to/java`), not the jar. Added `.jar`
   suffix check before returning code source path.

2. **InetRunTime no-op setters** — cherry-picked from `7c4ff0ef180`.
   GraalVM requires both get and set methods for `@InjectAccessors` on
   non-final `@Alias` fields.

New fix:

3. **sun.java.command fallback for resolveJarPath** — commit `4a28192842f`.
   When AOT-compiled into libjvm.so, BOTH `getCodeSource().getLocation()` AND
   `getResource("QuarkusEntryPoint.class")` fail:
   - `getCodeSource()` returns the `java` binary path (not `.jar` → skipped)
   - `getResource()` returns `null` because the class bytecode is baked into
     libjvm.so and is not available as a resource from any jar on the classpath

   The original `resolveJarPathFromClassResource` fallback was designed for
   JDK-8376576 where `getCodeSource()` returns null but `getResource()` works
   (finding the class in a jar via `jar:file:///...!/...` URL). In the Crema
   case, neither works.

   Added a third fallback: `resolveJarPathFromCommand(sun.java.command)` which:
   - Reads the `sun.java.command` system property (contains the `-jar` arg path)
   - Extracts the jar path (`getting-started/target/quarkus-app/quarkus-run.jar`)
   - Since `resolveJarPath` is expected to return a path 3 levels below the app
     root (like `lib/boot/xxx.jar`), it finds any boot jar in `lib/boot/` under
     the `quarkus-run.jar` parent directory and returns that path instead
   - This ensures `jarPath.getParent().getParent().getParent()` correctly
     computes the `quarkus-app/` app root

### Build Script: `build-libjvm.sh`

**113 Preserved jars** (16 boot + 97 main). No optional/CP-only jars.

**Build-time initialized packages/classes:**
- JDK: `jdk.internal.jrtfs`, `jdk.nio.zipfs.ZipFileSystemProvider`,
  `sun.nio.fs.LinuxFileSystemProvider`, `sun.nio.fs.LinuxFileSystem`
- Logging: `org.jboss.logging`, `org.jboss.logmanager`,
  `io.quarkus.bootstrap.logging`, `io.quarkus.vertx.mdc.provider`,
  `io.smallrye.common.ref`
- Framework: `io.smallrye`, `io.quarkus`, `org.crac`, `io.netty`, `io.vertx`,
  `org.eclipse.microprofile`, `org.eclipse.parsson`, `org.jctools`, `org.jspecify`,
  `org.reactivestreams`, `org.wildfly.common`, `org.slf4j`, `jakarta`,
  `tools.jackson`, `org.jboss.resteasy.reactive`, `org.jboss.threads`,
  `mutiny.zero.flow.adapters`, `org.apache.commons.logging`

**Run-time initialized classes/packages:** See `build-libjvm.sh` for the full
list. Key categories:
- **smallrye**: `Process`, `net.*`, `Files2` — native/host-specific data
- **logmanager**: `ConsoleHandler$ConsoleHolder`, `SyslogHandler`,
  `ObjectBuilder$ModuleFinder$Holder`, `JsonFormatter` — native/optional deps
- **netty** (28 entries): platform-specific transports (kqueue, uring, epoll),
  native code (unix.Errors, tcnative), compression (Brotli, Zstd),
  SSL/TLS (handler.ssl), HTTP/2, QUIC, `CleanerJava24Linker$Holder`
  (native MemorySegment), `PlatformDependent`/`PlatformDependent0`
- **vertx** (14 entries): buffer allocator, JSON/Jackson, TLS, HTTP/2, HTTP/3,
  QUIC, clustered event bus, auth/JWT
- **quarkus runtime**: `InetRunTime`, `ExecutorRecorder`,
  `RuntimeConfigBuilder$UuidConfigSource$Holder`, `EmptyByteBufStub`,
  `AppendBuffer`
- **JDK GUI subsystem**: `java.awt`, `javax.swing`, `sun.awt`, `sun.java2d`,
  `sun.font`, etc.
- **Other**: `jdk.jpackage`, `org.jcp.xml.dsig.internal.dom`,
  `java.io.ObjectInputStream$Logging`

### Build Issues Resolved (new since previous experiments)

#### 1. Brotli4jLoader ClassNotFoundException

**Error:** `ClassNotFoundException: com.aayushatharva.brotli4j.Brotli4jLoader`
when `io.netty.handler.codec.compression.Brotli.<clinit>` runs at build time.

**Root cause:** `Brotli.<clinit>` calls `Class.forName("com.aayushatharva.brotli4j.Brotli4jLoader")`.
Brotli4j is not a Quarkus dependency. The build-time init of the `io.netty` package
triggers this.

**Fix:** Added `io.netty.handler.codec.compression.Brotli` and
`io.netty.handler.codec.compression.BrotliDecompressor` to run-time init.
`BrotliDecompressor.<clinit>` also references `Brotli` transitively.

#### 2. CleanerJava24Linker native MemorySegment in image heap

**Error:** `Detected a native MemorySegment in the image heap` from
`io.netty.util.internal.CleanerJava24Linker$Holder.INVOKE_MALLOC`.

**Root cause:** `CleanerJava24Linker$Holder.<clinit>` uses
`java.lang.foreign.Linker.downcallHandle()` to create a native function handle
for `malloc`. The resulting `MethodHandle` contains a `NativeMemorySegmentImpl`
pointing to unmanaged C memory. This was being initialized at build time because
`io.netty` was in the build-time init list, and while
`io.netty.util.internal.CleanerJava24Linker` was already in the run-time list,
the `$Holder` inner class was not explicitly listed (it's a separate class from
GraalVM's perspective).

**Fix:** Added `io.netty.util.internal.CleanerJava24Linker$Holder` to run-time init.

#### 3. JacksonCodec constant in image heap

**Error:** `An object of type 'io.vertx.core.json.jackson.v3.JacksonCodec' was
found in the image heap. This type, however, is marked for initialization at
image run time.`

**Root cause:** `io.vertx.core.json` was in the run-time init list (from previous
experiments that used Jackson 2.x multi-release JAR workarounds). With the
current Quarkus (Jackson 3.x only, no multi-release JAR issue), `JacksonCodec`
is used as a constant in `JsonObject.encode()` bytecode. Since `io.vertx` is
build-time initialized but `io.vertx.core.json` overrode that to run-time, the
constant couldn't be stored in the image heap.

**Fix:** Removed `io.vertx.core.json` from run-time init. It is now build-time
initialized as part of the `io.vertx` package.

#### 4. ZipFileSystemProvider in image heap

**Error:** `An object of type 'jdk.nio.zipfs.ZipFileSystemProvider' was found
in the image heap` via `io.smallrye.common.classloader.ClassPathUtils.processAsJarPath`.

**Root cause:** `ClassPathUtils` has a constant `ZipFileSystemProvider` instance
embedded in bytecode. It gets into the image heap when smallrye classes are
build-time initialized.

**Fix:** Added `jdk.nio.zipfs.ZipFileSystemProvider`, `sun.nio.fs.LinuxFileSystemProvider`,
and `sun.nio.fs.LinuxFileSystem` to build-time init.

### Benchmark Results

5 runs each, measured with [`benchmark.sh`](benchmark.sh):

| Configuration | Quarkus Startup | TTFR | RSS | libjvm.so size |
|--------------|----------------|------|-----|----------------|
| Custom libjvm.so (113 jars, -svm) | 0.45–0.50s | 920–1020ms | ~375MB | 601MB |
| Baseline Crema (no custom jars, -svm) | — (no log output) | 1040–1070ms | ~320MB | ~300MB (est.) |
| Plain OpenJDK 25 (no -svm) | 0.29–0.36s | 390–520ms | ~131MB | — |

**Detailed runs:**

```
--- Custom libjvm.so (all Quarkus framework jars) ---
custom-1: quarkus=0.501s ttfr=1024ms rss=376MB
custom-2: quarkus=0.491s ttfr=968ms  rss=374MB
custom-3: quarkus=0.474s ttfr=932ms  rss=375MB
custom-4: quarkus=0.459s ttfr=926ms  rss=373MB
custom-5: quarkus=0.467s ttfr=919ms  rss=376MB

--- Plain OpenJDK 25 ---
openjdk-1: quarkus=0.358s ttfr=523ms rss=130MB
openjdk-2: quarkus=0.310s ttfr=428ms rss=132MB
openjdk-3: quarkus=0.308s ttfr=406ms rss=130MB
openjdk-4: quarkus=0.296s ttfr=390ms rss=130MB
openjdk-5: quarkus=0.305s ttfr=414ms rss=130MB

--- Baseline Crema (no custom jars) ---
baseline-crema-1: ttfr=1072ms rss=316MB
baseline-crema-2: ttfr=1058ms rss=323MB
baseline-crema-3: ttfr=1042ms rss=318MB
```

### Analysis

**Within Crema:** The custom libjvm.so with all Quarkus framework jars
improves TTFR by ~10% over baseline Crema (~950ms vs ~1050ms). The Quarkus-
reported startup time (0.47s) is significantly faster than the previous
best (0.984s with 19 jars), showing that pre-compiling framework code into
libjvm.so does reduce Quarkus's internal initialization time.

**vs. Plain OpenJDK 25:** The custom libjvm.so is ~2.3x slower in TTFR
and uses ~2.9x more RSS. This gap suggests the overhead is in the Crema/SVM
runtime infrastructure itself, not in our customization.

**Key observations:**
1. **RSS gap** (375MB vs 131MB): The 601MB `libjvm.so` has a 411MB image heap.
   Even though only touched pages are resident, the image heap is large enough
   that startup-related page faults add up significantly.
2. **TTFR vs Quarkus startup gap**: Custom libjvm.so reports Quarkus startup
   at ~0.47s but TTFR is ~0.95s, meaning ~0.48s is spent outside Quarkus
   (JVM bootstrap + libjvm.so loading + first request processing). For plain
   OpenJDK, the gap is ~0.10s (0.31s Quarkus + 0.10s overhead).
3. **Baseline Crema has no log output**: The baseline Crema (without custom
   jars) doesn't output Quarkus logs, so we can't compare Quarkus-internal
   startup time for that configuration. The TTFR (~1050ms) is the only metric.
4. **First run is always slower**: Both configurations show the first run being
   slower (cold filesystem caches, page cache misses for the shared library).

### Learnings

1. **GraalVM PR #14901 eliminates optional dep jars**: The previous need for
   log4j, jboss-modules, osgi.core, jackson-2.x, and many other optional deps
   on the classpath was entirely due to `--link-at-build-time` + Preserve
   reflection registration. With PR #14901, the build script is dramatically
   simpler (no downloads, no CP-only jars).

2. **Inner classes need separate run-time init entries**: GraalVM treats
   `Foo` and `Foo$Bar` as separate classes for initialization purposes.
   Adding `Foo` to run-time init does NOT automatically defer `Foo$Bar`.
   This was the case for `CleanerJava24Linker$Holder`.

3. **Multi-release JAR issue is gone**: The Jackson 2.x `NoClassDefFoundError`
   that was the main blocker in the previous experiment no longer occurs.
   Quarkus main now ships `tools.jackson.core:jackson-core:3.1.8` (Jackson 3.x)
   and `vertx-core` no longer has Jackson 2.x root-level classes.

4. **AOT-compiled classes are invisible to getResource()**: When a class is
   compiled into libjvm.so, `Class.getResource("ClassName.class")` returns
   null. This is because the class bytecode is baked into the native image
   and is not available as a file resource from any classloader. The previous
   `resolveJarPath` fix only handled the `getCodeSource()` failure but not
   the `getResource()` failure.

5. **Build-time init of io.vertx.core.json is now safe**: Previous experiments
   required `io.vertx.core.json` to be run-time initialized due to Jackson 2.x
   multi-release JAR issues. With Jackson 3.x only, the `JacksonCodec` constant
   can safely be build-time initialized as part of the `io.vertx` package.

### Next Steps

Profiling/tracing of the Crema startup is needed to identify where the
time is spent and find optimization opportunities. Areas to investigate:
- libjvm.so load time (mmap + relocation of 601MB shared library)
- Image heap initialization and page fault overhead
- Interpreter warmup vs JIT compilation latency differences
- Whether the large image heap size can be reduced
- Whether build-time init of more framework classes helps or hurts

---

## Next Steps

### 1. Resolve split package filtering (MAIN BLOCKER)

Native-image's `NativeImageClassLoader` silently drops jars that create
split packages. The `smallrye-mutiny-vertx-auth-*` jars are affected.

Options:
- **GraalVM fix**: Allow split packages on the classpath, or add a flag
  to disable this filtering for specific jars
- **Upstream fix**: Merge auth provider classes into `smallrye-mutiny-vertx-auth-common`
  so they're all in one jar
- **Build workaround**: Create a fat jar with all auth provider classes
  and replace the individual jars
- **Skip these jars**: Exclude `vertx-web` and `smallrye-mutiny-vertx-web`
  from Preserve since they trigger the auth provider reflection. This
  would sacrifice AOT compilation of vertx-web but allow the rest to work.

### 2. Try incremental main jar addition

Instead of adding all 100 main jars at once, add them in groups to
isolate which jars trigger the split package issue:
1. Jakarta API jars (pure interfaces)
2. smallrye-config, smallrye-context-propagation, mutiny
3. netty jars
4. vertx-core (without vertx-web)
5. quarkus-core
6. vertx-web and rest-reactive (these trigger the auth split package issue)

### 3. Update `build-libjvm.sh` for main jars

Once the blocker is resolved, update the working build script to include
all main jars with the resolved init flags and optional deps.

---

## Side Experiment: Why JBoss LogManager Can't Be Runtime Initialized

### Root Cause Found

When jboss-logmanager is on the native-image classpath, it registers itself
as the system `System.LoggerFinder` via:
```
META-INF/services/java.lang.System$LoggerFinder → org.jboss.logmanager.JBossLoggerFinder
```

During `TrustStoreManagerFeature.afterRegistration()`, GraalVM loads the
trust store certificates. This triggers a chain:
```
TrustStoreManagerFeature.afterRegistration()
  → TrustStoreManager.getTrustedCerts() (via reflection)
    → KeyStore.load() → PKCS12KeyStore.engineLoad()
      → CertificateFactory.generateCertificate()
        → JCAUtil.tryCommitCertEvent()
          → EventHelper.isLoggingSecurity()
            → LazyLoggers → BootstrapLogger
              → System.LoggerFinder.getLoggerFinder()
                → JBossLoggerFinder (via SPI)
                  → JBossLoggerFinder.getLogger()
                    → JBossSystemLogger.<init>
```

The `JBossSystemLogger` instance is created at build time and ends up in
the image heap. Since `org.jboss.logmanager` classes are marked for run-time
init, this creates a conflict.

**Confirmed with debug tracing** (`-Dsvm.debug.trustStoreInit=true`):
```
TRUST_STORE_DEBUG: System.LoggerFinder = org.jboss.logmanager.JBossLoggerFinder
```

### Option 2 Experiment: Stripping SPI Files from Jars

**Approach:** Create modified jars with SPI service files removed so that
JBoss LogManager / JBossLoggerFinder / ModuleLoggerFinder are never
auto-discovered during native-image build.

**Files removed:**
- `jboss-logmanager`: `META-INF/services/java.util.logging.LogManager`,
  `META-INF/services/java.lang.System$LoggerFinder`
- `jboss-modules`: `META-INF/services/java.lang.System$LoggerFinder`

**Script:** [`strip-spi-files.sh`](strip-spi-files.sh) — strips the files,
backs up originals, supports `--restore`.

**Build result:** `libjvm.so` builds successfully with
[`build-libjvm-minimal-build-init.sh`](build-libjvm-minimal-build-init.sh).
No build-time init needed for `io.smallrye.common`, `io.quarkus`,
`org.jboss.logging`, `org.jboss.logmanager`, etc. Only a small set of
specific classes need init policy, plus `-H:-AssertInitializationSpecifiedForAllClasses`.

**Additional optional dep required:** `osgi.core-5.0.0.jar` — log4j2's
`OsgiServiceLocator` references `org.osgi.framework.FrameworkUtil`. Added
as CP-only (not Preserved), deferred to run-time init.

**Runtime result:** Quarkus starts and responds to HTTP requests (`/hello`
returns `"hello"`), **but produces no log output** — no banner, no startup
time. The JDK default `LogManager` gets baked into the image heap at build
time. At runtime, `QuarkusEntryPoint.main()` calls
`System.setProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")`
but `LogManager.<clinit>` has already run in the image, so the property
is ignored and JBoss LogManager is never selected.

Marking `java.util.logging.LogManager` for run-time init does NOT work
either — the same `TrustStoreManagerFeature` → `EventHelper.securityLogger`
chain embeds `Logger` objects with `LogManager` references as constants in
JDK bytecode, causing image heap conflicts.

**Conclusion:** Stripping SPI files solves the build-time problem completely
but breaks runtime logging. The Quarkus application functions correctly
(HTTP works) but is silent. A runtime mechanism to re-initialize or replace
the `LogManager` singleton after image startup is still needed.

### Awaiting Next Steps

The fix needs to prevent JBossLoggerFinder from being used during
native-image build. Options:
1. Temporarily override System.LoggerFinder before TrustStoreManager loads
2. ~~Filter out jboss-logmanager's System$LoggerFinder service file from CP~~
   **Tested** — solves build but breaks runtime logging (see above)
3. Have native-image ignore non-JDK LoggerFinder implementations during build

### Detailed Analysis of JBoss LogManager Runtime Init Options

#### Option 1: Reset `manager` field on ALL `Logger` instances

**How it would work:** After `TrustStoreManagerFeature` runs, iterate all
`Logger` instances via `LogManager.loggerContext.namedLoggers` (a
`ConcurrentHashMap<String, LoggerWeakRef>`) and reset each `Logger.manager`
field to a JDK default `LogManager`.

**Feasibility:** Technically possible but fragile:
- `LogManager` has `systemContext` and `userContext`, each with `namedLoggers`
- `Logger.manager` is a volatile field, accessible via reflection
- Would also need to replace the `LogManager.manager` static field
- Risk: JDK code might have cached Logger references elsewhere (thread-locals,
  static fields in JDK classes like `XmlAttrStack`)
- The XML security class `XmlAttrStack.printStep()` embeds a `Logger` as a
  method constant — these constants are embedded in bytecode and can't be
  reset via reflection

**Conclusion:** Impractical — too many places hold `Logger` references.

#### Option 2: Prevent JBoss LogManager from being selected

**How `LogManager` selection works:**
1. `LogManager.<clinit>` calls `initLogManager()`
2. `initLogManager()` checks `System.getProperty("java.util.logging.manager")`
3. If set, it instantiates that class via `Class.newInstance()`
4. If not set (or fails), creates default `java.util.logging.LogManager`
5. The result is cached in `LogManager.manager` static field (final after init)

**At build time:** Quarkus's `QuarkusEntryPoint.main()` sets
`java.util.logging.manager=org.jboss.logmanager.LogManager`, but this
runs at RUNTIME, not during build. At build time, the property might
not be set. However, the jboss-logmanager jar has:
```
META-INF/services/java.util.logging.LogManager → org.jboss.logmanager.LogManager
```
This SPI registration is NOT used by `initLogManager()` — that method
only checks the system property. The SPI is for module-mode discovery.

**But:** The `LogManager` gets instantiated because `java.util.logging.LogManager`
class initialization is triggered by `TrustStoreManagerFeature` → trust store
loading → JDK internal logging. Since the system property IS set (by whom?),
the JBoss LogManager is selected.

**Investigation:** The system property `java.util.logging.manager` might be
set by the native-image launcher or by one of GraalVM's own features.
Preventing this property from being set during build would make the JDK
default LogManager be used instead.

**How to implement:** In the `ResetLoggerFinderFeature.afterRegistration()`,
BEFORE any other feature runs:
1. Save the current value of `java.util.logging.manager`
2. Clear it: `System.clearProperty("java.util.logging.manager")`
3. This would prevent `initLogManager()` from finding JBoss LogManager
4. Restore it in `afterAnalysis()`

**Problem:** `LogManager.<clinit>` might have already run before our Feature.
Feature ordering is not guaranteed.

**Tested via SPI file stripping** — see
[Option 2 Experiment](#option-2-experiment-stripping-spi-files-from-jars)
above. Build succeeds but runtime logging is broken because the JDK
default `LogManager` is baked into the image heap and cannot be replaced
at runtime.

#### Option 3: Exclude jboss-logmanager from libjvm.so

**Tested:** Excluded `jboss-logmanager-3.2.2.Final.jar` from both CP and
Preserve. Result: **still fails** with the same `LogManager` error.

**Why:** The build JVM's `java.util.logging.LogManager` class is initialized
once per JVM process. Even if the jar is not on our `-cp`, the native-image
launcher process may have already loaded it (from a previous analysis
iteration, or from the builder's own classpath). Once
`LogManager.manager = org.jboss.logmanager.LogManager`, it persists for the
entire JVM lifetime.

**The `Logger` instances** created during `TrustStoreManagerFeature` (loading
trust store certificates) hold references to the cached `LogManager` via
their `manager` field. These `Logger` instances are constants embedded in
JDK classes (e.g., `XmlAttrStack.printStep()`) and cannot be removed from
the image heap.

**Conclusion:** Simply excluding the jar from `-cp` is NOT sufficient.
The build JVM itself uses the JBoss LogManager, and its state leaks into
the image heap via cached `Logger` instances.

#### Summary: What We Know

1. **The root trigger** is `TrustStoreManagerFeature.afterRegistration()` which
   loads trust store certificates, triggering JDK internal logging
2. **Three SPI mechanisms** are involved:
   - `System.LoggerFinder` (via `META-INF/services/java.lang.System$LoggerFinder`)
     — in both `jboss-logmanager` and `jboss-modules`
   - `java.util.logging.LogManager` (via system property or `META-INF/services`)
     — in `jboss-logmanager`
3. **The `ResetLoggerFinderFeature`** successfully handles `System.LoggerFinder`
   but cannot fully handle `java.util.logging.LogManager` because:
   - `Logger` instances are embedded as constants in JDK class bytecode
   - These constants hold references to the `LogManager` via `Logger.manager`
   - Constants embedded in method bytecode cannot be reset via reflection
4. **Excluding the jar** doesn't help because the build JVM has already cached
   the `LogManager`
5. **Stripping SPI files** (Option 2 experiment) solves the build completely
   but bakes the JDK default `LogManager` into the image heap, preventing
   Quarkus from selecting JBoss LogManager at runtime. Quarkus works (HTTP
   responds) but produces no log output.

#### Most Promising Approach

The most effective fix would be at the GraalVM level:
- A `@RecomputeFieldValue(kind=Reset)` substitution on `Logger.manager` to
  null out the field in the image, forcing re-initialization at runtime
- Or a substitution on `LogManager.manager` to replace the JBoss instance
  with the JDK default in the image

This would need to be a GraalVM-level change (not JBoss-specific) that
resets the `LogManager` singleton and all `Logger.manager` fields to allow
re-initialization at runtime when the `java.util.logging.manager` system
property is set.

---

## Side Experiment: Logging and modules build time init

### Context

The `build-libjvm-logmanager-build-init.sh` script attempts a **minimal build-time
initialization** approach — unlike `build-libjvm.sh` which initializes entire packages
(`io.smallrye.common`, `io.quarkus`, `org.crac`, `org.jboss.logging`,
`org.jboss.logmanager`, `org.jboss.modules`, `org.apache.log4j`,
`org.apache.logging.log4j`, `org.slf4j`, `jakarta.json`), this script uses
`-H:-AssertInitializationSpecifiedForAllClasses` and only lists specific classes for
build-time init. With this flag, unspecified classes default to **run-time initialization**.

The script previously listed only four JBoss logmanager classes:
- `org.jboss.logmanager.JBossLoggerFinder`
- `org.jboss.logmanager.JBossLoggerFinder$JBossSystemLogger`
- `org.jboss.logmanager.Level`
- `org.jboss.logmanager.LogManager`

The build succeeded (`libjvm.so` was produced), but at runtime the Quarkus
process produced **no log output** — only:
```
Log4j API could not find a logging provider.
```
The `/hello` endpoint worked correctly, meaning the application was functional
but silent.

### Root Cause Analysis

During the native-image build, this warning appeared:
```
org.jboss.logmanager.JBossLoggerFinder getLogger
ERROR: The LogManager accessed before the "java.util.logging.manager" system property
was set to "org.jboss.logmanager.LogManager". Results may be unexpected.
```

**What happened at build time:**

1. `java.util.logging.LogManager` was initialized during the native-image build
   (triggered by `TrustStoreManagerFeature` → certificate loading → JDK internal logging).
2. At that point, the `java.util.logging.manager` system property was **not yet set**.
3. The JDK's `LogManager.initLogManager()` created the **default JDK LogManager** singleton.
4. Later, `JBossLoggerFinder.getLogger()` ran (build-time init of `JBossLoggerFinder`),
   set the property, and called `java.util.logging.Logger.getLogger()` — but the LogManager
   singleton was already the JDK default.
5. `tryJBossLogManager()` in `org.jboss.logging.LoggerProviders` checks
   `LogManager.getLogManager().getClass()` against `org.jboss.logmanager.LogManager` —
   they didn't match, so it fell through to Log4j 2.
6. Log4j 2 API jar was on the classpath but had no implementation, yielding
   "could not find a logging provider."
7. This broken state (`PROPERTY_SET = true`, wrong LogManager singleton,
   `LoggerProviders.PROVIDER` = failed Log4j 2) was **baked into the image heap**.

**At runtime:** The baked-in state meant JBoss LogManager was never used.
Quarkus logging was completely silent.

### Fix 1: `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager`

Added to the `native-image` command:
```bash
-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager
```

This sets the system property in the **native-image tool's JVM** before any class
initialization occurs. When `java.util.logging.LogManager.<clinit>` runs, it reads
the property and instantiates `org.jboss.logmanager.LogManager` instead of the JDK
default. The build-time warning disappeared, confirming proper initialization.

### Fix 2: Additional build-time initialized packages/classes

With the JBoss LogManager properly initializing at build time, several additional
types ended up in the image heap (created transitively during LogManager/LogContext
initialization). Since `-H:-AssertInitializationSpecifiedForAllClasses` defaults
unspecified classes to **run-time init**, each of these needed explicit build-time
registration.

The additions were made iteratively, each resolving one build failure:

#### Addition 1: `org.jboss.logging` (package)

**Error without it:**
```
An object of type 'org.jboss.logmanager.Logger' was found in the image heap.
This type, however, is marked for initialization at image run time by default.
```
The JBoss LogManager creates `org.jboss.logmanager.Logger` instances during
initialization (via `LogContext.getLogger()`). These loggers are referenced by
`JBossLoggerFinder$JBossSystemLogger.delegate` and baked into the image heap.

Adding `org.jboss.logmanager.Logger` alone resolved that error but immediately
hit `org.jboss.logmanager.Logger$1` (anonymous inner class in `Logger.getResourceBundle`).
Rather than chasing individual inner classes, the entire `org.jboss.logmanager`
package was already listed. The `org.jboss.logging` package was also needed because
`LoggerProviders.PROVIDER` (a static final set during `<clinit>`) discovers and
caches the JBoss LogManager provider — this cached provider object ends up in the
image heap.

Replaced the four individual logmanager classes with two package-level entries:
```
org.jboss.logging
org.jboss.logmanager
```

#### Addition 2: `io.quarkus.bootstrap.logging` (package)

**Error without it:**
```
An object of type 'io.quarkus.bootstrap.logging.InitialConfigurator' was found in the
image heap. This type, however, is marked for initialization at image run time by default.
```
`org.jboss.logmanager.LogContext` holds a reference to `InitialConfigurator` in its
`initializer` field. The LogContext is embedded as a constant in
`LogContext$1.getLogContext()`. `QuarkusDelayedHandler` (also in this package) is
referenced by `InitialConfigurator.getInitialHandlers()`.

Used the package prefix to cover both classes and any inner classes:
```
io.quarkus.bootstrap.logging
```

#### Addition 3: `io.quarkus.vertx.mdc.provider` (package)

**Error without it:**
```
An object of type 'io.quarkus.vertx.mdc.provider.LateBoundMDCProvider' was found in
the image heap. This type, however, is marked for initialization at image run time by default.
```
The `org.jboss.logmanager.MDC` class discovers the MDC provider via ServiceLoader.
`LateBoundMDCProvider` is found and cached in `MDC.getMDCProvider()`. Its inner class
`LateBoundMDCProvider$1` (a `ThreadLocal` subclass in field `threadLocalMap`) also
ended up in the heap.

Used the package prefix:
```
io.quarkus.vertx.mdc.provider
```

#### Addition 4: `io.smallrye.common.ref` (package)

**Error without it:**
```
An object of type 'io.smallrye.common.ref.PhantomReference' was found in the image heap.
This type, however, is marked for initialization at image run time by default.
```
`org.jboss.logmanager.LoggerNode` uses `io.smallrye.common.ref.PhantomReference` for
weak logger tracking (field `activeLoggers` is a `ConcurrentHashMap` containing phantom
references). These are created when loggers are instantiated during LogManager init.

```
io.smallrye.common.ref
```

#### Addition 5: `org.jboss.modules` (package)

**Error without it:**
```
An object of type 'org.jboss.modules.ModularContentHandlerFactory' was found in the
image heap. This type, however, is marked for initialization at image run time by default.
```
`org.jboss.logmanager.LogManager.<clinit>` triggers loading of `StandardOutputStreams`,
which (via transitive initialization) causes `org.jboss.modules` to register a
`ModularContentHandlerFactory` on `java.net.URLConnection.factory`. This static field
is scanned during image building and the factory object must be build-time initialized.

```
org.jboss.modules
```

### Final Build-Time Init List

```bash
INIT_BUILD=(
    "jdk.internal.jrtfs"
    "org.jboss.logging"                # Logger provider discovery
    "org.jboss.logmanager"             # LogManager, Logger, LogContext, etc.
    "io.quarkus.bootstrap.logging"     # InitialConfigurator, QuarkusDelayedHandler
    "io.quarkus.vertx.mdc.provider"    # LateBoundMDCProvider (MDC SPI)
    "io.smallrye.common.ref"           # PhantomReference (LoggerNode tracking)
    "org.jboss.modules"                # ModularContentHandlerFactory
)
```

Plus the native-image JVM flag:
```bash
-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager
```

### Result

**Build:** `libjvm.so` builds successfully (3m 35s, no errors or warnings about
LogManager).

**Runtime:** Quarkus starts with full logging output:
```
09:31:04,407 INFO  [io.quarkus] getting-started 1.0.0-SNAPSHOT native (powered by Quarkus 999-SNAPSHOT) started in 1.028s. Listening on: http://0.0.0.0:8080
09:31:04,407 INFO  [io.quarkus] Profile prod activated.
09:31:04,407 INFO  [io.quarkus] Installed features: [cdi, rest, smallrye-context-propagation, vertx]
```

`/hello` responds correctly. The `verify-quarkus.sh` script passes with `=== SUCCESS ===`.

### Comparison with Other Approaches

| Approach | Build | Runtime Logging | Extra Build-Time Classes |
|----------|-------|----------------|-------------------------|
| `build-libjvm.sh` (full package init) | ✅ | ✅ | 10 packages (broad) |
| SPI file stripping (`build-libjvm-minimal-build-init.sh`) | ✅ | ❌ Silent | ~4 classes only |
| **This fix** (`build-libjvm-logmanager-build-init.sh`) | ✅ | ✅ | 7 packages (targeted) |

This approach sits between the full-init and minimal-init scripts: it initializes
only the packages/classes needed for the logging chain, without pulling in unrelated
packages like `io.smallrye.common` (broad), `io.quarkus` (broad), `org.crac`,
`org.apache.log4j`, `org.apache.logging.log4j`, `org.slf4j`, or `jakarta.json`.

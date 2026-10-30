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

### Current Blocker: Split Package Filtering in NativeImageClassLoader

Native-image's `NativeImageClassLoader` **silently drops jars** that create
split packages. The `smallrye-mutiny-vertx-auth-*` optional jars share the
`io.vertx.mutiny.ext.auth` package hierarchy with
`smallrye-mutiny-vertx-auth-common` from `lib/main/`.

When both are on the classpath, native-image only loads the main jar and
ignores the optional ones. This causes:
```
java.lang.NoClassDefFoundError: io/vertx/mutiny/ext/auth/htdigest/HtdigestAuth
```

The class IS in the optional jar, the jar IS on the `-cp` argument, but the
`NativeImageClassLoader` silently excludes it.

**Evidence:** The native-image error report lists 49 of 51 optional jars on
the classpath — the 10 `smallrye-mutiny-vertx-auth-*` and
`smallrye-mutiny-vertx-sql-client` jars are absent.

**Workarounds attempted:**
- Merging jars: failed (same ClassNotFoundException even after merge)
- Wildcard classpath (`lib/optional/*`): failed (same filtering)
- Explicit jar paths: failed (same filtering)

**This is a native-image limitation, not a configuration issue.**

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
| All boot + all main jars | ❌ build fails | 119 (attempted) |

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

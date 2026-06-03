# Progress

## 2026-06-02

### Goal

Build a Quarkus native image application using layered native images:
- **Base layer:** `java.base` module + `jdk.localedata` module + `io.netty.*` packages
- **App layer:** Remaining Quarkus, Vert.x, SmallRye, and user code

### Non-layered build

The non-layered build (`build-non-layered.sh`) was already working. The `getting-started`
Quarkus application builds and runs successfully, responding to `curl http://localhost:8080/hello`
with "hello" and HTTP 200.

### Base layer build errors

#### Error 1: `ProcessHandleImpl$Info` in image heap

**Error:** `io.smallrye.common.os.Process.currentInfo` holds a `ProcessHandleImpl$Info` object.

**Fix:** Added `--initialize-at-run-time=io.smallrye.common.os.Process` to `build-layer-base.sh`.
This was already known from the `../quarkus-base` experiment.

#### Error 2: `NetworkInterface` in image heap

**Error:** `io.netty.util.NetUtil.LOOPBACK_IF` holds a `NetworkInterface` object.

**Fix:** Added `--initialize-at-run-time=io.netty.util.NetUtil` to `build-layer-base.sh`.

#### Error 3: `ImageReader` in image heap (jdk.tools.jlink)

**Error:** `jdk.tools.jlink.internal.plugins.OrderResourcesPlugin.JRT_FILE_SYSTEM` holds a JRT
filesystem reference that embeds an `ImageReader`.

**Fix:** Added `--initialize-at-run-time=jdk.tools.jlink.internal.plugins` to `build-layer-base.sh`.

#### Error 4: `SecureRandom` in image heap (java.rmi)

**Error:** `java.rmi.server.ObjID.secureRandom` holds a `SecureRandom` instance.

**Fix:** Added `--initialize-at-run-time=java.rmi` and `--initialize-at-run-time=sun.rmi`.

#### Error 5: `ClassCastException` in `CrossLayerConstantRegistryFeature`

**Error:** During image creation phase:
```
java.lang.ClassCastException: class java.lang.Package cannot be cast to class
com.sun.org.apache.xml.internal.serializer.CharInfo$CharKey
```

**Root cause:** `isConstantRegistered()` uses `ConcurrentHashMap.containsValue()` which calls
`equals()` on all values. `CharInfo$CharKey.equals()` casts without `instanceof` check, causing
`ClassCastException` when called with a `Package` object.

**Fix:** Modified `CrossLayerConstantRegistryFeature.isConstantRegistered()` in Mandrel to use
identity comparison (`==`) instead of `containsValue()`:
```java
for (Object value : constantCandidates.values()) {
    if (value == obj) return true;
}
return false;
```

Committed as: `Fix cross-layer issues for layered native image builds` in Mandrel.

#### Comprehensive runtime-init classes

Added all runtime-init classes from the non-layered build's class initialization report to
`build-layer-base.sh`. This includes classes from:
- **Netty:** buffer, codec, handler, resolver, transport, util packages (38 classes)
- **Quarkus:** EmptyByteBufStub, ExecutorRecorder, InetRunTime, etc.
- **Vert.x:** PartialPooledByteBufAllocator, VertxByteBufAllocator, ClusteredEventBus, etc.
- **SmallRye:** Process, HostName, References$ReaperThread
- **JBoss:** ConsoleHandler$ConsoleHolder, SyslogHandler, JDKSpecific$ThreadAccess
- **JDK:** java.rmi, sun.rmi, jdk.tools.jlink, jdk.jpackage

### App layer build errors

#### Error 1: `jdk.jpackage.internal.LinuxPackageArch$DebPackageArch` init failure

**Error:** Class initialization fails because `dpkg` command is not available.

**Fix:** Added `--initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch$DebPackageArch`
and `$RpmPackageArch` to `build-layer-app.sh`.

#### Error 2: Class initialization not stable between layers

**Error:** Multiple netty classes had `buildTimeInit=false` in the base layer but
`buildTimeInit=true` in the app layer (due to netty's `native-image.properties` having
`--initialize-at-build-time=io.netty`).

**Fix:** Added ALL runtime-init classes from the base layer to the app layer script, matching
each class individually. Also added non-netty runtime-init classes (io.quarkus, io.vertx,
io.smallrye, org.jboss, java.rmi, etc.) to ensure stability.

#### Error 3: `Newly seen platform package sun.util.resources.cldr.ext`

**Error:** App layer uses `-H:IncludeLocales=en-GB` which pulls CLDR locale classes from
`jdk.localedata` module, but the base layer only had `module=java.base`.

**Fix:** Added `module=jdk.localedata` to the base layer's `-H:LayerCreate` option:
`-H:LayerCreate=libnettybaselayer.nil,module=java.base,module=jdk.localedata,package=io.netty.*`

Required rebuilding the base layer.

### Runtime error: `WrongMethodTypeException` in `ConfigMappingLoader`

**Error:** After both layers built successfully, the native binary crashed at startup:
```
WrongMethodTypeException: handle's method type (ConfigMappingContext)Object
but found (ConfigMappingContext)Object
  at java.lang.invoke.Invokers.checkExactType(Invokers.java:531)
  at io.smallrye.config.ConfigMappingLoader.configMappingObject(ConfigMappingLoader.java:114)
```

**Root cause:** In layered native images, `MethodType` reference equality across layers is not
guaranteed. The `invokeExact()` call in `configMappingObject()` is compiled in the base layer
with a specific `MethodType` constant embedded in the code. At runtime, the MethodHandle created
by `constructor()` has a MethodType from the interning table, but the interning table doesn't
contain the base layer's MethodType constants (the base layer registered 0 MethodType objects
with the `MethodHandleFeature`). The `checkExactType` method uses reference comparison (`!=`)
which fails because the two MethodType objects are logically equal but physically different.

**Investigation:**
- Added tracing to `MethodHandleFeature.registerHeapMethodType()` (gated behind
  `-J-Dsvm.traceMethodTypeInterning=true`)
- Base layer registered 0 MethodType objects
- App layer registered 2 MethodType objects for ConfigMappingContext
- The mismatch causes the `WrongMethodTypeException` at runtime

**Attempted fixes:**
1. `--initialize-at-run-time=io.smallrye.config.ConfigMappingLoader` in base layer → didn't help
   because the METHOD compilation (not initialization) happens in the base layer
2. Restricting base layer classpath to only netty jars → introduced AWT initialization errors
3. Seeding app layer intern table with base layer MethodType objects → caused builder-internal
   type errors

**Working fix:** Added a Quarkus substitution for `ConfigMappingLoader.configMappingObject()` that
uses `invoke()` instead of `invokeExact()`. The `invoke()` method is more lenient and doesn't
require exact MethodType reference equality.

Committed as: `Substitute ConfigMappingLoader.configMappingObject to use invoke() instead of
invokeExact()` in Quarkus.

### Final result

Both layers build successfully and the layered native image runs correctly:

```
$ LD_LIBRARY_PATH=target target/getting-started-1.0.0-SNAPSHOT-runner
getting-started 1.0.0-SNAPSHOT native (powered by Quarkus 999-SNAPSHOT) started in 0.035s.
Listening on: http://0.0.0.0:8080

$ curl http://localhost:8080/hello
hello  (HTTP 200)
```

**Build artifacts:**
- Base layer: `libnettybaselayer.so` (274.88 MiB), `libnettybaselayer.nil` (1.32 GiB), build time ~1m 40s
- App layer: `getting-started-1.0.0-SNAPSHOT-runner` (16.25 MiB), build time ~32s

### Summary of changes

**Mandrel** (1 commit):
- `CrossLayerConstantRegistryFeature`: Fixed `ClassCastException` in `isConstantRegistered()`
- `MethodHandleFeature`: Added optional MethodType interning trace logging

**Quarkus** (1 commit):
- `Substitutions.java`: Added substitution for `ConfigMappingLoader.configMappingObject()` using
  `invoke()` instead of `invokeExact()` + alias for `ConfigMappingImplementation`

**Mendrugo** (1 commit):
- `build-layer-base.sh`: Full set of `--initialize-at-run-time` flags, `jdk.localedata` module
- `build-layer-app.sh`: Matching `--initialize-at-run-time` flags + `ApplicationLayerInitializedClasses`

## 2026-06-03

### Attempted: Restrict base layer classpath to netty-only jars

The full classpath (`lib/*`) includes all Quarkus, Vert.x, SmallRye jars which
causes non-netty classes like `ConfigMappingLoader` to be compiled in the base
layer. Investigated restricting the classpath to only netty jars to avoid this.

**Attempt 1: Netty jars + brotli4j only**

Failed with cascading AWT initialization errors (`sun.awt.X11.XWM`,
`sun.java2d.SurfaceData`, `java.awt.font.FontRenderContext`, etc.). These come
from JDK modules (java.desktop) that get pulled in during analysis even though
they're not on the classpath. Adding `--initialize-at-run-time` for AWT packages
requires many entries and cascades into more image heap issues.

**Attempt 2: Netty jars + Quarkus netty runtime jar**

Failed because the `io.quarkus.quarkus-netty` jar contains substitutions that
reference `quarkus-core` classes and other Quarkus packages. Adding
`quarkus-core` to the classpath triggers `MissingType` `ClassCastException`
because `quarkus-core` itself depends on many other jars.

**Conclusion: Netty-only classpath is not feasible**

The Quarkus ecosystem has deep interdependencies between its jars. The netty
substitutions in `io.quarkus.quarkus-netty` are essential for correct netty
class initialization (e.g., `EmptyByteBuf.EMPTY_BYTE_BUFFER`), and they depend
transitively on many other Quarkus jars. The full classpath is required.

The Quarkus substitution fix for `ConfigMappingLoader.configMappingObject()` (using
`invoke()` instead of `invokeExact()`) remains necessary as the workaround for the
cross-layer MethodType reference inequality issue.

### Successful: Restricted classpath with quarkus-core and quarkus-netty

Adding just the essential Quarkus jars to the base layer classpath (instead of all
`lib/*`) works. The required jars beyond netty are:

- `io.quarkus.quarkus-core` — AWT substitutions, logging substitutions, config substitutions
- `io.quarkus.quarkus-netty` — netty-specific substitutions (EmptyByteBuf, SSL, etc.)
- `io.quarkus.quarkus-bootstrap-runner` — InitialConfigurator (referenced by core substitutions)
- `io.quarkus.quarkus-classloader-commons` — classloader utilities
- `io.smallrye.common.smallrye-common-*` — constraint, ref, net, etc. (transitive deps)
- `io.smallrye.config.smallrye-config-{core,common}` — ConfigurationSubstitutions target
- `org.jboss.logmanager.jboss-logmanager` — LoggingSubstitutions target
- `org.jboss.logging.jboss-logging` — logging API
- `org.jboss.threads.jboss-threads` — thread utilities
- `org.slf4j.slf4j-api` — LoggingSubstitutions target
- `org.wildfly.common.wildfly-common` — CidrAddress/Inet substitutions
- `org.eclipse.microprofile.config.microprofile-config-api` — SmallRyeConfigProviderResolver superclass

This reduces the base layer classpath from ~80 jars to ~40 jars. The non-netty
runtime-init flags for vertx, quarkus.runtime.ExecutorRecorder, and
quarkus.runtime.configuration.RuntimeConfigBuilder are no longer needed since
those classes aren't on the restricted classpath.

**However**, `smallrye-config-core` (which contains `ConfigMappingLoader`) is
still required because `quarkus-core`'s `ConfigurationSubstitutions` targets
`SmallRyeConfigProviderResolver`. This means the `WrongMethodTypeException`
from `invokeExact` still occurs, and the Quarkus substitution fix remains
necessary.

The restricted classpath was applied to `build-layer-base.sh`.

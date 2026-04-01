# Layered Quarkus App Build Progress

## Goal
Build and run a layered Quarkus native application. The non-layered app and base layer already build successfully. Now working on the app layer (`build-layer-app.sh`).

## Errors and Resolutions

(tracking starts below)

### Error 1: NULL_CONSTANT in relinked field ConsoleHandler$ConsoleHolder.console

**Error:**
```
com.oracle.svm.shared.util.VMError$HostedError: Found NULL_CONSTANT when reading the hosted value of relinked field AnalysisField<ConsoleHandler$ConsoleHolder.console -> ... PrintWriter:120>. Since relinked fields should have a concrete non-null value there may be a class initialization mismatch.
```

**Cause:** `org.jboss.logmanager.handlers.ConsoleHandler$ConsoleHolder` was initialized at build time in the base layer, capturing a null `console` field. The app layer then fails when trying to relink it.

**Fix:** Add `--initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder` to `build-layer-base.sh` so the class is deferred to runtime and not captured in the base layer.

**Status:** RESOLVED - rebuilt base layer with the fix, Error 1 no longer occurs.

### Error 2: Found unexpected snapshot value for base layer value (java.util.logging.Level)

**Error:**
```
Error: Found unexpected snapshot value for base layer value.
Existing value: ImageHeapConstant<java.util.logging.Level, reachable: false, reader installed: true, compressed: false, backed: true, id: 24688>.
New value: ImageHeapConstant<java.util.logging.Level, reachable: false, reader installed: true, compressed: false, backed: true, id: 24729>.
Hosted value: Object[Level@748246100].
Reason: Persisted in a previous layer..
Internal exception: com.oracle.graal.pointsto.util.AnalysisError
```

**Cause:** `java.util.logging.Level` instances are created at build time in both layers, but as different object instances. The base layer persists one set, the app layer creates new ones, causing ID conflicts during relinking. Likely triggered by `org.jboss.logmanager.Level` which extends `java.util.logging.Level`.

**Workaround attempted:** Add `--initialize-at-run-time=org.jboss.logmanager.Level` to `build-layer-base.sh` to defer Level creation to runtime.

**Workaround 1 (FAILED):** Adding `--initialize-at-run-time=org.jboss.logmanager.Level` to base layer caused "object in image heap" error since Level instances are already referenced by other build-time classes. Reverted.

**Resolution:** The Error 2 turned out to be resolved by rebuilding the base layer with the ConsoleHandler$ConsoleHolder fix from Error 1. The Level conflict was a side effect of the original base layer.

**Status:** RESOLVED

### Error 3: Class initialization info not stable between layers (AbstractPlugin)

**Error:**
```
com.oracle.svm.shared.util.VMError$HostedError: Class initialization info not stable between layers for type AnalysisType<AbstractPlugin -> HotSpotType<Ljdk/tools/jlink/internal/plugins/AbstractPlugin;>>.
Previous info: ClassInitializationInfo {initState = Linked, buildTimeInit = false}
New info: ClassInitializationInfo {initState = FullyInitialized, buildTimeInit = true}
```

**Cause:** The base layer marks `jdk.tools.jlink.internal.plugins` as runtime-init, but the app layer doesn't include this flag, so the JVM eagerly initializes it at build time, creating a mismatch.

**Fix:** Add `--initialize-at-run-time=jdk.tools.jlink.internal.plugins` to `build-layer-app.sh`.

**Status:** RESOLVED — added all base layer `--initialize-at-run-time` flags to app layer to keep init consistency.

### Error 4: jakarta.el.ELManager fails to initialize (ClassNotFoundException)

**Error:**
```
Class initialization of jakarta.el.ELManager failed.
jakarta.el.ELException: Provider com.sun.el.ExpressionFactoryImpl not found
Caused by: java.lang.ClassNotFoundException: com.sun.el.ExpressionFactoryImpl
```

**Cause:** The base layer defers `jakarta.el.ELManager` to runtime, but the app layer didn't include this flag. The app layer tried to initialize it at build time but the EL implementation isn't on the filtered classpath.

**Fix:** Added ALL `--initialize-at-run-time` flags from base layer to app layer to ensure consistency across layers.

**Status:** RESOLVED

### Error 5: io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator fails at build time

**Error:**
```
Class initialization of io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator failed.
Caused by: java.lang.NoClassDefFoundError: org/eclipse/jetty/npn/NextProtoNego$Provider
```

**Cause:** The class references Jetty NPN provider which isn't on the classpath. Needs to be deferred to runtime.

**Fix:** Added `--initialize-at-run-time=io.netty.handler.ssl.JdkNpnApplicationProtocolNegotiator` to both base and app layer scripts.

**Status:** RESOLVED (added to both layers)

### Error 6: java.util.logging.Level snapshot mismatch (recurrence after base layer rebuild)

**Error:**
```
Error: Found unexpected snapshot value for base layer value.
Existing value: ImageHeapConstant<java.util.logging.Level, ...id: 38406>.
New value: ImageHeapConstant<java.util.logging.Level, ...id: 38439>.
Reason: Persisted in a previous layer.
```

**Cause:** The base layer does NOT use `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager` but the app layer does. This means the base layer initializes `java.util.logging.Level` with the default JDK log manager (creating standard Level instances), while the app layer uses JBoss LogManager (which creates different Level instances). The persisted Level objects from the base layer don't match.

**Fix:** Add `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager` to `build-layer-base.sh` so both layers use the same log manager, creating identical Level instances.

**Status:** RESOLVED — Level mismatch fixed by using same log manager in both layers.

### Error 7: NoClassDefFoundError com/fasterxml/jackson/databind/JsonSerializer (CURRENT)

**Error:**
```
com.oracle.graal.pointsto.util.ParallelExecutionException: com/fasterxml/jackson/databind/JsonSerializer
Caused by: java.lang.ClassNotFoundException: com.fasterxml.jackson.databind.JsonSerializer
```

**Cause:** The app layer's classpath (filtered JAR + extracted-classes.jar) doesn't include Jackson databind classes. The app layer tries to register a class that extends `JsonSerializer` for reflection, but the Jackson library is only in the base layer's classpath (the `lib/*` jars).

**Potential fixes to investigate:**
1. Add the Jackson databind JAR to the app layer's `-cp` alongside the extracted/filtered JARs
2. Extract the needed Jackson class(es) into `extracted-classes.jar` via `split-jar.sh`
3. Add the full `lib/*` to the app layer classpath (may cause duplication issues)

**Investigation:**
The classes aren't in any reflect-config.json or the Feature class. They come from the base layer's `.nil` file replaying reflection registrations during `initializeBaseLayerType`. The app layer tries to link these types, which requires resolving their supertypes.

Specific culprit classes:
- `io.quarkus.vertx.runtime.jackson.JsonObjectSerializer` extends `com.fasterxml.jackson.databind.JsonSerializer` (jackson-databind not on classpath)
- `io.netty.util.internal.logging.Log4J2Logger` references `org.apache.logging.log4j.message.Message` (log4j not on classpath)
- `io.vertx.core.logging.Log4j2LogDelegate` references `org.apache.logging.log4j.message.Message`
- `org.jboss.logging.Log4j2Logger` references `org.apache.logging.log4j.message.Message`
- `io.vertx.core.json.jackson.DatabindCodec` references `com.fasterxml.jackson.databind.ObjectMapper`

These classes are in `quarkus-vertx`, `vertx-core`, `netty-common`, and `jboss-logging` JARs (all in `lib/*`).
The base layer succeeds because `lib/*` is on its classpath. The app layer fails because its classpath only has the filtered/extracted JARs.

**Potential fix:** Add `lib/*` to the app layer's `-cp` so it can resolve all supertypes needed for linking base layer types.

**Fix applied:** Added `lib/*` to app layer `-cp`. Jackson/log4j errors resolved.

**Status:** RESOLVED

### Error 8: NoClassDefFoundError io/netty/internal/tcnative/SSLPrivateKeyMethod

**Error:**
```
NoClassDefFoundError: io/netty/internal/tcnative/SSLPrivateKeyMethod
```

**Cause:** With `--link-at-build-time`, all classes in `lib/*` are eagerly linked, including `netty-handler` classes that reference optional dependency `netty-tcnative`. The tcnative library isn't on the classpath.

**Workaround attempted:** Removing `--link-at-build-time` — this fixed the tcnative issue but caused cascading failures:
1. `ApplicationImpl` class init mismatch (base layer had it as Linked, app layer eagerly initialized it) — fixed with `--initialize-at-run-time=io.quarkus.runner.ApplicationImpl`
2. `Newly seen platform package sun.util.resources.cldr.ext` — JDK locale classes not in base layer become reachable without link-at-build-time

**Conclusion:** Removing `--link-at-build-time` creates too many cascading issues. Better to keep it and handle tcnative specifically.

**Status:** RESOLVED — explored two approaches below.

## Two Approaches to Error 8

### Approach A: Keep `--link-at-build-time` (build-layer-app.link-at-build-time.sh)

With `--link-at-build-time` and `lib/*` on the classpath, all classes get eagerly linked, including those that reference optional dependencies not on the classpath. Each missing optional dep requires `--initialize-at-run-time` on the class that references it, to prevent linking.

**Fixes applied:**
- `--initialize-at-run-time=io.netty.handler.ssl` (whole package) — prevents linking of classes referencing tcnative, Jetty NPN, Conscrypt
- `--initialize-at-run-time=io.netty.util.internal.logging.Log4J2Logger` — references log4j2
- `--initialize-at-run-time=io.netty.util.internal.logging.Log4JLogger` — references log4j1
- `--initialize-at-run-time=io.vertx.core.logging.Log4j2LogDelegate` — references log4j2
- `--initialize-at-run-time=io.vertx.core.logging.Log4jLogDelegate` — references log4j1
- `--initialize-at-run-time=org.jboss.logging.Log4j2Logger` — references log4j2
- `--initialize-at-run-time=org.jboss.logging.Log4jLogger` — references log4j1
- `--initialize-at-run-time=io.vertx.core.json.jackson.DatabindCodec` — references jackson-databind
- `--initialize-at-run-time=io.quarkus.vertx.runtime.jackson` — references jackson-databind

**Remaining errors:** `NoClassDefFoundError: net/jpountz/xxhash/XXHashFactory` (lz4-java optional dep). More classes with missing optional deps likely remain — this is a whack-a-mole pattern.

**Status:** IN PROGRESS — builds fail, more `--initialize-at-run-time` entries needed.

### Approach B: Remove `--link-at-build-time` (build-layer-app.link-at-run-time.sh)

Without `--link-at-build-time`, classes are linked lazily, avoiding the missing optional dep errors entirely.

**Fixes applied:**
- Removed `--link-at-build-time`
- Removed `-H:IncludeLocales=en-GB` — this pulled in `jdk.localedata` module which wasn't in the base layer's `LayerCreate` scope (`module=java.base`), causing `Newly seen platform package sun.util.resources.cldr.ext`. The `--initialize-at-run-time` workaround does NOT help for this error since it fires at type reachability, not initialization.

**Build result:** SUCCESS — 27.9s, 8.82MiB executable.

**Runtime result:** Crashes with `NoClassDefFoundError: io.quarkus.runtime.generated.Config` at `ApplicationImpl.<clinit>`. This is likely a split-jar issue — the `Config` class needs to be included in extracted-classes.jar or the filtered JAR.



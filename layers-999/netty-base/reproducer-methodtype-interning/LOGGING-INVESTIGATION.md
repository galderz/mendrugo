# Logging Investigation: Silent Startup Failures in Layered Native Images

## Context

When the `WrongMethodTypeException` occurs at runtime in a layered native image
build, the error may or may not be visible depending on the build environment.
Two bugs were identified during investigation.

## Bug 1: `LoggerProviders.PROVIDER` resolves to wrong backend

### Background

`org.jboss.logging.LoggerProviders` has a static final field `PROVIDER` that
determines the logging backend. In Quarkus, this should be
`JBossLogManagerProvider` (which delegates to JBoss LogManager).

The selection logic in `LoggerProviders.findProvider()` is:

1. Check system property `org.jboss.logging.provider`
2. Try `ServiceLoader<LoggerProvider>`
3. Try `tryJBossLogManager()` — checks if `LogManager.getLogManager().getClass()` is
   `org.jboss.logmanager.LogManager` (reference equality `==`)
4. Fall through to log4j2, logback, log4j, JDK

Step 3 requires `-Djava.util.logging.manager=org.jboss.logmanager.LogManager` to be
set BEFORE `LogManager.getLogManager()` is first called, because `LogManager` is a
singleton initialized on first access.

### The Problem

`LoggerProviders` is initialized at build time in the **base layer** (because of
`--initialize-at-build-time=""`). The base layer's `build-layer-base.sh` does NOT
set `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager`. Only the app
layer's `build-layer-app.sh` sets it.

Therefore, when `LoggerProviders.<clinit>` runs during the base layer build:

1. `LogManager.getLogManager()` returns `java.util.logging.LogManager` (not JBoss)
2. `tryJBossLogManager()` fails the class identity check → throws `IllegalStateException`
3. `findProvider()` catches it and falls through
4. Eventually reaches `tryJDK()` → `PROVIDER = JDKLoggerProvider`
5. This is persisted in the base layer's image heap

At runtime, all `org.jboss.logging.Logger` calls go through `JDKLoggerProvider`,
which delegates to `java.util.logging` (JUL). JUL has a built-in `ConsoleHandler`
that writes to stderr, so error messages ARE visible.

### Why the user's environment behaves differently

In the user's environment (building outside the container), `LoggerProviders` may
be initialized in the **app layer** instead (different Mandrel version, different
class init ordering). The app layer DOES set
`-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager`, so:

1. `LogManager.getLogManager()` returns `org.jboss.logmanager.LogManager`
2. `tryJBossLogManager()` succeeds → `PROVIDER = JBossLogManagerProvider`
3. At runtime, logging goes through JBoss LogManager → `QuarkusDelayedHandler`

This leads to Bug 2.

### Fix

Add `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager` to
`build-layer-base.sh`. This ensures `LoggerProviders.PROVIDER` correctly resolves
to `JBossLogManagerProvider` regardless of which layer initializes it.

**However**, this fix alone does NOT solve the silent exit problem — it actually
makes it worse (see Bug 2). Both bugs need to be addressed together.

## Bug 2: `QuarkusDelayedHandler` never flushes when app crashes early

### Background

When `JBossLogManagerProvider` is the active backend, the logging flow is:

```
org.jboss.logging.Logger.error(msg)
  → JBossLogManagerProvider
    → JBoss LogManager (LogContext)
      → LoggerNode.publish()
        → LoggerNode.getHandlers()
          → InitialConfigurator.getInitialHandlers("")
            → returns { DELAYED_HANDLER }
```

`InitialConfigurator.DELAYED_HANDLER` is a `QuarkusDelayedHandler` — a queuing
handler that stores log records until real handlers (like `ConsoleHandler`) are
installed.

The `QuarkusDelayedHandler` is activated (and queued messages flushed) when
`setHandlers()` or `addHandler()` is called. This happens during the
**RUNTIME_INIT** phase in `LoggingSetupRecorder.initializeLogging()` (called from
`LoggingResourceProcessor`). The recorder:

1. Creates a `ConsoleHandler` based on runtime config
2. Calls `InitialConfigurator.DELAYED_HANDLER.setHandlers(handlers)` (line ~293)
3. This triggers `activate()` which drains the queue to the newly installed handlers

### The Problem

The `WrongMethodTypeException` occurs during `Config.runtimeConfig()` which builds
`SmallRyeConfig`. This happens during the **RUNTIME_INIT** phase, but BEFORE the
logging recorder runs. The startup order is approximately:

```
ApplicationImpl.doStart()
  → STATIC_INIT steps (Arc, context propagation, etc.)
  → RUNTIME_INIT steps:
      1. Config.runtimeConfig()          ← CRASHES HERE (WrongMethodTypeException)
      2. ... other recorders ...
      3. LoggingSetupRecorder.initializeLogging()  ← NEVER REACHED
```

When the exception occurs:
1. `ApplicationLifecycleManager` catches it
2. `applicationLogger.errorv(t, "Failed to start application")` is called
3. JBoss Logging → JBoss LogManager → `DELAYED_HANDLER.doPublish(record)`
4. `DELAYED_HANDLER` is not activated (no handlers installed) → record queued
5. `ensureConsoleLogsDrained()` looks for an `AsyncHandler` with `ConsoleHandler` → finds none
6. `System.exit(1)` → process exits, queued records are lost

### The `close()` method exists but is never called

`QuarkusDelayedHandler.close()` has a fallback that drains to stderr:

```java
public final void close() {
    if (!logRecords.isEmpty()) {
        StandardOutputStreams.printError("The DelayedHandler was closed before...");
        while ((record = logRecords.pollFirst()) != null) {
            StandardOutputStreams.printError(formatter.format(record));
        }
    }
}
```

But `System.exit(1)` is called before `close()`, so this fallback never triggers.
The shutdown hook (`ShutdownHookThread`) is removed before `System.exit()` at line
232 of `ApplicationLifecycleManager`.

### Should the console handler be installed earlier?

Yes. The core issue is that the `ConsoleHandler` installation depends on runtime
config (`LoggingSetupRecorder` reads `quarkus.log.console.*` properties), but
runtime config initialization is what fails. This creates a chicken-and-egg problem:
logging setup needs config, but config failures need logging.

Possible approaches:

1. **Install a temporary ConsoleHandler before config initialization** — Add a
   bootstrap `ConsoleHandler` to `DELAYED_HANDLER` at the very start of
   `ApplicationImpl.doStart()`, before any RUNTIME_INIT steps. Replace it with the
   properly configured one when `LoggingSetupRecorder` runs. This ensures any crash
   during RUNTIME_INIT produces visible output.

2. **Call `DELAYED_HANDLER.close()` on startup failure** — In
   `ApplicationLifecycleManager`'s catch block, call
   `InitialConfigurator.DELAYED_HANDLER.close()` before `System.exit()`. The
   `close()` method already has the stderr fallback logic.

3. **Direct stderr fallback (current workaround)** — Add
   `t.printStackTrace(System.err)` in `ApplicationLifecycleManager`'s catch block.
   This bypasses all logging frameworks entirely. Simple and reliable but results
   in duplicate output when logging IS working (the JDKLoggerProvider case).

### Recommended fix

Option 2 is the cleanest — it uses the existing `QuarkusDelayedHandler.close()`
fallback which was designed for exactly this scenario ("closed before any children
handlers were configured"). The fix would be in `ApplicationLifecycleManager`:

```java
} else {
    applicationLogger.errorv(t, "Failed to start application");
    ensureConsoleLogsDrained();
    // Ensure queued messages are flushed if the logging system
    // was not fully initialized before the failure
    InitialConfigurator.DELAYED_HANDLER.close();
}
```

## Current state of fixes

### In Quarkus (on top of d8cb9aed824):

1. **`c23529fc907`** — `t.printStackTrace(System.err)` fallback in
   `ApplicationLifecycleManager` (option 3 above — works but may produce
   duplicate output)

2. **`fc383d6f35b`** — `invoke()` substitution for `ConfigMappingLoader`
   (workaround for the MethodType interning bug)

### Quarkus git log:

```
fc383d6f35b Substitute ConfigMappingLoader.configMappingObject to use invoke() instead of invokeExact()
c23529fc907 Always print startup failures to stderr as fallback
d8cb9aed824 Store transformed bytecode in modified dependency JARs instead of runner JAR
```

### In Mandrel:

1. **`CrossLayerConstantRegistryFeature`** — Fixed `ClassCastException` in
   `isConstantRegistered()` (identity comparison instead of `containsValue()`)

2. **`MethodHandleFeature`** — Added optional MethodType interning tracing
   (gated behind `-J-Dsvm.traceMethodTypeInterning=true`)

### In build scripts:

- `build-layer-base.sh` — Missing `-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager`
  (Bug 1 — not yet fixed, as fixing it without addressing Bug 2 would make
  the silent exit problem universal)

## Summary of issues

| # | Issue | Location | Status |
|---|-------|----------|--------|
| 1 | `LoggerProviders.PROVIDER` resolves to wrong backend | `build-layer-base.sh` missing `-J-D` flag | Identified, not fixed |
| 2 | `QuarkusDelayedHandler` never flushes on early crash | `ApplicationLifecycleManager` / `QuarkusDelayedHandler` | Workaround applied (`System.err` fallback) |
| 3 | `MethodType` interning across layers | GraalVM `MethodHandleFeature` | Workaround applied (`invoke()` substitution) |
| 4 | `CharInfo$CharKey.equals()` ClassCastException | GraalVM `CrossLayerConstantRegistryFeature` | Fixed in Mandrel |

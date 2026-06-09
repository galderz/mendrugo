# Logging Investigation Follow-up

## Where the exception is swallowed

**Location:** `org.jboss.logmanager.LoggerNode.publish()` (jboss-logmanager-3.2.2.Final)

**Mechanism:**

```
LoggerNode.publish()
  └→ handler.publish(record)        ← ConsoleHandler.publish()
       └→ formatter.format(record)  ← SimpleFormatter.format() 
            └→ String.format(locale, "%tc", date)
                 └→ DateFormatSymbols → FormatData_en_GB → THROWS UnsupportedFeatureError
```

`LoggerNode.publish()` has this exception table:
```
from=27 to=105 target=108 type=VirtualMachineError  → re-throws
from=27 to=105 target=113 type=Throwable            → CATCHES AND HANDLES
```

At target=113:
1. Catches `Throwable` (including `UnsupportedFeatureError` which extends `Error`)
2. Wraps non-`Exception` in `UndeclaredThrowableException` with **empty stack trace**
3. Calls `ErrorManager.error("Handler publication threw an exception", wrapped, 1)`
4. `ErrorManager.error()` only prints the **first** error (has a `reported` boolean flag)
5. Continues the handler loop — the original error message is lost

**Result:** The startup failure exception is completely invisible because:
- The `ErrorManager` may have already been triggered by an earlier logging failure
- Even if it prints, the message is generic ("Handler publication threw an exception")
  with an empty stack trace — no information about the actual startup error
- The actual startup error (`WrongMethodTypeException`) is never printed

## Why it only affects the external environment

The `SimpleFormatter` formats dates using `Locale.getDefault()`:

| Environment | `Locale.getDefault()` | `user.country` | Date formatting |
|---|---|---|---|
| Container (Fedora 44, `LANG=C.UTF-8`) | `en` | `null` | ✅ works (uses `FormatData` base bundle) |
| External (likely `LANG=en_GB.UTF-8`) | `en_GB` | `GB` | ❌ needs `FormatData_en_GB` CLDR bundle |

The `user.country=GB` is baked into the native image by `-J-Duser.country=GB` in
the app layer build. In the container, the base layer initializes `Locale.getDefault()`
first with `country=null` (from `LANG=C.UTF-8`), and the app layer's setting doesn't
override the already-captured singleton. In the external environment, the locale
initialization happens differently and `en_GB` takes effect.

## Recommendations

### Fix 1: Reliable error output (Quarkus)

Use `t.printStackTrace(System.err)` directly in `ApplicationLifecycleManager`:

```java
} else {
    applicationLogger.errorv(t, "Failed to start application");
    ensureConsoleLogsDrained();
    // Fallback: bypass logging framework entirely
    t.printStackTrace(System.err);
    System.err.flush();
}
```

This is the only approach that works regardless of locale, logging backend,
handler state, or ErrorManager history.

The `DELAYED_HANDLER.close()` approach does NOT work because its `PatternFormatter`
also formats timestamps and hits the same `FormatData_en_GB` issue.

### Fix 2: Align locale between environments

Set the container's locale in the external environment, or set explicit locale
in the build scripts.

**Option A:** Set `LANG=C.UTF-8` in the external environment before building:
```bash
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
```

**Option B:** Set explicit locale flags in the base layer build to match:
```
-J-Duser.language=en -J-Duser.country=  (empty country)
```

**Option C (recommended):** Don't rely on system locale — set the same locale
in BOTH layer builds explicitly:
```
# In both build-layer-base.sh and build-layer-app.sh:
-J-Duser.language=en -J-Duser.country=GB
```
And ensure the base layer also has `-H:IncludeLocales=en-GB` so that
`FormatData_en_GB` is available at runtime.

# All Framework Jars Experiment (Nov 2026)

## Goal

Include ALL Quarkus framework jars (boot + main, 113 total) into `libjvm.so`
and measure startup performance against plain OpenJDK 25.

## Scripts

- [`build-libjvm.sh`](../build-libjvm.sh) — full build-time init (framework packages)
- [`build-libjvm-minimal-init.sh`](../build-libjvm-minimal-init.sh) — minimal build-time init (JDK + logging only)
- [`benchmark.sh`](../benchmark.sh) — benchmarking (Quarkus startup, TTFR, RSS)
- [`verify-quarkus.sh`](../verify-quarkus.sh) — functional verification

## Context: GraalVM PR #14901

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

## Updated Dependency Versions

Quarkus was updated to latest `main` (from the Oct 2026 `topic.crema.oct26`
branch base). Key version changes:
- netty: 4.2.18 → 4.2.20
- vertx: 5.1.8 → 5.2.3
- smallrye-mutiny-vertx: 4.0.2 → 4.1.1
- Jackson: `tools.jackson.core:jackson-core:3.1.8` (no Jackson 2.x at all)
- slf4j: 2.0.19 → 2.0.20
- Main jar count: 100 → 97 (some jars merged/removed upstream)

## Quarkus Changes (branch `topic.crema.new`)

Based on latest `main` at `c64e3b71dc4`. Commits:

### 1. resolveJarPath .jar suffix check (cherry-pick from `8d1ef7795af`)

When AOT-compiled into libjvm.so, `getCodeSource().getLocation()` returns
the executable path (e.g., `/path/to/java`), not the jar. Added `.jar`
suffix check before returning code source path.

### 2. InetRunTime no-op setters (cherry-pick from `7c4ff0ef180`)

GraalVM requires both get and set methods for `@InjectAccessors` on
non-final `@Alias` fields.

### 3. sun.java.command fallback for resolveJarPath (new)

When AOT-compiled into libjvm.so, BOTH `getCodeSource().getLocation()` AND
`getResource("QuarkusEntryPoint.class")` fail:
- `getCodeSource()` returns the `java` binary path (not `.jar` → skipped)
- `getResource()` returns `null` because the class bytecode is baked into
  libjvm.so and is not available as a resource

Added `resolveJarPathFromCommand(sun.java.command)` which extracts the
`-jar` argument path, then finds a boot jar in `lib/boot/` so the
`jarPath.getParent().getParent().getParent()` app root calculation works.

### 4. Wildfly CidrAddress accessor type fix (new)

`wildfly-common-2.0.1.jar` has `org.wildfly.common.net.CidrAddress` which
shadows `io.smallrye.common.net.CidrAddress`. The shared `Inet4AnyCidrAccessor`
and `Inet6AnyCidrAccessor` had setters typed as `Target_io_smallrye_common_net_CidrAddress`,
causing `@InjectAccessors` type mismatch when the wildfly substitution was used.

Fixed by creating separate `WildflyCidrAddress4Accessor` and `WildflyCidrAddress6Accessor`
classes in `Target_org_wildfly_common_net_CidrAddress.java` with correctly-typed setters.

## Build Issues Resolved

### Brotli4jLoader ClassNotFoundException

**Error:** `ClassNotFoundException: com.aayushatharva.brotli4j.Brotli4jLoader`

`io.netty.handler.codec.compression.Brotli.<clinit>` calls `Class.forName()` for Brotli4j.
Not a Quarkus dependency. Fixed by adding `Brotli` and `BrotliDecompressor` to run-time init.

### CleanerJava24Linker native MemorySegment

**Error:** `Detected a native MemorySegment in the image heap`

`CleanerJava24Linker$Holder.<clinit>` uses `Linker.downcallHandle()` to create a native
function handle for `malloc`. The `$Holder` inner class needed its own run-time init
entry (GraalVM treats inner classes as separate classes for init purposes).

### JacksonCodec constant in image heap

**Error:** `JacksonCodec was found in the image heap but marked for run-time init`

`io.vertx.core.json` was in the run-time init list from previous experiments
(Jackson 2.x multi-release JAR workarounds). With Jackson 3.x only, removed
`io.vertx.core.json` from run-time init. It's now build-time initialized as
part of the `io.vertx` package.

### ZipFileSystemProvider in image heap

`io.smallrye.common.classloader.ClassPathUtils` has a `ZipFileSystemProvider` constant.
Added `jdk.nio.zipfs.ZipFileSystemProvider`, `sun.nio.fs.LinuxFileSystemProvider`,
`sun.nio.fs.LinuxFileSystem` to build-time init.

## Build-Time Init: Full vs Minimal

### Why the full script has broad framework packages

The logging chain creates a cascade of transitive dependencies during build:
1. `org.jboss.logmanager` → creates SLF4J loggers → `org.slf4j`
2. `org.jboss.logmanager` → `jboss-modules` ContentHandlerFactory → `org.jboss.modules`
3. If `io.smallrye.common` is build-time initialized, `wildfly-common`
   references it, which pulls in `io.vertx.core.impl.ContextLocalImpl`, etc.

**It is not possible to build-time init just logging without eventually
cascading into the entire framework graph.** The alternative is the
minimal-init approach: only init the JDK + logging chain + `org.jboss.modules`
+ `org.slf4j` + `org.apache.commons.logging`, and defer everything else
(including `io.smallrye.common`, `org.wildfly.common`, `io.vertx`, `io.netty`,
etc.) to run-time, with explicit run-time init entries to prevent build failures
from classes with native deps or missing optional deps.

### Key difference

| | Full init (`build-libjvm.sh`) | Minimal init (`build-libjvm-minimal-init.sh`) |
|---|---|---|
| Build-time init | 28 package entries (all framework) | 13 entries (JDK + logging + transitive) |
| Run-time init | 56 specific overrides | 73 entries (all netty/vertx/quarkus specifics) |
| libjvm.so size | 601 MB | 603 MB |
| svm_heap | 410 MB | 410 MB |

## Benchmark Results

5 runs each:

```
--- Minimal build-time init ---
minimal-1: quarkus=0.446s ttfr=875ms  rss=374MB
minimal-2: quarkus=0.458s ttfr=901ms  rss=377MB
minimal-3: quarkus=0.456s ttfr=883ms  rss=376MB
minimal-4: quarkus=0.467s ttfr=907ms  rss=375MB
minimal-5: quarkus=0.456s ttfr=888ms  rss=376MB

--- Full build-time init ---
full-1: quarkus=0.505s ttfr=1059ms rss=374MB
full-2: quarkus=0.514s ttfr=980ms  rss=378MB
full-3: quarkus=0.503s ttfr=942ms  rss=373MB
full-4: quarkus=0.464s ttfr=904ms  rss=371MB
full-5: quarkus=0.465s ttfr=906ms  rss=375MB

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

### Summary (median values)

| Configuration | Quarkus Startup | TTFR | RSS | libjvm.so |
|---|---|---|---|---|
| Minimal build-time init | 0.456s | 888ms | 376MB | 603MB |
| Full build-time init | 0.503s | 942ms | 374MB | 601MB |
| Baseline Crema (no jars) | — | 1050ms | 320MB | ~300MB |
| **Plain OpenJDK 25** | **0.308s** | **414ms** | **131MB** | **—** |

### Observations

1. **Minimal init is slightly faster than full init** (~888ms vs ~942ms TTFR).
   Build-time initializing framework packages adds ~54ms overhead — likely
   because more image heap pages need to be faulted in during startup.

2. **RSS is identical** (~375MB both). The image heap size is the same (410MB)
   because both scripts preserve the same 113 jars — the only difference is
   whether framework classes are build-time or run-time initialized.

3. **Both Crema configurations are ~2x slower than plain OpenJDK 25**. The
   overhead is in the Crema/SVM runtime infrastructure itself.

## RSS Analysis

### Memory breakdown (custom libjvm.so after first request)

```
VmRSS:    387 MB total
  RssAnon:  143 MB  (37%)  — JVM heap, thread stacks, native allocs
  RssFile:  244 MB  (63%)  — mmap'd files (mostly libjvm.so)
```

### libjvm.so virtual memory regions

| Permissions | Virtual Size | Description |
|---|---|---|
| `r-xp` | 189.7 MB | .text (code) |
| `rw-p` | 410.8 MB | .svm_heap (image heap) |
| `r--p` | 192.2 MB | .rodata + other read-only |
| `rw-p` | 0.2 MB | .data |
| `---p` | 0.1 MB | guard page |
| **Total** | **1011 MB** | **virtual** |

The 410MB svm_heap is writable and mapped but only ~38% of total virtual
mappings become resident (~387MB RSS out of 1011MB virtual).

### Image heap size (ELF section)

```
.svm_heap: 410 MB (0x19aa0000 bytes)
.text:     189 MB
Total:     601 MB on disk
```

The image heap contains 6.8M objects and 1,736 resources.
Most of this comes from the `jvm-library` macro's base JDK coverage,
not from the Quarkus jars — both minimal and full init produce
the same 410MB heap.

### Crema's RSS footprint

Baseline Crema (no custom jars) uses ~320MB RSS. Custom libjvm.so
uses ~375MB. The difference (~55MB) is the additional Quarkus framework
classes resident in memory. Plain OpenJDK uses ~131MB, so Crema itself
adds ~190MB RSS overhead (mostly from the base JDK image heap).

## Pyronaut Analysis

[Pyronaut](https://github.com/micronaut-projects/pyronaut) is a Micronaut-based
project that uses Crema (GraalVM native image with runtime class loading).

### Key findings

1. **Pyronaut does NOT use `--macro:jvm-library`**. It builds a **standalone
   native executable** (not a libjvm.so). It uses `native-image` with a main
   class, `-cp`, and many `-H:Preserve=` entries.

2. **Selective build-time init**: Pyronaut uses a fine-grained approach:
   - Specific packages/classes for build-time init (~80 entries)
   - Catch-all `--initialize-at-run-time=io.micronaut` and
     `--initialize-at-run-time=io.netty` with specific overrides
   - Jackson, annotations, inject, and core utilities are build-time init
   - IO-bound, networking, and runtime-specific classes are run-time init

3. **`-H:+RuntimeClassLoading`**: Pyronaut enables runtime class loading
   which allows the native image to load application classes at runtime
   (like Crema's -svm mode).

4. **Service loader feature**: `PyronautDevServiceLoaderFeature` captures
   bean metadata at build time and stores service names (not instances)
   in `NativeLauncherServices`. Application-specific services are discovered
   dynamically at runtime.

5. **`-H:-PreserveIncludesJNI`**: Pyronaut explicitly disables this to
   avoid including JNI metadata for preserved classes.

6. **`-Os` optimization**: Pyronaut uses size-optimized compilation.

7. **No `jvm-library` macro overhead**: Since Pyronaut builds a standalone
   executable, it doesn't inherit the `--link-at-build-time`,
   `-H:+AssertInitializationSpecifiedForAllClasses`, or
   `-H:+GuaranteeSubstrateTypesLinked` flags that the `jvm-library` macro
   imposes. This avoids many of the issues we've encountered.

### Best practices applicable to Quarkus/Crema

- **Consider `-H:-PreserveIncludesJNI`** to reduce image size
- **Consider `-Os`** to reduce code section size
- **Specific class-level build-time init** rather than broad package-level
  catches to avoid cascading dependencies
- **`-H:+RuntimeClassLoading`** may be needed for the application classloader

## Learnings

1. **GraalVM PR #14901 eliminates optional dep jars**: The build script is
   dramatically simpler — no downloads, no CP-only jars.

2. **Inner classes need separate run-time init entries**: `Foo` and `Foo$Bar`
   are separate classes for GraalVM init purposes.

3. **Multi-release JAR issue is gone**: Jackson 3.x only, no Jackson 2.x.

4. **AOT-compiled classes are invisible to getResource()**: Both
   `getCodeSource()` and `getResource()` fail for classes in libjvm.so.

5. **Build-time init cascades through the dependency graph**: The logging
   chain (jboss-logmanager → SLF4J → smallrye-common → wildfly-common →
   vertx → netty) makes it infeasible to init only logging without
   eventually pulling in the entire framework.

6. **Minimal build-time init is slightly faster**: Less image heap to
   fault in at startup. The difference is ~5% TTFR.

7. **The RSS and TTFR overhead is from Crema itself**: The base JDK image
   heap (~300MB) dominates RSS. The additional ~55MB from Quarkus framework
   jars is comparatively small.

## Next Steps

Profiling/tracing of the Crema startup is needed to find optimization
opportunities. The ~500ms gap between Crema and plain OpenJDK TTFR is
likely split between:
- libjvm.so mmap + page fault overhead
- Interpreter startup vs JIT warmup
- Image heap initialization

Specific areas to investigate:
- `perf` tracing of `mprotect` syscalls to see glibc arena allocations
- Time breakdown of JVM bootstrap phases with `-svm`
- Whether reducing the image heap size (e.g., fewer JDK packages) helps

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

3 runs each, using [`benchmark.sh`](../benchmark.sh).
Last run of each config includes detailed RSS region breakdown.

### Summary (median of 3 runs)

| Configuration | Quarkus Startup | TTFR | RSS |
|---|---|---|---|
| Minimal build-time init (Crema) | 0.460s | 916ms | 373MB |
| Full build-time init (Crema) | 0.457s | 905ms | 376MB |
| Baseline Crema (no jars) | — (no log) | 1022ms | 320MB |
| **Plain OpenJDK 25** | **0.293s** | **410ms** | **131MB** |

### Observations

1. **Minimal and full init are comparable** — within noise of each other
   (~900–920ms TTFR). The difference from earlier 5-run batches (888 vs 942ms)
   was partially a warm-cache effect in run ordering.

2. **Both Crema configurations are ~2.2x slower than plain OpenJDK 25** in TTFR
   and use ~2.9x more RSS.

3. **Adding Quarkus jars to libjvm.so does help within Crema** — custom
   libjvm.so TTFR (~910ms) is ~10% faster than baseline Crema (~1020ms).

## RSS Region Breakdown (after first request)

Data captured from `/proc/PID/status` on the last run of each configuration:

**Physical memory (RSS):**

| Region | Minimal-init | Full-init | Baseline Crema | Plain OpenJDK |
|---|---|---|---|---|
| **VmRSS** | **382 MB** | **385 MB** | **329 MB** | **133 MB** |
| RssAnon | 137 MB | 140 MB | 142 MB | 111 MB |
| RssFile | 245 MB | 245 MB | 187 MB | 22 MB |
| RssShmem | 0 | 0 | 0 | 0 |
| VmSwap | 0 | 0 | 0 | 0 |

`VmRSS` = `RssAnon` + `RssFile` + `RssShmem` — this is the actual physical
memory consumed by the process. `RssAnon` is anonymous pages (JVM heap,
thread stacks, native `malloc` allocations). `RssFile` is file-backed pages
(mmap’d shared libraries, jars, etc.).

**Virtual address space (not physical memory):**

| Metric | Minimal-init | Full-init | Baseline Crema | Plain OpenJDK |
|---|---|---|---|---|
| VmSize | 34.7 GB | 34.7 GB | 34.6 GB | 9.1 GB |
| VmPeak | 34.8 GB | 34.8 GB | 34.6 GB | 9.2 GB |

`VmSize` is the total virtual address space reserved, NOT physical memory.
The kernel marks these address ranges as claimed in the page tables but does
not back them with physical RAM until they are actually touched. Crema’s SVM
runtime pre-reserves ~34 GB of contiguous virtual space at startup for the
image heap, the Java heap, and the code cache. This has no impact on actual
memory pressure — only VmRSS reflects real consumption.

**Key observations:**

- **RssAnon is similar across all Crema configs** (~137–142 MB). This is the
  JVM heap, thread stacks, and native allocations. It is comparable to plain
  OpenJDK (111 MB), suggesting the JVM heap usage is roughly the same.
- **RssFile is the main RSS differentiator**. Crema with custom jars uses
  245 MB of file-backed pages (mostly libjvm.so), baseline Crema uses 187 MB,
  and plain OpenJDK uses only 22 MB. The ~58 MB difference between custom and
  baseline Crema is the resident portion of the additional Quarkus framework
  code and metadata in libjvm.so.

## libjvm.so Virtual Memory Regions (after first request)

Data from `/proc/PID/maps`. All Crema configurations map libjvm.so into the
same region types but with different sizes.

| Region / Permissions | Minimal-init | Full-init | Baseline Crema | Plain OpenJDK |
|---|---|---|---|---|
| `r-xp` (code / .text) | 189.7 MB | 189.7 MB | 129.1 MB | 18.6 MB |
| `r--p` (read-only data) | 137.5 + 54.7 = 192.2 MB | 137.5 + 54.7 = 192.2 MB | 87.0 + 38.9 = 125.9 MB | 0.9 MB |
| `rw-p` (writable: .data + .svm_heap) | 0.2 + 218.1 + 410.8 = 629.1 MB | 0.2 + 218.1 + 410.8 = 629.1 MB | 0.1 + 140.8 + 267.1 = 408.0 MB | 0.2 MB |
| `---p` (guard pages) | 0.1 MB | 0.1 MB | 0.1 MB | 0.0 MB |
| **Total virtual** | **1011 MB** | **1011 MB** | **663 MB** | **20 MB** |

**Key observations:**

- The two `rw-p` regions in Crema are:
  - A smaller region (~218 MB for custom, ~141 MB for baseline) which is
    part of the image heap mapped early
  - A larger region (~411 MB for custom, ~267 MB for baseline) which is
    the main `.svm_heap` section
- The total writable mapping for custom libjvm.so (629 MB) vs baseline (408 MB)
  = **221 MB** additional writable virtual for Quarkus framework data.
- Plain OpenJDK's libjvm.so is only 20 MB virtual — it's the HotSpot JIT
  engine, not an AOT-compiled image.

## Image Heap and Object Counts (from native-image build output)

These numbers come from the native-image build output line:
```
X MiB (Y%) for image heap: N objects and M resources
```
The `N objects` number is the **count of distinct Java objects** stored in the
image heap section (`.svm_heap`), not a size. These are objects whose state was
captured at build time and serialized into the binary.

| Metric | Baseline Crema | Minimal-init | Full-init |
|---|---|---|---|
| .text (code area) | 128.7 MB | 190.6 MB | 189.4 MB |
| Compilation units | 250,814 | 397,819 | 397,196 |
| .svm_heap (image heap) | 266.9 MB | 410.9 MB | 410.6 MB |
| Object count | 4,426,014 | 6,816,356 | 6,819,274 |
| Resource count | 554 | 1,736 | 1,736 |
| Total image size | 642.96 MB | 984.87 MB | 983.16 MB |
| File size on disk | 396.19 MB (397 MB) | 602.00 MB (603 MB) | 600.50 MB (601 MB) |

**Key observations:**

1. **The base JDK (baseline Crema) contributes 4.43M objects out of 6.82M total
   (65%)** in the custom builds. The additional 2.39M objects are from the
   113 Quarkus framework jars. The JDK objects take 267 MB of image heap;
   Quarkus adds ~144 MB.

2. **Full vs minimal init produce nearly identical images** — the object count
   differs by only ~3,000 (6,819,274 vs 6,816,356). The extra objects in
   full-init are framework singletons and cached state created during build-time
   class initialization. The image heap size difference is <0.3 MB.

3. **Compilation units are essentially identical** between full and minimal
   (397K). This is because both scripts preserve the same 113 jars — the
   `-H:Preserve=path=` flag determines which classes get compiled, not the
   init-time policy.

4. **Resource count** (1,736 vs 554) increases because the Quarkus jars
   contain additional classpath resources (properties files, service loader
   descriptors, etc.) that get embedded in the image.

### Crema's RSS footprint breakdown

| Component | Calculation | RSS |
|---|---|---|
| Plain OpenJDK baseline | measured | 133 MB |
| Crema base overhead | baseline Crema − OpenJDK = 329 − 133 | **~196 MB** |
| Quarkus framework addition | custom Crema − baseline Crema = 383 − 329 | **~54 MB** |
| Custom Crema total | measured | **~383 MB** |

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

6. **Full vs minimal build-time init produces nearly identical results**:
   Same image size, same RSS, comparable TTFR. The init-time policy
   determines which objects are pre-initialized in the heap but does NOT
   change which classes are compiled or preserved.

7. **The base JDK image heap dominates** — 4.43M objects (267 MB) out of
   6.82M (411 MB) total. The Quarkus addition is 2.39M objects (144 MB).

8. **RssAnon is comparable across all configs** (~111–142 MB). The RSS gap
   is entirely in RssFile (mmap’d libjvm.so pages).

9. **Wildfly CidrAddress accessor type conflict**: When both `io.smallrye.common`
   and `org.wildfly.common` are on the classpath, shared `@InjectAccessors`
   cause type mismatches. Each substitution target needs its own accessor
   classes with correctly-typed set methods.

## Reproducing This Experiment

### Prerequisites

- GraalVM CE 25.6.0-dev (built from `~/src/graal` with PR #14901)
- Quarkus `topic.crema.new` branch in `~/src/quarkus` (4 commits on top of main)
- OpenJDK 25 at `/usr/lib/jvm/java-25-openjdk`

### Steps

```bash
# 1. Build Quarkus
cd ~/src/quarkus
git checkout topic.crema.new  # or topic.crema.nov26
mvn -Dquickly

# 2. Build the getting-started app
cd ~/src/mendrugo/crema-999/getting-started
mvn package -DskipTests

# 3. Build libjvm.so (choose one)
cd ~/src/mendrugo/crema-999
./build-libjvm.sh              # full build-time init
# OR
./build-libjvm-minimal-init.sh # minimal build-time init

# 4. Verify
./verify-quarkus.sh

# 5. Benchmark
./benchmark.sh 5

# 6. Build baseline for comparison
$GRAALVM_HOME/bin/native-image --macro:jvm-library -H:Preserve=package=sun.misc
./benchmark.sh 3
```

### Quarkus branch commits (`topic.crema.new`)

1. `2c6c2ca5896` — resolveJarPath .jar suffix check (cherry-pick)
2. `360ac02d8ea` — InetRunTime no-op setters (cherry-pick)
3. `4a28192842f` — sun.java.command fallback for resolveJarPath
4. `7bb8e2916db` — Wildfly CidrAddress accessor type fix

Files changed:
- `independent-projects/bootstrap/runner/.../QuarkusEntryPoint.java`
- `core/runtime/.../graal/InetRunTime.java`
- `core/runtime/.../graal/Target_org_wildfly_common_net_CidrAddress.java`

## Next Steps

Profiling/tracing of the Crema startup is needed to find optimization
opportunities. The ~500ms gap between Crema and plain OpenJDK TTFR is
likely split between:
- libjvm.so loading (mmap + demand paging of ~600MB file)
- SVM runtime bootstrap (image heap setup, isolate creation)
- Interpreter warmup vs JIT compilation latency

Specific areas to investigate:
- `perf` tracing of `mprotect` syscalls to see if Crema causes glibc arena
  allocations during startup
- Time breakdown of JVM bootstrap phases with `-svm`
- Whether reducing the image heap size (e.g., fewer JDK packages via selective
  `-H:Preserve=` instead of full `--macro:jvm-library`) would help
- Whether Pyronaut's approach of building a standalone executable (without
  `--macro:jvm-library`) would avoid the base JDK overhead entirely

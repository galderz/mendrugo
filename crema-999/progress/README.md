# Crema + Quarkus libjvm.so Optimization Progress

## Goal

Improve Quarkus startup time when running inside Crema by pre-compiling
Quarkus framework jars into `$GRAALVM_HOME/lib/svm/libjvm.so`.

Dependencies are added via:
```
-cp dependency.jar -H:Preserve=path=dependency.jar
```
appended to the base `native-image --macro:jvm-library` command.

## Repository Layout

```
crema-999/
├── progress/
│   ├── README.md                  ← this file
│   ├── PROGRESS-oct2026.md        ← Oct 2026: boot jars, logging, main jars blocker
│   └── PROGRESS-nov2026.md        ← Nov 2026: all framework jars, benchmarking
├── build-libjvm.sh                ← current: all 113 jars, full build-time init
├── build-libjvm-minimal-init.sh   ← variant: all 113 jars, minimal build-time init
├── benchmark.sh                   ← startup benchmarking (TTFR, RSS, Quarkus time)
├── verify-quarkus.sh              ← functional smoke test
├── 00-build-jvm.sh                ← reference: baseline libjvm.so
├── 01-build-libjvm-minimal-build-init.sh  ← reference: boot jars, stripped SPI
├── 02-build-libjvm-logmanager-build-init.sh ← reference: boot jars, logging init
├── 03-build-libjvm-all.sh         ← reference: all jars attempt (failed)
├── 04-build-libjvm-all-false-success-reference.sh ← reference: broken script
├── find-dep.sh                    ← helper: find dependency jars
├── strip-spi-files.sh             ← helper: strip SPI files from jars
└── getting-started/               ← Quarkus test application
```

## Progress Documents

### [PROGRESS-oct2026.md](PROGRESS-oct2026.md)

Initial experiments (Oct 2026):
- Baseline establishment (1.023s startup)
- Boot jar preservation (16 jars → 0.984s)
- Logging and LogManager build-time init challenges
- Main jars experiment (blocked by Multi-Release JAR Jackson 2.x)
- Key technical discoveries (Preserve reflection, getCodeSource in Crema, etc.)

### [PROGRESS-nov2026.md](PROGRESS-nov2026.md)

All framework jars experiment (Nov 2026):
- GraalVM PR #14901 removes need for optional dependency jars
- All 113 jars (boot + main) successfully included
- Full vs minimal build-time init comparison
- Benchmarking against plain OpenJDK 25
- RSS and memory analysis
- Pyronaut (Micronaut) code study for Crema best practices

## Quarkus Changes

Quarkus source: `~/src/quarkus` on branch `topic.crema.new`

Key commits:
1. resolveJarPath .jar suffix check for AOT
2. InetRunTime no-op setters for @InjectAccessors
3. sun.java.command fallback when both getCodeSource and getResource fail
4. Wildfly CidrAddress accessor type fix (separate accessors per type)

## Current Status (Nov 2026)

All 113 Quarkus framework jars are successfully included in libjvm.so.
The application starts and responds to HTTP requests.

| Configuration | TTFR | RSS |
|---|---|---|
| Minimal build-time init (Crema) | ~888ms | ~376MB |
| Full build-time init (Crema) | ~942ms | ~375MB |
| Baseline Crema (no jars) | ~1050ms | ~320MB |
| **Plain OpenJDK 25** | **~414ms** | **~131MB** |

**Main finding:** Crema with custom libjvm.so improves over baseline Crema
(~10-15% faster) but is still ~2x slower than plain OpenJDK 25. The overhead
is in the Crema/SVM runtime infrastructure, not in the Quarkus customization.

Profiling/tracing is the next step to identify specific optimization
opportunities in the Crema startup path.

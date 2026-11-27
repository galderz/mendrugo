# Quarkus startup profiler

`ProfileStartup.java` is a Linux companion to `crema-999/benchmark.sh` from https://github.com/galderz/mendrugo. Run it from that directory. It accepts arbitrary native-image or JVM launch commands after `--`.

## Dependencies

JDK 25 (a Linux JVM with FFM support), a C compiler (`cc`), curl, Linux perf, Git and Perl (for FlameGraph). Supported host ABIs: Linux x86-64, AArch64 and RISC-V64 (the JDK must provide a native linker on that architecture). No Maven, Gradle, JNI library, or third-party Java dependency is required. The small C exec gate is compiled on startup, outside the measured window. Python is needed only if you choose to run the upstream FlameScope viewer. `taskset` is needed only with `--app-cpus`. The selected perf events must be permitted by your kernel/security configuration. Kernel tracepoints and system-wide events may require elevated perf privileges. Do not run untrusted applications as root to acquire profiles. The script does not change sysctls, kill unrelated processes, change CPU policy, or enable JVM frame pointers. It refuses to start if the endpoint already accepts connections.

FlameGraph and Netflix FlameScope are cloned if absent, into `~/.cache/startup-profiler/{FlameGraph,flamescope}`. Use `--tools-dir` or preinstall and pin checkouts with `--no-download`. The revisions are saved in the result directory. The script uses FlameGraph's `flamegraph.pl` with locally folded perf script stacks. The FlameScope Python runtime is installed separately (below).

## Quick start

```bash
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 5 --out profiles/native -- getting-started/target/getting-started-runner
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 5 --out profiles/crema -- "$GRAALVM_HOME/bin/java" -svm -jar getting-started/target/quarkus-app/quarkus-run.jar
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 5 --out profiles/openjdk -- /usr/lib/jvm/java-25-openjdk/bin/java -jar getting-started/target/quarkus-app/quarkus-run.jar
```

Java 25 supports the single-source-file launcher; no explicit compilation is needed for an initial smoke test. For repeatable performance experiments, prefer compiling the harness separately so the source compiler is not loaded into the profiling JVM:

```bash
mkdir -p .startup-profiler-classes
javac --release 25 -d .startup-profiler-classes ProfileStartup.java
java --enable-native-access=ALL-UNNAMED -cp .startup-profiler-classes ProfileStartup \
    --runs 2 --modes baseline,cpu --out profiles/smoke -- /path/to/app-runner
```

The `--enable-native-access=ALL-UNNAMED` flag belongs to the harness JVM, not to the workload JVM. The FFM API is final in Java 25; no `--enable-preview` is needed. The application may use any Java version, GraalVM or native executable. For the remaining examples, replace the single-source launcher prefix with the compiled-class prefix if desired. `java --enable-native-access=ALL-UNNAMED ProfileStartup.java --help` shows the full option list.

Use the actual runner filename. Each output directory must be new. `--help` lists all controls. Debug files, binaries and shared libraries must remain accessible and unchanged through symbolization. JIT-generated JVM code needs independent perf-compatible JIT symbol support; native debug information alone does not symbolize JIT code. The script saves an existing `/tmp/perf-PID.map` if available, but does not create one or enable JIT profiling. Symbolization still depends on perf being able to resolve the original map or jitdump data. No JVM instrumentation is added.

## Measurement design

* All runs use the same small C launcher that writes its PID, waits for a FIFO release, then `execvp()`s the requested app. Perf launches the gate with events disabled (`--delay=-1`) and is enabled through the control FIFO with acknowledgement before T0. The same PID becomes the application PID.
* Java calls libc `clock_gettime(CLOCK_MONOTONIC)` via the Foreign Function & Memory API, rather than assuming that `System.nanoTime()` has the same epoch as perf. It also uses native non-blocking FIFO I/O with close-on-exec descriptors and captured `errno`. A small pre-acquisition warmup exercises the FFM clock and FIFO calls and starts `curl --version`. T0 is `CLOCK_MONOTONIC` immediately before releasing the gate, T1 immediately after curl returns the expected body. The gate's wakeup/exec latency is included; perf setup and application process creation before the gate are excluded. This changes the absolute boundary from the original Bash benchmark; compare only baselines collected by the same harness. `sched` also records the kernel exec timestamp when available.
* The endpoint, response and 10 ms failed-poll sleep match `benchmark_one`; curl has a bounded request timeout and the harness has an actual wall-clock timeout. Matching is body-based (like the original), with trailing newlines stripped. The sleep is *between attempts*, not a fixed 10 ms sampling period; curl startup and scheduling also matter. A 10 ms polling interval can hide sub-millisecond improvements. Use `--poll-ms 1` in a separate experiment if appropriate, but remeasure the baseline because it can add contention.
* Perf events are disabled immediately after T1 and the app is terminated only afterwards. `perf stat` counters cover the slightly wider enable/disable interval, and perf stat's own elapsed-time line includes gate/setup/shutdown effects. Use the harness's TTFR, not perf stat's elapsed time. The enable/disable acknowledgement timestamps are saved in `measurement.json`. Every rendered event is restricted to the same process (TGID) and T0–T1. Child processes with different TGIDs are not included in graphs.
* Raw captures use `/dev/shm` by default; `--capture-dir` can point to a larger tmpfs. `--mmap-pages 512K` may help with ring-buffer memory limits. The requested size applies to each applicable ring buffer, not total storage. Raw data is moved to the output directory after recording. No symbolization/rendering occurs between acquisition runs. Quarkus log parsing occurs after shutdown, and RSS/maps are collected after disabling perf, so RSS is a near-TTFR snapshot, not a simultaneous atomic sample.
* The harness is itself a JVM, so JIT compilation, GC, its threads, and JVM process-management overhead can introduce noise even with the same C gate and curl polling. Use its own interleaved baseline, not the Python or historical Bash baseline. The pre-acquisition warmup reduces initial native-call overhead but is not a guarantee of steady-state harness behavior. Keep profiler JVM options constant across modes; consider pinning the harness separately from the workload using taskset if the host allows it. `--app-cpus` pins only the workload. System-wide scheduler traces may contain harness threads, which are excluded from application graphs but still incur capture overhead.
* Modes are separate runs, with the complete order rotated across rounds. Use 50–100 runs per configuration for meaningful overhead comparisons; 5 is a functional first pass. `--cooldown` (default 1s) runs before each acquisition. No cache eviction or CPU frequency changes are performed. Establish separate cold-cache and warm-cache experiments externally if needed.

## Profiling modes

Default: `baseline,stat,cpu,latency,sched,sched-stacks,faults` (5 runs per mode).

| Mode | Configuration |
|---|---|
| `baseline` | No perf |
| `stat` | CPU time, cycles, instructions, context switches, migrations, faults |
| `cpu`, `cpu-99`, `cpu-499`, `cpu-2003`, `cpu-4001`, `cpu-10007` | cpu-clock at 997, 99, 499, 2003, 4001, 10007 Hz respectively; 8 KiB DWARF |
| `cpu-4k`, `cpu-16k`, `cpu-32k` | 997 Hz with alternate DWARF dump sizes |
| `cpu-no-stacks` | 997 Hz, leaf-symbol samples |
| `latency` | 997 Hz CPU profile plus perf latency analysis (requires recent perf) |
| `cycles`, `instructions`, `cache-misses` | Hardware event sampling, 997 Hz, DWARF |
| `lbr` | cycles:u, 997 Hz, hardware LBR call chains (where supported) |
| `sched`, `sched-stacks` | Scheduler events, or switch events with DWARF |
| `faults`, `minor-faults`, `major-faults` | Page fault events, DWARF; major faults always period 1 |
| `syscalls` | Selected syscall entry/exit tracepoints, DWARF |

The CLI, default values, recording commands and output filenames from the Python version are retained. Scheduler processing uses two streaming passes over the raw perf text instead of retaining every system-wide sample in memory. `config.json` also records the harness Java version and relevant perf sysctls. Nonzero exit status indicates capture/render failures; optional auxiliary report failures are visible as render warnings.

## Suggested investigation order

```bash
# Small CPU-overhead experiment
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 50 --modes baseline,cpu --no-slices --out profiles/calibrate -- /path/to/app-runner

# Frequency and depth comparison (repeat separately for each runtime)
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 30 --modes baseline,cpu-no-stacks,cpu-499,cpu,cpu-2003,cpu-4001,cpu-4k,cpu-16k,lbr --no-slices --out profiles/frequency -- /path/to/app-runner

# Scheduling and blocking
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 10 --modes baseline,sched,sched-stacks --out profiles/scheduler -- /path/to/app-runner

# Memory/syscall diagnostics
java --enable-native-access=ALL-UNNAMED ProfileStartup.java --runs 5 --modes baseline,stat,faults,major-faults,syscalls --fault-period 10 --out profiles/memory -- /path/to/app-runner
```

Use `--fault-period 1` for every page fault only after establishing overhead. `--app-cpus` restricts the application via taskset, not the harness or recorder. `--sched-cpus` restricts scheduler recording; this can omit remote wakeups or migrations and invalidate time reconstruction. Prefer all CPUs for correctness, unless an experimentally controlled CPU partition makes restriction safe. System-wide stack recording can be expensive and is diagnostic, not a default claim of low overhead. Unsupported modes fail visibly in their `perf.log`; subsequent rounds skip a mode if it failed before T0. Other modes continue. A nonzero final exit indicates failed measurements or rendering errors.

## Files

```text
profiles/native/
  config.json                         command, environment controls, perf version
  measurements.csv                    individual observations / sample quality
  summary.csv                         medians, p95, baseline-relative TTFR overhead
  view-flamescope.sh                   local viewer launcher
  flamescope/                          timestamped, per-run/per-event perf text
  cpu/
    combined-cpu-clock-samples.svg     sum of samples across successful runs
    combined-slice-cpu-clock-00000ms.svg
    run-001/
      app.log, perf.log, command.txt, measurement.json
      perf.data, perf-script.txt, perf-check.log, perf-check.txt
      cpu-clock-samples.svg            stack sample counts
      cpu-clock-threads.svg            per-thread stack sample counts
      cpu-clock.stacks                 T0-relative perf script input
      slices/                          10 ms bins, measured from T0
  latency/run-001/latency-report.txt
  sched/run-001/sched-timehist.txt
  sched/run-001/thread-intervals.csv
  sched/run-001/thread-state-time.svg
  sched-stacks/run-001/offcpu-time.svg
  stat/run-001/stat.csv
  syscalls/run-001/syscall-latencies.csv
```

Only nonempty graphs are generated. A sample-free fault recording may be valid. Each event is separate; no arbitrary mixing of cycles, faults, syscall events or scheduler events. SVGs are interactive FlameGraph SVGs. `.folded` inputs are also kept. Use `--slice-ms 5` for narrower bins or `--no-slices` to limit rendering output. Combined sample graphs weight runs by sample counts, not equally by run. Per-thread graphs keep TID and are not combined across runs. The script renders sample-count CPU graphs (not hardware-period or latency weighted). `latency-report.txt` contains perf's separate latency-centric analysis; its ordinary flame graphs are still sample-count graphs.

Scheduler graphs: `sched` pairs switch-outs/ins by TID and uses observed wakeups to separate blocked and runnable intervals; its `perf sched timehist` report provides additional detail. `sched-stacks` captures only switch events with DWARF; off-CPU time includes blocking plus wakeup-to-run delay unless a switch-out explicitly indicates a runnable task. Duration widths are nanoseconds of observed thread time, not critical-path wall time. Open intervals at T1 are right-censored, and intervals whose start was not observed are omitted. Simultaneous thread intervals overlap; their sum can exceed TTFR. Missing trace records can invalidate duration estimates, so inspect `perf-check.log`, `perf-check.txt`, and `perf.log` before interpreting these graphs. The script's loss warning is heuristic; no warning does not establish loss-free recording. System-wide raw traces contain other processes' events; do not share them without reviewing.

`syscalls` emits per-event sample graphs and pairs selected entry/exit events in the same TID for a latency CSV. Incomplete pairs are omitted. It does not reconstruct a critical path or account for every syscall. `cpu-no-stacks` produces leaf-symbol counts only. `stat` and baseline cannot honestly produce call-stack flame graphs.

Check `samples`, `no_stack_samples`, `total_frames`, and `unknown_frames` in measurements.csv, inspect actual stack depth and application startup entrypoints; unresolved frames are not the same as truncated DWARF stacks. A 1–3% overhead target is a proposed experimental criterion, not a guarantee. Use both median and p95, and enough repeated runs to characterize uncertainty. `summary.csv` computes median ratio to the baseline; if no baseline mode ran, the overhead column is blank.

## FlameScope

Install its dependencies once, in an isolated virtualenv. Upstream's requirements are old; if your system Python cannot install them, use a compatible older Python (e.g. 3.10/3.11, if supported by the packages) or upstream's Docker build. Install libmagic if needed.

```bash
cd ~/.cache/startup-profiler/flamescope
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

Then, from the benchmark directory:

```bash
profiles/native/view-flamescope.sh
```

Browse `http://127.0.0.1:5000/`. The generated launcher sets PROFILE_DIR to the result's `flamescope/` directory and binds only to loopback. For a remote Linux host use SSH port forwarding: `ssh -L 5000:127.0.0.1:5000 host`. FlameScope's default 20 ms heatmap bins are coarse for a 50 ms startup; the script also generates native 10 ms slices aligned at T0, and `--slice-ms` allows smaller bins. Individual FlameScope files use timestamps relative to T0; the UI may display offsets relative to its first sample. No time stretching or synthetic samples are introduced. The viewer shows event counts rather than off-CPU time or latency weights.

## Validation and limitations

The Java port compiled and ran with the available OpenJDK 21 using `--enable-preview --release 21` (the FFM methods used here are also present in the final Java 25 API). Real HTTP-server baseline smoke tests passed, as did a mock-perf/mock-FlameGraph end-to-end run covering all seven default modes plus syscalls, including time-window filtering, folded counts, scheduler blocked/runnable durations and syscall entry/exit pairing. JDK 25 was unavailable in the development container and download access was blocked, so actual JDK 25 compilation/execution has not been verified. There was no real perf executable or PMU/tracepoint access, nor a real FlameGraph or FlameScope checkout. Real recordings, stack unwinding, tool-version compatibility, architecture support and observer effect remain to be validated on the target Linux machine. Start with `--modes baseline,cpu --runs 2`. If the actual perf version emits an unrecognized text format, raw perf data and perf script output are retained; inspect `render_warning` or `render_error`.

References: https://man7.org/linux/man-pages/man1/perf-record.1.html ; https://man7.org/linux/man-pages/man1/perf-script.1.html ; https://man7.org/linux/man-pages/man1/perf-sched.1.html ; https://github.com/Netflix/flamescope ; https://github.com/brendangregg/FlameGraph .

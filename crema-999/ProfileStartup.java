// Linux startup/TTFR profiler. Java 25, no Java dependencies.
// java --enable-native-access=ALL-UNNAMED ProfileStartup.java --help
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.Linker.Option;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.invoke.MethodHandle;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class ProfileStartup {
    static final String GATE =
            """
        #include <errno.h>
        #include <fcntl.h>
        #include <stdio.h>
        #include <stdlib.h>
        #include <unistd.h>
        int main(int argc, char **argv) {
            if (argc < 5) return 2;
            int fd = open(argv[3], O_WRONLY|O_CREAT|O_TRUNC, 0600);
            if (fd < 0 || dup2(fd, STDOUT_FILENO) < 0 || dup2(fd, STDERR_FILENO) < 0) return 3;
            if (fd > 2) close(fd);
            int gate = open(argv[1], O_RDONLY);
            if (gate < 0) { perror("gate"); return 4; }
            FILE *ready = fopen(argv[2], "w");
            if (!ready) { perror("ready"); return 5; }
            fprintf(ready, "%ld\\n", (long)getpid());
            if (fclose(ready)) return 6;
            char byte; ssize_t n;
            do { n = read(gate, &byte, 1); } while (n < 0 && errno == EINTR);
            close(gate);
            if (n != 1) return 7;
            execvp(argv[4], argv + 4);
            perror("execvp"); return 127;
        }
        """;

    record Cpu(int hz, int depth) {}

    static final Map<String, Cpu> CPU_MODES = Map.ofEntries(
            Map.entry("cpu", new Cpu(997, 8192)), Map.entry("cpu-99", new Cpu(99, 8192)),
            Map.entry("cpu-499", new Cpu(499, 8192)), Map.entry("cpu-2003", new Cpu(2003, 8192)),
            Map.entry("cpu-4001", new Cpu(4001, 8192)), Map.entry("cpu-10007", new Cpu(10007, 8192)),
            Map.entry("cpu-4k", new Cpu(997, 4096)), Map.entry("cpu-16k", new Cpu(997, 16384)),
            Map.entry("cpu-32k", new Cpu(997, 32768)), Map.entry("latency", new Cpu(997, 8192)));
    static final String DEFAULT_MODES = "baseline,stat,cpu,latency,sched,sched-stacks,faults";
    static final List<String> ALL_MODES = List.of(
            "baseline",
            "stat",
            "cpu",
            "cpu-99",
            "cpu-499",
            "cpu-2003",
            "cpu-4001",
            "cpu-10007",
            "cpu-4k",
            "cpu-16k",
            "cpu-32k",
            "latency",
            "cpu-no-stacks",
            "cycles",
            "instructions",
            "cache-misses",
            "lbr",
            "sched",
            "sched-stacks",
            "faults",
            "minor-faults",
            "major-faults",
            "syscalls");
    static final List<String> SCHED_EVENTS = List.of("sched:sched_switch", "sched:sched_wakeup", "sched:sched_wakeup_new",
            "sched:sched_migrate_task", "sched:sched_process_fork", "sched:sched_process_exec");
    static final List<String> SYSCALLS = List.of("mmap", "mprotect", "futex", "openat", "getrandom", "read",
            "connect");
    static final Pattern HEADER = Pattern.compile("^\\s*(.*?)\\s+(\\d+)\\s*/\\s*(\\d+)\\s+\\[(\\d+)\\]\\s+(\\d+\\.\\d+):\\s+(\\S+):(?:\\s+(.*))?\\s*$");
    static final Pattern FRAME = Pattern.compile("^\\s*[0-9a-fA-F]+\\s+(.+)\\s+\\((.*)\\)\\s*$");
    static final Pattern SWITCH = Pattern.compile("\\bprev_pid=(\\d+).*?\\bprev_state=(\\S+).*?\\bnext_pid=(\\d+)");
    static final Pattern WAKE = Pattern.compile("\\bpid=(\\d+)");
    static final Pattern OFFSET = Pattern.compile("\\+0x[0-9a-fA-F]+$");
    static final Pattern RSS = Pattern.compile("^VmRSS:\\s+(\\d+)", Pattern.MULTILINE);
    static final Pattern QUARKUS = Pattern.compile("started in ([0-9.]+)s");
    static final Locale ROOT = Locale.ROOT;

    // Use Linux's actual CLOCK_MONOTONIC rather than relying on the unspecified
    // origin of System.nanoTime(). FFM also provides non-blocking FIFO descriptors.
    // Linux LP64 ABI, on x86-64, AArch64 and RISC-V64.
    static final class Linux implements AutoCloseable {
        static final OfInt I = ValueLayout.JAVA_INT;
        static final OfLong L = ValueLayout.JAVA_LONG;
        static final AddressLayout P = ValueLayout.ADDRESS;
        static final Linker LINKER = Linker.nativeLinker();

        static MethodHandle bind(String name, FunctionDescriptor desc, Option... opts) {
            return LINKER.downcallHandle(LINKER.defaultLookup()
                    .find(name)
                    .orElseThrow(),
                    desc, opts);
        }

        static final MethodHandle CLOCK = bind("clock_gettime", FunctionDescriptor.of(I, I, P));
        static final Option ERRNO_CAPTURE = Option.captureCallState("errno");
        static final StructLayout CAPTURE_LAYOUT = Option.captureStateLayout();
        static final long ERRNO_OFFSET = CAPTURE_LAYOUT.byteOffset(PathElement.groupElement("errno"));
        static final MethodHandle MKFIFO = bind("mkfifo", FunctionDescriptor.of(I, P, I), ERRNO_CAPTURE);
        static final MethodHandle OPEN = bind("open", FunctionDescriptor.of(I, P, I), Option.firstVariadicArg(2), ERRNO_CAPTURE);
        static final MethodHandle READ = bind("read", FunctionDescriptor.of(L, I, P, L), ERRNO_CAPTURE);
        static final MethodHandle WRITE = bind("write", FunctionDescriptor.of(L, I, P, L), ERRNO_CAPTURE);
        static final MethodHandle CLOSE = bind("close", FunctionDescriptor.of(I, I));
        static final int O_RDWR = 2, O_NONBLOCK = 04000, O_CLOEXEC = 02000000;
        final Arena arena = Arena.ofConfined();
        final MemorySegment timespec = arena.allocate(16, 8), buffer = arena.allocate(4096, 8), callState = arena.allocate(CAPTURE_LAYOUT);

        static RuntimeException failure(Throwable t) {
            return t instanceof RuntimeException r ? r : new IllegalStateException(t);
        }

        int errno() {
            return callState.get(I, ERRNO_OFFSET);
        }

        long mono() {
            try {
                if ((int) CLOCK.invokeExact(1, timespec) != 0) {
                    throw new IllegalStateException("clock_gettime failed");
                }
                return timespec.get(L, 0) * 1_000_000_000L + timespec.get(L, 8);
            } catch (Throwable t) {
                throw failure(t);
            }
        }

        static MemorySegment cstr(Arena a, String s) {
            byte[] b = (s + "\0").getBytes(StandardCharsets.UTF_8);
            MemorySegment p = a.allocate(b.length);
            p.copyFrom(MemorySegment.ofArray(b));
            return p;
        }

        Fifo fifo(Path p) throws IOException {
            try (Arena a = Arena.ofConfined()) {
                MemorySegment name = cstr(a, p.toString());
                if ((int) MKFIFO.invokeExact(callState, name, 0600) != 0) {
                    throw new IOException("mkfifo " + p + ": errno " + errno());
                }
                int fd = (int) OPEN.invokeExact(callState, name, O_RDWR | O_NONBLOCK | O_CLOEXEC);
                if (fd < 0) {
                    throw new IOException("open FIFO " + p + ": errno " + errno());
                }
                return new Fifo(fd);
            } catch (IOException e) {
                throw e;
            } catch (Throwable t) {
                throw failure(t);
            }
        }

        final class Fifo implements AutoCloseable {
            final int fd;

            Fifo(int fd) {
                this.fd = fd;
            }

            void write(String s) throws IOException {
                byte[] b = s.getBytes(StandardCharsets.US_ASCII);
                try {
                    buffer.asSlice(0, b.length).copyFrom(MemorySegment.ofArray(b));
                    long n;
                    do {
                        n = (long) WRITE.invokeExact(callState, fd, buffer, (long) b.length);
                    } while (n < 0 && errno() == 4);
                    if (n != b.length) {
                        throw new IOException("FIFO write failed, wrote " + n + " bytes");
                    }
                } catch (IOException e) {
                    throw e;
                } catch (Throwable t) {
                    throw failure(t);
                }
            }

            String read() throws IOException {
                try {
                    long n = (long) READ.invokeExact(callState, fd, buffer, buffer.byteSize());
                    if (n < 0) {
                        int e = errno();
                        if (e == 11 || e == 4) {
                            return "";
                        }
                        throw new IOException("FIFO read errno " + e);
                    }
                    return StandardCharsets.US_ASCII.decode(buffer.asSlice(0, n)
                            .asByteBuffer())
                            .toString();
                } catch (IOException e) {
                    throw e;
                } catch (Throwable t) {
                    throw failure(t);
                }
            }

            public void close() {
                try {
                    int ignored = (int) CLOSE.invokeExact(fd);
                } catch (Throwable t) {
                    throw failure(t);
                }
            }
        }

        public void close() {
            arena.close();
        }
    }

    static final class Args {
        int runs = 5, faultPeriod = 10, traceStack = 8192, sliceMs = 10;
        double pollMs = 10, timeout = 60, requestTimeout = 2, setupTimeout = 30, shutdownGrace = 3, cooldown = 1;
        String perf = "perf", cc = "cc", url = "http://localhost:8080/hello", expected = "hello", mmapPages = "1M", appCpus = null, schedCpus = null;
        Path out, toolsDir = Path.of(System.getProperty("user.home"), ".cache", "startup-profiler"), captureDir = Path.of("/dev/shm"), fg;
        boolean noDownload, noRender, slices = true;
        List<String> modes = List.of(DEFAULT_MODES.split(",")), command = List.of();

        static Args parse(String[] argv) {
            Args a = new Args();
            for (int i = 0; i < argv.length; i++) {
                String k = argv[i];
                if (k.equals("--")) {
                    a.command = List.of(Arrays.copyOfRange(argv, i + 1, argv.length));
                    break;
                }
                if (!k.startsWith("-")) {
                    a.command = List.of(Arrays.copyOfRange(argv, i, argv.length));
                    break;
                }
                switch (k) {
                    case "--no-download" -> {
                        a.noDownload = true;
                        continue;
                    }
                    case "--no-render" -> {
                        a.noRender = true;
                        continue;
                    }
                    case "--no-slices" -> {
                        a.slices = false;
                        continue;
                    }
                    default -> {}
                }
                String v;
                int eq = k.indexOf('=');
                if (eq >= 0) {
                    v = k.substring(eq + 1);
                    k = k.substring(0, eq);
                } else {
                    if (++i == argv.length) {
                        throw new IllegalArgumentException("Missing value for " + k);
                    }
                    v = argv[i];
                }
                switch (k) {
                    case "--runs" -> a.runs = Integer.parseInt(v);
                    case "--modes" -> a.modes = List.copyOf(new LinkedHashSet<>(Arrays.stream(v.split(",", -1))
                            .map(String::strip)
                            .toList()));
                    case "--out" -> a.out = path(v);
                    case "--tools-dir" -> a.toolsDir = path(v);
                    case "--capture-dir" -> a.captureDir = path(v);
                    case "--perf" -> a.perf = v;
                    case "--cc" -> a.cc = v;
                    case "--url" -> a.url = v;
                    case "--expected" -> a.expected = v;
                    case "--poll-ms" -> a.pollMs = Double.parseDouble(v);
                    case "--timeout" -> a.timeout = Double.parseDouble(v);
                    case "--request-timeout" -> a.requestTimeout = Double.parseDouble(v);
                    case "--setup-timeout" -> a.setupTimeout = Double.parseDouble(v);
                    case "--shutdown-grace" -> a.shutdownGrace = Double.parseDouble(v);
                    case "--cooldown" -> a.cooldown = Double.parseDouble(v);
                    case "--mmap-pages" -> a.mmapPages = v;
                    case "--fault-period" -> a.faultPeriod = Integer.parseInt(v);
                    case "--trace-stack" -> a.traceStack = Integer.parseInt(v);
                    case "--slice-ms" -> a.sliceMs = Integer.parseInt(v);
                    case "--app-cpus" -> a.appCpus = v;
                    case "--sched-cpus" -> a.schedCpus = v;
                    default -> throw new IllegalArgumentException("Unknown option " + k);
                }
            }
            if (a.command.isEmpty() || a.modes.isEmpty() || !ALL_MODES.containsAll(a.modes)) {
                throw new IllegalArgumentException("Provide valid modes and an application command after --");
            }
            if (a.runs < 1
                    || a.faultPeriod < 1
                    || a.traceStack < 1
                    || a.sliceMs < 1
                    || !positive(a.timeout, a.requestTimeout, a.setupTimeout)
                    || !nonnegative(a.pollMs, a.cooldown, a.shutdownGrace)) {
                throw new IllegalArgumentException("Invalid numerical option");
            }
            a.captureDir = a.captureDir.toAbsolutePath().normalize();
            a.toolsDir = a.toolsDir.toAbsolutePath().normalize();
            if (a.out == null) {
                a.out = Path.of("startup-profiles", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
            }
            a.out = a.out.toAbsolutePath().normalize();
            return a;
        }

        Map<String, Object> metadata() {
            Map<String, Object> m = new LinkedHashMap<>();
            put(
                    m,
                    "runs",
                    runs,
                    "modes",
                    modes,
                    "out",
                    out,
                    "tools_dir",
                    toolsDir,
                    "capture_dir",
                    captureDir,
                    "no_download",
                    noDownload,
                    "perf",
                    perf,
                    "cc",
                    cc,
                    "url",
                    url,
                    "expected",
                    expected,
                    "poll_ms",
                    pollMs,
                    "timeout",
                    timeout,
                    "request_timeout",
                    requestTimeout,
                    "setup_timeout",
                    setupTimeout,
                    "shutdown_grace",
                    shutdownGrace,
                    "cooldown",
                    cooldown,
                    "mmap_pages",
                    mmapPages,
                    "fault_period",
                    faultPeriod,
                    "trace_stack",
                    traceStack,
                    "slice_ms",
                    sliceMs,
                    "slices",
                    slices,
                    "app_cpus",
                    appCpus,
                    "sched_cpus",
                    schedCpus,
                    "no_render",
                    noRender,
                    "command",
                    command,
                    "fg",
                    fg);
            return m;
        }
    }

    static boolean positive(double... v) {
        return Arrays.stream(v).allMatch(x -> Double.isFinite(x) && x > 0);
    }

    static boolean nonnegative(double... v) {
        return Arrays.stream(v).allMatch(x -> Double.isFinite(x) && x >= 0);
    }

    static Path path(String s) {
        return Path.of(s.startsWith("~/") ? System.getProperty("user.home") + s.substring(1) : s);
    }

    static void help() {
        System.out
                .println("""
        Usage: java --enable-native-access=ALL-UNNAMED ProfileStartup.java [options] -- command [args...]
        Linux startup/TTFR profiler; output directory must be new.
          --runs N (5)              Runs per mode; rotate order across rounds
          --modes CSV               Default: baseline,stat,cpu,latency,sched,sched-stacks,faults
          --out PATH                Default startup-profiles/<timestamp>
          --tools-dir PATH          Default ~/.cache/startup-profiler
          --capture-dir PATH        Default /dev/shm
          --no-download             Require existing tool checkouts
          --no-render               Record only; skip tool downloads and report generation
          --perf PATH (perf)        perf executable
          --cc PATH (cc)            C compiler for exec gate
          --url URL                 Default http://localhost:8080/hello
          --expected BODY (hello)   Expected body, trailing LF stripped from curl output
          --poll-ms N (10)          Sleep between unsuccessful curl attempts
          --timeout SEC (60)        TTFR deadline
          --request-timeout SEC (2) Maximum per-request duration
          --setup-timeout SEC (30)  Wait for launch gate
          --shutdown-grace SEC (3)  Graceful shutdown before SIGKILL
          --cooldown SEC (1)        Sleep before each acquisition
          --mmap-pages SIZE (1M)    perf ring buffer size
          --fault-period N (10)     Sample every N faults (major-faults always 1)
          --trace-stack BYTES (8192) DWARF stack size for tracepoint/fault modes
          --slice-ms N (10)         T0-relative graph bins
          --no-slices               Skip per-bin flame graphs
          --app-cpus LIST           taskset CPU list for application only
          --sched-cpus LIST         Restrict scheduler CPUs (may miss events)
        """
                        + "Modes: "
                        + String.join(", ", ALL_MODES));
    }

    static void put(Map<String, Object> m, Object... kv) {
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], kv[i + 1]);
        }
    }

    static String ts(long n) {
        return String.format(ROOT, "%d.%09d", n / 1_000_000_000L, n % 1_000_000_000L);
    }

    static long parseNs(String s) {
        int i = s.indexOf('.');
        return Long.parseLong(s.substring(0, i)) * 1_000_000_000L + Long.parseLong((s.substring(i + 1) + "000000000").substring(0, 9));
    }

    static void sleep(double seconds) throws InterruptedException {
        TimeUnit.NANOSECONDS.sleep((long) (seconds * 1e9));
    }

    static long deadline(double seconds) {
        return System.nanoTime() + (long) (seconds * 1e9);
    }

    static boolean before(long deadline) {
        return System.nanoTime() - deadline < 0;
    }

    static String quote(String s) {
        return s.matches("[a-zA-Z0-9_@%+=:,./-]+") ? s : "'" + s.replace("'", "'\\''") + "'";
    }

    static String shell(List<String> cmd) {
        return String.join(" ",
                cmd.stream()
                   .map(ProfileStartup::quote)
                   .toList());
    }

    static boolean existsCommand(String name) {
        if (name.contains("/")) {
            return Files.isExecutable(Path.of(name));
        }
        for (String dir : System.getenv()
                .getOrDefault("PATH", "")
                .split(":")) {
            if (Files.isExecutable(Path.of(dir, name)) && !Files.isDirectory(Path.of(dir, name))) {
                return true;
            }
        }
        return false;
    }

    static void checked(List<String> cmd) throws Exception {
        int rc = new ProcessBuilder(cmd)
                .inheritIO()
                .start()
                .waitFor();
        if (rc != 0) {
            throw new IOException("exit " + rc + ": " + shell(cmd));
        }
    }

    static String output(List<String> cmd) throws Exception {
        Process p = new ProcessBuilder(cmd)
                .redirectError(Redirect.DISCARD)
                .start();
        String s = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (p.waitFor() != 0) {
            throw new IOException("Command failed: " + shell(cmd));
        }
        return s.strip();
    }

    static boolean runFile(List<String> cmd, Path out, Path err) throws Exception {
        Files.writeString(err, "\n$ " + shell(cmd) + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return new ProcessBuilder(cmd)
                .redirectOutput(out.toFile())
                .redirectError(Redirect.appendTo(err.toFile()))
                .start()
                .waitFor()
                == 0;
    }

    static void cloneTools(Args a) throws Exception {
        Files.createDirectories(a.toolsDir);
        for (String[] tool : List.of(new String[] {"FlameGraph", "https://github.com/brendangregg/FlameGraph.git", "flamegraph.pl"},
                new String[] {"flamescope", "https://github.com/Netflix/flamescope.git", "run.py"})) {
            Path dir = a.toolsDir.resolve(tool[0]);
            if (!Files.isRegularFile(dir.resolve(tool[2]))) {
                if (Files.exists(dir) || a.noDownload) {
                    throw new IOException("Missing " + dir.resolve(tool[2]) + "; install checkout manually or remove --no-download");
                }
                checked(List.of("git", "clone", "--depth", "1", tool[1],
                        dir.toString()));
            }
            String rev;
            try {
                rev = output(List.of("git", "-C", dir.toString(), "rev-parse", "HEAD"));
            } catch (IOException e) {
                rev = "unknown";
            }
            Files.writeString(a.out.resolve(tool[0] + ".revision"), rev + "\n");
        }
        a.fg = a.toolsDir.resolve("FlameGraph");
        Path launch = a.out.resolve("view-flamescope.sh");
        Files.writeString(launch, "#!/usr/bin/env bash\nset -euo pipefail\ncd "
                + quote(a.toolsDir.resolve("flamescope").toString())
                + "\nexport STARTUP_PROFILES="
                + quote(a.out.resolve("flamescope").toString())
                + "\n"
                + """
            VENV=${FLAMESCOPE_VENV:-.venv}
            if [[ ! -x "$VENV/bin/python" ]]; then
              echo 'Install FlameScope dependencies first: python3 -m venv .venv && .venv/bin/pip install -r requirements.txt' >&2
              exit 1
            fi
            exec "$VENV/bin/python" -c 'import os; from app import APP, config; config.PROFILE_DIR=os.environ["STARTUP_PROFILES"]; APP.config["PROFILE_DIR"]=config.PROFILE_DIR; APP.run(host="127.0.0.1", port=5000, debug=False, use_reloader=False)'
            """);
        launch.toFile().setExecutable(true, false);
    }

    static Path makeGate(Args a) throws Exception {
        Path src = a.out.resolve("exec-gate.c"), exe = a.out.resolve("exec-gate");
        Files.writeString(src, GATE);
        checked(List.of(a.cc, "-O2", "-Wall", "-Wextra", src.toString(),
                "-o", exe.toString()));
        return exe;
    }

    static void portCheck(String url) throws IOException {
        URI u = URI.create(url);
        if (!Set.of("http", "https").contains(u.getScheme()) || u.getHost() == null) {
            throw new IllegalArgumentException("URL must be HTTP(S)");
        }
        try (Socket s = new Socket()) {
            s.connect(
                    new InetSocketAddress(u.getHost(),
                            u.getPort() < 0
                                    ? (u.getScheme().equals("https") ? 443 : 80)
                                    : u.getPort()),
                    200);
        } catch (IOException e) {
            return;
        }
        throw new IOException(u.getAuthority() + " already accepts connections; refusing to benchmark existing service");
    }

    static List<String> config(Args a, String mode, Path data,
            Path ctl, Path ack) {
        if (mode.equals("baseline")) {
            return new ArrayList<>();
        }
        List<String> c = new ArrayList<>(List.of(a.perf, mode.equals("stat") ? "stat" : "record", "--delay=-1",
                "--control=fifo:" + ctl + "," + ack));
        if (mode.equals("stat")) {
            c.addAll(List.of("-x", ";", "-o", data.toString(), "-e",
                    "task-clock,cycles,instructions,context-switches,cpu-migrations,page-faults,minor-faults,major-faults", "--"));
            return c;
        }
        c.addAll(List.of("-k", "mono", "-T", "--sample-cpu", "--no-buildid-cache", "-m",
                a.mmapPages, "-o", data.toString()));
        Cpu cpu = CPU_MODES.get(mode);
        if (cpu != null) {
            c.addAll(List.of("-e", "cpu-clock", "-F", "" + cpu.hz, "--strict-freq",
                    "--call-graph", "dwarf," + cpu.depth));
            if (mode.equals("latency")) {
                c.add("--latency");
            }
        } else {
            switch (mode) {
                case "cpu-no-stacks" -> c.addAll(List.of("-e", "cpu-clock", "-F", "997", "--strict-freq"));
                case "cycles", "instructions", "cache-misses" -> c.addAll(List.of("-e", mode, "-F", "997", "--strict-freq", "--call-graph",
                        "dwarf,8192"));
                case "lbr" -> c.addAll(List.of("-e", "cycles:u", "-F", "997", "--strict-freq", "--call-graph",
                        "lbr"));
                case "sched", "sched-stacks" -> {
                    c.addAll(List.of("-a", "-c", "1"));
                    if (a.schedCpus != null) {
                        c.addAll(List.of("-C", a.schedCpus));
                    }
                    for (String e : mode.equals("sched") ? SCHED_EVENTS : List.of("sched:sched_switch")) {
                        c.addAll(List.of("-e", e));
                    }
                    if (mode.equals("sched-stacks")) {
                        c.addAll(List.of("--call-graph", "dwarf," + a.traceStack));
                    }
                }
                case "faults", "minor-faults", "major-faults" -> c.addAll(
                        List.of(
                                "-e",
                                mode.equals("faults") ? "page-faults" : mode,
                                "-c",
                                "" + (mode.equals("major-faults") ? 1 : a.faultPeriod),
                                "--call-graph",
                                "dwarf," + a.traceStack));
                case "syscalls" -> {
                    c.addAll(List.of("-c", "1", "--call-graph", "dwarf," + a.traceStack));
                    for (String name : SYSCALLS) {
                        for (String dir : List.of("enter", "exit")) {
                            c.addAll(List.of("-e", "syscalls:sys_" + dir + "_" + name));
                        }
                    }
                }
                default -> throw new IllegalArgumentException("Unknown mode: " + mode);
            }
        }
        c.add("--");
        return c;
    }

    static void ctlSend(Linux.Fifo w, Linux.Fifo r, String cmd,
                        Process p, double timeout)
            throws Exception {
        w.write(cmd + "\n");
        long end = deadline(timeout);
        StringBuilder b = new StringBuilder();
        while (before(end)) {
            b.append(r.read());
            if (b.indexOf("ack\n") >= 0) {
                return;
            }
            if (!p.isAlive()) {
                break;
            }
            sleep(.001);
        }
        throw new IOException("perf did not acknowledge " + cmd + "; see perf.log");
    }

    static void deleteTree(Path p) throws IOException {
        try (Stream<Path> paths = Files.walk(p)) {
            for (Path f : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(f);
            }
        }
    }

    static void copyIfPresent(Path from, Path to) {
        try {
            Files.copy(from, to, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    static volatile Process activeProcess, activeCurl;
    static volatile Long activePid;

    static void signalTree(ProcessHandle p, boolean force) {
        p.descendants().forEach(c -> {
            if (force) {
                c.destroyForcibly();
            } else {
                c.destroy();
            }
        });
        if (force) {
            p.destroyForcibly();
        } else {
            p.destroy();
        }
    }

    static void signalApp(Process p, Long pid, boolean force) {
        if (pid != null) {
            ProcessHandle.of(pid).ifPresent(h -> signalTree(h, force));
        } else {
            signalTree(p.toHandle(), force);
        }
    }

    static Integer stopProcess(Process p, Long pid, double grace,
            boolean profiled)
            throws InterruptedException {
        if (p == null) {
            return null;
        }
        // If the recorder exited first, still clean up the known workload.
        if (p.isAlive() || (pid != null && ProcessHandle.of(pid)
                .map(ProcessHandle::isAlive)
                .orElse(false))) {
            signalApp(p, pid, false);
        }
        if (p.waitFor((long) (grace * 1000), TimeUnit.MILLISECONDS)) {
            if (pid != null) {
                ProcessHandle.of(pid)
                        .filter(ProcessHandle::isAlive)
                        .ifPresent(h -> signalTree(h, true));
            }
            return p.exitValue();
        }
        if (pid != null) {
            ProcessHandle.of(pid).ifPresent(h -> signalTree(h, true));
        } else {
            signalTree(p.toHandle(), true);
        }
        if (!p.waitFor(profiled ? 60 : 2, TimeUnit.SECONDS)) {
            signalTree(p.toHandle(), true);
            p.waitFor();
        }
        return p.exitValue();
    }

    static byte[] curl(Args a, double seconds) throws Exception {
        Process p = new ProcessBuilder("curl", "-s", "--max-time", Double.toString(seconds), a.url)
                .redirectError(Redirect.DISCARD)
                .start();
        activeCurl = p;
        try {
            byte[] body = p.getInputStream().readAllBytes();
            p.waitFor();
            return body;
        } finally {
            if (p.isAlive()) {
                p.destroyForcibly();
            }
            activeCurl = null;
        }
    }

    static boolean matches(byte[] body, byte[] expected) {
        int len = body.length;
        while (len > 0 && body[len - 1] == '\n') {
            len--;
        }
        return Arrays.equals(body, 0, len, expected, 0, expected.length);
    }

    record Result(Map<String, Object> m, Path dir) {
        String mode() {
            return (String) m.get("mode");
        }

        int run() {
            return ((Number) m.get("run")).intValue();
        }

        boolean ok() {
            return "ok".equals(m.get("status"));
        }

        long t0() {
            return ((Number) m.get("t0_ns")).longValue();
        }

        long t1() {
            return ((Number) m.get("t1_ns")).longValue();
        }

        long pid() {
            return ((Number) m.get("pid")).longValue();
        }
    }

    static Result capture(Args a, Linux linux, String mode,
                          int n, Path gateExe)
            throws Exception {
        Path d = a.out.resolve(mode).resolve(String.format(ROOT, "run-%03d", n));
        Files.createDirectories(d);
        Map<String, Object> m = new LinkedHashMap<>();
        put(
                m,
                "mode",
                mode,
                "run",
                n,
                "status",
                "failed",
                "pid",
                null,
                "t0_ns",
                null,
                "t1_ns",
                null,
                "ttfr_ms",
                null,
                "quarkus_ms",
                null,
                "rss_kib",
                null,
                "attempts",
                0,
                "perf_rc",
                null,
                "error",
                "");
        Path tmp = Files.createTempDirectory(a.captureDir, "qstartup-");
        Path data = tmp.resolve(mode.equals("stat") ? "stat.csv" : "perf.data"), gate = tmp.resolve("gate"), ready = tmp.resolve("ready"), ctl = tmp.resolve("ctl"), ack = tmp.resolve("ack");
        Linux.Fifo g = null, w = null, r = null;
        Process p = null;
        Long pid = null;
        boolean enabled = false;
        Integer rc = null;
        try {
            portCheck(a.url);
            g = linux.fifo(gate);
            if (!mode.equals("baseline")) {
                w = linux.fifo(ctl);
                r = linux.fifo(ack);
            }
            List<String> cmd = config(a, mode, data, ctl, ack);
            if (a.appCpus != null) {
                cmd.addAll(List.of("taskset", "-c", a.appCpus));
            }
            cmd.addAll(
                    List.of(gateExe.toString(), gate.toString(), ready.toString(),
                            d.resolve("app.log").toString()));
            cmd.addAll(a.command);
            Files.writeString(d.resolve("command.txt"), shell(cmd) + "\n");
            p = new ProcessBuilder(cmd)
                    .redirectErrorStream(true)
                    .redirectOutput(d.resolve("perf.log").toFile())
                    .start();
            activeProcess = p;
            long end = deadline(a.setupTimeout);
            while (before(end)) {
                if (Files.exists(ready)) {
                    String s = Files.readString(ready).strip();
                    if (s.matches("[0-9]+")) {
                        pid = Long.parseLong(s);
                        activePid = pid;
                        break;
                    }
                }
                if (!p.isAlive()) {
                    break;
                }
                sleep(.001);
            }
            if (pid == null) {
                throw new IOException("workload failed to reach exec gate; see perf.log/app.log");
            }
            m.put("pid", pid);
            if (w != null) {
                ctlSend(w, r, "enable", p, 10);
                enabled = true;
                m.put("enabled_ack_ns", linux.mono());
            }
            byte[] expected = a.expected.getBytes(StandardCharsets.UTF_8);
            long t0 = linux.mono();
            m.put("t0_ns", t0);
            g.write("x");
            long limit = t0 + (long) (a.timeout * 1e9);
            Long t1 = null;
            int attempts = 0;
            while (linux.mono() < limit) {
                m.put("attempts", ++attempts);
                double rem = Math.max(.001, (limit - linux.mono()) / 1e9);
                byte[] body = curl(a, Math.min(a.requestTimeout, rem));
                long received = linux.mono();
                if (matches(body, expected) && received <= limit) {
                    t1 = received;
                    break;
                }
                if (!p.isAlive()) {
                    throw new IOException("application/recorder exited before readiness");
                }
                sleep(a.pollMs / 1000);
            }
            if (t1 == null) {
                throw new IOException("first-response timeout");
            }
            put(m, "t1_ns", t1, "ttfr_ms", (t1 - t0) / 1e6);
            if (enabled) {
                ctlSend(w, r, "disable", p, 10);
                enabled = false;
                m.put("disabled_ack_ns", linux.mono());
            }
            Path proc = Path.of("/proc", pid.toString());
            try {
                Matcher match = RSS.matcher(Files.readString(proc.resolve("status")));
                if (match.find()) {
                    m.put("rss_kib", Long.parseLong(match.group(1)));
                }
            } catch (IOException ignored) {
            }
            Map<String, String> threads = new LinkedHashMap<>();
            try (Stream<Path> tasks = Files.list(proc.resolve("task"))) {
                for (Path t : tasks.toList()) {
                    try {
                        threads.put(t.getFileName().toString(),
                                Files.readString(t.resolve("comm")).strip());
                    } catch (IOException ignored) {
                    }
                }
            } catch (IOException ignored) {
            }
            m.put("threads", threads);
            for (String name : List.of("maps", "status")) {
                copyIfPresent(proc.resolve(name), d.resolve("proc-" + name + ".txt"));
            }
            Path pm = Path.of("/tmp", "perf-" + pid + ".map");
            if (Files.isRegularFile(pm)) {
                copyIfPresent(pm, d.resolve(pm.getFileName()));
            }
            m.put("status", "ok");
        } catch (IOException | RuntimeException e) {
            m.put("error", e.toString());
        } finally {
            try {
                if (enabled && p != null && p.isAlive()) {
                    try {
                        ctlSend(w, r, "disable", p, 2);
                    } catch (IOException ignored) {
                    }
                }
                rc = stopProcess(p, pid, a.shutdownGrace, !mode.equals("baseline"));
            } finally {
                activeProcess = null;
                activePid = null;
                if (g != null) {
                    g.close();
                }
                if (w != null) {
                    w.close();
                }
                if (r != null) {
                    r.close();
                }
                if (Files.exists(data)) {
                    Files.move(data, d.resolve(data.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                }
                deleteTree(tmp);
            }
        }
        m.put("perf_rc", rc);
        if (!mode.equals("baseline") && !Objects.equals(rc, 0) && !Objects.equals(rc, 143)) {
            m.put("status", "failed");
            m.put("error", m.get("error") + " (recorder exit " + rc + ")");
        }
        try {
            Matcher match = QUARKUS.matcher(readText(d.resolve("app.log")));
            if (match.find()) {
                m.put("quarkus_ms", Double.parseDouble(match.group(1)) * 1000);
            }
        } catch (IOException ignored) {
        }
        if (!mode.equals("baseline") && !Files.isRegularFile(d.resolve(data.getFileName()))) {
            m.put("status", "failed");
            m.put("error", m.get("error") + " (no " + data.getFileName() + ")");
        }
        writeJson(d.resolve("measurement.json"), m);
        System.out.printf(
                ROOT,
                "%-15s %3d  TTFR=%12s ms  Quarkus=%s ms  %s %s%n",
                mode,
                n,
                m.get("ttfr_ms") == null ? "N/A" : String.format(ROOT, "%.3f", m.get("ttfr_ms")),
                m.get("quarkus_ms"),
                m.get("status"),
                m.get("error"));
        return new Result(m, d);
    }

    record Sample(
            String comm,
            long pid,
            long tid,
            int cpu,
            String timestamp,
            long ns,
            String event,
            String payload,
            List<String> lines) {}

    static String readText(Path p) throws IOException {
        return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
    }

    @FunctionalInterface
    interface SampleConsumer {
        void accept(Sample s) throws Exception;
    }

    static void readSamples(Path path, SampleConsumer consumer) throws Exception {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
            Sample head = null;
            List<String> lines = null;
            String line;
            while ((line = r.readLine()) != null) {
                Matcher m = HEADER.matcher(line);
                if (m.matches()) {
                    if (head != null) {
                        consumer.accept(head);
                    }
                    lines = new ArrayList<>();
                    lines.add(line.stripLeading());
                    head = new Sample(
                            m.group(1),
                            Long.parseLong(m.group(2)),
                            Long.parseLong(m.group(3)),
                            Integer.parseInt(m.group(4)),
                            m.group(5),
                            parseNs(m.group(5)),
                            m.group(6),
                            Objects.toString(m.group(7), ""),
                            lines);
                } else if (head != null && !line.isBlank() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
            if (head != null) {
                consumer.accept(head);
            }
        }
    }

    static String sanitize(String s) {
        return s.replace(';', ':');
    }

    static String safeEvent(String s) {
        return s.replaceAll("[^a-zA-Z0-9_.-]+", "-");
    }

    static List<String> frames(Sample s) {
        List<String> frames = new ArrayList<>();
        for (int i = 1; i < s.lines.size(); i++) {
            Matcher m = FRAME.matcher(s.lines.get(i));
            if (m.matches()) {
                String name = OFFSET.matcher(m.group(1)).replaceAll("");
                if (name.equals("[unknown]")) {
                    String dso = m.group(2);
                    name += " (" + dso.substring(dso.lastIndexOf('/') + 1) + ")";
                }
                frames.add(sanitize(name));
            }
        }
        return frames;
    }

    static String stackKey(Sample s, List<String> frames, boolean thread) {
        List<String> f = new ArrayList<>(frames);
        if (f.isEmpty()) {
            Matcher m = FRAME.matcher(s.payload);
            if (m.matches() && !SCHED_EVENTS.contains(s.event)) {
                f.add(sanitize(OFFSET.matcher(m.group(1)).replaceAll("")));
            } else {
                f.add("[no call stack]");
            }
        }
        Collections.reverse(f);
        return sanitize(s.comm)
                + (thread ? " [" + s.tid + "]" : "")
                + ";"
                + String.join(";", f);
    }

    static void inc(Map<String, Long> counter, String key, long n) {
        counter.merge(key, n, Long::sum);
    }

    static <K> Map<String, Long> counter(Map<K, Map<String, Long>> groups, K key) {
        return groups.computeIfAbsent(key, k -> new TreeMap<>());
    }

    static long sum(Map<String, Long> counter) {
        return counter.values()
                      .stream()
                      .mapToLong(Long::longValue)
                      .sum();
    }

    static void flame(Args a, Map<String, Long> counter, Path prefix,
                      String title, String unit, String color)
            throws Exception {
        if (counter.isEmpty() || sum(counter) == 0) {
            return;
        }
        Path folded = Path.of(prefix + ".folded"), svg = Path.of(prefix + ".svg");
        try (BufferedWriter w = Files.newBufferedWriter(folded)) {
            for (var e : new TreeMap<>(counter).entrySet()) {
                if (e.getValue() > 0) {
                    w.write(e.getKey() + " " + e.getValue());
                    w.newLine();
                }
            }
        }
        if (!runFile(
                List.of(
                        "perl",
                        a.fg.resolve("flamegraph.pl").toString(),
                        "--title",
                        title,
                        "--countname",
                        unit,
                        "--colors",
                        color,
                        "--width",
                        "1600",
                        folded.toString()),
                svg,
                prefix.getParent().resolve("render.log"))) {
            Files.deleteIfExists(svg);
            throw new IOException("FlameGraph failed for " + prefix + "; see render.log");
        }
    }

    record Wait(long start, String state, String key,
                Long wake) {}

    static final class Scheduler {
        final Result result;
        final boolean withStacks;
        final long t0, t1;
        final Map<Long, String> names = new HashMap<>();
        final Set<Long> tids = new HashSet<>();
        final Map<Long, Wait> pending = new HashMap<>();
        final Map<Long, Long> running = new HashMap<>();
        final Map<String, Long> totals = new TreeMap<>(), details = new TreeMap<>();
        final List<List<?>> intervals = new ArrayList<>();

        Scheduler(Result r, boolean stacks) {
            result = r;
            withStacks = stacks;
            t0 = r.t0();
            t1 = r.t1();
            tids.add(r.pid());
            if (r.m.get("threads") instanceof Map<?, ?> map) {
                map.forEach((k, v) -> {
                    long id = Long.parseLong(k.toString());
                    names.put(id, v.toString());
                    tids.add(id);
                });
            }
        }

        void discover(Sample s) {
            if (s.pid == result.pid()) {
                tids.add(s.tid);
                names.put(s.tid, s.comm);
            }
        }

        void add(long tid, long start, long end,
                 String state, String key, boolean censored) {
            long lo = Math.max(start, t0), hi = Math.min(end, t1);
            if (hi <= lo) {
                return;
            }
            long dt = hi - lo;
            String name = sanitize(names.getOrDefault(tid, Long.toString(tid)));
            inc(totals, name + " [" + tid + "];" + state, dt);
            if (!state.equals("on-cpu") && key != null) {
                inc(details, state + (censored ? " [right-censored]" : "") + ";" + key, dt);
            }
            intervals.add(List.of(tid, name, state, (lo - t0) / 1e6,
                    (hi - t0) / 1e6, dt / 1e6, censored));
        }

        void endWait(long tid, long end, boolean censored) {
            Wait w = pending.remove(tid);
            if (w == null) {
                return;
            }
            if (w.state.startsWith("R")) {
                add(tid, w.start, end, "runnable", w.key, censored);
            } else if (w.wake != null) {
                add(tid, w.start, w.wake, "blocked", w.key, false);
                add(tid, w.wake, end, "runnable", w.key, censored);
            } else {
                add(tid, w.start, end, "off-cpu (no wakeup observed)", w.key, censored);
            }
        }

        void sample(Sample s) {
            long t = s.ns;
            if (t < t0 || t > t1) {
                return;
            }
            if (s.event.equals("sched:sched_switch")) {
                Matcher m = SWITCH.matcher(s.payload);
                if (!m.find()) {
                    return;
                }
                long prev = Long.parseLong(m.group(1)), next = Long.parseLong(m.group(3));
                String state = m.group(2);
                if (tids.contains(prev)) {
                    Long start = running.remove(prev);
                    if (start != null) {
                        add(prev, start, t, "on-cpu", null, false);
                    }
                    if (!Set.of("X", "Z", "x").contains(state)) {
                        pending.put(
                                prev,
                                new Wait(t, state, withStacks ? stackKey(s, frames(s), true) : null,
                                        null));
                    }
                }
                if (tids.contains(next)) {
                    endWait(next, t, false);
                    running.put(next, t);
                }
            } else if (Set.of("sched:sched_wakeup", "sched:sched_wakeup_new").contains(s.event)) {
                Matcher m = WAKE.matcher(s.payload);
                if (m.find()) {
                    long tid = Long.parseLong(m.group(1));
                    Wait w = pending.get(tid);
                    if (w != null && w.wake == null) {
                        pending.put(tid, new Wait(w.start, w.state, w.key, t));
                    }
                }
            }
        }

        void finish(Args a) throws Exception {
            for (long tid : List.copyOf(pending.keySet())) {
                endWait(tid, t1, true);
            }
            running.forEach((tid, start) -> add(tid, start, t1, "on-cpu", null, true));
            Path d = result.dir;
            writeCsv(
                    d.resolve("thread-intervals.csv"),
                    List.of("tid", "comm", "state", "start_ms", "end_ms", "duration_ms",
                            "right_censored"),
                    intervals);
            flame(a, totals, d.resolve("thread-state-time"), "Observed thread-state durations (not elapsed critical path)", "ns",
                    "aqua");
            flame(a, details, d.resolve("offcpu-time"), "Off-CPU durations by switch-out stack (includes runnable waits)", "ns",
                    "aqua");
        }
    }

    record Syscall(String name, long start) {}

    static final class SyscallTracker {
        final Map<Long, Syscall> pending = new HashMap<>();
        final List<List<?>> rows = new ArrayList<>();
        final long t0;

        SyscallTracker(long t0) {
            this.t0 = t0;
        }

        void sample(Sample s) {
            String ev = s.event;
            if (ev.startsWith("syscalls:sys_enter_")) {
                pending.put(s.tid, new Syscall(ev.substring("syscalls:sys_enter_".length()), s.ns));
            } else if (ev.startsWith("syscalls:sys_exit_")) {
                String name = ev.substring("syscalls:sys_exit_".length());
                Syscall prev = pending.remove(s.tid);
                if (prev != null && prev.name.equals(name) && prev.start <= s.ns) {
                    rows.add(List.of(s.tid, name, (prev.start - t0) / 1e6, (s.ns - prev.start) / 1e6));
                }
            }
        }

        void finish(Path d) throws IOException {
            writeCsv(d.resolve("syscall-latencies.csv"), List.of("tid", "syscall", "start_ms", "duration_ms"), rows);
        }
    }

    record Bin(String event, long index) {}

    static final class Rendering implements AutoCloseable {
        final Args a;
        final Result r;
        final long t0, t1, pid;
        final Map<String, Map<String, Long>> counters = new TreeMap<>(), threads = new TreeMap<>();
        final Map<Bin, Map<String, Long>> bins = new LinkedHashMap<>();
        final Map<String, BufferedWriter> eventFiles = new LinkedHashMap<>();
        final Scheduler scheduler;
        final SyscallTracker syscall;
        long samples, noStack, totalFrames, unknownFrames;
        Long execNs;

        Rendering(Args a, Result r) {
            this.a = a;
            this.r = r;
            t0 = r.t0();
            t1 = r.t1();
            pid = r.pid();
            scheduler = Set.of("sched", "sched-stacks").contains(r.mode()) ? new Scheduler(r, r.mode().equals("sched-stacks")) : null;
            syscall = r.mode().equals("syscalls") ? new SyscallTracker(t0) : null;
        }

        void sample(Sample s) throws Exception {
            if (scheduler != null) {
                scheduler.sample(s);
            }
            if (s.pid != pid || s.ns < t0 || s.ns > t1) {
                return;
            }
            if (syscall != null) {
                syscall.sample(s);
            }
            if (s.event.equals("sched:sched_process_exec") && execNs == null) {
                execNs = s.ns;
            }
            String ev = safeEvent(s.event);
            samples++;
            List<String> frames = frames(s);
            if (frames.isEmpty()) {
                noStack++;
            }
            totalFrames += frames.size();
            unknownFrames += frames.stream()
                                   .filter(f -> f.contains("[unknown]"))
                                   .count();
            String key = stackKey(s, frames, false), tk = stackKey(s, frames, true);
            inc(counter(counters, ev), key, 1);
            inc(counter(threads, ev), tk, 1);
            if (a.slices) {
                inc(counter(bins, new Bin(ev, (s.ns - t0) / (a.sliceMs * 1_000_000L))), key, 1);
            }
            BufferedWriter w = eventFiles.get(ev);
            if (w == null) {
                w = Files.newBufferedWriter(r.dir.resolve(ev + ".stacks"));
                eventFiles.put(ev, w);
            }
            String h = s.lines.getFirst(), needle = s.timestamp + ":";
            int i = h.indexOf(needle);
            w.write(h.substring(0, i)
                    + ts(s.ns - t0)
                    + ":"
                    + h.substring(i + needle.length()));
            w.newLine();
            for (int j = 1; j < s.lines.size(); j++) {
                w.write(s.lines.get(j));
                w.newLine();
            }
            w.newLine();
        }

        public void close() throws IOException {
            IOException failure = null;
            for (BufferedWriter w : eventFiles.values()) {
                try {
                    w.close();
                } catch (IOException e) {
                    failure = e;
                }
            }
            if (failure != null) {
                throw failure;
            }
        }

        void graphs() throws Exception {
            Path d = r.dir;
            String mode = r.mode();
            for (var e : counters.entrySet()) {
                String ev = e.getKey();
                Map<String, Long> c = e.getValue();
                if (mode.equals("cpu-no-stacks")) {
                    flame(a, c, d.resolve(ev + "-leaf-samples"), "CPU sampled leaf symbols (no call chains)", "samples",
                            "hot");
                } else if (!mode.equals("sched")) {
                    flame(a, c, d.resolve(ev + "-samples"), mode + ": " + ev + " sampled stacks",
                            "samples", "hot");
                    flame(a, threads.get(ev), d.resolve(ev + "-threads"),
                            mode + ": " + ev + " sampled stacks by thread", "samples", "hot");
                    if (a.slices) {
                        for (var bin : bins.entrySet()) {
                            Bin b = bin.getKey();
                            if (!b.event.equals(ev)) {
                                continue;
                            }
                            Path sub = d.resolve("slices");
                            Files.createDirectories(sub);
                            long ms = b.index * a.sliceMs;
                            flame(a, bin.getValue(), sub.resolve(String.format(ROOT, "%s-%05dms", ev, ms)),
                                    ev
                                            + ": "
                                            + ms
                                            + "–"
                                            + (ms + a.sliceMs)
                                            + " ms since gate release",
                                    "samples", "hot");
                        }
                    }
                    Files.copy(d.resolve(ev + ".stacks"),
                            a.out.resolve("flamescope").resolve(String.format(ROOT, "%s-run-%03d-%s.stacks", mode, r.run(), ev)), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            if (scheduler != null) {
                scheduler.finish(a);
            }
            if (syscall != null) {
                syscall.finish(d);
            }
        }
    }

    static void render(Args a, Result r) throws Exception {
        if (!r.ok() || Set.of("baseline", "stat").contains(r.mode())) {
            return;
        }
        Path d = r.dir, data = d.resolve("perf.data"), err = d.resolve("render.log"), raw = d.resolve("perf-script.txt");
        String mode = r.mode();
        boolean trace = Set.of("sched", "sched-stacks", "syscalls").contains(mode);
        String fields = "comm,pid,tid,cpu,time,event" + (trace ? ",trace" : "") + ",ip,sym,dso";
        if (!runFile(
                List.of(a.perf, "script", "-i", data.toString(), "--ns",
                        "--inline", "-F", fields),
                raw,
                err)) {
            throw new IOException("perf script failed");
        }
        Rendering rendering = new Rendering(a, r);
        try (rendering) {
            if (rendering.scheduler != null) {
                readSamples(raw, rendering.scheduler::discover);
            }
            readSamples(raw, rendering::sample);
        }
        rendering.graphs();
        String times = ts(r.t0()) + "," + ts(r.t1());
        List<String> warnings = new ArrayList<>();
        if (mode.equals("latency") && !runFile(
                List.of(a.perf, "report", "--stdio", "--latency", "-i",
                        data.toString(), "--time", times),
                d.resolve("latency-report.txt"),
                err)) {
            warnings.add("perf latency report failed; see render.log");
        }
        if (mode.equals("sched") && !runFile(
                List.of(
                        a.perf,
                        "sched",
                        "timehist",
                        "-i",
                        data.toString(),
                        "--pid",
                        Long.toString(r.pid()),
                        "-w",
                        "-M",
                        "--state",
                        "--time",
                        times),
                d.resolve("sched-timehist.txt"),
                err)) {
            warnings.add("perf sched timehist failed; see render.log");
        }
        Path checkLog = d.resolve("perf-check.log"), checkOut = d.resolve("perf-check.txt");
        if (!runFile(List.of(a.perf, "script", "-d", "-i", data.toString()), checkOut, checkLog)) {
            warnings.add("perf script -d failed; see perf-check.log");
        }
        Pattern lost = Pattern.compile("\\b(?:lost|drop(?:ped)?)\\b", Pattern.CASE_INSENSITIVE), nonzero = Pattern.compile("[1-9]\\d*");
        // Inspect both channels; perf versions print diagnostics in different places.
        boolean lostWarning = false;
        for (Path path : List.of(checkLog, checkOut)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.startsWith("$") && lost.matcher(line).find() && nonzero.matcher(line).find()) {
                        lostWarning = true;
                        break;
                    }
                }
            }
        }
        if (rendering.samples == 0) {
            warnings.add("no application samples (zero events or unsupported format); inspect raw trace");
        }
        if (lostWarning) {
            warnings.add("possible lost events, inspect perf-check.log/perf-check.txt");
        }
        Map<String, Long> events = new TreeMap<>();
        rendering.counters.forEach((k, v) -> events.put(k, sum(v)));
        put(
                r.m,
                "samples",
                rendering.samples,
                "no_stack_samples",
                rendering.noStack,
                "events",
                events,
                "total_frames",
                rendering.totalFrames,
                "unknown_frames",
                rendering.unknownFrames,
                "exec_ns",
                rendering.execNs,
                "exec_from_gate_ms",
                rendering.execNs == null ? null : (rendering.execNs - r.t0()) / 1e6,
                "render_warning",
                String.join("; ", warnings));
    }

    static List<Path> glob(Path dir, String pattern) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, pattern)) {
            List<Path> out = new ArrayList<>();
            for (Path p : ds) {
                out.add(p);
            }
            return out;
        }
    }

    static void addFolded(Map<String, Map<String, Long>> groups, Path f, String prefix) throws IOException {
        String filename = f.getFileName().toString();
        Map<String, Long> c = counter(groups, prefix + filename.substring(0, filename.length() - ".folded".length()));
        try (BufferedReader r = Files.newBufferedReader(f)) {
            String line;
            while ((line = r.readLine()) != null) {
                int i = line.lastIndexOf(' ');
                inc(c, line.substring(0, i), Long.parseLong(line.substring(i + 1)));
            }
        }
    }

    static void aggregate(Args a, List<Result> results) throws Exception {
        for (String mode : a.modes) {
            Path md = a.out.resolve(mode);
            Map<String, Map<String, Long>> groups = new TreeMap<>();
            for (Result r : results) {
                if (!r.mode().equals(mode) || !r.ok() || r.m.containsKey("render_error")) {
                    continue;
                }
                for (Path f : glob(r.dir, "*-samples.folded")) {
                    addFolded(groups, f, "");
                }
                for (Path f : glob(r.dir.resolve("slices"), "*.folded")) {
                    addFolded(groups, f, "slice-");
                }
            }
            for (var e : groups.entrySet()) {
                flame(a, e.getValue(), md.resolve("combined-" + e.getKey()),
                        mode + ": combined " + e.getKey() + " (all successful runs)", "samples", "hot");
            }
        }
    }

    static String csv(Object value) {
        String s = value == null ? "" : value.toString();
        return s.matches("(?s).*[\",\r\n].*") ? "\"" + s.replace("\"", "\"\"") + "\"" : s;
    }

    static void writeCsv(Path p, List<String> header,
                         List<? extends List<?>> rows)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(p)) {
            w.write(String.join(",", header));
            w.newLine();
            for (List<?> row : rows) {
                w.write(String.join(",",
                        row.stream()
                           .map(ProfileStartup::csv)
                           .toList()));
                w.newLine();
            }
        }
    }

    static List<Double> numbers(List<Result> rows, String key) {
        return rows.stream()
                   .map(r -> r.m.get(key))
                   .filter(Number.class::isInstance)
                   .map(n -> ((Number) n).doubleValue())
                   .sorted()
                   .toList();
    }

    static Double median(List<Double> v) {
        int n = v.size();
        return n == 0
                ? null
                : n % 2 == 1 ? v.get(n / 2) : (v.get(n / 2 - 1) + v.get(n / 2)) / 2;
    }

    static Object round(Double n) {
        return n == null ? "" : Math.round(n * 1000) / 1000.0;
    }

    static void summaries(Args a, List<Result> results) throws IOException {
        List<String> fields = List.of(
                "mode",
                "run",
                "status",
                "ttfr_ms",
                "quarkus_ms",
                "rss_kib",
                "attempts",
                "pid",
                "t0_ns",
                "t1_ns",
                "perf_rc",
                "samples",
                "no_stack_samples",
                "total_frames",
                "unknown_frames",
                "exec_from_gate_ms",
                "error",
                "render_error",
                "render_warning");
        List<List<?>> rows = new ArrayList<>();
        for (Result r : results) {
            rows.add(fields.stream()
                           .map(r.m::get)
                           .toList());
        }
        writeCsv(a.out.resolve("measurements.csv"), fields, rows);
        rows.clear();
        Double base = median(numbers(results.stream()
                .filter(r -> r.ok() && r.mode().equals("baseline"))
                .toList(),
                "ttfr_ms"));
        for (String mode : a.modes) {
            List<Result> ok = results.stream()
                    .filter(r -> r.ok() && r.mode().equals(mode))
                    .toList();
            List<Double> tt = numbers(ok, "ttfr_ms");
            Double med = median(tt);
            long failed = results.stream()
                                 .filter(r -> !r.ok() && r.mode().equals(mode))
                                 .count();
            long samples = ok.stream()
                             .mapToLong(r -> ((Number) r.m.getOrDefault("samples", 0)).longValue())
                             .sum();
            rows.add(
                    List.of(
                            mode,
                            ok.size(),
                            failed,
                            round(med),
                            round(tt.isEmpty() ? null : tt.get((int) Math.ceil(tt.size() * .95) - 1)),
                            round(median(numbers(ok, "quarkus_ms"))),
                            round(median(numbers(ok, "rss_kib"))),
                            round(med == null || base == null || base == 0
                                    ? null
                                    : (med / base - 1) * 100),
                            samples));
        }
        writeCsv(
                a.out.resolve("summary.csv"),
                List.of("mode", "successful_runs", "failed_runs", "ttfr_median_ms", "ttfr_p95_ms", "quarkus_median_ms",
                        "rss_median_kib", "ttfr_overhead_pct_vs_baseline", "samples_total"),
                rows);
    }

    // Small dependency-free JSON serializer for our own metadata types only.
    static String jsonString(String s) {
        StringBuilder b = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 32) {
                        b.append(String.format(ROOT, "\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }

    static String json(Object v, int depth) {
        if (v == null) {
            return "null";
        }
        if (v instanceof Number || v instanceof Boolean) {
            return v.toString();
        }
        if (v instanceof Map<?, ?> m) {
            if (m.isEmpty()) {
                return "{}";
            }
            List<String> parts = new ArrayList<>();
            for (var e : m.entrySet()) {
                parts.add("  ".repeat(depth + 1)
                        + jsonString(e.getKey().toString())
                        + ": "
                        + json(e.getValue(), depth + 1));
            }
            return "{\n"
                    + String.join(",\n", parts)
                    + "\n"
                    + "  ".repeat(depth)
                    + "}";
        }
        if (v instanceof Collection<?> c) {
            return "[" + String.join(", ",
                    c.stream()
                     .map(x -> json(x, depth + 1))
                     .toList())
                    + "]";
        }
        return jsonString(v.toString());
    }

    static void writeJson(Path p, Map<String, Object> m) throws IOException {
        Files.writeString(p, json(m, 0) + "\n");
    }

    static int run(String[] argv) throws Exception {
        // Help is a harness option only, not a workload argument.
        for (String s : argv) {
            if (s.equals("--")) {
                break;
            }
            if (s.equals("--help") || s.equals("-h")) {
                help();
                return 0;
            }
        }
        Args a = Args.parse(argv);
        if (!System.getProperty("os.name").equals("Linux") || !Set.of("amd64", "x86_64", "aarch64", "riscv64").contains(System.getProperty("os.arch"))) {
            throw new IllegalArgumentException("Linux x86-64, AArch64 or RISC-V64 required");
        }
        List<String> required = new ArrayList<>(List.of(a.cc, "curl"));
        if (a.modes.stream().anyMatch(m -> !m.equals("baseline"))) {
            required.add(a.perf);
        }
        if (a.appCpus != null) {
            required.add("taskset");
        }
        for (String s : required) {
            if (!existsCommand(s)) {
                throw new IOException(s + " not found");
            }
        }
        if (!Files.isDirectory(a.captureDir)) {
            throw new IOException("capture dir missing: " + a.captureDir);
        }
        Files.createDirectories(a.out.getParent());
        Files.createDirectory(a.out);
        Files.createDirectory(a.out.resolve("flamescope"));
        if (!a.noRender && a.modes.stream().anyMatch(m -> !Set.of("baseline", "stat").contains(m))) {
            cloneTools(a);
        }
        Path exe = makeGate(a);
        Map<String, Object> metadata = a.metadata();
        put(
                metadata,
                "cwd",
                Path.of("").toAbsolutePath(),
                "kernel",
                System.getProperty("os.version"),
                "profiler_java_version",
                Runtime.version().toString(),
                "profiler_java_vm",
                System.getProperty("java.vm.name"));
        if (existsCommand(a.perf)) {
            try {
                metadata.put("perf_version", output(List.of(a.perf, "version")));
            } catch (IOException e) {
                metadata.put("perf_version", "unknown");
            }
        }
        for (String key : List.of("perf_event_paranoid", "perf_event_mlock_kb", "perf_event_max_sample_rate",
                "perf_cpu_time_max_percent", "kptr_restrict")) {
            try {
                metadata.put(key, Files.readString(Path.of("/proc/sys/kernel", key))
                        .strip());
            } catch (IOException ignored) {
            }
        }
        writeJson(a.out.resolve("config.json"), metadata);
        List<Result> results = new ArrayList<>();
        List<String> ordered = new ArrayList<>();
        if (a.modes.contains("baseline")) {
            ordered.add("baseline");
        }
        for (String mode : a.modes) {
            if (!mode.equals("baseline")) {
                ordered.add(mode);
            }
        }
        Set<String> failedSetup = new HashSet<>();
        Thread hook = new Thread(() -> {
            Process c = activeCurl;
            if (c != null) {
                c.destroyForcibly();
            }
            Process p = activeProcess;
            Long pid = activePid;
            if (p != null) {
                signalApp(p, pid, false);
                try {
                    if (!p.waitFor(2, TimeUnit.SECONDS)) {
                        signalTree(p.toHandle(), true);
                    }
                } catch (InterruptedException ignored) {
                }
            }
        },
                "profiler-cleanup");
        Runtime.getRuntime().addShutdownHook(hook);
        try (Linux linux = new Linux()) {
            // Initialize libc downcall machinery before the first measured release.
            Path warmup = Files.createTempDirectory(a.captureDir, "qstartup-warmup-");
            try (Linux.Fifo fifo = linux.fifo(warmup.resolve("fifo"))) {
                for (int i = 0; i < 16; i++) {
                    linux.mono();
                    fifo.write("x");
                    if (!fifo.read().equals("x") || !fifo.read().isEmpty()) {
                        throw new IOException("FIFO warmup failed");
                    }
                }
            } finally {
                deleteTree(warmup);
            }
            // Initialize subprocess launch/capture machinery before timing the first run.
            output(List.of("curl", "--version"));
            try {
                for (int n = 1; n <= a.runs; n++) {
                    int shift = (n - 1) % ordered.size();
                    for (int i = 0; i < ordered.size(); i++) {
                        String mode = ordered.get((i + shift) % ordered.size());
                        if (failedSetup.contains(mode)) {
                            continue;
                        }
                        sleep(a.cooldown);
                        Result r = capture(a, linux, mode, n, exe);
                        results.add(r);
                        summaries(a, results);
                        if (!r.ok() && r.m.get("t0_ns") == null) {
                            failedSetup.add(mode);
                            System.out.println("  skipping further " + mode + " runs after setup failure; see " + r.dir.resolve("perf.log"));
                            if (mode.equals("baseline")) {
                                throw new IOException("baseline setup failed");
                            }
                        }
                    }
                }
            } finally {
                summaries(a, results);
            }
        } finally {
            Runtime.getRuntime().removeShutdownHook(hook);
        }
        if (!a.noRender) {
            for (Result r : results) {
                try {
                    render(a, r);
                } catch (Exception e) {
                    r.m.put("render_error", e.toString());
                    System.err.println("Render failed for " + r.dir + ": " + e);
                }
                writeJson(r.dir.resolve("measurement.json"), r.m);
            }
            summaries(a, results);
            aggregate(a, results);
        }
        System.out.println("Output: " + a.out);
        if (!a.noRender && !glob(a.out.resolve("flamescope"), "*.stacks").isEmpty()) {
            System.out.println("FlameScope: " + a.out.resolve("view-flamescope.sh"));
        }
        return results.stream().anyMatch(r -> !r.ok() || r.m.containsKey("render_error")) ? 1 : 0;
    }

    public static void main(String[] args) {
        try {
            System.exit(run(args));
        } catch (Exception e) {
            System.err.println("Error: " + e);
            System.exit(1);
        }
    }
}

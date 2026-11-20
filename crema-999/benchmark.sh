#!/usr/bin/env bash
#
# Benchmark Quarkus startup: custom libjvm.so (Crema) vs plain OpenJDK 25.
#
# Measures:
#   1. Quarkus-reported startup time
#   2. Time to first HTTP request (independent measurement)
#   3. RSS after first HTTP request (total + per-region breakdown on last run)
#
# Usage:
#   ./benchmark.sh [runs]  # default: 5 runs per configuration
#
set -euo pipefail

RUNS=${1:-5}
QUARKUS_JAR="getting-started/target/quarkus-app/quarkus-run.jar"
URL="http://localhost:8080/hello"
EXPECTED="hello"
TIMEOUT_SECS=60

CREMA_JAVA="$GRAALVM_HOME/bin/java"
PLAIN_JAVA="/usr/lib/jvm/java-25-openjdk/bin/java"

# Kill anything on port 8080
kill_port() {
    local pids
    pids=$(lsof -t -i:8080 2>/dev/null) || true
    if [ -n "$pids" ]; then
        kill -9 $pids 2>/dev/null || true
        sleep 1
    fi
}

# Collect RSS region breakdown from /proc/PID/status
collect_rss_regions() {
    local pid=$1
    local label=$2
    echo "=== RSS regions for $label (PID $pid) ==="
    grep -E 'VmPeak|VmSize|VmRSS|RssAnon|RssFile|RssShmem|VmSwap' /proc/$pid/status 2>/dev/null || echo "  (unavailable)"
}

# Collect libjvm.so virtual memory regions from /proc/PID/maps
collect_libjvm_regions() {
    local pid=$1
    local label=$2
    echo "=== libjvm.so virtual regions for $label (PID $pid) ==="
    if grep -q 'libjvm.so' /proc/$pid/maps 2>/dev/null; then
        grep 'libjvm.so' /proc/$pid/maps 2>/dev/null | python3 -c "
import sys
total = 0
for line in sys.stdin:
    parts = line.split()
    addrs, perms = parts[0], parts[1]
    start, end = addrs.split('-')
    size = int(end, 16) - int(start, 16)
    total += size
    print(f'  {perms} {size/1024/1024:8.1f} MB  ({addrs})')
print(f'  Total: {total/1024/1024:.1f} MB virtual')
" 2>/dev/null
    else
        echo "  (no libjvm.so mapped)"
    fi
}

benchmark_one() {
    local java_bin="$1"
    local label="$2"
    local extra_args="${3:-}"
    local is_last_run="${4:-false}"
    local LOG_FILE
    LOG_FILE=$(mktemp /tmp/quarkus-bench-XXXXXX.log)

    local START_NS
    START_NS=$(date +%s%N)

    if [ -n "$extra_args" ]; then
        $java_bin $extra_args -jar "$QUARKUS_JAR" > "$LOG_FILE" 2>&1 &
    else
        $java_bin -jar "$QUARKUS_JAR" > "$LOG_FILE" 2>&1 &
    fi
    local JAVA_PID=$!

    local RESPONSE=""
    for i in $(seq 1 $((TIMEOUT_SECS * 100))); do
        RESPONSE=$(curl -s "$URL" 2>/dev/null) || true
        if [ "$RESPONSE" = "$EXPECTED" ]; then
            local END_NS
            END_NS=$(date +%s%N)
            local ELAPSED_MS=$(( (END_NS - START_NS) / 1000000 ))

            local RSS_KB
            RSS_KB=$(ps -o rss= -p $JAVA_PID 2>/dev/null || echo 0)
            local RSS_MB=$(( RSS_KB / 1024 ))

            local QUARKUS_TIME
            QUARKUS_TIME=$(grep -oP 'started in \K[0-9.]+s' "$LOG_FILE" 2>/dev/null | head -1 || echo "N/A")

            echo "$label: quarkus=${QUARKUS_TIME} ttfr=${ELAPSED_MS}ms rss=${RSS_MB}MB"

            # On last run, collect detailed memory info before killing
            if [ "$is_last_run" = "true" ]; then
                echo ""
                collect_rss_regions $JAVA_PID "$label"
                echo ""
                collect_libjvm_regions $JAVA_PID "$label"
                echo ""
            fi

            kill $JAVA_PID 2>/dev/null || true
            wait $JAVA_PID 2>/dev/null || true
            rm -f "$LOG_FILE"
            return 0
        fi
        sleep 0.01
    done

    echo "$label: FAILED (timeout)"
    kill -9 $JAVA_PID 2>/dev/null || true
    wait $JAVA_PID 2>/dev/null || true
    rm -f "$LOG_FILE"
    return 1
}

run_config() {
    local config_label="$1"
    local java_bin="$2"
    local extra_args="${3:-}"

    echo "--- $config_label ---"
    for i in $(seq 1 $RUNS); do
        kill_port
        sleep 1
        local is_last="false"
        if [ "$i" -eq "$RUNS" ]; then
            is_last="true"
        fi
        benchmark_one "$java_bin" "${config_label}-${i}" "$extra_args" "$is_last"
    done
    echo ""
}

echo "=== Quarkus Startup Benchmark ==="
echo "Runs per configuration: $RUNS"
echo "libjvm.so: $(ls -lh $GRAALVM_HOME/lib/svm/libjvm.so 2>/dev/null | awk '{print $5}')"
echo ""

run_config "crema" "$CREMA_JAVA" "-svm"
run_config "openjdk" "$PLAIN_JAVA" ""

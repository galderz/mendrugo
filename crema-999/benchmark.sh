#!/usr/bin/env bash
#
# Benchmark Quarkus startup: custom libjvm.so (Crema) vs plain OpenJDK 25.
#
# Measures:
#   1. Quarkus-reported startup time
#   2. Time to first HTTP request (independent measurement)
#   3. RSS after first HTTP request (total + per-region breakdown on last run)
#
# Computes median values for Quarkus startup, TTFR, and RSS across runs.
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

# Collect RSS region breakdown from /proc/PID/status (in MB)
collect_rss_regions() {
    local pid=$1
    local label=$2
    echo "=== RSS regions for $label (PID $pid) ==="
    if [ -f "/proc/$pid/status" ]; then
        grep -E 'VmPeak|VmSize|VmRSS|RssAnon|RssFile|RssShmem|VmSwap' /proc/$pid/status 2>/dev/null | \
            awk '{
                val_kb = $2;
                unit = $3;
                if (unit == "kB") {
                    val_mb = val_kb / 1024;
                    if (val_mb >= 1024) {
                        printf "  %-12s %8.1f GB\n", $1, val_mb / 1024;
                    } else {
                        printf "  %-12s %8.0f MB\n", $1, val_mb;
                    }
                } else {
                    printf "  %-12s %s %s\n", $1, $2, $3;
                }
            }'
    else
        echo "  (unavailable)"
    fi
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
            # Strip trailing 's' for numeric collection
            local QUARKUS_NUM
            QUARKUS_NUM=$(echo "$QUARKUS_TIME" | sed 's/s$//')

            echo "$label: quarkus=${QUARKUS_TIME} ttfr=${ELAPSED_MS}ms rss=${RSS_MB}MB"

            # Record values for median calculation (written to temp files by caller)
            echo "$QUARKUS_NUM" >> "${QUARKUS_FILE:-/dev/null}"
            echo "$ELAPSED_MS" >> "${TTFR_FILE:-/dev/null}"
            echo "$RSS_MB" >> "${RSS_FILE:-/dev/null}"

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

    # Create temp files for median collection
    export QUARKUS_FILE=$(mktemp /tmp/bench-quarkus-XXXXXX)
    export TTFR_FILE=$(mktemp /tmp/bench-ttfr-XXXXXX)
    export RSS_FILE=$(mktemp /tmp/bench-rss-XXXXXX)

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

    # Compute and display medians
    local med_quarkus med_ttfr med_rss
    med_quarkus=$(grep -v 'N/A' "$QUARKUS_FILE" 2>/dev/null | sort -n | awk '{a[NR]=$1} END { if (NR==0) {print "N/A"; exit} if (NR%2==1) {printf "%.3f", a[(NR+1)/2]} else {printf "%.3f", (a[NR/2]+a[NR/2+1])/2}}')
    med_ttfr=$(sort -n "$TTFR_FILE" 2>/dev/null | awk '{a[NR]=$1} END { if (NR==0) {print "N/A"; exit} if (NR%2==1) {print a[(NR+1)/2]} else {printf "%.0f", (a[NR/2]+a[NR/2+1])/2}}')
    med_rss=$(sort -n "$RSS_FILE" 2>/dev/null | awk '{a[NR]=$1} END { if (NR==0) {print "N/A"; exit} if (NR%2==1) {print a[(NR+1)/2]} else {printf "%.0f", (a[NR/2]+a[NR/2+1])/2}}')

    echo "  Median ($RUNS runs): quarkus=${med_quarkus}s ttfr=${med_ttfr}ms rss=${med_rss}MB"
    echo ""

    rm -f "$QUARKUS_FILE" "$TTFR_FILE" "$RSS_FILE"
}

echo "=== Quarkus Startup Benchmark ==="
echo "Runs per configuration: $RUNS"
echo "libjvm.so: $(ls -lh $GRAALVM_HOME/lib/svm/libjvm.so 2>/dev/null | awk '{print $5}')"
echo ""

run_config "crema" "$CREMA_JAVA" "-svm"
run_config "openjdk" "$PLAIN_JAVA" ""

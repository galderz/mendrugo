#!/usr/bin/env bash
#
# Benchmark Quarkus startup: custom libjvm.so (Crema) vs plain OpenJDK 25.
#
# Measures:
#   1. Quarkus-reported startup time
#   2. Time to first HTTP request (independent measurement)
#   3. RSS after first HTTP request
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

benchmark_one() {
    local java_bin="$1"
    local label="$2"
    local extra_args="${3:-}"
    local LOG_FILE
    LOG_FILE=$(mktemp /tmp/quarkus-bench-XXXXXX.log)

    # Start Quarkus
    local START_NS
    START_NS=$(date +%s%N)

    if [ -n "$extra_args" ]; then
        $java_bin $extra_args -jar "$QUARKUS_JAR" > "$LOG_FILE" 2>&1 &
    else
        $java_bin -jar "$QUARKUS_JAR" > "$LOG_FILE" 2>&1 &
    fi
    local JAVA_PID=$!

    # Poll for first HTTP response
    local RESPONSE=""
    local ELAPSED_MS=0
    for i in $(seq 1 $((TIMEOUT_SECS * 100))); do
        RESPONSE=$(curl -s "$URL" 2>/dev/null) || true
        if [ "$RESPONSE" = "$EXPECTED" ]; then
            local END_NS
            END_NS=$(date +%s%N)
            ELAPSED_MS=$(( (END_NS - START_NS) / 1000000 ))
            break
        fi
        sleep 0.01
    done

    if [ "$RESPONSE" != "$EXPECTED" ]; then
        echo "$label: FAILED (timeout)"
        kill -9 $JAVA_PID 2>/dev/null || true
        wait $JAVA_PID 2>/dev/null || true
        rm -f "$LOG_FILE"
        return 1
    fi

    # Get RSS (in KB)
    local RSS_KB
    RSS_KB=$(ps -o rss= -p $JAVA_PID 2>/dev/null || echo 0)

    # Get Quarkus-reported startup time
    local QUARKUS_TIME
    QUARKUS_TIME=$(grep -oP 'started in \K[0-9.]+s' "$LOG_FILE" 2>/dev/null | head -1 || echo "N/A")

    # Shutdown
    kill $JAVA_PID 2>/dev/null || true
    wait $JAVA_PID 2>/dev/null || true

    local RSS_MB=$(( RSS_KB / 1024 ))

    echo "$label: quarkus=${QUARKUS_TIME} ttfr=${ELAPSED_MS}ms rss=${RSS_MB}MB"

    rm -f "$LOG_FILE"
}

echo "=== Quarkus Startup Benchmark ==="
echo "Runs per configuration: $RUNS"
echo ""

# Warm up: make sure nothing is listening on port 8080
kill $(lsof -t -i:8080 2>/dev/null) 2>/dev/null || true
sleep 1

echo "--- Crema (custom libjvm.so with -svm) ---"
for i in $(seq 1 $RUNS); do
    sleep 1
    benchmark_one "$CREMA_JAVA" "crema-$i" "-svm"
done

echo ""
echo "--- Plain OpenJDK 25 ---"
for i in $(seq 1 $RUNS); do
    sleep 1
    benchmark_one "$PLAIN_JAVA" "openjdk-$i"
done

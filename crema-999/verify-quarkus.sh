#!/usr/bin/env bash
#
# Verify that Quarkus starts under Crema, responds to /hello, then shut it down.
#
# Usage:
#   ./verify-quarkus.sh
#
# Exit codes:
#   0 - success
#   1 - startup timeout or /hello check failed
#
set -euo pipefail

QUARKUS_JAR="getting-started/target/quarkus-app/quarkus-run.jar"
TIMEOUT_SECS=60
URL="http://localhost:8080/hello"
EXPECTED="hello"
LOG_FILE=$(mktemp /tmp/quarkus-verify-XXXXXX.log)

echo "=== Starting Quarkus under Crema ==="
$GRAALVM_HOME/bin/java -svm -jar "$QUARKUS_JAR" > "$LOG_FILE" 2>&1 &
PID=$!

cleanup() {
    kill -9 "$PID" 2>/dev/null || true
    wait "$PID" 2>/dev/null || true
    rm -f "$LOG_FILE"
}
trap cleanup EXIT

echo "Waiting for Quarkus to start (timeout: ${TIMEOUT_SECS}s)..."
for i in $(seq 1 "$TIMEOUT_SECS"); do
    RESPONSE=$(curl -s "$URL" 2>/dev/null) || true
    if [ "$RESPONSE" = "$EXPECTED" ]; then
        echo "=== /hello responded: \"$RESPONSE\" ==="
        STARTUP_TIME=$(grep -o 'started in [0-9.]*s' "$LOG_FILE" 2>/dev/null || true)
        if [ -n "$STARTUP_TIME" ]; then
            echo "=== Quarkus $STARTUP_TIME ==="
        fi
        echo "=== SUCCESS ==="
        exit 0
    fi
    sleep 1
done

echo "=== FAILED: Quarkus did not respond within ${TIMEOUT_SECS}s ==="
echo "=== Log output ==="
cat "$LOG_FILE"
exit 1

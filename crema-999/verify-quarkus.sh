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

echo "=== Starting Quarkus under Crema ==="
$GRAALVM_HOME/bin/java -svm -jar "$QUARKUS_JAR" &
PID=$!

cleanup() {
    echo "=== Shutting down Quarkus (PID $PID) ==="
    kill "$PID" 2>/dev/null || true
    wait "$PID" 2>/dev/null || true
}
trap cleanup EXIT

echo "Waiting for Quarkus to start (timeout: ${TIMEOUT_SECS}s)..."
for i in $(seq 1 "$TIMEOUT_SECS"); do
    RESPONSE=$(curl -s "$URL" 2>/dev/null) || true
    if [ "$RESPONSE" = "$EXPECTED" ]; then
        echo "=== /hello responded: \"$RESPONSE\" ==="

        # Extract startup time from process output
        STARTUP_TIME=$(grep -o 'started in [0-9.]*s' /proc/$PID/fd/2 2>/dev/null || true)
        if [ -n "$STARTUP_TIME" ]; then
            echo "=== Quarkus $STARTUP_TIME ==="
        fi

        echo "=== SUCCESS ==="
        exit 0
    fi
    sleep 1
done

echo "=== FAILED: Quarkus did not respond within ${TIMEOUT_SECS}s ==="
exit 1

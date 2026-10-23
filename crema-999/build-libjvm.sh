#!/usr/bin/env bash
#
# Build libjvm.so with Quarkus boot jar dependencies pre-compiled.
#
# This script rebuilds $GRAALVM_HOME/lib/svm/libjvm.so with additional
# Quarkus dependencies included via -cp and -H:Preserve, which should
# improve Crema startup time by avoiding JIT compilation of those classes.
#
# Usage:
#   ./build-libjvm.sh
#
# After building, test with:
#   $GRAALVM_HOME/bin/java -svm -jar getting-started/target/quarkus-app/quarkus-run.jar
#   curl http://localhost:8080/hello
#
set -ex

BOOT_DIR="getting-started/target/quarkus-app/lib/boot"

# ──────────────────────────────────────────────────────────────────────
# Jars added to BOTH classpath and Preserve (AOT compiled into libjvm.so)
# ──────────────────────────────────────────────────────────────────────
PRESERVED_JARS=(
    "$BOOT_DIR/io.smallrye.common.smallrye-common-cpu-2.20.0.jar"
    "$BOOT_DIR/io.smallrye.common.smallrye-common-os-2.20.0.jar"
    # NOTE: ref needs constraint, constraint needs jboss-logging,
    # jboss-logging needs log4j/slf4j (not available) for Preserve.
    # So ref and everything downstream is excluded.
    #"$BOOT_DIR/io.smallrye.common.smallrye-common-ref-2.20.0.jar"
    #"$BOOT_DIR/io.smallrye.common.smallrye-common-constraint-2.20.0.jar"
    #"$BOOT_DIR/io.smallrye.common.smallrye-common-function-2.20.0.jar"
    #"$BOOT_DIR/io.smallrye.common.smallrye-common-expression-2.20.0.jar"
)

# ──────────────────────────────────────────────────────────────────────
# Jars added to classpath only (NOT Preserved — needed for linking
# but have unresolvable transitive dependencies)
# ──────────────────────────────────────────────────────────────────────
CP_ONLY_JARS=(
    # Currently empty. jboss-logging was removed because:
    # - Cannot be Preserved (references log4j/slf4j not on CP)
    # - Cannot be CP-only (crashes at runtime: "Unable to call AOT method")
    # Adding it requires either:
    #   a) Providing stub jars for log4j/slf4j so linking succeeds
    #   b) A finer-grained Preserve mechanism to exclude specific classes
)

# ──────────────────────────────────────────────────────────────────────
# Build classpath and Preserve args
# ──────────────────────────────────────────────────────────────────────
CP_ARGS=""
PRESERVE_ARGS=()

for jar in "${PRESERVED_JARS[@]}"; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
    PRESERVE_ARGS+=("-H:Preserve=path=$jar")
done

for jar in "${CP_ONLY_JARS[@]}"; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
done

# ──────────────────────────────────────────────────────────────────────
# Classes that must be initialized at build time
# ──────────────────────────────────────────────────────────────────────
INIT_BUILD=(
    "io.smallrye.common.cpu"
    "io.smallrye.common.os"
)

INIT_BUILD_ARG=$(IFS=,; echo "${INIT_BUILD[*]}")

# ──────────────────────────────────────────────────────────────────────
# Classes that must be deferred to run time
# ──────────────────────────────────────────────────────────────────────
INIT_RUN=(
    # Calls native Process APIs
    "io.smallrye.common.os.Process"
)

INIT_RUN_ARG=$(IFS=,; echo "${INIT_RUN[*]}")

# ──────────────────────────────────────────────────────────────────────
# Build libjvm.so
# ──────────────────────────────────────────────────────────────────────
${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -H:Preserve=package=sun.misc \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}"

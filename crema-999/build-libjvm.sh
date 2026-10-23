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
#   ./verify-quarkus.sh
#
set -ex

BOOT_DIR="getting-started/target/quarkus-app/lib/boot"
MAIN_DIR="getting-started/target/quarkus-app/lib/main"
OPT_DIR="getting-started/target/quarkus-app/lib/optional"

# ──────────────────────────────────────────────────────────────────────
# Download optional dependency jars (needed for linking only)
# ──────────────────────────────────────────────────────────────────────
mkdir -p "$OPT_DIR"

if [ ! -f "$OPT_DIR/log4j-1.2.17.jar" ]; then
    echo "Downloading log4j 1.x..."
    curl -sL "https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar" \
        -o "$OPT_DIR/log4j-1.2.17.jar"
fi

if [ ! -f "$OPT_DIR/log4j-api-2.24.3.jar" ]; then
    echo "Downloading log4j 2.x API..."
    curl -sL "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar" \
        -o "$OPT_DIR/log4j-api-2.24.3.jar"
fi

if [ ! -f "$OPT_DIR/jboss-modules-2.1.5.Final.jar" ]; then
    echo "Downloading jboss-modules..."
    curl -sL "https://repo1.maven.org/maven2/org/jboss/modules/jboss-modules/2.1.5.Final/jboss-modules-2.1.5.Final.jar" \
        -o "$OPT_DIR/jboss-modules-2.1.5.Final.jar"
fi

# ──────────────────────────────────────────────────────────────────────
# Jars added to BOTH classpath and Preserve (AOT compiled into libjvm.so)
#
# NOTE: quarkus-bootstrap-runner is excluded because it uses
# getProtectionDomain().getCodeSource().getLocation() to find
# quarkus-application.dat at runtime. When baked into libjvm.so,
# the code source points to the native-image build dir instead.
# ──────────────────────────────────────────────────────────────────────
PRESERVED_JARS=()
SKIP_PRESERVE="io.quarkus.quarkus-bootstrap-runner"

for jar in "$BOOT_DIR"/*.jar; do
    bn=$(basename "$jar")
    if echo "$bn" | grep -q "$SKIP_PRESERVE"; then
        echo "Skipping Preserve (uses getCodeSource path): $bn"
        continue
    fi
    PRESERVED_JARS+=("$jar")
done

# Main jars needed as transitive deps (also Preserved)
PRESERVED_JARS+=(
    "$MAIN_DIR/jakarta.json.jakarta.json-api-2.1.3.jar"
    "$MAIN_DIR/org.slf4j.slf4j-api-2.0.19.jar"
    "$MAIN_DIR/io.smallrye.common.smallrye-common-search-2.20.0.jar"
)

# ──────────────────────────────────────────────────────────────────────
# Jars on classpath only (NOT Preserved)
#
# These are optional dependencies that jboss-logging and jboss-logmanager
# reference but are never called at runtime (Quarkus uses jboss-logmanager,
# not log4j). They exist only to satisfy the --link-at-build-time
# requirement from the jvm-library macro.
# ──────────────────────────────────────────────────────────────────────
CP_ONLY_JARS=(
    "$OPT_DIR/log4j-1.2.17.jar"
    "$OPT_DIR/log4j-api-2.24.3.jar"
    "$OPT_DIR/jboss-modules-2.1.5.Final.jar"
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
    "io.smallrye.common"
    "io.quarkus"
    "org.crac"
    "org.jboss.logging"
    "org.jboss.logmanager"
    "org.jboss.modules"
    "org.apache.log4j"
    "org.apache.logging.log4j"
    "org.slf4j"
    "jakarta.json"
)

INIT_BUILD_ARG=$(IFS=,; echo "${INIT_BUILD[*]}")

# ──────────────────────────────────────────────────────────────────────
# Classes that must be deferred to run time
# ──────────────────────────────────────────────────────────────────────
INIT_RUN=(
    # smallrye: native/host-specific data
    "io.smallrye.common.os.Process"
    "io.smallrye.common.net"
    "io.smallrye.common.io.Files2"
    "io.smallrye.common.io.Files2\$PrivateAttrs"
    # Creates a jboss Logger at class init time
    "io.smallrye.common.function.FunctionsLogging"
    # logmanager: native/optional deps
    "org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "org.jboss.logmanager.handlers.SyslogHandler"
    "org.jboss.logmanager.configuration.ObjectBuilder\$ModuleFinder\$Holder"
    "org.jboss.logmanager.formatters.JsonFormatter"
    # log4j 1.x: subpackages with native/external deps (JMS, JDBC, NT, Swing)
    "org.apache.log4j.net"
    "org.apache.log4j.jdbc"
    "org.apache.log4j.nt"
    "org.apache.log4j.or.jms"
    "org.apache.log4j.chainsaw"
    "org.apache.log4j.lf5"
    "org.apache.log4j.jmx"
)

INIT_RUN_ARG=$(IFS=,; echo "${INIT_RUN[*]}")

# ──────────────────────────────────────────────────────────────────────
# Build libjvm.so
# ──────────────────────────────────────────────────────────────────────
${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -H:Preserve=package=sun.misc \
    -H:-DetectUserDirectoriesInImageHeap \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}"

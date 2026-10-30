#!/usr/bin/env bash
#
# Strip SPI service files from jboss-logmanager and jboss-modules jars
# that cause their LogManager/LoggerFinder implementations to be
# auto-discovered during native-image build.
#
# Removed files:
#   jboss-logmanager: META-INF/services/java.util.logging.LogManager
#                     META-INF/services/java.lang.System$LoggerFinder
#   jboss-modules:    META-INF/services/java.lang.System$LoggerFinder
#
# This prevents JBoss LogManager and JBossLoggerFinder from being selected
# at build time via SPI, avoiding image heap conflicts with run-time init.
#
# Quarkus runtime is NOT affected because it sets the system property
# java.util.logging.manager=org.jboss.logmanager.LogManager explicitly
# in QuarkusEntryPoint.main(), which is how LogManager selection works
# at runtime (system property, not SPI).
#
# NOTE: With this change, Quarkus starts and responds to HTTP but produces
# no log output (no banner, no startup time). This is under investigation —
# the JDK default LogManager gets baked into the image heap at build time,
# and Quarkus's System.setProperty() call at runtime comes too late since
# LogManager.<clinit> has already run in the image.
#
# Usage:
#   ./strip-spi-files.sh
#
# To restore originals:
#   ./strip-spi-files.sh --restore
#
set -euo pipefail

BOOT_DIR="getting-started/target/quarkus-app/lib/boot"
OPT_DIR="getting-started/target/quarkus-app/lib/optional"

LOGMANAGER_JAR="$BOOT_DIR/org.jboss.logmanager.jboss-logmanager-3.2.2.Final.jar"
MODULES_JAR="$OPT_DIR/jboss-modules-2.1.5.Final.jar"

strip_from_jar() {
    local jar="$1"
    shift
    local files=("$@")

    local tmpdir
    tmpdir=$(mktemp -d)

    # Back up original if not already backed up
    if [ ! -f "${jar}.bak" ]; then
        cp "$jar" "${jar}.bak"
        echo "  Backed up: ${jar}.bak"
    fi

    # Extract, remove files, repackage
    (cd "$tmpdir" && jar xf "$(realpath "$jar")")
    for f in "${files[@]}"; do
        rm -f "$tmpdir/$f"
    done
    (cd "$tmpdir" && jar cf "$(realpath "$jar")" .)
    rm -rf "$tmpdir"
}

if [ "${1:-}" = "--restore" ]; then
    echo "Restoring original jars..."
    for jar in "$LOGMANAGER_JAR" "$MODULES_JAR"; do
        if [ -f "${jar}.bak" ]; then
            cp "${jar}.bak" "$jar"
            echo "  Restored: $jar"
        else
            echo "  No backup for: $jar"
        fi
    done
    exit 0
fi

echo "Stripping SPI files from jboss-logmanager..."
strip_from_jar "$LOGMANAGER_JAR" \
    "META-INF/services/java.util.logging.LogManager" \
    "META-INF/services/java.lang.System\$LoggerFinder"

echo "Stripping SPI files from jboss-modules..."
strip_from_jar "$MODULES_JAR" \
    "META-INF/services/java.lang.System\$LoggerFinder"

echo ""
echo "Verify:"
echo "  jboss-logmanager services:"
jar tf "$LOGMANAGER_JAR" | grep "META-INF/services/" || echo "    (none)"
echo "  jboss-modules services:"
jar tf "$MODULES_JAR" | grep "META-INF/services/" || echo "    (none)"

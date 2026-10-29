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

if [ ! -f "$OPT_DIR/osgi.core-5.0.0.jar" ]; then
    echo "Downloading osgi core..."
    curl -sL "https://repo1.maven.org/maven2/org/osgi/osgi.core/5.0.0/osgi.core-5.0.0.jar" \
        -o "$OPT_DIR/osgi.core-5.0.0.jar"
fi

# Additional optional deps needed when adding main jars (not yet used):
# javax.jms-api-2.0.1.jar       - log4j -> or.jms.MessageRenderer
# javax.mail-api-1.6.2.jar       - log4j -> net.SMTPAppender
# jboss-marshalling-osgi-2.1.4.Final.jar - netty -> codec-marshalling
# protobuf-java-4.29.3.jar       - netty -> codec-protobuf
# protobuf-javanano-3.1.0.jar    - netty -> codec-protobuf (nano)
# lz4-java-1.8.0.jar             - netty -> codec-compression (Lz4)
# zstd-jni-1.5.6-8.jar           - netty -> codec-compression (Zstd)
# compress-lzf-1.1.2.jar         - netty -> codec-compression (LZF)
# lzma-java-1.3.jar              - netty -> codec-compression (LZMA)
# asm-9.7.1.jar                  - smallrye-config -> ConfigMappingGenerator
# jackson-core-2.18.3.jar        - vertx-core -> json.jackson (Jackson 2.x compat)
# jackson-databind-2.18.3.jar    - vertx-core -> json.jackson
# conscrypt-openjdk-uber-2.5.2.jar - netty -> handler.ssl
# bcpkix-jdk18on-1.80.jar        - smallrye-certs -> X509 cert builder
# bcprov-jdk18on-1.80.jar        - BouncyCastle provider
# bcutil-jdk18on-1.80.jar        - BouncyCastle util
# jzlib-1.1.3.jar                - netty -> codec-compression (JZlib)

# ──────────────────────────────────────────────────────────────────────
# Jars added to BOTH classpath and Preserve (AOT compiled into libjvm.so)
#
# All 16 boot jars are now included. The quarkus-bootstrap-runner was
# previously excluded because getCodeSource() returned the java binary
# path when AOT-compiled. Fixed in Quarkus by adding a .jar suffix check
# in resolveJarPath() and falling through to getResource() fallback.
# ──────────────────────────────────────────────────────────────────────
PRESERVED_JARS=()

for jar in "$BOOT_DIR"/*.jar; do
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
    "$OPT_DIR/osgi.core-5.0.0.jar"
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
#    "io.smallrye.common"
#    "io.quarkus"
#    "org.crac"
#    "org.jboss.logging"
#    "org.jboss.logmanager"
#    "org.jboss.modules"
#    "org.apache.log4j"
#    "org.apache.logging.log4j"
#    "org.slf4j"
#    "jakarta.json"
    "jdk.internal.jrtfs"
    # JBoss System Logger initialized by Native Image itself
    "org.jboss.logmanager.JBossLoggerFinder"
    "org.jboss.logmanager.JBossLoggerFinder\$JBossSystemLogger"
    "org.jboss.logmanager.Level"
    "org.jboss.logmanager.LogManager"
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
    # Graphics, Fonts...etc
    "com.sun.imageio"
    "com.sun.java.swing"
    "java.awt"
    "javax.imageio"
    "javax.swing"
    "sun.awt"
    "sun.datatransfer"
    "sun.java2d"
    "sun.font"
    "sun.swing"
    # JPackage
    #"jdk.jpackage.internal"
    #"jdk.jpackage.main"
    "jdk.jpackage"
    # XML
    "org.jcp.xml.dsig.internal.dom"
    # Logging
    "java.io.ObjectInputStream\$Logging"
    # "java.util.logging.LogManager" # Can't be runtime init due to JBoss LogManager build time init, see above
)

INIT_RUN_ARG=$(IFS=,; echo "${INIT_RUN[*]}")

# ──────────────────────────────────────────────────────────────────────
# Build libjvm.so
# ──────────────────────────────────────────────────────────────────────
${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -H:Preserve=package=sun.misc \
    -H:-AssertInitializationSpecifiedForAllClasses \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    --trace-object-instantiation=org.jboss.logging.JBossLogManagerLogger \
    --trace-object-instantiation=sun.awt.SunHints\$LCDContrastKey \
    --trace-object-instantiation=sun.java2d.HeadlessGraphicsEnvironment \
    --trace-object-instantiation=sun.awt.HeadlessToolkit \
    --trace-object-instantiation=java.awt.font.FontRenderContext \
    --trace-object-instantiation=sun.awt.datatransfer.DesktopDatatransferServiceImpl \
    --trace-object-instantiation=java.awt.color.ICC_ColorSpace \
    --trace-object-instantiation=java.util.PropertyResourceBundle \
    --trace-object-instantiation=jdk.internal.jrtfs.SystemImage\$1 \
    --trace-object-instantiation=com.sun.tools.javac.parser.Tokens\$TokenKind \
    --trace-object-instantiation=org.jboss.logmanager.JBossLoggerFinder\$JBossSystemLogger \
    --trace-object-instantiation=org.jboss.logmanager.Level \
    --trace-object-instantiation=org.jboss.logmanager.JBossLoggerFinder \
    --trace-object-instantiation=org.jboss.logmanager.LogManager \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}"

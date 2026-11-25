#!/usr/bin/env bash
#
# Build libjvm.so with ALL Quarkus framework jars (boot + main) pre-compiled.
#
# This script rebuilds $GRAALVM_HOME/lib/svm/libjvm.so with Quarkus framework
# dependencies included via -cp and -H:Preserve, improving Crema startup time
# by avoiding JIT compilation of those classes.
#
# Since GraalVM PR #14901, --link-at-build-time issues should be resolved,
# so optional dependency jars should no longer be needed.
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

# ──────────────────────────────────────────────────────────────────────
# Jars added to BOTH classpath and Preserve (AOT compiled into libjvm.so)
#
# All boot jars (16) + all main jars (97) = 113 jars total.
# These are all Quarkus framework jars; the only jar NOT included is
# quarkus-run.jar (the application entry point).
# ──────────────────────────────────────────────────────────────────────
PRESERVED_JARS=()

for jar in "$BOOT_DIR"/*.jar; do
    PRESERVED_JARS+=("$jar")
done

for jar in "$MAIN_DIR"/*.jar; do
    PRESERVED_JARS+=("$jar")
done

# ──────────────────────────────────────────────────────────────────────
# Build classpath and Preserve args
# ──────────────────────────────────────────────────────────────────────
CP_ARGS=""
PRESERVE_ARGS=()

for jar in "${PRESERVED_JARS[@]}"; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
    PRESERVE_ARGS+=("-H:Preserve=path=$jar")
done

echo "Total jars preserved: ${#PRESERVED_JARS[@]}"

# ──────────────────────────────────────────────────────────────────────
# Classes that must be initialized at build time
#
# Using -H:-AssertInitializationSpecifiedForAllClasses so unspecified
# classes default to run-time initialization. Only packages/classes
# that need build-time init are listed here.
# ──────────────────────────────────────────────────────────────────────
INIT_BUILD=(
    # JDK classes needed at build time
    "jdk.internal.jrtfs"
    "jdk.nio.zipfs.ZipFileSystemProvider"
    "sun.nio.fs.LinuxFileSystemProvider"
    "sun.nio.fs.LinuxFileSystem"
    # JBoss logging/logmanager (needed for Quarkus logging at build time)
    "org.jboss.logging"
    "org.jboss.logmanager"
    # Quarkus initial logging configurator (referenced by LogContext)
    "io.quarkus.bootstrap.logging"
    # MDC provider (referenced by org.jboss.logmanager.MDC)
    "io.quarkus.vertx.mdc.provider"
    # smallrye ref types (referenced via logmanager LoggerNode)
    "io.smallrye.common.ref"
    # Application framework packages
    "io.smallrye"
    "io.quarkus"
    "org.crac"
    "io.netty"
    "io.vertx"
    "org.eclipse.microprofile"
    "org.eclipse.parsson"
    "org.jctools"
    "org.jspecify"
    "org.reactivestreams"
    "org.wildfly.common"
    "org.slf4j"
    "jakarta"
    "tools.jackson"
    # RESTEasy Reactive, threads, logging bridges
    "org.jboss.resteasy.reactive"
    "org.jboss.threads"
    "mutiny.zero.flow.adapters"
    "org.apache.commons.logging"
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
    "io.smallrye.common.function.FunctionsLogging"
    # logmanager: native/optional deps
    "org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "org.jboss.logmanager.handlers.SyslogHandler"
    "org.jboss.logmanager.configuration.ObjectBuilder\$ModuleFinder\$Holder"
    "org.jboss.logmanager.formatters.JsonFormatter"
    # netty platform-specific / native
    "io.netty.channel.DefaultChannelId"
    "io.netty.channel.unix.Errors"
    "io.netty.channel.unix.FileDescriptor"
    "io.netty.channel.unix.IovArray"
    "io.netty.channel.unix.Limits"
    "io.netty.handler.codec.ReplayingDecoderByteBuf"
    "io.netty.internal.tcnative"
    "io.netty.util.AbstractReferenceCounted"
    "io.netty.util.NetUtil"
    "io.netty.util.internal.CleanerJava24Linker"
    "io.netty.util.internal.CleanerJava24Linker\$Holder"
    "io.netty.util.internal.PlatformDependent"
    "io.netty.util.internal.PlatformDependent0"
    "io.netty.handler.codec.compression.Brotli"
    "io.netty.handler.codec.compression.BrotliOptions"
    "io.netty.handler.codec.compression.BrotliDecompressor"
    "io.netty.handler.codec.compression.ZstdConstants"
    "io.netty.handler.codec.compression.ZstdOptions"
    "io.netty.handler.codec.http.HttpContentCompressor"
    "io.netty.handler.codec.http.HttpServerExpectContinueHandler"
    "io.netty.handler.codec.http.HttpObjectAggregator"
    "io.netty.handler.codec.http.HttpObjectEncoder"
    "io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder"
    "io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder"
    "io.netty.handler.codec.http2"
    "io.netty.handler.codec.quic"
    "io.netty.handler.ssl"
    "io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder"
    "io.netty.pkitesting"
    "io.netty.channel.kqueue"
    "io.netty.channel.uring"
    # quarkus netty
    "io.quarkus.netty.runtime.EmptyByteBufStub"
    # vertx runtime-specific
    "io.vertx.core.impl.buffer.VertxByteBufAllocator"
    "io.vertx.core.parsetools.impl"
    "io.vertx.core.internal.tls.SslContextManager"
    "io.vertx.core.http.impl.http2"
    "io.vertx.core.http.impl.http3.Http3Stream"
    "io.vertx.core.net.impl.quic"
    "io.vertx.core.file.FileSystemOptions"
    "io.vertx.core.eventbus.impl.clustered"
    "io.vertx.core.http.impl.ClientMultipartFormUpload"
    "io.vertx.core.http.impl.Http1xServerResponse"
    "io.vertx.core.http.impl.VertxHttp2ClientUpgradeCodec"
    "io.vertx.core.http.impl.http1.Http1ServerResponse"
    "io.vertx.core.http.impl.tcp.VertxHttp2ClientUpgradeCodec"
    "io.vertx.ext.auth.impl.jose.JWT"
    "io.vertx.ext.web.handler.sockjs.impl.XhrTransport"
    # quarkus runtime
    "io.quarkus.runtime.configuration.RuntimeConfigBuilder\$UuidConfigSource\$Holder"
    "io.quarkus.runtime.graal.InetRunTime"
    "io.quarkus.runtime.ExecutorRecorder"
    # jboss threads
    "org.jboss.threads.JDKSpecific\$ThreadAccess"
    # jakarta
    "jakarta.el.ELManager"
    "jakarta.activation.MailcapCommandMap"
    "jakarta.activation.MimetypesFileTypeMap"
    # parsson
    "org.eclipse.parsson.JsonMessages"
    # vertx buffer allocator
    "io.quarkus.vertx.utils.AppendBuffer"
    # Graphics, Fonts, etc
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
    "jdk.jpackage"
    # XML
    "org.jcp.xml.dsig.internal.dom"
    # Logging
    "java.io.ObjectInputStream\$Logging"
)

INIT_RUN_ARG=$(IFS=,; echo "${INIT_RUN[*]}")

# ──────────────────────────────────────────────────────────────────────
# Build libjvm.so
# ──────────────────────────────────────────────────────────────────────
source "$(dirname "$0")/build-summary.sh"

${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -H:Preserve=package=sun.misc \
    -H:-AssertInitializationSpecifiedForAllClasses \
    -H:-DetectUserDirectoriesInImageHeap \
    -J-Xmx16g \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}" \
    2>&1 | print_build_summary

#!/usr/bin/env bash
#
# Build libjvm.so with ALL Quarkus framework jars, but MINIMAL build-time init.
#
# This variant keeps only JDK classes and logging at build-time init.
# All application framework packages default to run-time initialization.
# Compare startup metrics with build-libjvm.sh to measure the impact.
#
# Usage:
#   ./build-libjvm-minimal-init.sh
#
# After building, test with:
#   ./verify-quarkus.sh
#
set -ex

BOOT_DIR="getting-started/target/quarkus-app/lib/boot"
MAIN_DIR="getting-started/target/quarkus-app/lib/main"

# ──────────────────────────────────────────────────────────────────────
# Jars: same as build-libjvm.sh (all 113 framework jars)
# ──────────────────────────────────────────────────────────────────────
PRESERVED_JARS=()

for jar in "$BOOT_DIR"/*.jar; do
    PRESERVED_JARS+=("$jar")
done

for jar in "$MAIN_DIR"/*.jar; do
    PRESERVED_JARS+=("$jar")
done

CP_ARGS=""
PRESERVE_ARGS=()

for jar in "${PRESERVED_JARS[@]}"; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
    PRESERVE_ARGS+=("-H:Preserve=path=$jar")
done

echo "Total jars preserved: ${#PRESERVED_JARS[@]}"

# ──────────────────────────────────────────────────────────────────────
# MINIMAL build-time init: only JDK classes and logging chain
#
# Everything else defaults to run-time init via
# -H:-AssertInitializationSpecifiedForAllClasses
# ──────────────────────────────────────────────────────────────────────
INIT_BUILD=(
    # JDK classes that ended up in the image heap
    "jdk.internal.jrtfs"
    "jdk.nio.zipfs.ZipFileSystemProvider"
    "sun.nio.fs.LinuxFileSystemProvider"
    "sun.nio.fs.LinuxFileSystem"
    # Logging chain (required for Quarkus logging to work at startup)
    "org.jboss.logging"
    "org.jboss.logmanager"
    "io.quarkus.bootstrap.logging"
    "io.quarkus.vertx.mdc.provider"
    "io.smallrye.common.ref"
    # Pulled in by logging chain (SLF4J bridge, jboss-modules ContentHandlerFactory)
    "org.jboss.modules"
    "org.slf4j"
    "org.jboss.slf4j"
    "org.apache.commons.logging"
)

INIT_BUILD_ARG=$(IFS=,; echo "${INIT_BUILD[*]}")

# ──────────────────────────────────────────────────────────────────────
# Run-time init: only override for classes whose build-time init
# would fail (within the packages above) or JDK classes that need
# explicit deferral.
# ──────────────────────────────────────────────────────────────────────
INIT_RUN=(
    # logmanager classes that reference native/optional deps
    "org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "org.jboss.logmanager.handlers.SyslogHandler"
    "org.jboss.logmanager.configuration.ObjectBuilder\$ModuleFinder\$Holder"
    "org.jboss.logmanager.formatters.JsonFormatter"
    # smallrye.common.ref is build-time, but FunctionsLogging creates Logger at <clinit>
    "io.smallrye.common.function.FunctionsLogging"
    # netty: native code, platform-specific, compression (same as full script)
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
    # smallrye: native/host-specific data
    "io.smallrye.common.os.Process"
    "io.smallrye.common.net"
    "io.smallrye.common.io.Files2"
    "io.smallrye.common.io.Files2\$PrivateAttrs"
    # quarkus runtime
    "io.quarkus.runtime.configuration.RuntimeConfigBuilder\$UuidConfigSource\$Holder"
    "io.quarkus.runtime.graal.InetRunTime"
    "io.quarkus.runtime.ExecutorRecorder"
    "io.quarkus.netty.runtime.EmptyByteBufStub"
    "io.quarkus.vertx.utils.AppendBuffer"
    # jboss threads
    "org.jboss.threads.JDKSpecific\$ThreadAccess"
    # jakarta
    "jakarta.el.ELManager"
    "jakarta.activation.MailcapCommandMap"
    "jakarta.activation.MimetypesFileTypeMap"
    # parsson
    "org.eclipse.parsson.JsonMessages"
    # JDK GUI subsystem
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
${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -H:Preserve=package=sun.misc \
    -H:-AssertInitializationSpecifiedForAllClasses \
    -H:-DetectUserDirectoriesInImageHeap \
    -J-Xmx16g \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}"

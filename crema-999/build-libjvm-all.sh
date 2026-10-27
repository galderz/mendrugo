#!/usr/bin/env bash
#
# Build libjvm.so with ALL boot + main jar dependencies pre-compiled.
#
# This is the experimental "full build" script that attempts to include
# all main jars. It currently FAILS due to a split package issue where
# native-image's NativeImageClassLoader silently drops jars that create
# split packages (see PROGRESS.md for details).
#
# The working boot-only build is in build-libjvm.sh.
#
# Usage:
#   ./build-libjvm-all.sh
#
set -ex

BOOT_DIR="getting-started/target/quarkus-app/lib/boot"
MAIN_DIR="getting-started/target/quarkus-app/lib/main"
OPT_DIR="getting-started/target/quarkus-app/lib/optional"

# ──────────────────────────────────────────────────────────────────────
# Download all optional dependency jars (needed for linking only)
# ──────────────────────────────────────────────────────────────────────
mkdir -p "$OPT_DIR"

declare -A DEPS=(
    # jboss-logging optional backends
    ["log4j-1.2.17.jar"]="https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar"
    ["log4j-api-2.24.3.jar"]="https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.24.3/log4j-api-2.24.3.jar"
    # jboss-logmanager optional dep
    ["jboss-modules-2.1.5.Final.jar"]="https://repo1.maven.org/maven2/org/jboss/modules/jboss-modules/2.1.5.Final/jboss-modules-2.1.5.Final.jar"
    # log4j optional deps
    ["javax.jms-api-2.0.1.jar"]="https://repo1.maven.org/maven2/javax/jms/javax.jms-api/2.0.1/javax.jms-api-2.0.1.jar"
    ["javax.mail-api-1.6.2.jar"]="https://repo1.maven.org/maven2/javax/mail/javax.mail-api/1.6.2/javax.mail-api-1.6.2.jar"
    # netty codec deps
    ["jboss-marshalling-osgi-2.1.4.Final.jar"]="https://repo1.maven.org/maven2/org/jboss/marshalling/jboss-marshalling-osgi/2.1.4.Final/jboss-marshalling-osgi-2.1.4.Final.jar"
    ["protobuf-java-4.29.3.jar"]="https://repo1.maven.org/maven2/com/google/protobuf/protobuf-java/4.29.3/protobuf-java-4.29.3.jar"
    ["protobuf-javanano-3.1.0.jar"]="https://repo1.maven.org/maven2/com/google/protobuf/nano/protobuf-javanano/3.1.0/protobuf-javanano-3.1.0.jar"
    # netty compression deps
    ["lz4-java-1.8.0.jar"]="https://repo1.maven.org/maven2/org/lz4/lz4-java/1.8.0/lz4-java-1.8.0.jar"
    ["zstd-jni-1.5.6-8.jar"]="https://repo1.maven.org/maven2/com/github/luben/zstd-jni/1.5.6-8/zstd-jni-1.5.6-8.jar"
    ["compress-lzf-1.1.2.jar"]="https://repo1.maven.org/maven2/com/ning/compress-lzf/1.1.2/compress-lzf-1.1.2.jar"
    ["lzma-java-1.3.jar"]="https://repo1.maven.org/maven2/com/github/jponge/lzma-java/1.3/lzma-java-1.3.jar"
    ["jzlib-1.1.3.jar"]="https://repo1.maven.org/maven2/com/jcraft/jzlib/1.1.3/jzlib-1.1.3.jar"
    # smallrye-config dep
    ["asm-9.7.1.jar"]="https://repo1.maven.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar"
    # vertx-core Jackson 2.x backward compat
    ["jackson-core-2.18.3.jar"]="https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-core/2.18.3/jackson-core-2.18.3.jar"
    ["jackson-databind-2.18.3.jar"]="https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.18.3/jackson-databind-2.18.3.jar"
    ["jackson-annotations-2.18.3.jar"]="https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.18.3/jackson-annotations-2.18.3.jar"
    # TLS/crypto deps
    ["conscrypt-openjdk-uber-2.5.2.jar"]="https://repo1.maven.org/maven2/org/conscrypt/conscrypt-openjdk-uber/2.5.2/conscrypt-openjdk-uber-2.5.2.jar"
    ["bcpkix-jdk18on-1.80.jar"]="https://repo1.maven.org/maven2/org/bouncycastle/bcpkix-jdk18on/1.80/bcpkix-jdk18on-1.80.jar"
    ["bcprov-jdk18on-1.80.jar"]="https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.80/bcprov-jdk18on-1.80.jar"
    ["bcutil-jdk18on-1.80.jar"]="https://repo1.maven.org/maven2/org/bouncycastle/bcutil-jdk18on/1.80/bcutil-jdk18on-1.80.jar"
    # netty SSL testing
    ["netty-pkitesting-4.2.18.Final.jar"]="https://repo1.maven.org/maven2/io/netty/netty-pkitesting/4.2.18.Final/netty-pkitesting-4.2.18.Final.jar"
    # vertx-web auth providers (optional deps of vertx-web)
    ["vertx-auth-jwt-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-jwt/5.1.8/vertx-auth-jwt-5.1.8.jar"
    ["vertx-auth-htdigest-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-htdigest/5.1.8/vertx-auth-htdigest-5.1.8.jar"
    ["vertx-auth-htpasswd-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-htpasswd/5.1.8/vertx-auth-htpasswd-5.1.8.jar"
    ["vertx-auth-oauth2-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-oauth2/5.1.8/vertx-auth-oauth2-5.1.8.jar"
    ["vertx-auth-otp-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-otp/5.1.8/vertx-auth-otp-5.1.8.jar"
    ["vertx-auth-ldap-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-ldap/5.1.8/vertx-auth-ldap-5.1.8.jar"
    ["vertx-auth-properties-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-properties/5.1.8/vertx-auth-properties-5.1.8.jar"
    ["vertx-auth-sql-client-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-sql-client/5.1.8/vertx-auth-sql-client-5.1.8.jar"
    ["vertx-auth-webauthn4j-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-auth-webauthn4j/5.1.8/vertx-auth-webauthn4j-5.1.8.jar"
    ["vertx-sql-client-5.1.8.jar"]="https://repo1.maven.org/maven2/io/vertx/vertx-sql-client/5.1.8/vertx-sql-client-5.1.8.jar"
    # webauthn4j deps
    ["webauthn4j-core-0.28.4.RELEASE.jar"]="https://repo1.maven.org/maven2/com/webauthn4j/webauthn4j-core/0.28.4.RELEASE/webauthn4j-core-0.28.4.RELEASE.jar"
    ["webauthn4j-core-async-0.28.4.RELEASE.jar"]="https://repo1.maven.org/maven2/com/webauthn4j/webauthn4j-core-async/0.28.4.RELEASE/webauthn4j-core-async-0.28.4.RELEASE.jar"
    ["webauthn4j-metadata-0.28.4.RELEASE.jar"]="https://repo1.maven.org/maven2/com/webauthn4j/webauthn4j-metadata/0.28.4.RELEASE/webauthn4j-metadata-0.28.4.RELEASE.jar"
    ["webauthn4j-metadata-async-0.28.4.RELEASE.jar"]="https://repo1.maven.org/maven2/com/webauthn4j/webauthn4j-metadata-async/0.28.4.RELEASE/webauthn4j-metadata-async-0.28.4.RELEASE.jar"
    # reactor blockhound
    ["blockhound-1.0.10.RELEASE.jar"]="https://repo1.maven.org/maven2/io/projectreactor/tools/blockhound/1.0.10.RELEASE/blockhound-1.0.10.RELEASE.jar"
    # XZ compression (netty)
    ["xz-1.10.jar"]="https://repo1.maven.org/maven2/org/tukaani/xz/1.10/xz-1.10.jar"
    # netty platform-specific transport classes
    ["netty-transport-classes-kqueue-4.2.18.Final.jar"]="https://repo1.maven.org/maven2/io/netty/netty-transport-classes-kqueue/4.2.18.Final/netty-transport-classes-kqueue-4.2.18.Final.jar"
    ["netty-transport-classes-io_uring-4.2.18.Final.jar"]="https://repo1.maven.org/maven2/io/netty/netty-transport-classes-io_uring/4.2.18.Final/netty-transport-classes-io_uring-4.2.18.Final.jar"
    ["netty-transport-classes-epoll-4.2.18.Final.jar"]="https://repo1.maven.org/maven2/io/netty/netty-transport-classes-epoll/4.2.18.Final/netty-transport-classes-epoll-4.2.18.Final.jar"
    # smallrye-mutiny wrapped vertx auth providers
    ["smallrye-mutiny-vertx-auth-htdigest-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-htdigest/4.0.2/smallrye-mutiny-vertx-auth-htdigest-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-htpasswd-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-htpasswd/4.0.2/smallrye-mutiny-vertx-auth-htpasswd-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-jwt-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-jwt/4.0.2/smallrye-mutiny-vertx-auth-jwt-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-oauth2-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-oauth2/4.0.2/smallrye-mutiny-vertx-auth-oauth2-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-otp-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-otp/4.0.2/smallrye-mutiny-vertx-auth-otp-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-ldap-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-ldap/4.0.2/smallrye-mutiny-vertx-auth-ldap-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-properties-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-properties/4.0.2/smallrye-mutiny-vertx-auth-properties-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-sql-client-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-sql-client/4.0.2/smallrye-mutiny-vertx-auth-sql-client-4.0.2.jar"
    ["smallrye-mutiny-vertx-auth-webauthn4j-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-auth-webauthn4j/4.0.2/smallrye-mutiny-vertx-auth-webauthn4j-4.0.2.jar"
    ["smallrye-mutiny-vertx-sql-client-4.0.2.jar"]="https://repo1.maven.org/maven2/io/smallrye/reactive/smallrye-mutiny-vertx-sql-client/4.0.2/smallrye-mutiny-vertx-sql-client-4.0.2.jar"
)

for name in "${!DEPS[@]}"; do
    if [ ! -f "$OPT_DIR/$name" ] || [ "$(stat -c%s "$OPT_DIR/$name" 2>/dev/null || echo 0)" -lt 1000 ]; then
        echo "Downloading $name..."
        curl -sL -o "$OPT_DIR/$name" "${DEPS[$name]}"
    fi
done

# Verify all are valid jars
for f in "$OPT_DIR"/*.jar; do
    size=$(stat -c%s "$f" 2>/dev/null || echo 0)
    if [ "$size" -lt 1000 ]; then
        echo "WARNING: Broken download ($size bytes): $(basename "$f")"
    fi
done

# ──────────────────────────────────────────────────────────────────────
# Build classpath and Preserve args
# ALL boot + ALL main jars are Preserved
# Optional jars are CP-only (for linking, never called at runtime)
# ──────────────────────────────────────────────────────────────────────
CP_ARGS=""
PRESERVE_ARGS=()

for jar in "$BOOT_DIR"/*.jar; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
    PRESERVE_ARGS+=("-H:Preserve=path=$jar")
done

for jar in "$MAIN_DIR"/*.jar; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
    PRESERVE_ARGS+=("-H:Preserve=path=$jar")
done

for jar in "$OPT_DIR"/*.jar; do
    CP_ARGS="${CP_ARGS:+$CP_ARGS:}$jar"
done

echo "Total jars on CP: $(echo "$CP_ARGS" | tr ':' '\n' | wc -l)"

# ──────────────────────────────────────────────────────────────────────
# Classes that must be initialized at build time
# ──────────────────────────────────────────────────────────────────────
INIT_BUILD=(
    # Application packages
    "io.smallrye"
    "io.quarkus"
    "org.crac"
    "org.jboss"
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
    "com.aayushatharva.brotli4j"
    "com.fasterxml.jackson"
    # Optional dep packages (on CP for linking)
    "org.apache.log4j"
    "org.apache.logging.log4j"
    "javax.jms"
    "javax.mail"
    "com.google.protobuf"
    "net.jpountz"
    "com.github.luben.zstd"
    "com.ning.compress"
    "org.objectweb.asm"
    "org.conscrypt"
    "org.bouncycastle"
    "com.jcraft.jzlib"
    "lzma.sdk"
    "com.webauthn4j"
    "reactor.blockhound"
    # JDK classes needed at build time
    "jdk.nio.zipfs.ZipFileSystemProvider"
    "sun.nio.fs.LinuxFileSystemProvider"
    "sun.nio.fs.LinuxFileSystem"
)

INIT_BUILD_ARG=$(IFS=,; echo "${INIT_BUILD[*]}")

# ──────────────────────────────────────────────────────────────────────
# Classes that must be deferred to run time
# ──────────────────────────────────────────────────────────────────────
INIT_RUN=(
    # smallrye: native/host-specific data
    "io.smallrye.common.os.Process"
    "io.smallrye.common.net.HostName"
    "io.smallrye.common.io.Files2"
    "io.smallrye.common.io.Files2\$PrivateAttrs"
    "io.smallrye.common.function.FunctionsLogging"
    "io.smallrye.common.classloader.ClassPathUtils"
    # logmanager: native/optional deps
    "org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "org.jboss.logmanager.handlers.SyslogHandler"
    "org.jboss.logmanager.configuration.ObjectBuilder\$ModuleFinder\$Holder"
    "org.jboss.logmanager.formatters.JsonFormatter"
    # log4j 1.x: subpackages with native/external deps
    "org.apache.log4j.net"
    "org.apache.log4j.jdbc"
    "org.apache.log4j.nt"
    "org.apache.log4j.or.jms"
    "org.apache.log4j.chainsaw"
    "org.apache.log4j.lf5"
    "org.apache.log4j.jmx"
    # netty (from layers-999/build-layer-base.sh)
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
    "io.netty.util.internal.PlatformDependent"
    "io.netty.util.internal.PlatformDependent0"
    "io.netty.handler.codec.compression.BrotliOptions"
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
    # vertx (from layers-999/build-layer-base.sh)
    "io.vertx.core.impl.buffer.VertxByteBufAllocator"
    "io.vertx.core.parsetools.impl.RecordParserImpl"
    "io.vertx.core.json.Json"
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
    # Classes that embed netty buffer allocators (CURRENT BLOCKER)
    # These classes reference AdaptiveByteBufAllocator which netty forces
    # to run-time init via its own native-image.properties. Deferring these
    # to run-time doesn't help because Preserve still parses their methods.
    "io.quarkus.vertx.utils.AppendBuffer"
    "org.jboss.resteasy.reactive.server.vertx.VertxResteasyReactiveRequestContext"
)

INIT_RUN_ARG=$(IFS=,; echo "${INIT_RUN[*]}")

# ──────────────────────────────────────────────────────────────────────
# Build libjvm.so
#
# KNOWN ISSUE: This build currently FAILS with:
#   java.lang.NoClassDefFoundError: io/vertx/mutiny/ext/auth/htdigest/HtdigestAuth
#
# Root cause: native-image's NativeImageClassLoader silently drops jars
# that create split packages. The smallrye-mutiny-vertx-auth-* optional
# jars share the io.vertx.mutiny.ext.auth package hierarchy with
# smallrye-mutiny-vertx-auth-common from main/, and get filtered out.
#
# Previous blockers resolved:
# - CidrAddress/Inet6Address: solved by quarkus-core GraalVM substitution
# - AdaptiveByteBufAllocator: solved by fixing wrong package name
#   (io.vertx.core.buffer.impl -> io.vertx.core.impl.buffer)
#
# See PROGRESS.md for details and possible solutions.
# ──────────────────────────────────────────────────────────────────────
${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -H:Preserve=package=sun.misc \
    -H:-DetectUserDirectoriesInImageHeap \
    -J-Xmx16g \
    -cp "$CP_ARGS" \
    "${PRESERVE_ARGS[@]}" \
    "--initialize-at-build-time=${INIT_BUILD_ARG}" \
    "--initialize-at-run-time=${INIT_RUN_ARG}"

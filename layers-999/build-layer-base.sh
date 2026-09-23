#!/usr/bin/env bash
set -eux

mkdir -p target

TRACE_ARGS=(
#    "--trace-object-instantiation=io.netty.buffer.EmptyByteBuf"
#    "--trace-object-instantiation=io.netty.handler.codec.compression.BrotliOptions"
#    "--trace-object-instantiation=io.netty.handler.codec.http2.EmptyHttp2Headers"
#    "--trace-object-instantiation=io.netty.handler.codec.quic.QuicCongestionControlAlgorithm"
#    "--trace-object-instantiation=io.netty.handler.ssl.ClientAuth"
)

RUN_INIT_BASE_ARGS=(
    "--initialize-at-run-time=io.netty.buffer"
    "--initialize-at-run-time=io.netty.channel.DefaultChannelId"
    "--initialize-at-run-time=io.netty.channel.unix.Errors"
    "--initialize-at-run-time=io.netty.channel.unix.FileDescriptor"
    "--initialize-at-run-time=io.netty.channel.unix.IovArray"
    "--initialize-at-run-time=io.netty.channel.unix.Limits"
    "--initialize-at-run-time=io.netty.handler.codec.ReplayingDecoderByteBuf"
    "--initialize-at-run-time=io.netty.internal.tcnative"
    "--initialize-at-run-time=io.netty.util.AbstractReferenceCounted"
    "--initialize-at-run-time=io.netty.util.NetUtil"
    "--initialize-at-run-time=io.netty.util.internal.PlatformDependent"
    "--initialize-at-run-time=io.netty.util.internal.PlatformDependent0"
    "--initialize-at-run-time=io.quarkus.netty.runtime.EmptyByteBufStub"
    "--initialize-at-run-time=io.quarkus.runtime.configuration.RuntimeConfigBuilder\$UuidConfigSource\$Holder"
    "--initialize-at-run-time=io.quarkus.runtime.graal.InetRunTime"
    "--initialize-at-run-time=io.quarkus.runtime.ExecutorRecorder"
    "--initialize-at-run-time=io.smallrye.common.os.Process"
    "--initialize-at-run-time=io.smallrye.common.net.HostName"
    "--initialize-at-run-time=io.vertx.core.buffer.impl.PartialPooledByteBufAllocator"
    "--initialize-at-run-time=io.vertx.core.buffer.impl.VertxByteBufAllocator"
    "--initialize-at-run-time=io.vertx.core.parsetools.impl.RecordParserImpl"
    "--initialize-at-run-time=jakarta.el.ELManager"
    "--initialize-at-run-time=java.rmi"
    "--initialize-at-run-time=java.util.logging.ConsoleHandler"
    "--initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch"
    "--initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch"
    "--initialize-at-run-time=jdk.package"
    "--initialize-at-run-time=jdk.tools.jlink.internal.plugins"
    "--initialize-at-run-time=org.jboss.threads.JDKSpecific\$ThreadAccess"
    "--initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "--initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler"
    "--initialize-at-run-time=sun.rmi"
)

RUN_INIT_FEATURE_ARGS=(
    # http / http1
    "--initialize-at-run-time=io.netty.handler.codec.http.HttpContentCompressor"
    "--initialize-at-run-time=io.netty.handler.codec.http.HttpServerExpectContinueHandler"
    "--initialize-at-run-time=io.netty.handler.codec.http.HttpObjectAggregator"
    "--initialize-at-run-time=io.netty.handler.codec.http.HttpObjectEncoder"
    "--initialize-at-run-time=io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder"
    "--initialize-at-run-time=io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder"
    "--initialize-at-run-time=io.vertx.core.http.impl.ClientMultipartFormUpload"
    "--initialize-at-run-time=io.vertx.core.http.impl.Http1xServerResponse"
    "--initialize-at-run-time=io.vertx.core.http.impl.VertxHttp2ClientUpgradeCodec"
    "--initialize-at-run-time=io.vertx.core.http.impl.http1.Http1ServerResponse"
    # http2
    "--initialize-at-run-time=io.netty.handler.codec.http2"
    "--initialize-at-run-time=io.vertx.core.http.impl.http2"
    # http3
    "--initialize-at-run-time=io.vertx.core.http.impl.http3.Http3Stream"
    # quick
    "--initialize-at-run-time=io.netty.handler.codec.quic"
    "--initialize-at-run-time=io.vertx.core.net.impl.quic"
    # clustered eventbus
    "--initialize-at-run-time=io.vertx.core.eventbus.impl.clustered"
    # ssl / tls
    "--initialize-at-run-time=io.netty.handler.ssl"
    "--initialize-at-run-time=io.vertx.core.internal.tls.SslContextManager"
    # pcap
    "--initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder"
    # jwt
    "--initialize-at-run-time=io.vertx.ext.auth.impl.jose.JWT"
    # compression
    "--initialize-at-run-time=io.netty.handler.codec.compression.BrotliOptions"
    "--initialize-at-run-time=io.netty.handler.codec.compression.ZstdConstants"
    "--initialize-at-run-time=io.netty.handler.codec.compression.ZstdOptions"
    # sockjs
    "--initialize-at-run-time=io.vertx.ext.web.handler.sockjs.impl.XhrTransport"
)

LAYER_ARGS=(
    "--initialize-at-build-time="
    "-H:+PrintClassInitialization"
    "-H:ApplicationLayerInitializedClasses=io.quarkus.arc.Arc"
    "-H:ApplicationLayerInitializedClasses=io.quarkus.smallrye.context.runtime.SmallRyeContextPropagationRecorder"
    "-H:ApplicationLayerInitializedClasses=io.quarkus.arc.runtime.ArcRecorder"
    "-H:BuildOutputJSONFile=target/build-output-layer-base.json"
    "-H:LayerCreate=libquarkusbaselayer.nil,module=java.base,module=jdk.localedata,package=io.quarkus.*,package=io.netty.*,package=io.vertx.*,package=jakarta.*"
    "-cp" "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*"
    "-o" "libquarkusbaselayer"
    "-H:Path=./target"
)

${GRAALVM_HOME}/bin/native-image \
    "${TRACE_ARGS[@]}" \
    "${RUN_INIT_BASE_ARGS[@]}" \
    "${RUN_INIT_FEATURE_ARGS[@]}" \
    "${LAYER_ARGS[@]}"

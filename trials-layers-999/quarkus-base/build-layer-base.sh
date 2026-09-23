#!/usr/bin/env bash
set -eux

native_image=$HOME/src/mandrel/sdk/latest_graalvm_home/bin/native-image

mkdir -p target

# Build package list from scanned packages
# packages=$(while IFS= read -r pkg; do printf ",package=%s.*" "$pkg"; done < target/layer-packages.txt)
packages=,package=io.quarkus.*,package=io.netty.*,package=io.vertx.*,package=jakarta.*

# getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/extracted-classes.jar \

# Add to enable remote debugging
# --debug-attach=*:8000 \

# Other flags:
#    --initialize-at-run-time=jdk.internal.jimage \

# --initialize-at-build-time=jdk.internal.jimage.ImageReader \
#     --initialize-at-build-time=jdk.internal.jimage.ImageReader\$SharedImageReader \
#     --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Directory \
#     --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Directory \

# --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \

# --trace-object-instantiation=jdk.internal.jimage.ImageReader \
#     --trace-object-instantiation=java.net.Inet4Address \
#     --trace-object-instantiation=java.security.SecureRandom \
# --trace-object-instantiation=jdk.internal.module.ServicesCatalog \
# --trace-object-instantiation=java.util.Random \

# --initialize-at-build-time=jdk.internal.jimage \

# --initialize-at-run-time=jdk.internal \

# --initialize-at-run-time=jdk.internal.jimage \
#     --initialize-at-run-time=jdk.internal.jrtfs \
#     --initialize-at-run-time=jdk.package \
#     --initialize-at-run-time=jdk.tools \

# --initialize-at-run-time=jdk.internal.jimage \
#     --initialize-at-run-time=jdk.internal.jrtfs \
#     --initialize-at-run-time=jdk.package \
#     --initialize-at-run-time=jdk.tools \

# --trace-object-instantiation=jdk.internal.jrtfs.JrtFileSystemProvider \

#--initialize-at-run-time=io.netty.channel.unix.Errors \
#    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor \
#    --initialize-at-run-time=io.netty.channel.unix.IovArray \
#    --initialize-at-run-time=io.netty.channel.unix.Limits \

#--trace-object-instantiation=jdk.internal.loader.ClassLoaders\$PlatformClassLoader \
#    --trace-object-instantiation=jdk.internal.loader.ClassLoaders\$AppClassLoader \
#    --initialize-at-run-time=jdk.internal.jimage \
#    --initialize-at-run-time=jdk.internal.jrtfs \
#    --initialize-at-run-time=jdk.internal.loader \
#    --initialize-at-run-time=jdk.package \
#    --initialize-at-run-time=jdk.tools \
#    --initialize-at-run-time=jdk.internal.module \
#    --initialize-at-run-time=java.lang.ModuleLayer.EMPTY_LAYER \

#--initialize-at-run-time=io.netty.channel \
#    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
#    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \

#--initialize-at-run-time=io.netty.channel.unix.Errors \
#    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor \
#    --initialize-at-run-time=io.netty.channel.unix.IovArray \
#    --initialize-at-run-time=io.netty.channel.unix.Limits \
#    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
#    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
#     -H:+PrintClassInitialization \

#--trace-object-instantiation=io.netty.buffer.UnpooledByteBufAllocator\$InstrumentedUnpooledUnsafeDirectByteBuf \
#    --trace-object-instantiation=io.vertx.core.buffer.impl.VertxUnsafeHeapByteBuf \
#    --trace-object-instantiation=java.net.NetworkInterface \

# io.netty.channel.unix.* runtime inits as per NettyProcessor.build()


# -J-Dsvm.traceClassInit=true \

#     --initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder \

#    --initialize-at-run-time=io.netty.buffer.PoolThreadCache \
#    --initialize-at-run-time=io.netty.buffer.AdvancedLeakAwareByteBuf \
#    --initialize-at-run-time=io.netty.channel.ChannelOutboundBuffer \
#    --initialize-at-run-time=io.netty.channel.PendingWriteQueue \
#    --initialize-at-run-time=io.netty.handler.codec.http2.DefaultHttp2Connection \
#    --initialize-at-run-time=io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder \
#    --initialize-at-run-time=io.netty.util.Recycler \
#    --initialize-at-run-time=io.netty.util.concurrent.DefaultPromise \
#    --initialize-at-run-time=io.netty.util.internal.logging.LocationAwareSlf4JLogger \
#    --initialize-at-run-time=io.quarkus.bootstrap.logging.InitialConfigurator \
#    --initialize-at-run-time=org.jboss.logmanager.LogContext \
#    --trace-object-instantiation=io.quarkus.bootstrap.logging.InitialConfigurator \
#    --trace-object-instantiation=org.jboss.logmanager.LogContext \
#    --trace-object-instantiation=io.netty.util.internal.logging.LocationAwareSlf4JLogger \
#    -J-Dsvm.traceClassInitDemotions=true \


# -J-Dlogging.initial-configurator.min-level=500 \

# -J-Dsvm.traceClassForName=true \
#    -J-Dsvm.traceClassInitDemotions=true \
#        -J-Dsvm.traceLayerTypes=true \

# -J-Dsvm.traceClassInit=true \

${native_image} \
    -J-Dquarkus.native.base-layer-build=true \
    --initialize-at-run-time=io.netty.channel.DefaultChannelId \
    --initialize-at-run-time=io.netty.channel.unix.Errors \
    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor \
    --initialize-at-run-time=io.netty.channel.unix.IovArray \
    --initialize-at-run-time=io.netty.channel.unix.Limits \
    --initialize-at-run-time=io.netty.handler.codec.http.HttpServerExpectContinueHandler \
    --initialize-at-run-time=io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler \
    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
    --initialize-at-run-time=io.netty.util.internal.PlatformDependent \
    --initialize-at-run-time=io.netty.util.internal.PlatformDependent0 \
    --initialize-at-run-time=io.netty.util.NetUtil \
    --initialize-at-run-time=io.quarkus.netty.runtime.EmptyByteBufStub \
    --initialize-at-run-time=io.quarkus.runtime.configuration.RuntimeConfigBuilder\$UuidConfigSource\$Holder \
    --initialize-at-run-time=io.quarkus.runtime.graal.InetRunTime \
    --initialize-at-run-time=io.quarkus.runtime.ExecutorRecorder \
    --initialize-at-run-time=io.vertx.core.buffer.impl.PartialPooledByteBufAllocator \
    --initialize-at-run-time=io.vertx.core.buffer.impl.VertxByteBufAllocator \
    --initialize-at-run-time=io.vertx.core.eventbus.impl.clustered.ClusteredEventBus \
    --initialize-at-run-time=io.vertx.ext.auth.impl.jose.JWT \
    --initialize-at-run-time=jakarta.el.ELManager \
    --initialize-at-run-time=java.rmi \
    --initialize-at-run-time=java.util.logging.ConsoleHandler \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
    --initialize-at-run-time=jdk.package \
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins \
    --initialize-at-run-time=org.jboss.threads.JDKSpecific\$ThreadAccess \
    --initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder \
    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler \
    --initialize-at-run-time=sun.rmi \
    --initialize-at-run-time=io.smallrye.common.os.Process \
    --initialize-at-run-time=io.smallrye.common.net.HostName \
    --initialize-at-run-time=io.vertx.core.http.impl.Http1xServerResponse \
    --initialize-at-run-time=io.vertx.core.http.impl.VertxHttp2ClientUpgradeCodec \
    --initialize-at-run-time=io.vertx.core.parsetools.impl.RecordParserImpl \
    --initialize-at-run-time=io.netty.handler.ssl.ConscryptAlpnSslEngine \
    --initialize-at-run-time=io.netty.handler.ssl.ReferenceCountedOpenSslEngine \
    --initialize-at-run-time=io.netty.handler.ssl.ReferenceCountedOpenSslContext \
    --initialize-at-run-time=io.netty.handler.ssl.JdkSslServerContext \
    --initialize-at-run-time=io.netty.handler.ssl.util.ThreadLocalInsecureRandom \
    --initialize-at-run-time=io.netty.handler.codec.http.HttpObjectEncoder \
    --initialize-at-run-time=io.netty.handler.codec.http.websocketx.extensions.compression.DeflateDecoder \
    --initialize-at-run-time=io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder \
    --initialize-at-run-time=io.netty.handler.codec.http2.Http2CodecUtil \
    --initialize-at-run-time=io.netty.handler.codec.http2.DefaultHttp2FrameWriter \
    --initialize-at-run-time=io.netty.handler.codec.http2.Http2ConnectionHandler \
    --initialize-at-run-time=io.netty.handler.codec.http2.Http2ClientUpgradeCodec \
    --initialize-at-run-time=io.netty.util.AbstractReferenceCounted \
    --initialize-at-run-time=io.netty.buffer.AbstractReferenceCountedByteBuf \
    --initialize-at-run-time=io.netty.buffer.UnpooledByteBufAllocator \
    --initialize-at-run-time=io.netty.buffer.Unpooled \
    --initialize-at-run-time=io.netty.handler.codec.http.HttpObjectAggregator \
    --initialize-at-run-time=io.netty.handler.codec.ReplayingDecoderByteBuf \
    --initialize-at-run-time=io.netty.buffer.ByteBufUtil\$HexUtil \
    --initialize-at-run-time=io.netty.buffer.PooledByteBufAllocator \
    --initialize-at-run-time=io.netty.buffer.ByteBufAllocator \
    --initialize-at-run-time=io.netty.buffer.ByteBufUtil \
    --initialize-at-run-time=io.netty.handler.codec.compression.BrotliOptions \
    --initialize-at-run-time=io.netty.handler.codec.compression.ZstdConstants \
    --initialize-at-run-time=io.netty.handler.codec.compression.ZstdOptions \
    --initialize-at-run-time=io.vertx.ext.web.handler.sockjs.impl.XhrTransport \
    --initialize-at-build-time="" \
    -H:+PrintClassInitialization \
    -H:ApplicationLayerInitializedClasses=io.quarkus.arc.Arc \
    -H:ApplicationLayerInitializedClasses=io.quarkus.smallrye.context.runtime.SmallRyeContextPropagationRecorder \
    -H:ApplicationLayerInitializedClasses=io.quarkus.arc.runtime.ArcRecorder \
    -H:BuildOutputJSONFile=target/build-output-layer-base.json \
    -H:LayerCreate=libquarkusbaselayer.nil,module=java.base,module=jdk.localedata${packages} \
    -cp "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*":getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/extracted-classes.jar \
    -o libquarkusbaselayer -H:Path=./target

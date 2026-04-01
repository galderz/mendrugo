#!/usr/bin/env bash
set -eux

native_image=$HOME/src/mandrel/sdk/latest_graalvm_home/bin/native-image

source_jar_dir=getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar

#--initialize-at-run-time=io.netty.handler.codec.http2.Http2Headers\$PseudoHeaderName \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HttpConversionUtil\$ExtensionHeaderNames \

#--initialize-at-run-time=io.netty.handler.codec.http2.Http2Headers\$PseudoHeaderName \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HttpConversionUtil\$ExtensionHeaderNames \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HpackDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksAuthResponseDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksAuthRequestDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksCmdRequestDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksCmdResponseDecoder \
#    --initialize-at-run-time=io,netty.handler.codec.ReplayingDecoder \
#    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \

#--initialize-at-run-time=io.netty.channel.unix.Errors \
#    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor \
#    --initialize-at-run-time=io.netty.channel.unix.Limits \
#    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
#    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \

#    --initialize-at-run-time=io.netty.channel.pool.FixedChannelPool \
#    --initialize-at-run-time=io.netty.channel.pool.SimpleChannelPool \
#    --initialize-at-run-time=io.netty.channel.socket.nio.NioServerSocketChannel \
#    --initialize-at-run-time=io.netty.handler.codec.DecoderResult \
#    --initialize-at-run-time=io.netty.handler.codec.ReplayingDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.http.HttpMessageDecoderResult \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HpackDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.http2.Http2Headers\$PseudoHeaderName \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HttpConversionUtil \
#    --initialize-at-run-time=io.netty.handler.codec.http2.HttpConversionUtil\$ExtensionHeaderNames \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksAuthRequestDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksAuthResponseDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksCmdRequestDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.socks.SocksCmdResponseDecoder \
#    --initialize-at-run-time=io.netty.handler.codec.spdy.SpdyHttpHeaders\$Names \
#    --initialize-at-run-time=io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler \
#    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
#    --initialize-at-run-time=io.netty.util.internal.ObjectCleaner \
#    --initialize-at-run-time=jakarta.el.ELManager \
#    --initialize-at-run-time=java.rmi \
#    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
#    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
#    --initialize-at-run-time=jdk.tools.jlink.internal.plugins \
#    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler \

#     --initialize-at-run-time=io.quarkus.runner.ApplicationImpl

base_layer_args="--initialize-at-run-time=io.netty.channel.DefaultChannelId
    --initialize-at-run-time=io.netty.channel.unix.Errors
    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor
    --initialize-at-run-time=io.netty.channel.unix.IovArray
    --initialize-at-run-time=io.netty.channel.unix.Limits
    --initialize-at-run-time=io.netty.handler.codec.http.HttpServerExpectContinueHandler
    --initialize-at-run-time=io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler
    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils
    --initialize-at-run-time=io.netty.util.NetUtil
    --initialize-at-run-time=io.quarkus.netty.runtime.EmptyByteBufStub
    --initialize-at-run-time=io.quarkus.runtime.graal.InetRunTime
    --initialize-at-run-time=io.quarkus.runtime.ExecutorRecorder
    --initialize-at-run-time=io.vertx.core.buffer.impl.PartialPooledByteBufAllocator
    --initialize-at-run-time=io.vertx.core.buffer.impl.VertxByteBufAllocator
    --initialize-at-run-time=io.vertx.core.eventbus.impl.clustered.ClusteredEventBus
    --initialize-at-run-time=io.vertx.ext.auth.impl.jose.JWT
    --initialize-at-run-time=jakarta.el.ELManager
    --initialize-at-run-time=java.rmi
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch
    --initialize-at-run-time=jdk.package
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins
    --initialize-at-run-time=org.jboss.threads.JDKSpecific\$ThreadAccess
    --initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder
    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler
    --initialize-at-run-time=sun.rmi
"

${native_image} \
    ${base_layer_args} \
    --initialize-at-run-time=io.quarkus.runner.ApplicationImpl \
    --features=io.quarkus.runner.Feature \
    -H:LayerUse=target/libquarkusbaselayer.nil \
    -H:LinkerRPath=. \
    -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.tryReflectionSetAccessible=true -J-Dio.netty.noUnsafe=false -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -J-Duser.language=en -J-Duser.country=GB -J-Dlogging.initial-configurator.min-level=500 -H:+UnlockExperimentalVMOptions -H:-UnlockExperimentalVMOptions --enable-native-access=ALL-UNNAMED -J-Dfile.encoding=UTF-8 -J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk=ALL-UNNAMED --features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature,io.quarkus.runtime.graal.JVMChecksFeature,io.quarkus.runtime.graal.SkipConsoleServiceProvidersFeature -J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED -J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED -J--add-opens=java.base/java.text=ALL-UNNAMED -J--add-opens=java.base/java.io=ALL-UNNAMED -J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED -J--add-opens=java.base/java.util=ALL-UNNAMED -H:+UnlockExperimentalVMOptions -H:BuildOutputJSONFile=getting-started-1.0.0-SNAPSHOT-runner-build-output-stats.json -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+GenerateBuildArtifactsFile -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+AllowFoldMethods -H:-UnlockExperimentalVMOptions -J-Djava.awt.headless=true --no-fallback \
    -H:+UnlockExperimentalVMOptions -H:+ReportExceptionStackTraces -H:-UnlockExperimentalVMOptions -J-Xmx6g -H:-AddAllCharsets --enable-url-protocols=http -H:NativeLinkerOption=-no-pie -H:+UnlockExperimentalVMOptions -H:-UseServiceLoaderFeature -H:-UnlockExperimentalVMOptions -J--add-exports=org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED --exclude-config io\.netty\.netty-codec /META-INF/native-image/io\.netty/netty-codec/generated/handlers/reflect-config\.json --exclude-config io\.netty\.netty-handler /META-INF/native-image/io\.netty/netty-handler/generated/handlers/reflect-config\.json target/getting-started-1.0.0-SNAPSHOT-runner \
    -cp "${source_jar_dir}/lib/*":${source_jar_dir}/extracted-classes.jar \
    -jar ${source_jar_dir}/getting-started-1.0.0-SNAPSHOT-runner-filtered.jar \
    -o getting-started-1.0.0-SNAPSHOT-runner -H:Path=./target

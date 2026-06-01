#!/usr/bin/env bash
set -eux

native_image=$JAVA_HOME/bin/native-image

${native_image} \
    -H:+PrintClassInitialization \
    --features=io.quarkus.runner.Feature \
    -H:LayerUse=target/libnettybaselayer.nil \
    -H:LinkerRPath=. \
    -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.tryReflectionSetAccessible=true -J-Dio.netty.noUnsafe=false -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -J-Duser.language=en -J-Duser.country=GB --enable-native-access=ALL-UNNAMED -J-Dfile.encoding=UTF-8 -J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk=ALL-UNNAMED --features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature,io.quarkus.runtime.graal.JVMChecksFeature,io.quarkus.runtime.graal.SkipConsoleServiceProvidersFeature -J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED -J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED -J--add-opens=java.base/java.text=ALL-UNNAMED -J--add-opens=java.base/java.io=ALL-UNNAMED -J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED -J--add-opens=java.base/java.util=ALL-UNNAMED -H:+UnlockExperimentalVMOptions \
    -H:BuildOutputJSONFile=target/build-output-layer-app.json \
    -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+GenerateBuildArtifactsFile -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+AllowFoldMethods -H:-UnlockExperimentalVMOptions -J-Djava.awt.headless=true --no-fallback -H:+UnlockExperimentalVMOptions -H:+ReportExceptionStackTraces -H:-UnlockExperimentalVMOptions -J-Xmx6g -H:-AddAllCharsets --enable-url-protocols=http -H:NativeLinkerOption=-no-pie -H:+UnlockExperimentalVMOptions -H:-UseServiceLoaderFeature -H:-UnlockExperimentalVMOptions -J--add-exports=org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED \
    --exclude-config io\.netty\.netty-codec /META-INF/native-image/io\.netty/netty-codec/generated/handlers/reflect-config\.json \
    --exclude-config io\.netty\.netty-handler /META-INF/native-image/io\.netty/netty-handler/generated/handlers/reflect-config\.json \
    --exclude-config io\.netty\.netty-codec-http /META-INF/native-image/io\.netty/netty-codec-http/native-image\.properties \
    --exclude-config io\.netty\.netty-codec-http2 /META-INF/native-image/io\.netty/netty-codec-http2/native-image\.properties \
    --initialize-at-run-time=io.netty.channel.unix.Errors \
    --initialize-at-run-time=io.netty.channel.unix.Limits \
    --initialize-at-run-time=io.netty.channel.unix.IovArray \
    --initialize-at-run-time=io.netty.util.NetUtil \
    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
    --initialize-at-run-time=io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler \
    --initialize-at-run-time=io.quarkus.netty.runtime.EmptyByteBufStub \
    --initialize-at-run-time=io.quarkus.netty.runtime.graal.Holder_io_netty_util_concurrent_ScheduledFutureTask \
    --initialize-at-run-time=io.quarkus.runtime.ExecutorRecorder \
    --initialize-at-run-time=io.quarkus.runtime.configuration.RuntimeConfigBuilder\$UuidConfigSource\$Holder \
    --initialize-at-run-time=io.quarkus.runtime.graal.InetRunTime \
    --initialize-at-run-time=io.smallrye.common.net.HostName \
    --initialize-at-run-time=io.smallrye.common.os.Process \
    --initialize-at-run-time=io.smallrye.common.ref.References\$ReaperThread \
    --initialize-at-run-time=io.vertx.core.buffer.impl.PartialPooledByteBufAllocator \
    --initialize-at-run-time=io.vertx.core.buffer.impl.VertxByteBufAllocator \
    --initialize-at-run-time=io.vertx.core.eventbus.impl.clustered.ClusteredEventBus \
    --initialize-at-run-time=io.vertx.core.http.impl.Http1xServerResponse \
    --initialize-at-run-time=io.vertx.core.http.impl.VertxHttp2ClientUpgradeCodec \
    --initialize-at-run-time=io.vertx.core.parsetools.impl.RecordParserImpl \
    --initialize-at-run-time=io.vertx.ext.auth.impl.jose.JWT \
    --initialize-at-run-time=io.vertx.ext.web.handler.sockjs.impl.XhrTransport \
    --initialize-at-run-time=java.util.logging.ConsoleHandler \
    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler \
    --initialize-at-run-time=org.jboss.threads.EnhancedQueueExecutor\$RuntimeFields \
    --initialize-at-run-time=org.wildfly.common.net.HostName \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins.OrderResourcesPlugin \
    -jar getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner.jar \
    -o getting-started-1.0.0-SNAPSHOT-runner \
    -H:Path=./target

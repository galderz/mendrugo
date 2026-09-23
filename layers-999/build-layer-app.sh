#!/usr/bin/env bash
set -eux

native_image=$HOME/src/mandrel/sdk/latest_graalvm_home/bin/native-image

source_jar_dir=getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar

BASE_ARGS=(
    "-J-Djava.util.logging.manager=org.jboss.logmanager.LogManager"
    "-J-Dsun.nio.ch.maxUpdateArraySize=100"
    "-J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory"
    "-J-Dvertx.disableDnsResolver=true"
    "-J-Dio.netty.tryReflectionSetAccessible=true"
    "-J-Dio.netty.noUnsafe=true"
    "-J-Dio.netty.leakDetection.level=DISABLED"
    "-J-Dio.netty.allocator.maxOrder=3"
    "-J-Duser.language=en"
    "-J-Dlogging.initial-configurator.min-level=500" "-H:+UnlockExperimentalVMOptions"
    "-H:IncludeLocales=en" "-H:-UnlockExperimentalVMOptions"
    "--enable-native-access=ALL-UNNAMED"
    "-J-Dfile.encoding=UTF-8"
    "-J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk=ALL-UNNAMED"
    "--features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature,io.quarkus.runtime.graal.JVMChecksFeature,io.quarkus.runtime.graal.SkipConsoleServiceProvidersFeature"
    "-J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED"
    "-J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED"
    "-J--add-opens=java.base/java.text=ALL-UNNAMED"
    "-J--add-opens=java.base/java.io=ALL-UNNAMED"
    "-J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED"
    "-J--add-opens=java.base/java.util=ALL-UNNAMED" "-H:+UnlockExperimentalVMOptions"
    "-H:BuildOutputJSONFile=target/build-output-layer-app.json" "-H:-UnlockExperimentalVMOptions" "-H:+UnlockExperimentalVMOptions"
    "-H:+GenerateBuildArtifactsFile" "-H:-UnlockExperimentalVMOptions"
    "-H:+PrintClassInitialization"
    "-H:-CheckToolchain" "-H:+UnlockExperimentalVMOptions"
    "-H:+AllowFoldMethods" "-H:-UnlockExperimentalVMOptions" "-H:+UnlockExperimentalVMOptions"
    "-H:+SharedArenaSupport" "-H:-UnlockExperimentalVMOptions"
    "-J-Djava.awt.headless=true"
    "-H:+UnlockExperimentalVMOptions"
    "-H:+ReportExceptionStackTraces" "-H:-UnlockExperimentalVMOptions"
    "-H:-AddAllCharsets"
    "--enable-url-protocols=http"
    "-H:NativeLinkerOption=-no-pie"
    "--enable-monitoring=heapdump,threaddump" "-H:+UnlockExperimentalVMOptions"
    "-H:-UseServiceLoaderFeature" "-H:-UnlockExperimentalVMOptions"
    "--exclude-config" 'io\.netty\.netty-codec.*' '/META-INF/native-image/io\.netty/netty-codec.*/generated/handlers/reflect-config\.json'
    "--exclude-config" 'io\.netty\.netty-handler' '/META-INF/native-image/io\.netty/netty-handler/generated/handlers/reflect-config\.json'
)

RUN_INIT_BASE_ARGS=(
    "--initialize-at-run-time=io.netty.handler.timeout"
    "--initialize-at-run-time=io.netty.handler.traffic"
    "--initialize-at-run-time=io.netty.util.NetUtil"
    "--initialize-at-run-time=io.netty.util.internal.PlatformDependent"
    "--initialize-at-run-time=io.netty.util.internal.PlatformDependent0"
    "--initialize-at-run-time=io.vertx.ext.web.handler.impl"
    "--initialize-at-run-time=org.jboss.logmanager.handlers.ConsoleHandler\$ConsoleHolder"
    "--initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler"
    "--initialize-at-run-time=jakarta.el.ELManager"
    "--initialize-at-run-time=java.rmi"
    "--initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch"
    "--initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch"
    "--initialize-at-run-time=jdk.tools.jlink.internal.plugins"
    "--initialize-at-run-time=sun.rmi"
)

RUN_INIT_FEATURE_ARGS=(
    "--initialize-at-run-time=io.netty.resolver.dns"
    "--initialize-at-run-time=io.netty.handler.codec.http"
    "--initialize-at-run-time=io.netty.handler.codec.http2"
    "--initialize-at-run-time=io.netty.handler.proxy"
    "--initialize-at-run-time=io.netty.handler.codec.rtsp"
    "--initialize-at-run-time=io.netty.handler.ssl"
    "--initialize-at-run-time=io.netty.handler.codec.compression"
    "--initialize-at-run-time=io.netty.handler.codec.socks"
    "--initialize-at-run-time=io.netty.handler.codec.spdy"
    "--initialize-at-run-time=io.netty.handler.codec.marshalling"
    "--initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder"
)

LAYER_ARGS=(
    "-H:LayerUse=target/libquarkusbaselayer.nil"
    "-H:LinkerRPath=."
    "-jar" "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner.jar"
    "-o" "getting-started-1.0.0-SNAPSHOT-runner"
    "-H:Path=./target"
)

${native_image} \
    "${BASE_ARGS[@]}" \
    "${RUN_INIT_BASE_ARGS[@]}" \
    "${RUN_INIT_FEATURE_ARGS[@]}" \
    "${LAYER_ARGS[@]}"

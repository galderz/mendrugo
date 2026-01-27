#!/usr/bin/env bash
set -eux

# --debug-attach=*:8000 \
#    --initialize-at-run-time=jdk.jpackage.internal \
#        --initialize-at-run-time=sun.awt \
#        --initialize-at-run-time=sun.java2d \
#        --initialize-at-run-time=com.sun.tools.javac \

native-image \
    --initialize-at-run-time=com.sun.imageio \
    --initialize-at-run-time=com.sun.jmx.remote.security \
    --initialize-at-run-time=com.sun.media \
    --initialize-at-run-time=com.sun.org.apache \
    --initialize-at-run-time=com.sun.tools \
    --initialize-at-run-time=java.awt \
    --initialize-at-run-time=java.rmi \
    --initialize-at-run-time=javax.imageio \
    --initialize-at-run-time=jdk.javadoc \
    --initialize-at-run-time=jdk.jpackage \
    --initialize-at-run-time=jdk.tools \
    --initialize-at-run-time=sun.awt \
    --initialize-at-run-time=sun.font \
    --initialize-at-run-time=sun.java2d \
    --initialize-at-run-time=sun.rmi \
    --initialize-at-run-time=sun.security.jgss \
    --initialize-at-run-time=sun.tools \
    -H:LayerUse=../../../target/libjavabaselayer.nil \
    -H:LinkerRPath=. \
    -H:-LayerOptionVerification \
    -J-Xmx6g \
    -J-Dlogging.initial-configurator.min-level=500 -J-Duser.language=en -J-Duser.country=GB -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -H:+UnlockExperimentalVMOptions -H:IncludeLocales=en-GB -H:-UnlockExperimentalVMOptions -J-Dfile.encoding=UTF-8 -J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk=ALL-UNNAMED --features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature,io.quarkus.runtime.graal.JVMChecksFeature,io.quarkus.runtime.graal.SkipConsoleServiceProvidersFeature -J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED -J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED -J--add-opens=java.base/java.text=ALL-UNNAMED -J--add-opens=java.base/java.io=ALL-UNNAMED -J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED -J--add-opens=java.base/java.util=ALL-UNNAMED -H:+UnlockExperimentalVMOptions -H:BuildOutputJSONFile=build-output-layer-app.json -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+GenerateBuildArtifactsFile -H:-UnlockExperimentalVMOptions -H:+PrintClassInitialization -H:+UnlockExperimentalVMOptions -H:+AllowFoldMethods -H:-UnlockExperimentalVMOptions -J-Djava.awt.headless=true --no-fallback --link-at-build-time -H:+UnlockExperimentalVMOptions -H:+ReportExceptionStackTraces -H:-UnlockExperimentalVMOptions -H:-AddAllCharsets --enable-url-protocols=http -H:NativeLinkerOption=-no-pie --enable-monitoring=heapdump -H:+UnlockExperimentalVMOptions -H:-UseServiceLoaderFeature -H:-UnlockExperimentalVMOptions -J--add-exports=org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED --exclude-config io\.netty\.netty-codec /META-INF/native-image/io\.netty/netty-codec/generated/handlers/reflect-config\.json --exclude-config io\.netty\.netty-handler /META-INF/native-image/io\.netty/netty-handler/generated/handlers/reflect-config\.json getting-started-1.0.0-SNAPSHOT-runner -jar getting-started-1.0.0-SNAPSHOT-runner.jar

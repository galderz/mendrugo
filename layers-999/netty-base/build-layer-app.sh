#!/usr/bin/env bash
set -eux

native_image=$JAVA_HOME/bin/native-image

${native_image} \
    -H:ApplicationLayerInitializedClasses=io.quarkus.arc.Arc \
    -H:ApplicationLayerInitializedClasses=io.quarkus.smallrye.context.runtime.SmallRyeContextPropagationRecorder \
    -H:ApplicationLayerInitializedClasses=io.quarkus.arc.runtime.ArcRecorder \
    -H:+PrintClassInitialization \
    --features=io.quarkus.runner.Feature \
    -H:LayerUse=target/libnettybaselayer.nil \
    -H:LinkerRPath=. \
    -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.tryReflectionSetAccessible=true -J-Dio.netty.noUnsafe=false -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -J-Duser.language=en -J-Duser.country=GB -H:+UnlockExperimentalVMOptions -H:IncludeLocales=en-GB -H:-UnlockExperimentalVMOptions --enable-native-access=ALL-UNNAMED -J-Dfile.encoding=UTF-8 -J--add-exports=org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk=ALL-UNNAMED --features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature,io.quarkus.runtime.graal.JVMChecksFeature,io.quarkus.runtime.graal.SkipConsoleServiceProvidersFeature -J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED -J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED -J--add-opens=java.base/java.text=ALL-UNNAMED -J--add-opens=java.base/java.io=ALL-UNNAMED -J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED -J--add-opens=java.base/java.util=ALL-UNNAMED -H:+UnlockExperimentalVMOptions \
    -H:BuildOutputJSONFile=target/build-output-layer-app.json \
    -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+GenerateBuildArtifactsFile -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+AllowFoldMethods -H:-UnlockExperimentalVMOptions -J-Djava.awt.headless=true --no-fallback -H:+UnlockExperimentalVMOptions -H:+ReportExceptionStackTraces -H:-UnlockExperimentalVMOptions -J-Xmx6g -H:-AddAllCharsets --enable-url-protocols=http -H:NativeLinkerOption=-no-pie -H:+UnlockExperimentalVMOptions -H:-UseServiceLoaderFeature -H:-UnlockExperimentalVMOptions -J--add-exports=org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED --exclude-config io\.netty\.netty-codec /META-INF/native-image/io\.netty/netty-codec/generated/handlers/reflect-config\.json --exclude-config io\.netty\.netty-handler /META-INF/native-image/io\.netty/netty-handler/generated/handlers/reflect-config\.json \
    -jar getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner.jar \
    -o getting-started-1.0.0-SNAPSHOT-runner \
    -H:Path=./target

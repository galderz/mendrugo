#!/bin/bash

set -eux

# e.g. mandrel-mandrel
ID=$1
# e.g. branches, branch-misses
EVENT=$2
# html or jfr
FORMAT=$3

ARGS="-J-Dlogging.initial-configurator.min-level=500 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -J-Duser.language=en -J-Duser.country=GB -J-Dfile.encoding=UTF-8 --features=io.quarkus.runner.Feature,io.quarkus.runtime.graal.DisableLoggingFeature -J--add-exports=java.security.jgss/sun.security.krb5=ALL-UNNAMED -J--add-exports=java.security.jgss/sun.security.jgss=ALL-UNNAMED -J--add-opens=java.base/java.text=ALL-UNNAMED -J--add-opens=java.base/java.io=ALL-UNNAMED -J--add-opens=java.base/java.lang.invoke=ALL-UNNAMED -J--add-opens=java.base/java.util=ALL-UNNAMED -H:+UnlockExperimentalVMOptions -H:BuildOutputJSONFile=getting-started-reactive-1.0.0-SNAPSHOT-runner-build-output-stats.json -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+GenerateBuildArtifactsFile -H:-UnlockExperimentalVMOptions -H:+UnlockExperimentalVMOptions -H:+AllowFoldMethods -H:-UnlockExperimentalVMOptions -J-Djava.awt.headless=true --no-fallback --link-at-build-time -H:+UnlockExperimentalVMOptions -H:+ReportExceptionStackTraces -H:-UnlockExperimentalVMOptions -H:-AddAllCharsets --enable-url-protocols=http -H:NativeLinkerOption=-no-pie --enable-monitoring=heapdump -H:+UnlockExperimentalVMOptions -H:-UseServiceLoaderFeature -H:-UnlockExperimentalVMOptions -J--add-exports=org.graalvm.nativeimage/org.graalvm.nativeimage.impl=ALL-UNNAMED --exclude-config io.netty.netty-codec /META-INF/native-image/io.netty/netty-codec/generated/handlers/reflect-config.json --exclude-config io.netty.netty-handler /META-INF/native-image/io.netty/netty-handler/generated/handlers/reflect-config.json getting-started-reactive-1.0.0-SNAPSHOT-runner -jar getting-started-reactive-1.0.0-SNAPSHOT-runner.jar"
OPT="${HOME}/opt"
AP_LIB="${OPT}/async-profiler/lib"
JAVA_BIN="${OPT}/${ID}/bin"
JAVA="${JAVA_BIN}/java"
NATIVE_IMAGE="${JAVA_BIN}/native-image"

echo "# Profile with perf stat"
perf stat ${NATIVE_IMAGE} ${ARGS}

echo "# Profile with async profiler"
${NATIVE_IMAGE} -J-agentpath:${AP_LIB}/libasyncProfiler.so=start,event=${EVENT},file=${EVENT}-${ID}.${FORMAT} ${ARGS}

if [ "${FORMAT}" = "jfr" ]; then
  ${JAVA} -jar ${AP_LIB}/converter.jar jfr2flame --lines ${EVENT}-${ID}.${FORMAT} ${EVENT}-${ID}.html
fi

# Requirements

* Linux env.
* Build [this Netty branch](https://github.com/galderz/netty/commits/t_buildtime_ref_counted).
* GraalVM or Mandrel.

# Run

```bash
$ make
```

# Example Output

```bash
$ make
rm -drf reports || true
mvn clean package dependency:copy-dependencies -DskipTests
[INFO] Scanning for projects...
[INFO]
[INFO] ----------------------------< epoll:epoll >-----------------------------
[INFO] Building epoll 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ epoll ---
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ epoll ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/g/1/mendrugo/epoll/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ epoll ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 4 source files to /home/g/1/mendrugo/epoll/target/classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ epoll ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/g/1/mendrugo/epoll/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ epoll ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ epoll ---
[INFO] Tests are skipped.
[INFO]
[INFO] --- maven-jar-plugin:3.1.0:jar (default-jar) @ epoll ---
[INFO] Building jar: /home/g/1/mendrugo/epoll/target/epoll-1.0-SNAPSHOT.jar
[INFO]
[INFO] --- maven-dependency-plugin:2.8:copy-dependencies (default-cli) @ epoll ---
[INFO] Copying compiler-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/compiler-20.1.0.jar
[INFO] Copying netty-transport-4.1.52.Final-SNAPSHOT.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying pointsto-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/pointsto-20.1.0.jar
[INFO] Copying truffle-api-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/truffle-api-20.1.0.jar
[INFO] Copying svm-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/svm-20.1.0.jar
[INFO] Copying graal-sdk-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/graal-sdk-20.1.0.jar
[INFO] Copying truffle-nfi-native-linux-amd64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/truffle-nfi-native-linux-amd64-20.1.0.tar.gz
[INFO] Copying netty-resolver-4.1.52.Final-SNAPSHOT.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-resolver-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying svm-hosted-native-darwin-amd64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/svm-hosted-native-darwin-amd64-20.1.0.tar.gz
[INFO] Copying objectfile-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/objectfile-20.1.0.jar
[INFO] Copying svm-hosted-native-linux-amd64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/svm-hosted-native-linux-amd64-20.1.0.tar.gz
[INFO] Copying truffle-nfi-20.1.0.jar to /home/g/1/mendrugo/epoll/target/dependency/truffle-nfi-20.1.0.jar
[INFO] Copying truffle-nfi-native-linux-aarch64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/truffle-nfi-native-linux-aarch64-20.1.0.tar.gz
[INFO] Copying truffle-nfi-native-darwin-amd64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/truffle-nfi-native-darwin-amd64-20.1.0.tar.gz
[INFO] Copying netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar
[INFO] Copying netty-buffer-4.1.52.Final-SNAPSHOT.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying svm-hosted-native-windows-amd64-20.1.0.tar.gz to /home/g/1/mendrugo/epoll/target/dependency/svm-hosted-native-windows-amd64-20.1.0.tar.gz
[INFO] Copying netty-common-4.1.52.Final-SNAPSHOT.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar to /home/g/1/mendrugo/epoll/target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 3.903 s
[INFO] Finished at: 2020-09-03T11:00:04+01:00
[INFO] ------------------------------------------------------------------------
native-image \
-jar target/epoll-1.0-SNAPSHOT.jar \
-cp target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar:target/dependency/netty-native-epoll-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar \
--initialize-at-build-time= \
--enable-all-security-services \
--no-fallback \
--no-server \
--verbose \
-J-Dfile.encoding=UTF-8 \
-J-Dio.netty.allocator.maxOrder=1 \
-J-Dio.netty.leakDetection.level=DISABLED \
-J-Dsun.nio.ch.maxUpdateArraySize=100 \
-J-Duser.language=en \
-H:EnableURLProtocols=http,https \
-H:FallbackThreshold=0 \
-H:+JNI \
-H:+ReportExceptionStackTraces \
-H:+StackTrace \
-H:+TraceClassInitialization \
-H:-AddAllCharsets \
-H:-UseServiceLoaderFeature \
target/epoll
Warning: Ignoring server-mode native-image argument --no-server.
Executing [
/home/g/.qollider/cache/0209/graalvm_home/bin/java \
-XX:+UseParallelGC \
-XX:+UnlockExperimentalVMOptions \
-XX:+EnableJVMCI \
-Dtruffle.TrustAllTruffleRuntimeProviders=true \
-Dtruffle.TruffleRuntime=com.oracle.truffle.api.impl.DefaultTruffleRuntime \
-Dgraalvm.ForcePolyglotInvalid=true \
-Dgraalvm.locatorDisabled=true \
-Dsubstratevm.IgnoreGraalVersionCheck=true \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.aarch64=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.amd64=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code.site=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code.stack=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.common=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot.aarch64=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot.amd64=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot.sparc=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.hotspot=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.meta=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.runtime=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.services.internal=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.services=ALL-UNNAMED \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.sparc=ALL-UNNAMED \
--add-exports=org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED \
--add-opens=jdk.internal.vm.compiler/org.graalvm.compiler.debug=ALL-UNNAMED \
--add-opens=jdk.internal.vm.compiler/org.graalvm.compiler.nodes=ALL-UNNAMED \
--add-opens=jdk.unsupported/sun.reflect=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.module=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.reflect=ALL-UNNAMED \
--add-opens=java.base/java.io=ALL-UNNAMED \
--add-opens=java.base/java.lang=ALL-UNNAMED \
--add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
--add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
--add-opens=java.base/java.lang.ref=ALL-UNNAMED \
--add-opens=java.base/java.net=ALL-UNNAMED \
--add-opens=java.base/java.nio=ALL-UNNAMED \
--add-opens=java.base/java.nio.file=ALL-UNNAMED \
--add-opens=java.base/java.security=ALL-UNNAMED \
--add-opens=java.base/javax.crypto=ALL-UNNAMED \
--add-opens=java.base/java.util=ALL-UNNAMED \
--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
--add-opens=java.base/sun.security.x509=ALL-UNNAMED \
--add-opens=java.base/jdk.internal.logger=ALL-UNNAMED \
--add-opens=org.graalvm.sdk/org.graalvm.nativeimage.impl=ALL-UNNAMED \
--add-opens=org.graalvm.sdk/org.graalvm.polyglot=ALL-UNNAMED \
--add-opens=org.graalvm.truffle/com.oracle.truffle.polyglot=ALL-UNNAMED \
--add-opens=org.graalvm.truffle/com.oracle.truffle.api.impl=ALL-UNNAMED \
-XX:-UseJVMCICompiler \
-Xss10m \
-Xms1g \
-Xmx13286971800 \
-Duser.country=US \
-Duser.language=en \
-Djava.awt.headless=true \
-Dorg.graalvm.version=d60078b \
'-Dorg.graalvm.config=(Mandrel Distribution)' \
-Dcom.oracle.graalvm.isaot=true \
-Djava.system.class.loader=com.oracle.svm.hosted.NativeImageSystemClassLoader \
-Xshare:off \
--module-path \
/home/g/.qollider/cache/0209/graalvm_home/lib/jvmci/graal-sdk.jar:/home/g/.qollider/cache/0209/graalvm_home/lib/truffle/truffle-api.jar \
--upgrade-module-path \
/home/g/.qollider/cache/0209/graalvm_home/lib/jvmci/graal.jar \
-javaagent:/home/g/.qollider/cache/0209/graalvm_home/lib/svm/builder/svm.jar=traceInitialization \
-Djdk.internal.lambda.disableEagerInitialization=true \
-Djdk.internal.lambda.eagerlyInitialize=false \
-Djava.lang.invoke.InnerClassLambdaMetafactory.initializeLambdas=false \
--add-exports=jdk.internal.vm.ci/jdk.vm.ci.code=jdk.internal.vm.compiler \
-Dfile.encoding=UTF-8 \
-Dio.netty.allocator.maxOrder=1 \
-Dio.netty.leakDetection.level=DISABLED \
-Dsun.nio.ch.maxUpdateArraySize=100 \
-Duser.language=en \
-cp \
/home/g/.qollider/cache/0209/graalvm_home/lib/jvmci/graal-sdk.jar:/home/g/.qollider/cache/0209/graalvm_home/lib/jvmci/graal.jar:/home/g/.qollider/cache/0209/graalvm_home/lib/svm/builder/svm.jar:/home/g/.qollider/cache/0209/graalvm_home/lib/svm/builder/pointsto.jar:/home/g/.qollider/cache/0209/graalvm_home/lib/svm/builder/objectfile.jar \
'com.oracle.svm.hosted.NativeImageGeneratorRunner$JDK9Plus' \
-imagecp \
/home/g/1/mendrugo/epoll/target/epoll-1.0-SNAPSHOT.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-native-epoll-4.1.52.Final-SNAPSHOT.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar:/home/g/1/mendrugo/epoll/target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar \
-H:Path=/home/g/1/mendrugo/epoll \
-H:Class=epoll.CheckEpoll \
-H:ClassInitialization=io.netty.util.concurrent.GlobalEventExecutor:run_time,io.netty.util.concurrent.ImmediateEventExecutor:run_time,io.netty.util.concurrent.ScheduledFutureTask:run_time,io.netty.util.internal.ThreadLocalRandom:run_time \
-H:ReflectionConfigurationResources=META-INF/native-image/io.netty/transport/reflection-config.json \
-H:ClassInitialization=io.netty.buffer.PooledByteBufAllocator:run_time,io.netty.buffer.ByteBufAllocator:run_time,io.netty.buffer.ByteBufUtil:run_time \
-H:ClassInitialization=:build_time \
-H:+EnableAllSecurityServices \
-H:EnableURLProtocols=http,https \
-H:FallbackThreshold=0 \
-H:+JNI \
-H:+ReportExceptionStackTraces \
-H:+StackTrace \
-H:+TraceClassInitialization \
-H:-AddAllCharsets \
-H:-UseServiceLoaderFeature \
-H:CLibraryPath=/home/g/.qollider/cache/0209/graalvm_home/lib/svm/clibraries/linux-amd64 \
-H:Name=target/epoll \

]
[target/epoll:23590]    classlist:   4,067.66 ms,  0.96 GB
WARNING: Using an older version of the labsjdk-11.
[target/epoll:23590]        (cap):   1,319.32 ms,  0.96 GB
[target/epoll:23590]        setup:   4,141.81 ms,  0.96 GB
[target/epoll:23590]     (clinit):     363.36 ms,  3.19 GB
[target/epoll:23590]   (typeflow):  15,966.87 ms,  3.19 GB
[target/epoll:23590]    (objects):  22,263.00 ms,  3.19 GB
[target/epoll:23590]   (features):     935.19 ms,  3.19 GB
[target/epoll:23590]     analysis:  40,355.33 ms,  3.19 GB
[target/epoll:23590]     universe:   1,083.52 ms,  3.19 GB
[target/epoll:23590]      (parse):   5,143.57 ms,  3.19 GB
[target/epoll:23590]     (inline):   3,580.06 ms,  3.19 GB
[target/epoll:23590]    (compile):  25,039.94 ms,  4.98 GB
[target/epoll:23590]      compile:  36,678.34 ms,  4.98 GB
[target/epoll:23590]        image:   6,667.61 ms,  4.98 GB
[target/epoll:23590]        write:     829.05 ms,  4.98 GB
[target/epoll:23590]      [total]:  94,311.87 ms,  4.98 GB
./target/epoll
true
```

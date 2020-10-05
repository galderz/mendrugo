# Requirements

* Linux env.
* Build [this Netty branch](https://github.com/galderz/netty/tree/t_nonstatic_v2).
* GraalVM or Mandrel.


# Epoll Example

```bash
$ make build-netty-all
...

$ make
mvn clean package dependency:copy-dependencies -DskipTests -Dnetty.version=4.1.52.Final-SNAPSHOT
[INFO] Scanning for projects...

[INFO]
[INFO] ------------------------< org.example:epoll-c >-------------------------
[INFO] Building epoll-c 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ epoll-c ---
[INFO] Deleting /home/g/shared/mendrugo/epoll-c/target
[INFO]
[INFO] --- maven-resources-plugin:2.6:resources (default-resources) @ epoll-c ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/g/shared/mendrugo/epoll-c/src/main/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:compile (default-compile) @ epoll-c ---
[INFO] Changes detected - recompiling the module!
[INFO] Compiling 19 source files to /home/g/shared/mendrugo/epoll-c/target/classes
[INFO]
[INFO] --- maven-resources-plugin:2.6:testResources (default-testResources) @ epoll-c ---
[INFO] Using 'UTF-8' encoding to copy filtered resources.
[INFO] skip non existing resourceDirectory /home/g/shared/mendrugo/epoll-c/src/test/resources
[INFO]
[INFO] --- maven-compiler-plugin:3.1:testCompile (default-testCompile) @ epoll-c ---
[INFO] No sources to compile
[INFO]
[INFO] --- maven-surefire-plugin:2.12.4:test (default-test) @ epoll-c ---
[INFO] Tests are skipped.
[INFO]
[INFO] --- maven-jar-plugin:3.1.0:jar (default-jar) @ epoll-c ---
[INFO] Building jar: /home/g/shared/mendrugo/epoll-c/target/epoll-c-1.0-SNAPSHOT.jar
[INFO]
[INFO] --- maven-dependency-plugin:2.8:copy-dependencies (default-cli) @ epoll-c ---
[INFO] Copying truffle-api-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/truffle-api-20.2.0.jar
[INFO] Copying netty-transport-4.1.52.Final-SNAPSHOT.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying graal-sdk-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/graal-sdk-20.2.0.jar
[INFO] Copying compiler-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/compiler-20.2.0.jar
[INFO] Copying objectfile-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/objectfile-20.2.0.jar
[INFO] Copying netty-resolver-4.1.52.Final-SNAPSHOT.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-resolver-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying pointsto-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/pointsto-20.2.0.jar
[INFO] Copying truffle-nfi-native-linux-amd64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/truffle-nfi-native-linux-amd64-20.2.0.tar.gz
[INFO] Copying svm-hosted-native-darwin-amd64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/svm-hosted-native-darwin-amd64-20.2.0.tar.gz
[INFO] Copying truffle-nfi-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/truffle-nfi-20.2.0.jar
[INFO] Copying truffle-nfi-native-linux-aarch64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/truffle-nfi-native-linux-aarch64-20.2.0.tar.gz
[INFO] Copying truffle-nfi-native-darwin-amd64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/truffle-nfi-native-darwin-amd64-20.2.0.tar.gz
[INFO] Copying svm-hosted-native-linux-amd64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/svm-hosted-native-linux-amd64-20.2.0.tar.gz
[INFO] Copying netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar
[INFO] Copying svm-20.2.0.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/svm-20.2.0.jar
[INFO] Copying netty-buffer-4.1.52.Final-SNAPSHOT.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying svm-hosted-native-windows-amd64-20.2.0.tar.gz to /home/g/shared/mendrugo/epoll-c/target/dependency/svm-hosted-native-windows-amd64-20.2.0.tar.gz
[INFO] Copying netty-common-4.1.52.Final-SNAPSHOT.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar
[INFO] Copying netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar to /home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.432 s
[INFO] Finished at: 2020-10-05T12:45:50+02:00
[INFO] ------------------------------------------------------------------------
native-image \
-jar target/epoll-c-1.0-SNAPSHOT.jar \
-cp target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-resolver-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar:target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar:target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar \
--initialize-at-build-time= \
--initialize-at-run-time=io.netty.channel.epoll.Epoll,io.netty.channel.epoll.EpollEventArray,io.netty.channel.epoll.EpollEventLoop,io.netty.channel.epoll.Native,io.netty.channel.unix.Errors,io.netty.channel.unix.IovArray,io.netty.channel.unix.Limits,io.netty.channel.unix.Socket \
--enable-all-security-services \
--no-fallback \
--no-server \
--verbose \
-J-Dfile.encoding=UTF-8 \
-J-Dio.netty.allocator.maxOrder=1 \
-J-Dio.netty.leakDetection.level=DISABLED \
-J-Dsun.nio.ch.maxUpdateArraySize=100 \
-J-Duser.language=en \
-H:CLibraryPath=/home/g/shared/netty/transport-native-epoll/target/static-libs \
-H:EnableURLProtocols=http,https \
-H:FallbackThreshold=0 \
-H:+DumpTargetInfo \
-H:+PrintAnalysisCallTree \
-H:+ReportExceptionStackTraces \
-H:+StackTrace \
-H:+TraceClassInitialization \
-H:-AddAllCharsets \
-H:-UseServiceLoaderFeature \
target/epoll-c
Warning: Ignoring server-mode native-image argument --no-server.
Executing [
/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/bin/java \
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
-Xmx14g \
-Duser.country=US \
-Duser.language=en \
-Djava.awt.headless=true \
-Dorg.graalvm.version=dev \
'-Dorg.graalvm.config=(Mandrel Distribution)' \
-Dcom.oracle.graalvm.isaot=true \
-Djava.system.class.loader=com.oracle.svm.hosted.NativeImageSystemClassLoader \
-Xshare:off \
--module-path \
/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/jvmci/graal-sdk.jar:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/truffle/truffle-api.jar \
--upgrade-module-path \
/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/jvmci/graal.jar \
-javaagent:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/builder/svm.jar=traceInitialization \
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
/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/jvmci/graal-sdk.jar:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/jvmci/graal.jar:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/builder/svm.jar:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/builder/pointsto.jar:/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/builder/objectfile.jar \
'com.oracle.svm.hosted.NativeImageGeneratorRunner$JDK9Plus' \
-imagecp \
/home/g/shared/mendrugo/epoll-c/target/epoll-c-1.0-SNAPSHOT.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-buffer-4.1.52.Final-SNAPSHOT.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-common-4.1.52.Final-SNAPSHOT.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-resolver-4.1.52.Final-SNAPSHOT.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-4.1.52.Final-SNAPSHOT.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-native-epoll-4.1.52.Final-SNAPSHOT-linux-x86_64.jar:/home/g/shared/mendrugo/epoll-c/target/dependency/netty-transport-native-unix-common-4.1.52.Final-SNAPSHOT.jar \
-H:Path=/home/g/shared/mendrugo/epoll-c \
-H:Class=epoll.c.Main \
-H:ClassInitialization=io.netty.buffer.PooledByteBufAllocator:run_time,io.netty.buffer.ByteBufAllocator:run_time,io.netty.buffer.ByteBufUtil:run_time,io.netty.buffer.AbstractReferenceCountedByteBuf:run_time \
-H:ClassInitialization=io.netty.util.AbstractReferenceCounted:run_time,io.netty.util.concurrent.GlobalEventExecutor:run_time,io.netty.util.concurrent.ImmediateEventExecutor:run_time,io.netty.util.concurrent.ScheduledFutureTask:run_time,io.netty.util.internal.ThreadLocalRandom:run_time \
-H:ReflectionConfigurationResources=META-INF/native-image/io.netty/transport/reflection-config.json \
-H:ClassInitialization=:build_time \
-H:ClassInitialization=io.netty.channel.epoll.Epoll:run_time,io.netty.channel.epoll.EpollEventArray:run_time,io.netty.channel.epoll.EpollEventLoop:run_time,io.netty.channel.epoll.Native:run_time,io.netty.channel.unix.Errors:run_time,io.netty.channel.unix.IovArray:run_time,io.netty.channel.unix.Limits:run_time,io.netty.channel.unix.Socket:run_time \
-H:+EnableAllSecurityServices \
-H:EnableURLProtocols=http,https \
-H:FallbackThreshold=0 \
-H:+DumpTargetInfo \
-H:+PrintAnalysisCallTree \
-H:+ReportExceptionStackTraces \
-H:+StackTrace \
-H:+TraceClassInitialization \
-H:-AddAllCharsets \
-H:-UseServiceLoaderFeature \
-H:CLibraryPath=/home/g/.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/clibraries/linux-amd64,/home/g/shared/netty/transport-native-epoll/target/static-libs \
-H:Name=target/epoll-c \

]
[target/epoll-c:24449]    classlist:   1,249.64 ms,  0.96 GB
WARNING: Using an older version of the labsjdk-11.
[target/epoll-c:24449]        (cap):     366.27 ms,  0.96 GB
[target/epoll-c:24449]        setup:   1,110.75 ms,  0.96 GB
# Building image for target platform: org.graalvm.nativeimage.Platform$LINUX_AMD64
# Using native toolchain:
#   Name: GNU project C and C++ compiler (gcc)
#   Vendor: redhat
#   Version: 10.2.1
#   Target architecture: x86_64
#   Path: /usr/bin/gcc
# Using CLibrary: com.oracle.svm.core.posix.linux.libc.GLibC
[target/epoll-c:24449]     (clinit):     218.08 ms,  3.20 GB
# Static libraries:
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/clibraries/linux-amd64/liblibchelper.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libnet.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libextnet.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libnio.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libjava.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libfdlibm.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libsunec.a
#   ../../netty/transport-native-epoll/target/static-libs/libnetty-epoll.a
#   ../../netty/transport-native-epoll/target/static-libs/libnetty-unix-common.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/libzip.a
#   ../../../.qollider/cache/0110/mandrel-packaging/mandrel-java11-dev/lib/svm/clibraries/linux-amd64/libjvm.a
# Other libraries: stdc++,pthread,dl,z,rt
[target/epoll-c:24449]   (typeflow):   7,008.82 ms,  3.20 GB
[target/epoll-c:24449]    (objects):   7,976.32 ms,  3.20 GB
[target/epoll-c:24449]   (features):     324.75 ms,  3.20 GB
[target/epoll-c:24449]     analysis:  15,888.84 ms,  3.20 GB
Printing call tree to /home/g/shared/mendrugo/epoll-c/reports/call_tree_epoll-c_20201005_124610.txt
Printing list of used methods to /home/g/shared/mendrugo/epoll-c/reports/used_methods_epoll-c_20201005_124611.txt
Printing list of used classes to /home/g/shared/mendrugo/epoll-c/reports/used_classes_epoll-c_20201005_124611.txt
Printing list of used packages to /home/g/shared/mendrugo/epoll-c/reports/used_packages_epoll-c_20201005_124611.txt
[target/epoll-c:24449]     universe:     422.09 ms,  3.02 GB
[target/epoll-c:24449]      (parse):     641.95 ms,  3.02 GB
[target/epoll-c:24449]     (inline):   1,269.90 ms,  3.65 GB
[target/epoll-c:24449]    (compile):   3,521.57 ms,  4.43 GB
[target/epoll-c:24449]      compile:   6,302.33 ms,  4.43 GB
[target/epoll-c:24449]        image:   1,871.34 ms,  4.62 GB
[target/epoll-c:24449]        write:     250.96 ms,  4.62 GB
[target/epoll-c:24449]      [total]:  29,424.30 ms,  4.62 GB
./target/epoll-c
Netty version {}
Native.<clinit> begin
Native.<clinit> end
Is supporting sendmsg? true
TCP MD5 signature maximum length -1890972671
Kernel version 5.6.6-300.fc32.x86_64
Epoll available: true
Run echo
Server is up!
Channel active, send some data...
Data written and flushed
<- this is only a test
```

# Epoll Perf

In one terminal:

```bash
$ make epoll-server
...
./target/epoll-c -Drun=epoll-server
Server is up!
```

In another terminal:

```bash
$ make perf-tcpkali
tcpkali \
-m xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx \
--connect-rate=200 \
-c 500 \
-T 30 \
127.0.0.1:8081
Destination: [127.0.0.1]:8081
Interface lo address [127.0.0.1]:0
Using interface lo to connect to [127.0.0.1]:8081
Ramped up to 500 connections.
Total data sent:     70210.0 MiB (73620520960 bytes)
Total data received: 70004.0 MiB (73404543540 bytes)
Bandwidth per channel: 78.412⇅ Mbps (9801.5 kBps)
Aggregate bandwidth: 19574.293↓, 19631.887↑ Mbps
Packet rate estimate: 1792184.2↓, 1685018.3↑ (12↓, 45↑ TCP MSS/op)
Test duration: 30.0004 s.
```

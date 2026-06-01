#!/usr/bin/env bash
set -eux

native_image=$JAVA_HOME/bin/native-image

mkdir -p target

${native_image} \
    -H:+PrintClassInitialization \
    -H:BuildOutputJSONFile=target/build-output-layer-base.json \
    -H:+UnlockExperimentalVMOptions -H:IncludeLocales=en-GB -H:-UnlockExperimentalVMOptions \
    -H:LayerCreate=libnettybaselayer.nil,module=java.base,package=io.netty.* \
    --features=io.quarkus.runner.Feature \
    --initialize-at-run-time=io.netty.channel.unix.Errors \
    --initialize-at-run-time=io.netty.channel.unix.Limits \
    --initialize-at-run-time=io.netty.channel.unix.IovArray \
    --initialize-at-run-time=io.netty.util.NetUtil \
    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
    --initialize-at-run-time=io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler \
    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler \
    --initialize-at-run-time=io.smallrye.common.os.Process \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins.OrderResourcesPlugin \
    -cp "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/getting-started-1.0.0-SNAPSHOT-runner.jar:getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*" \
    -o libnettybaselayer -H:Path=./target

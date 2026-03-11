#!/usr/bin/env bash
set -eux

native_image=$HOME/src/mandrel/sdk/latest_graalvm_home/bin/native-image

mkdir -p target

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

# io.netty.channel.unix.* runtime inits as per NettyProcessor.build()
${native_image} \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$RpmPackageArch \
    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
    --initialize-at-run-time=jdk.tools.jlink.internal.plugins \
    --initialize-at-run-time=io.netty.channel.unix.Errors \
    --initialize-at-run-time=io.netty.channel.unix.FileDescriptor \
    --initialize-at-run-time=io.netty.channel.unix.IovArray \
    --initialize-at-run-time=io.netty.channel.unix.Limits \
    --initialize-at-run-time=io.netty.handler.pcap.PcapWriteHandler\$WildcardAddressHolder \
    --initialize-at-run-time=io.netty.handler.ssl.BouncyCastleAlpnSslUtils \
    --initialize-at-run-time=io.quarkus.runtime.graal.InetRunTime \
    --initialize-at-run-time=jakarta.el.ELManager \
    --initialize-at-run-time=java.rmi \
    --initialize-at-run-time=jdk.package \
    --initialize-at-run-time=org.jboss.threads.JDKSpecific\$ThreadAccess \
    --initialize-at-run-time=org.jboss.logmanager.handlers.SyslogHandler \
    --initialize-at-run-time=sun.rmi \
    -H:+PrintClassInitialization \
    -H:BuildOutputJSONFile=target/build-output-layer-base.json \
    -H:LayerCreate=libquarkusbaselayer.nil,module=java.base,package=io.quarkus.*,package=io.netty.*,package=jakarta.* \
    --features=io.quarkus.runner.Feature \
    -cp "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*":getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/extracted-classes.jar \
    -o libquarkusbaselayer -H:Path=./target

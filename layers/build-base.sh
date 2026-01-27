#!/usr/bin/env bash
set -eux

mkdir -p target

#pushd dummy-module
#make
#popd

#    --initialize-at-run-time=sun.net.ext.ExtendedSocketOptions,com.sun.media.sound.PortMixerProvider \
#    --initialize-at-run-time=sun.net.ext.ExtendedSocketOptions \
#    --initialize-at-run-time=jdk.jpackage.internal \
# --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
#    --debug-attach=*:8000 \
#    -H:ApplicationLayerOnlySingletons=org.graalvm.nativeimage.impl.RuntimeSystemPropertiesSupport \
# --trace-object-instantiation=jdk.internal.jimage.ImageReader \
# --trace-object-instantiation=java.rmi.server.ObjID \
#--initialize-at-build-time=jdk.internal.jimage \

#--initialize-at-build-time= \
#                          --initialize-at-build-time=jdk.internal.jimage.ImageLocation \
#                          --initialize-at-build-time=jdk.internal.jimage.ImageReader \
#                          --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Directory \
#                          --initialize-at-build-time=jdk.internal.jimage.ImageReader\$SharedImageReader \
#                          --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Resource \
#                          --initialize-at-run-time=java.rmi.server.ObjID \
#                          --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
#                          --initialize-at-run-time=sun.awt.dnd \
#                          --initialize-at-run-time=sun.awt.X11 \
#                          --initialize-at-run-time=sun.awt.X11GraphicsConfig \
#                          --initialize-at-run-time=sun.rmi.registry \
#                          --initialize-at-run-time=sun.rmi.transport.DGCClient \

#native-image \
#    -H:+PrintClassInitialization \
#    -H:LayerCreate=base-layer.nil,module=java.base,module=dummy.module --module-path dummy-module/target/dummy.module.jar -o target/libjavabase

#native-image \
#    --initialize-at-build-time= \
#    --initialize-at-run-time=java.rmi.server.ObjID \
#    --initialize-at-run-time=jdk.jpackage.internal.LinuxPackageArch\$DebPackageArch \
#    --initialize-at-run-time=sun.awt.dnd \
#    --initialize-at-run-time=sun.awt.X11 \
#    --initialize-at-run-time=sun.awt.X11GraphicsConfig \
#    --initialize-at-run-time=sun.rmi.registry \
#    --initialize-at-run-time=sun.rmi.transport \
#    --initialize-at-build-time=jdk.internal.jimage.ImageLocation \
#    --initialize-at-build-time=jdk.internal.jimage.ImageReader \
#    --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Directory \
#    --initialize-at-build-time=jdk.internal.jimage.ImageReader\$Resource \
#    --initialize-at-build-time=jdk.internal.jimage.ImageReader\$SharedImageReader \
#    -H:LayerCreate=libjavabaselayer.nil,module=java.base -o libjavabaselayer -H:Path=./target

native-image \
    -H:+PrintClassInitialization \
    --initialize-at-run-time=java.util.logging.ConsoleHandler \
    -H:BuildOutputJSONFile=target/build-output-base.json \
    -H:LayerCreate=libjavabaselayer.nil,module=java.base,package=sun.management.spi,package=sun.util.resources.cldr.ext,package=sun.text.resources.cldr.ext -o libjavabaselayer -H:Path=./target


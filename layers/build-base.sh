#!/usr/bin/env bash
set -eux

mkdir -p target

pushd dummy-module
make
popd

#    --initialize-at-run-time=sun.net.ext.ExtendedSocketOptions,com.sun.media.sound.PortMixerProvider \
#    --debug-attach=*:8000 \
#    -H:ApplicationLayerOnlySingletons=org.graalvm.nativeimage.impl.RuntimeSystemPropertiesSupport \

native-image \
    -H:+PrintClassInitialization \
    --initialize-at-run-time=sun.net.ext.ExtendedSocketOptions \
    -H:LayerCreate=base-layer.nil,module=java.base,module=dummy.module --module-path dummy-module/target/dummy.module.jar -o target/libjavabase

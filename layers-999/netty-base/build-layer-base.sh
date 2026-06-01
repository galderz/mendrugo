#!/usr/bin/env bash
set -eux

native_image=$JAVA_HOME/bin/native-image

mkdir -p target

${native_image} \
    -H:+PrintClassInitialization \
    -H:BuildOutputJSONFile=target/build-output-layer-base.json \
    -H:LayerCreate=libnettybaselayer.nil,module=java.base,package=io.netty.* \
    --initialize-at-build-time="" \
    -cp "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*" \
    -o libnettybaselayer -H:Path=./target

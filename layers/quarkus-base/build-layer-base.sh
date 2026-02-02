#!/usr/bin/env bash
set -eux

native_image=$HOME/src/mandrel/sdk/latest_graalvm_home/bin/native-image

mkdir -p target

${native_image} \
    -H:+PrintClassInitialization \
    -H:BuildOutputJSONFile=target/build-output-layer-base.json \
    -H:LayerCreate=libquarkusbaselayer.nil,module=java.base,package=io.quarkus.*,package=io.netty.*,package=jakarta.* \
    -cp "getting-started/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar/lib/*" \
    -o libquarkusbaselayer -H:Path=./target

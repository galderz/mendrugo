#!/usr/bin/env bash
set -eux

mkdir -p target

pushd dummy-module
make
popd

native-image -H:LayerCreate=base-layer.nil,module=java.base,module=dummy.module --module-path dummy-module/target/dummy.module.jar -o target/libjavabase

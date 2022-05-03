#!/usr/bin/env bash

set -x -e

make clean-all

homeBench()
{
    local graalvm_home=$1
    local native_args=$2
    local builder_image=$3

    local target_dir="target/${graalvm_home}"

    GRAALVM_HOME=$graalvm_home \
    NATIVE_ARGS=${native_args} \
    make clean

    mkdir -p $target_dir
    touch "$target_dir/console.log"

    for i in `seq 1 10`
    do
        GRAALVM_HOME=$graalvm_home \
        NATIVE_ARGS=${native_args} \
        BUILDER_IMAGE=quay.io/quarkus/$builder_image \
        make bench | tee -a "$target_dir/console.log"
    done
}

homeBench graalvm-ce-java17-21.3.1 "" "ubi-quarkus-native-image:21.3.1-java17"
homeBench graalvm-ce-java17-22.0.0.2 "" "ubi-quarkus-native-image:22.0.0-java17"
homeBench graalvm-ce-java17-22.1.0 "" "ubi-quarkus-native-image:22.1.0-java17"

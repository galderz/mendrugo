#!/usr/bin/env bash

set -x -e

make clean-all

homeBench()
{
    local graalvm_home=$1
    local native_args=$2

    local target_dir="target/${graalvm_home}+${native_args}"

    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make clean

    mkdir -p $target_dir
    touch "$target_dir/console.log"

    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make reports
    cp -r hibernate-orm-quickstart/target/hibernate-orm-quickstart-1.0.0-SNAPSHOT-native-image-source-jar/reports $target_dir

    for i in `seq 1 10`
    do
        GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make bench | tee -a "$target_dir/console.log"
    done

    mv /tmp/times.log $target_dir
}

for graalvm_home in \
    graalvm-ce-java17-22.1.0
do
    for native_args in \
        "-Ob" \
        ""
    do
        homeBench $graalvm_home $native_args
    done
done

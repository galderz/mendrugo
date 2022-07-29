#!/usr/bin/env bash

set -x -e

make clean-all

homeBench()
{
    local graalvm_home=$1
    local native_args=$2

    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make clean
    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make bench
}

#        graalvm-ce-java17-21.3.1 <- fails with 2g
for native_args in \
    "-J-Xmx1g"
do
    for graalvm_home in \
        graalvm-ce-java17-22.2.0
    do
        homeBench $graalvm_home $native_args
    done
done

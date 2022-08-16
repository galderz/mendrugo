#!/usr/bin/env bash

set -e

make clean-all

run()
{
    local graalvm_home=$1
    local native_args=$2
    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make clean
    GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make bench
    # GRAALVM_HOME=$graalvm_home NATIVE_ARGS=${native_args} make profiling
}

#run graalvm-ce-java17-22.2.0 "-J-XX:+UseNUMA,-J-Xlog:os=debug"
#run graalvm-ce-java17-22.2.0 "-J-XX:+UseNUMA"
#run graalvm-ce-java17-22.2.0 "-J-XX:+UseNUMA,-J-Xlog:gc*\\\\,safepoint=info:file=gc_%p_%t.log:uptime:filecount=4\\\\,filesize=50M"
run graalvm-ce-java17-22.2.0 "-J-XX:+UseNUMA"

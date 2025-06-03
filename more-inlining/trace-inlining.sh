#!/usr/bin/env bash

set -ex

APP_DIR=quarkus-reactive-beer
CE_HOME=$HOME/opt/graal-24
EE_HOME=$HOME/opt/ee-graal-24

build()
{
    local extra_args=$1
    local graalvm_home=$2
    local log_id=$3
    local common_args="-H:+TraceInlining,-H:NumberOfThreads=1,-H:DeadlockWatchdogInterval=0"

    make clean
    GRAALVM_HOME=${graalvm_home} NATIVE_BUILD_ARGS="${common_args},${extra_args}" make build 2>&1 | tee ${log_id}
}

# Round 7
build "-H:-ClampMLInferredProfiles,-H:-MLProfileInference,-H:-MLProfileInferenceGuards" ${EE_HOME} "ee-disable-ml.trace-inlining"
build "" ${EE_HOME} "ee-base.trace-inlining"
build "" ${CE_HOME} "ce.trace-inlining"

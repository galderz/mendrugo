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
    local common_args="-H:+TraceInlining,-H:NumberOfThreads=2,-H:DeadlockWatchdogInterval=0,-J-Djdk.graal.LogFile=${log_id}.log"

    make clean
    GRAALVM_HOME=${graalvm_home} NATIVE_BUILD_ARGS="${common_args},${extra_args}" make build
    mv $APP_DIR/target/quarkus-reactive-beer-1.0.0-SNAPSHOT-native-image-source-jar/${log_id}.log $APP_DIR
}

# Round 7
build "-H:-ClampMLInferredProfiles,-H:-MLProfileInference,-H:-MLProfileInferenceGuards" ${EE_HOME} "ee-disable-ml.trace-inlining"
build "" ${EE_HOME} "ee-base.trace-inlining"
build "" ${CE_HOME} "ce.trace-inlining"

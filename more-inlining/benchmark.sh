#!/usr/bin/env bash

set -eux

APP_DIR=quarkus-reactive-beer

bench()
{
    local native_build_args=$1

    make clean
    NATIVE_BUILD_ARGS="" make build
    pushd $APP_DIR
    ./benchmarks.sh -n
    popd
}

bench "-H:MaxNodesInTrivialMethod=40"
bench "-H:MaxNodesInTrivialMethod=80"
bench "-H:MaxInvokesInTrivialMethod=2"
bench "-H:MaxInvokesInTrivialMethod=4"
bench "-H:MaxNodesInTrivialLeafMethod=80"
bench "-H:MaxNodesInTrivialLeafMethod=160"
bench ""

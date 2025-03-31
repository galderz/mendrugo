#!/usr/bin/env bash

set -eux

APP_DIR=quarkus-reactive-beer

bench()
{
    local native_build_args=$1

    make clean
    NATIVE_BUILD_ARGS="$native_build_args" make build
    pushd $APP_DIR/scripts
    ./benchmark.sh -n
    popd
}

# Round 2
bench ""
bench "-H:MaxNodesInTrivialMethod=80"
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=80"
bench "-H:MaxNodesInTrivialMethod=160"
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=160"
bench "-H:MaxNodesInTrivialMethod=320"
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320"
bench "-H:MaxNodesInTrivialMethod=640"
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=640"

# Round 1
# bench "-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxInvokesInTrivialMethod=2"
# bench "-H:MaxInvokesInTrivialMethod=4"
# bench "-H:MaxNodesInTrivialLeafMethod=80"
# bench "-H:MaxNodesInTrivialLeafMethod=160"
# bench ""

#!/usr/bin/env bash

set -ex

APP_DIR=quarkus-reactive-beer

bench()
{
    local native_build_args=$1

    make clean
    if [ ! -z $2 ]
    then
        GRAALVM_HOME=$2 NATIVE_BUILD_ARGS="$native_build_args" make build
    else
        NATIVE_BUILD_ARGS="$native_build_args" make build
    fi

    pushd $APP_DIR/scripts
    JAVA_HOME=$HOME/opt/boot-java-21 PATH=$JAVA_HOME/bin:$PATH ./benchmark.sh -n -p
    popd
}

pgo()
{
    local native_build_args=$1

    make clean
    GRAALVM_HOME=$2 NATIVE_BUILD_ARGS="$native_build_args,--pgo-instrument" make build

    pushd $APP_DIR/scripts
    JAVA_HOME=$HOME/opt/boot-java-21 PATH=$JAVA_HOME/bin:$PATH ./benchmark.sh -n -p
    popd

    # Touch file to force a rebuild
    touch $APP_DIR/pom.xml
    GRAALVM_HOME=$2 NATIVE_BUILD_ARGS="$native_build_args,--pgo=../../scripts/default.iprof" make build

    pushd $APP_DIR/scripts
    JAVA_HOME=$HOME/opt/boot-java-21 PATH=$JAVA_HOME/bin:$PATH ./benchmark.sh -n -p
    popd
}

# Round 6
pgo "" "$HOME/opt/ee-graal-21"
bench ""
bench "" "$HOME/opt/ee-graal-21"
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320"
bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160" # best

# Round 5
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxInvokesInTrivialMethod=32,-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=160"

# # Round 4
# bench ""
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxInvokesInTrivialMethod=8,-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxInvokesInTrivialMethod=8,-H:MaxNodesInTrivialMethod=80" # error
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxInvokesInTrivialMethod=32,-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxInvokesInTrivialMethod=32,-H:MaxNodesInTrivialMethod=80"

# # Round 3
# bench ""
# bench "-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160" # best
# bench "-H:MaxInvokesInTrivialMethod=8,-H:MaxNodesInTrivialMethod=160" # error
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=8,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxNodesInTrivialMethod=640"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=640"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=640"
# bench "-H:MaxInvokesInTrivialMethod=8,-H:MaxNodesInTrivialMethod=640"
# bench "-H:MaxInvokesInTrivialMethod=16,-H:MaxNodesInTrivialMethod=640"

# Round 2
# bench ""
# bench "-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=160"
# bench "-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320" # best
# bench "-H:MaxNodesInTrivialMethod=640"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=640"

# Round 1
# bench "-H:MaxNodesInTrivialMethod=40"
# bench "-H:MaxNodesInTrivialMethod=80"
# bench "-H:MaxInvokesInTrivialMethod=2"
# bench "-H:MaxInvokesInTrivialMethod=4"
# bench "-H:MaxNodesInTrivialLeafMethod=80"
# bench "-H:MaxNodesInTrivialLeafMethod=160"
# bench ""

#!/usr/bin/env bash

set -ex

APP_DIR=quarkus-reactive-beer
CE_HOME=$HOME/opt/graal-24
EE_HOME=$HOME/opt/ee-graal-24

bench()
{
    local native_build_args=$1
    local graalvm_home=$2

    make clean
    GRAALVM_HOME=${graalvm_home} NATIVE_BUILD_ARGS="${native_build_args}" make build

    pushd $APP_DIR/scripts
    JAVA_HOME=$HOME/opt/boot-java-21 PATH=$JAVA_HOME/bin:$PATH ./benchmark.sh -n -p
    popd
}

pgo()
{
    local native_build_args=$1
    local graalvm_home=$2

    make clean
    GRAALVM_HOME=${graalvm_home} NATIVE_BUILD_ARGS="$native_build_args,--pgo-instrument" make build

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

# Round 7
bench "-H:+TraceInlining" ${EE_HOME}
bench "-H:+TraceInlining,-H:-ClampMLInferredProfiles,-H:-MLProfileInference,-H:-MLProfileInferenceGuards" ${EE_HOME}
pgo "" ${EE_HOME}
bench "-H:+TraceInlining" ${CE_HOME}
bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320" ${CE_HOME}
bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160" ${CE_HOME} # best

# Round 6
# pgo "" "$HOME/opt/ee-graal-21"
# bench ""
# bench "" "$HOME/opt/ee-graal-21"
# bench "-H:MaxInvokesInTrivialMethod=2,-H:MaxNodesInTrivialMethod=320"
# bench "-H:MaxInvokesInTrivialMethod=4,-H:MaxNodesInTrivialMethod=160" # best

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

#!/usr/bin/env bash

set -e
set -x

benchmark()
{
    local quarkus=$1
    local app=jax-rs-minimal
    local xmx=1g
    local dir=target/benchmarks/memory.$xmx

    rm -drf $dir
    mkdir -p $dir

    for quarkus in \
	    2.13-debug \
	    2.7-22.3-debug
    do
        ./memory-flamegraphs.sh $quarkus $app $xmx

        trialdir=$dir/$quarkus.$app
        mkdir -p $trialdir
        cp -r target/flamegraphs $trialdir
    done
}

benchmark

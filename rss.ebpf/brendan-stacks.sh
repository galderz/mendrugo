#!/usr/bin/env bash

set -e
set -x

stackcount()
{
    local stacksdir=$1
    local name=$2
    local duration=$3
    local pid=$(pidof code-with-quarkus-1.0.0-SNAPSHOT-runner)

    mkdir -p $stacksdir
    rm -f $stacksdir/$name.stacks
    sudo /opt/BPF-tools/old/2017-12-23/mallocstacks.py -p $pid -f $duration > $stacksdir/$name.stacks
}

stackcount target/flamegraphs/stacks malloc-bytes-brendan $1

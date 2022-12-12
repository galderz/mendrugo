#!/usr/bin/env bash

set -e
set -x

stackcount()
{
    local stacksdir=$1
    local name=$2
    local duration=$3

    mkdir -p $stacksdir
    rm -f $stacksdir/$name.stacks
    sudo /opt/BPF-tools/old/2017-12-23/mallocstacks.py -p $(pidof quarkus-runner) -f $duration > $stacksdir/$name.stacks
}

stackcount target/stacks malloc-bytes-brendan $1

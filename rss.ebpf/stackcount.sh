#!/usr/bin/env bash

set -e
set -x

stackcount()
{
    local stacksdir=$1
    local name=$2
    local function=$3
    local duration=$4
    local pid=$(pidof code-with-quarkus-1.0.0-SNAPSHOT-runner)

    mkdir -p $stacksdir
    rm -f $stacksdir/$name.stacks
    sudo /usr/share/bcc/tools/stackcount -D $duration -P -p $pid -U "$function" > $stacksdir/$name.stacks
}

stackcount target/flamegraphs/stacks $1 $2 $3

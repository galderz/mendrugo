#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local filename=$1
    local version=$2
    local eventloops=$3
    local app=jax-rs-minimal

    APP=$app QUARKUS=$version XMX=64m EVENT_LOOPS=$eventloops make &

    sleep 2

    pid=`pidof quarkus-runner`

    rss=`ps -p $pid -o rss=`
    echo $rss >> $filename

    kill -SIGINT $pid

    sleep 2
}

trial()
{
    local filename=$1
    local version=$2

    for eventloops in \
	1 \
        2 \
	4 \
	8 \
	16 \
	32 \
	64
    do
	startstop $filename $version $eventloops
    done
}

dir=target/benchmarks/event-loops
rm -drf $dir
mkdir -p $dir
trial $dir/2.7-22.3.rss 2.7-22.3
trial $dir/2.13.rss 2.13

#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local filename=$1
    local version=$2
    local xmx=$3
    local app=jax-rs-minimal

    APP=$app QUARKUS=$version XMX=$xmx make &

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

    for xmx in \
      64m \
	    128m \
	    256m \
	    512m \
	    1g \
	    2g \
	    4g
    do
        startstop $filename $version $xmx
    done
}

dir=target/benchmarks/jax-rs-minimal
rm -drf $dir
mkdir -p $dir
trial $dir/2.13.4-29408-29842.rss 2.13.4-29408-29842
trial $dir/2.7-22.3.rss 2.7-22.3
trial $dir/2.13.rss 2.13

#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local filename=$1
    local version=$2
    local app=jax-rs-minimal

    if [ -z "$3" ]; then
      APP=$app QUARKUS=$version make &
    else
      local xmx=$3
      APP=$app QUARKUS=$version XMX=$xmx make &
    fi

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

    startstop $filename $version

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
trial $dir/2.7-22.3.rss 2.7-22.3
trial $dir/2.13.rss 2.13

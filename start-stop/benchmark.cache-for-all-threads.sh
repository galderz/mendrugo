#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local app=jax-rs-minimal
    local version=2.13
    local cache_all_threads=$1
    local filename=$2

    if [ -z "$3" ]; then
      APP=$app QUARKUS=$version CACHE_ALL_THREADS=$cache_all_threads make &
    else
      local xmx=$3
      APP=$app QUARKUS=$version XMX=$xmx CACHE_ALL_THREADS=$cache_all_threads make &
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
    local cache_all_threads=$1
    local filename=$2
    local xmx=$3

    startstop $cache_all_threads $filename

    for xmx in \
	64m \
	    128m \
	    256m \
	    512m \
	    1g \
	    2g \
	    4g
    do
	startstop $cache_all_threads $filename $xmx
    done
}

dir=target/benchmarks/2.13-jax-rs-minimal
rm -drf $dir
mkdir -p $dir
trial true $dir/with-cache.rss
trial false $dir/without-cache.rss

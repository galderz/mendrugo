#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local dir=$1
    local version=$2
    local xmx=$3
    local app=jax-rs-minimal
    local rssfile=$dir/memory.rss
    local logfile=$dir/out.$xmx.log

    mkdir -p $dir

    APP=$app QUARKUS=$version XMX=$xmx make &> $logfile &

    { tail -n +1 -f $logfile & } | sed -n '/started in/q'

    pid=`pidof quarkus-runner`

    psrss=`ps -p $pid -o rss=`
    pmaprss=`pmap -X $pid | tail -1 | awk -F ' ' '{print $2}'`

    kill -SIGINT $pid
    sleep 2

    maxrss=`grep "Maximum resident set size" $logfile | awk -F ' ' '{print $NF}'`

    printf '%s,%s,%s\n' "$psrss" "$pmaprss" "$maxrss" >> $rssfile
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

dir=target/benchmarks/jax-rs-minimal.xmx
rm -drf $dir
mkdir -p $dir
trial $dir/preconfig 999
trial $dir/2.13.4-29408-29842 2.13.4-29408-29842
trial $dir/2.7-22.3 2.7-22.3
trial $dir/2.13 2.13

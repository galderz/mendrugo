#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local dir=$1
    local version=$2
    local xmx=$3
    local index=$4
    local app=jax-rs-minimal
    local rssfile=$dir/memory.rss.csv
    local logfile=$dir/out.$index.log

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

    for i in {0..9}
    do
        startstop $filename $version 1g $i
    done
}

dir=target/benchmarks/jax-rs-minimal
rm -drf $dir
mkdir -p $dir
trial $dir/preconfig 999
trial $dir/2.13.4-29408-29842 2.13.4-29408-29842
trial $dir/2.7-22.3 2.7-22.3
trial $dir/2.13 2.13

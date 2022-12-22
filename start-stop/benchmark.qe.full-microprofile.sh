#!/usr/bin/env bash

set -e
set -x

startstop()
{
    local dir=$1
    local version=$2
    local xmx=$3
    local app=$4
    local rssfile=$dir/memory.rss.csv
    local logfile=$dir/out.log

    mkdir -p $dir

    APP=$app QUARKUS=$version XMX=$xmx make startstop &> $logfile

    # todo extract average numbers from output and put in csv file
}

trial()
{
    local filename=$1
    local version=$2

    startstop $filename $version 1g
}

app=jax-rs-minimal
dir=target/benchmarks/qe.$app
rm -drf $dir
mkdir -p $dir
trial $dir/preconfig 999 $app
trial $dir/2.13.4-29408-29842 2.13.4-29408-29842 $app
trial $dir/2.7-22.3 2.7-22.3 $app
trial $dir/2.13 2.13 $app

#!/usr/bin/env bash

set -e
#set -x

startstop()
{
    local xmx=$1
    local app=jax-rs-minimal
    local version=2.13

    APP=$app QUARKUS=$version XMX=$xmx make &
    sleep 2

    pid=`pidof quarkus-runner`

    rss=`ps -p $pid -o rss=`
    echo $rss

    kill -SIGINT $pid
    sleep 2
}

for xmx in \
    64m \
    128m
do
    startstop $xmx
done
	

#!/usr/bin/env bash

set -e

for name in graalvm-20.3-dev graalvm-21.2-dev
do
    NAME=$name make clean
    NAME=$name make
    for i in `seq 1 10`
    do
        NAME=$name make | tee -a "target/$name/console.log"
    done
done

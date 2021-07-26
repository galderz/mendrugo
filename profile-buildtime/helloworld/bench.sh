#!/usr/bin/env bash

set -e

make clean-all

homeBench()
{
    local graalvm_home=$1
    GRAALVM_HOME=$graalvm_home make clean
    GRAALVM_HOME=$graalvm_home make reports | tee -a "target/$graalvm_home/console.log"
    for i in `seq 1 10`
    do
        GRAALVM_HOME=$graalvm_home make | tee -a "target/$graalvm_home/console.log"
    done
}

for graalvm_home in graalvm-20.3-dev graalvm-21.2-dev
do
    homeBench $graalvm_home
done

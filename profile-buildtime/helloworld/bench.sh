#!/usr/bin/env bash

set -e

make clean-all

argsBench()
{
    local name=$1
    local args=$2
    NAME=$name EXTRA_ARGS=$args make clean
    NAME=$name EXTRA_ARGS=$args make reports | tee -a "target/$name/console.log"
    for i in `seq 1 10`
    do
        NAME=$name EXTRA_ARGS=$args make | tee -a "target/$name/console.log"
    done
}

argsBench "parse_once" "-H:-ParseOnce"
argsBench "locale_opt" "-H:+LocalizationOptimizedMode"
argsBench "locale_opt-parse_once" "-H:+LocalizationOptimizedMode,-H:-ParseOnce"

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

#!/usr/bin/env bash

set -e

make clean-all

homeBench()
{
    local graalvm_home=$1
    GRAALVM_HOME=$graalvm_home make clean

    mkdir -p "target/$graalvm_home"
    touch "target/$graalvm_home/console.log"

    GRAALVM_HOME=$graalvm_home make reports
    for i in `seq 1 10`
    do
        GRAALVM_HOME=$graalvm_home make | tee -a "target/$graalvm_home/console.log"
    done
}

for graalvm_home in "graalvm-ce-java11-20.3.3" "graalvm-ce-java11-21.0.0.2" "graalvm-ce-java11-21.1.0" "graalvm-ce-java11-21.2.0" "graalvm-21.3-dev"
do
    homeBench $graalvm_home
done

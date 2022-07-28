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
    cp -r hibernate-orm-quickstart/target/hibernate-orm-quickstart-1.0.0-SNAPSHOT-native-image-source-jar/reports "target/$graalvm_home"

    for i in `seq 1 10`
    do
        GRAALVM_HOME=$graalvm_home make bench | tee -a "target/$graalvm_home/console.log"
    done

    mv /tmp/times.log target/$graalvm_home
}

for graalvm_home in \
    graalvm-ce-java17-22.2.0 \
    graalvm-ce-java17-22.1.0 \
    graalvm-ce-java17-22.0.0.2 \
    graalvm-ce-java17-21.3.1
do
    homeBench $graalvm_home
done

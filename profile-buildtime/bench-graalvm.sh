#!/usr/bin/env bash

set -e

bench()
{
    local java_home=$HOME/opt/$1
    local app="hibernate-orm-quickstart"
    local target_dir="../target"

    pushd $app

    make clean-app

    mkdir -p "$target_dir/$java_home"
    touch "$target_dir/$java_home/console.log"

    make build \
        JAVA_HOME=$java_home \
        MAVEN_ARGS="-Dquarkus.native.enable-reports"
    cp -r $app/target/$app-1.0.0-SNAPSHOT-native-image-source-jar/reports "$target_dir/$java_home"

    for i in `seq 1 10`
    do
        rm -f $app/target/$app-1.0.0-SNAPSHOT-runner
        make build \
            JAVA_HOME=$java_home \
            | tee -a "$target_dir/$java_home/console.log"
    done

    mv /tmp/times.log $target_dir/$java_home

    popd
}

for java_home in \
    graalvm-community-openjdk-17.0.7+7.1 \
    graalvm-ce-java17-22.3.0 \
    graalvm-ce-java17-21.3.3.1
do
    bench $java_home
done

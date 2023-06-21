#!/usr/bin/env bash

set -e

if [ -z "$NUM_ITERATIONS" ]; then
    echo "NUM_ITERATIONS is not defined"
    exit 1
fi

bench()
{
    local java_dir=$1
    local java_home=$HOME/opt/$1
    local app="hibernate-orm-quickstart"
    local target_dir="../target/$java_dir"

    pushd $app

    make clean-app

    rm -drf "$target_dir"
    mkdir -p "$target_dir"
    touch "$target_dir/console.log"

    make build \
        JAVA_HOME=$java_home \
        MAVEN_ARGS="-Dquarkus.native.enable-reports" \
        NATIVE_BUILD_ARGS="--verbose" \
       | tee -a "$target_dir/console.log"

    cp -r $app/target/$app-1.0.0-SNAPSHOT-native-image-source-jar/reports "$target_dir"

    for i in `seq 1 $NUM_ITERATIONS`
    do
        rm -f $app/target/$app-1.0.0-SNAPSHOT-runner
        make build \
            JAVA_HOME=$java_home \
            | tee -a "$target_dir/console.log"
    done

    popd
}

for java_home in \
    graalvm-ce-java17-22.3.2 \
    graalvm-community-openjdk-17.0.7+7.1
do
    bench $java_home
done

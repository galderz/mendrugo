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
#    local iter_zero_dir="$target/0"

    pushd $app

    make clean-app

    mkdir -p "$target_dir"

#    make build \
#        JAVA_HOME=$java_home \
#        MAVEN_ARGS="-Dquarkus.native.enable-reports" \
#        NATIVE_BUILD_ARGS="--verbose"
#    cp "$app-1.0.0-SNAPSHOT-native-image-source-jar/$app-1.0.0-SNAPSHOT-runner-build-output-stats.json" "$iter_zero_dir"
#    cp -r $app/target/$app-1.0.0-SNAPSHOT-native-image-source-jar/reports "$iter_zero_dir"

    for i in `seq 1 $NUM_ITERATIONS`
    do
        rm -f $app/target/$app-1.0.0-SNAPSHOT-runner

        make build \
            JAVA_HOME=$java_home

        iter_dir="$target_dir/$i"
        mkdir -p "$iter_dir"
        cp "$app-1.0.0-SNAPSHOT-native-image-source-jar/$app-1.0.0-SNAPSHOT-runner-build-output-stats.json" "$iter_dir"
    done

    popd
}

rm -drf target

for java_home in \
    graalvm-ce-java17-22.3.2 \
    graalvm-community-openjdk-17.0.7+7.1
do
    bench $java_home
done

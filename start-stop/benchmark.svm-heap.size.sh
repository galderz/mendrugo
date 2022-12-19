#!/usr/bin/env bash

set -e
set -x

benchmark()
{
    local app=jax-rs-minimal
    local dir=target/benchmarks/svm-heap

    rm -drf $dir
    mkdir -p $dir

    for quarkus in \
        2.7 \
        2.7-22.3 \
        2.13
    do
        filename=$dir/$quarkus.$app.size
        count=3
        for i in $(seq $count); do
            runner=target/$quarkus/quarkus-startstop/app-$app/target/quarkus-runner
            QUARKUS=$quarkus APP=$app make clean $runner
            readelf -SW $runner | grep svm_heap | awk -F ' ' '{print $6}' >> $filename
        done
    done

}

benchmark

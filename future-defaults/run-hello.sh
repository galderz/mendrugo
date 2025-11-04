#!/usr/bin/env bash

set -e

CLEAN="false"

CONNECTIONS=10
DURATION=40
RATE=0
URL=hello
THREADS=2

FULL_URL=http://localhost:8080/${URL}
HYPERFOIL_HOME=${HOME}/opt/hyperfoil
WARMUP=$((${DURATION}*2/5))

# Check for clean parameter
if [[ "$1" == "--clean=true" ]]; then
    read -p "Are you sure you want to clean first? (yes/no): " RESPONSE
    if [[ "$RESPONSE" == "yes" ]]; then
        CLEAN="true"
    else
        echo "Exiting because you don't want to apply the changes."
        exit 1
    fi
fi

build()
{
    local quarkus_home=$1
    local native_build_args=$2
    if [[ $CLEAN == "true" ]]; then
        QUARKUS_HOME=${quarkus_home} make clean-app clean-quarkus
    fi

    QUARKUS_HOME=${quarkus_home} make build-quarkus
    QUARKUS_HOME=${quarkus_home} NATIVE_BUILD_ARGS=${native_build_args} make build
}

peakperf()
{
    local binary=quickstart/target/getting-started-1.0.0-SNAPSHOT-runner
    local java_home=${HOME}/opt/java-25

    echo "----- Benchmarking endpoint ${FULL_URL}"
    trap 'echo "cleaning up quarkus process";kill ${quarkus_pid}' SIGINT SIGTERM SIGKILL

    numactl --localalloc --physcpubind=29,30 ${binary} -Dquarkus.vertx.event-loops-pool-size=${THREADS} &
    quarkus_pid=$!
    sleep 2
    echo "----- Quarkus running at pid $quarkus_pid using ${THREADS} I/O threads"

    echo "----- Start all-out test and profiling"
    JAVA_HOME=${java_home} PATH=${java_home}/bin:${PATH} numactl --localalloc --cpunodebind=1-6 ${HYPERFOIL_HOME}/bin/wrk.sh -c ${CONNECTIONS} -t ${THREADS} -d ${DURATION}s ${FULL_URL} &
    wrk_pid=$!

    echo "----- Waiting $WARMUP seconds before collecting pid stats"
    sleep $WARMUP

    echo "----- Showing stats for $WARMUP seconds"

    pidstat -p $quarkus_pid -u -r 1 &
    pidstat_pid=$!
    sleep $WARMUP
    kill -SIGTERM $pidstat_pid

    echo "----- Stopped stats, waiting load to complete"

    wait $wrk_pid

    echo "----- Profiling and workload completed: killing server"

    kill -SIGTERM $quarkus_pid

    echo "----- Native binary size"
    ls -lha ${binary}
}

#build "${HOME}/src/quarkus-future-defaults.base" ""
peakperf

#build "${HOME}/src/quarkus-future-defaults" "--future-defaults=all"
#peakperf

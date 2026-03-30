#!/usr/bin/env bash
set -eux

JAVA_HOME=$HOME/src/mandrel/sdk/latest_graalvm_home \
    ./mvnw package -Dnative -DskipTests \
    -Dquarkus.native.native-image-xmx=6g \
    -Dquarkus.native.additional-build-args=-H:+PrintClassInitialization,-J-Dsvm.traceClassInit=true

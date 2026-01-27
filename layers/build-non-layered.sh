#!/usr/bin/env bash
set -eux

JAVA_HOME=$HOME/src/mandrel/sdk/latest_graalvm_home \
    ./mvnw package -Dnative -DskipTests

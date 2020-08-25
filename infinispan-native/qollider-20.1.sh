#!/usr/bin/env bash

ISPN_QUARKUS=https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17

set -x -e
pushd ~/1/qollider

git pull

./qollider.sh \
    jdk-get \
    --url https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u265-b01/OpenJDK8U-jdk_x64_linux_hotspot_8u265b01.tar.gz

./qollider.sh \
    graal-build \
    --tree https://github.com/graalvm/mandrel/tree/mandrel/20.1

./qollider.sh \
    maven-build \
    --tree https://github.com/infinispan/infinispan/tree/11.0.x \
    --additional-build-args \
        -s \
        maven-settings.xml

./qollider.sh \
    maven-build \
    --tree https://github.com/quarkusio/quarkus/tree/master

./qollider.sh \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -pl \
        '!:infinispan-quarkus-integration-test-server' \
        -Dquarkus.version=999-SNAPSHOT

./qollider.sh \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -Dnative \
        -pl \
        :infinispan-quarkus-server-runner \
        -Dquarkus.version=999-SNAPSHOT \
        -Dquarkus.native.debug.enabled=true \
        dependency:sources

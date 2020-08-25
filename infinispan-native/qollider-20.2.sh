#!/usr/bin/env bash

ISPN_QUARKUS=https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17

set -e

source ${HOME}/.dotfiles/qollider/qollider.sh

qollider \
    jdk-build \
    --tree https://github.com/openjdk/jdk11u-dev/tree/master

qollider \
    mandrel-build \
    --tree https://github.com/graalvm/mandrel/tree/mandrel/20.2

qollider \
    maven-build \
    --tree https://github.com/infinispan/infinispan/tree/11.0.x \
    --additional-build-args \
        -s \
        maven-settings.xml

qollider \
    maven-build \
    --tree https://github.com/quarkusio/quarkus/tree/master

qollider \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -pl \
        '!:infinispan-quarkus-integration-test-server' \
        -Dquarkus.version=999-SNAPSHOT

qollider \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -Dnative \
        -pl \
        :infinispan-quarkus-server-runner \
        -Dquarkus.version=999-SNAPSHOT \
        -Dquarkus.native.debug.enabled=true \
        dependency:sources
#!/usr/bin/env bash

# Avoid MBean server issue with 20.2
#ISPN_QUARKUS=https://github.com/infinispan/infinispan-quarkus/tree/master
ISPN_QUARKUS=https://github.com/galderz/infinispan-quarkus/tree/t_fix_cli_java_26
ISPN_QUARKUS_NATIVE_PROFILE=native-noargs
ISPN_VERSION=12.0.0-SNAPSHOT

set -e

source ${HOME}/.dotfiles/qollider/qollider.sh
git pull

qollider \
    jdk-build \
    --tree https://github.com/openjdk/jdk11u-dev/tree/master

qollider \
    mandrel-build \
    --tree https://github.com/graalvm/mandrel/tree/mandrel/20.2

qollider \
    maven-build \
    --tree https://github.com/infinispan/infinispan/tree/master \
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
        -D${ISPN_QUARKUS_NATIVE_PROFILE} \
        -pl \
        :infinispan-quarkus-server-runner \
        -Dquarkus.version=999-SNAPSHOT \
        -Dquarkus.native.debug.enabled=true \
        -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:+PreserveFramePointer,--allow-incomplete-classpath \
        dependency:sources

mv \
    ${HOME}/.qollider/cache/latest/infinispan-quarkus/server-runner/target/infinispan-quarkus-server-runner-${ISPN_VERSION}-runner \
    ${HOME}/.qollider/cache/latest/infinispan-quarkus/server-runner/target/infinispan-tracing-quarkus-server-runner-${ISPN_VERSION}-runner

mv \
    ${HOME}/.qollider/cache/latest/infinispan-quarkus/server-runner/target/infinispan-quarkus-server-runner-${ISPN_VERSION}-runner.debug \
    ${HOME}/.qollider/cache/latest/infinispan-quarkus/server-runner/target/infinispan-tracing-quarkus-server-runner-${ISPN_VERSION}-runner.debug

qollider \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -D${ISPN_QUARKUS_NATIVE_PROFILE} \
        -pl \
        :infinispan-quarkus-server-runner \
        -Dquarkus.version=999-SNAPSHOT \
        -Dquarkus.native.additional-build-args=--allow-incomplete-classpath \
        dependency:sources

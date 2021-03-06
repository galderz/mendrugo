#!/usr/bin/env bash

# Avoid MBean server issue with 20.2
ISPN_QUARKUS=https://github.com/galderz/infinispan-quarkus/tree/t_quarkus17

set -x -e

source ${HOME}/.dotfiles/qollider/qollider.sh
git pull

# TODO needs downloading static symbols (abstract away?)
#qollider \
#    jdk-get \
#    --url https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.8%2B10/OpenJDK11U-jdk_x64_linux_hotspot_11.0.8_10.tar.gz

qollider \
    jdk-build \
    --tree https://github.com/openjdk/jdk11u-dev/tree/jdk-11.0.8%2B10

qollider \
    mandrel-build \
    --tree https://github.com/graalvm/mandrel/tree/mandrel/20.1

qollider \
    maven-build \
    --tree https://github.com/infinispan/infinispan/tree/11.0.x \
    --additional-build-args \
        -s \
        maven-settings.xml

# Avoid object copy (ease of debug)
# https://github.com/quarkusio/quarkus/tree/master
qollider \
    maven-build \
    --tree https://github.com/galderz/quarkus/tree/t_no_obj_copy

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

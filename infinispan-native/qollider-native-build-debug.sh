#!/usr/bin/env bash

# Avoid MBean server issue with 20.2
ISPN_QUARKUS=https://github.com/infinispan/infinispan-quarkus/tree/master

set -e

source ${HOME}/.dotfiles/qollider/qollider.sh
git pull

qollider \
    maven-build \
    --tree ${ISPN_QUARKUS} \
    --additional-build-args \
        -Dnative \
        -pl \
        :infinispan-quarkus-server-runner \
        -Dquarkus.version=999-SNAPSHOT \
        -Dquarkus.native.additional-build-args=--allow-incomplete-classpath,--debug-attach=*:8000 \
        dependency:sources

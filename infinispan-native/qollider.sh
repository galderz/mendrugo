#!/usr/bin/env bash

set -x -e
pushd ~/1/qollider

git pull

./qollider.sh \
 graal-build \
 --jdk-tree https://github.com/openjdk/jdk11u-dev/tree/master \
 --graal-tree https://github.com/galderz/mandrel/tree/mandrel/20.1

./qollider.sh \
 maven-build \
 --tree https://github.com/infinispan/infinispan/tree/11.0.x

./qollider.sh \
 maven-build \
 --tree https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17 \
  --additional-build-args -pl,'!:infinispan-quarkus-integration-test-server'

./qollider.sh \
 maven-build \
 --tree https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17 \
  --additional-build-args -Dnative,-pl,infinispan-quarkus-server-runner

#!/usr/bin/env bash

set -x -e
pushd ~/1/qollider

git pull

./qollider.sh \
 graal-build \
 --jdk-tree https://github.com/openjdk/jdk11u-dev/tree/master \
 --graal-tree https://github.com/graalvm/mandrel/tree/mandrel/20.1

./qollider.sh \
 maven-build \
 --tree https://github.com/infinispan/infinispan/tree/11.0.x \
  --additional-build-args -s,maven-settings.xml

# Use snapshot Quarkus version that has includes third party to source cache
./qollider.sh \
 maven-build \
 --tree https://github.com/galderz/quarkus/tree/t_debug_symbols_next

./qollider.sh \
 maven-build \
 --tree https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17 \
 --additional-build-args -pl,'!:infinispan-quarkus-integration-test-server',-Dquarkus.version=999-SNAPSHOT,-Dquarkus.native.debug.enabled=true

./qollider.sh \
 maven-build \
 --tree https://github.com/galderz/infinispan-quarkus/tree/t_quarkus16_17 \
  --additional-build-args -Dnative,-pl,:infinispan-quarkus-server-runner

#!/usr/bin/env bash
set -eux

pushd getting-started

./mvnw package -Dnative -DskipTests \
  -Dquarkus.native.additional-build-args=-H:+PrintClassInitialization,-H:-CheckToolchain

popd

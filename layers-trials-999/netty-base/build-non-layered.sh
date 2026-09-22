#!/usr/bin/env bash
set -eux

./mvnw package -Dnative -DskipTests \
  -Dquarkus.native.additional-build-args=-H:+PrintClassInitialization,-H:-CheckToolchain

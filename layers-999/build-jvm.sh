#!/usr/bin/env bash
set -ex

pushd jvm/getting-started

./mvnw package -DskipTests

popd

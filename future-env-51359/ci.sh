#!/usr/bin/env bash

set -e -x

#QUARKUS_NATIVE_ADDITIONAL_BUILD_ARGS_APPEND=--future-defaults=all TEST_MODULE=main make test-quarkus
NATIVE_IMAGE_OPTIONS="--future-defaults=all -H:-CheckToolchain" TEST_MODULE=main make test-quarkus

#!/usr/bin/env bash

set -e -x

test_bare_modules=(
    "main"
    "amazon-lambda"
    "hibernate-orm-panache"
    "kafka"
    "bouncycastle"
    "vertx"
    "micrometer-prometheus"
    "spring-web"
)

for test_module in test_bare_modules
do
    NATIVE_IMAGE_OPTIONS="--future-defaults=all -H:-CheckToolchain" TEST_MODULE=${test_module} make test-quarkus
done

test_container_modules=(
    "kafka"
    "oidc-client-reactive"
)

for test_module in test_container_modules
do
    NATIVE_IMAGE_OPTIONS="--future-defaults=all -H:-CheckToolchain" TEST_MODULE=${test_module} CONTAINER=true make test-quarkus
done

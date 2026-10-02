#!/usr/bin/env bash
set -eux

pushd getting-started

DEBUG_ARGS=()
if [[ "$1" == "--with-debug=true" ]]; then
    DEBUG_ARGS+=(
        "-Dquarkus.native.debug.enabled"
#        "-H:+SourceLevelDebug"
#        "-H:+TrackNodeSourcePosition"
#        "-H:+DebugCodeInfoUseSourceMappings"
    )

    ./mvnw dependency:sources
fi

./mvnw package -Dnative -DskipTests \
    "${DEBUG_ARGS[@]}" \
    -Dquarkus.native.additional-build-args=-H:+PrintClassInitialization,-H:-CheckToolchain

popd

#!/usr/bin/env bash
#
# Build baseline libjvm.so without any Quarkus libraries.
#
# This builds the standard Crema libjvm.so using only the jvm-library macro
# with sun.misc preserved. No application jars are included.
#
# Usage:
#   ./build-libjvm-baseline.sh
#
# After building, test with:
#   ./verify-quarkus.sh
#
set -ex

source "$(dirname "$0")/build-summary.sh"

${GRAALVM_HOME}/bin/native-image --macro:jvm-library \
    -H:Preserve=package=sun.misc \
    2>&1 | print_build_summary

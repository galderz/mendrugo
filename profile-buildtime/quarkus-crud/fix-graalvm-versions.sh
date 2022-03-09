#!/usr/bin/env bash

set -x -e

fix()
{
    local graalvm_home=/opt/$1
    sed -i "s/EnableJVMCI/EnableJVMCI -Dorg.graalvm.version=22.1.0-dev/" $graalvm_home/lib/svm/bin/native-image
}

for graalvm_home in \
    graalvm-b53173b56c-java17-22.1.0-dev \
    graalvm-b53173b56c-java11-22.1.0-dev
do
    fix $graalvm_home
done

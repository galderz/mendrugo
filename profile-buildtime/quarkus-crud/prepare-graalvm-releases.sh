#!/usr/bin/env bash

set -x -e

prepare()
{
    local graalvm_home=/opt/$1

    # Check that native-image exists, otherwise install it
    native_image=$graalvm_home/bin/native-image
    if [ ! -f "$native_image" ]; then
        $graalvm_home/bin/gu install native-image
    fi

    pushd $graalvm_home/bin
    rm -f native-image.link
    ln -s ../lib/svm/bin/native-image native-image.link
    popd
    rm $graalvm_home/bin/native-image
    cp native-image.sh $graalvm_home/bin/native-image
}

#    graalvm-ce-java17-22.0.0.2 \
#    graalvm-ce-java17-21.3.1 \
#    graalvm-ce-java11-22.0.0.2 \
#    graalvm-ce-java11-21.3.1
#    graalvm-ce-java17-22.1.0
for graalvm_home in \
    graalvm-ce-java17-22.2.0
do
    prepare $graalvm_home
done

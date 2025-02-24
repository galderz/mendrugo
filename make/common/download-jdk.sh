#!/bin/bash
set -eux

VERSION=$1
BUILD=$2
DIR=$3

if [ "$(uname)" = "Darwin" ]; then
  OS="mac"
  ARCH="aarch64"
elif [ "$(uname -i)" = "aarch64" ]; then
  OS=$(uname | tr A-Z a-z)
  ARCH="aarch64"
else
  OS=$(uname | tr A-Z a-z)
  ARCH="x64"
fi

download_jdk()
{
    # Specify the Java version and platform
    API_URL="https://api.adoptium.net/v3/binary/version/jdk-${VERSION}%2B${BUILD}-ea-beta/${OS}/${ARCH}/jdk/hotspot/normal/adoptium"

    # Fetch the archive
    FETCH_URL=$(curl -s -w %{redirect_url} "${API_URL}")
    FILENAME=$(curl -OLs -w %{filename_effective} "${FETCH_URL}")

    echo "Downloaded successfully as ${FILENAME}"

    DIR=temurin-${VERSION}-${BUILD}-ea-beta

    mkdir -p ${DIR}
    pushd ${DIR}
    tar --strip-components 1 -xzvpf ../${FILENAME}
    popd

    if [ "$(uname)" = "Darwin" ]; then
      ln -s ${DIR}/Contents/Home boot-graal-${VERSION}-${BUILD}
    else
      ln -s ${DIR} boot-graal-${VERSION}-${BUILD}
    fi
}

download_static_libs()
{
    # Specify the Java version and platform
    API_URL="https://api.adoptium.net/v3/binary/version/jdk-${VERSION}%2B${BUILD}-ea-beta/${OS}/${ARCH}/staticlibs/hotspot/normal/adoptium"

    # Fetch the archive
    FETCH_URL=$(curl -s -w %{redirect_url} "${API_URL}")
    FILENAME=$(curl -OLs -w %{filename_effective} "${FETCH_URL}")

    echo "Downloaded successfully as ${FILENAME}"

    DIR=temurin-${VERSION}-${BUILD}-ea-beta

    pushd ${DIR}
    tar --strip-components 1 -xzvpf ../${FILENAME}
    popd
}

pushd ${DIR}
download_jdk
download_static_libs
popd
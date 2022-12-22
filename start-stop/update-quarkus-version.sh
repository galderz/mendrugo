#!/usr/bin/env bash

set -e
set -x

update_project()
{
    local version=$1
    local project=$2

    pushd $project
    mvn versions:set -DnewVersion=$version
    popd
}

update()
{
    local version=$1
    local home=/opt/quarkus-quarkus

    pushd $home
    ./update-version.sh $version

    update_project $version independent-projects/enforcer-rules
    update_project $version independent-projects/ide-config
}

update $1

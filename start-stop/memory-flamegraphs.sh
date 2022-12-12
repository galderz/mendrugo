#!/usr/bin/env bash

set -e
set -x

stack()
{
    local quarkus=$1
    local app=$2
    local name=$3

    QUARKUS=$quarkus APP=$app STACK_NAME=$name make stack
}

flamegraph()
{
    local name=$1
    local countname=$2
    local dir=target/stacks
    local fp=/opt/FlameGraph/flamegraph.pl
    local sp=/opt/FlameGraph/stackcollapse.pl

    if [[ "$name" = "malloc-bytes-brendan" ]]; then
        cat $dir/$name.stacks | $fp --color=mem --title="$name Flame Graph" --countname="$countname" > $dir/$name.svg
    else
        $sp < $dir/$name.stacks | $fp --color=mem --title="$name Flame Graph" --countname="$countname" > $dir/$name.svg
    fi
}

flamegraphs()
{
    local quarkus=$1
    local app=$2
    local stacksdir=target/stacks

    rm -drf $stacksdir
    mkdir -p $stacksdir

    for entry in \
        brk,calls \
        calloc,calls \
        malloc-bytes-brendan,bytes \
        malloc,calls \
        mmap,calls \
        pagefault,pages \
        realloc,calls
    do
        IFS=',' read name countname <<< "${entry}"
        stack $quarkus $app $name
        flamegraph $name $countname
    done
}

flamegraphs $1 $2

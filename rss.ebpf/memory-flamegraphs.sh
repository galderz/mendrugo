#!/usr/bin/env bash

set -e
set -x

stack()
{
    local name=$1

    STACK_NAME=$name make stack
}

flamegraph()
{
    local name=$1
    local countname=$2
    local fgdir=$3
    local stacksdir=$fgdir/stacks
    local fp=/opt/FlameGraph/flamegraph.pl
    local sp=/opt/FlameGraph/stackcollapse.pl

    if [[ "$name" = "malloc-bytes-brendan" ]]; then
        cat $stacksdir/$name.stacks | $fp --color=mem --title="$name Flame Graph" --countname="$countname" > $fgdir/$name.svg
    else
        $sp < $stacksdir/$name.stacks | $fp --color=mem --title="$name Flame Graph" --countname="$countname" > $fgdir/$name.svg
    fi
}

flamegraphs()
{
    local fgdir=target/flamegraphs

    rm -drf $fgdir
    mkdir -p $fgdir

    for entry in \
        brk,calls \
        free,calls \
        malloc-bytes-brendan,bytes \
        malloc,calls \
        mmap,calls \
        pagefault,pages
    do
        IFS=',' read name countname <<< "${entry}"
        stack $name
        flamegraph $name $countname $fgdir
    done
}

flamegraphs

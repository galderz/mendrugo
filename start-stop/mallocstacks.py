#!/usr/bin/python
#
# mallocstacks  Trace malloc() calls in a process and print the full
#               stack trace for all callsites.
#               For Linux, uses BCC, eBPF. Embedded C.
#
# This script is a basic example of the new Linux 4.6+ BPF_STACK_TRACE
# table API.
#
# Copyright 2016 GitHub, Inc.
# Licensed under the Apache License, Version 2.0 (the "License")

from __future__ import print_function
from bcc import BPF
from bcc.utils import printb
from time import sleep
import sys

if len(sys.argv) < 2:
    print("USAGE: mallocstacks PID [NUM_STACKS=1024]")
    exit()
pid = int(sys.argv[1])
if len(sys.argv) == 3:
    try:
        assert int(sys.argv[2]) > 0, ""
    except (ValueError, AssertionError) as e:
        print("USAGE: mallocstacks PID [NUM_STACKS=1024]")
        print("NUM_STACKS must be a non-zero, positive integer")
        exit()
    stacks = sys.argv[2]
else:
    stacks = "1024"

# load BPF program
b = BPF(text="""
#include <uapi/linux/ptrace.h>

BPF_HASH(calls, int);
BPF_STACK_TRACE(stack_traces, """ + stacks + """);

int alloc_enter(struct pt_regs *ctx, size_t size) {
    int key = stack_traces.get_stackid(ctx, BPF_F_USER_STACK);
    if (key < 0)
        return 0;

    // could also use `calls.increment(key, size);`
    u64 zero = 0, *val;
    val = calls.lookup_or_try_init(&key, &zero);
    if (val) {
      (*val) += size;
    }
    return 0;
};
TRACEPOINT_PROBE(syscalls, sys_enter_mmap) {
    int key = stack_traces.get_stackid(args, BPF_F_USER_STACK);
    if (key < 0)
        return 0;

    // could also use `calls.increment(key, args->len);`
    u64 zero = 0, *val;
    val = calls.lookup_or_try_init(&key, &zero);
    if (val) {
      (*val) += args->len;
    }
    return 0;
}
int mmap_enter(struct pt_regs *ctx, void *addr, size_t length, int prot, int flags, int fd, off_t offset) {
    int key = stack_traces.get_stackid(ctx, BPF_F_USER_STACK);
    if (key < 0)
        return 0;

    // could also use `calls.increment(key, length);`
    u64 zero = 0, *val;
    val = calls.lookup_or_try_init(&key, &zero);
    if (val) {
      (*val) += length;
    }
    return 0;
};
""")

#b.attach_uprobe(name="c", sym="malloc", fn_name="alloc_enter", pid=pid)
#print("Attaching to malloc in pid %d." % pid)

#b.attach_tracepoint(tp="syscalls:sys_enter_mmap", fn_name="mmap_enter")
#print("Attaching to mmap, Ctrl+C to quit." % pid)

print("Attaching to tracepoint for mmap in pid %d." % pid)

# sleep until Ctrl-C
try:
    sleep(99999999)
except KeyboardInterrupt:
    pass

calls = b.get_table("calls")
stack_traces = b.get_table("stack_traces")

for k, v in reversed(sorted(calls.items(), key=lambda c: c[1].value)):
    print("%d bytes allocated at:" % v.value)
    if k.value > 0 :
        for addr in stack_traces.walk(k.value):
            printb(b"\t%s" % b.sym(addr, pid, show_offset=True))

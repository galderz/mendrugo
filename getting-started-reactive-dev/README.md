# Useful Commands

## Build Single Threaded

```shell
NATIVE_BUILD_ARGS=-H:NumberOfThreads=1 gmake
```

## Generate Trace Inlining Log

```shell
NATIVE_BUILD_ARGS=-H:NumberOfThreads=1,-H:+TraceInlining,-J-Djdk.graal.LogFile=trace-inlining.log gmake
```

## Debug Compiler With IDE

Needs to be done with single threaded mode:

```shell
NATIVE_BUILD_ARGS=-H:NumberOfThreads=1,--debug-attach=*:8000 gmake
```

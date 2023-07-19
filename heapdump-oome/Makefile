APP := .
NAME := heapdump-oome
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk

opts += -XX:+HeapDumpOnOutOfMemoryError
opts += -XX:HeapDumpPath=target
opts += -Xmx1g

binary := target/heapdump-oome
native_image_opts += --enable-monitoring=heapdump
native_image_opts += --no-fallback
native_image_opts += -H:+ReportExceptionStackTraces

# todo add option to select when to use these
# todo only apply them when it's linux
native_image_opts += -g
native_image_opts += -O0
native_image_opts += -H:-DeleteLocalSymbols
native_image_opts += -H:+SourceLevelDebug
native_image_opts += -H:-OmitInlinedMethodDebugLineInfo

# todo add option to select when to use this
# native_image_opts += --debug-attach=*:8000

# todo add option to select when to use these
# native_image_opts += -ea
# native_image_opts += -esa

# todo add option to select when to enable trace logging
# native_image_opts += -H:+TraceVMOperations

run: $(binary)
> $< $(opts)
.PHONY: run

$(binary): $(jar) $(native_image)
> $(native_image) $(native_image_opts) -cp $< org.example.Main $@

# TODO make generic when opts splits into params and opts and the native opts can be copied to jvm ones
run-jvm: $(jar)
> $(java) $(opts) -cp $< org.example.Main
.PHONY: run-jvm

gdb: $(binary)
> gdb --args $< $(opts)
.PHONY: gdb

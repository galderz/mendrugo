APP := .
JDK_REPO := jdk21u
NAME := heapdump-oome
MAIN_CLASS ?= Main
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/JOL.gmk

run_args += -XX:+HeapDumpOnOutOfMemoryError
run_args += -XX:HeapDumpPath=target
run_args += -Xmx1g
run_jvm_opts += $(run_args)

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
> $< $(run_args)
.PHONY: run

$(binary): $(jar) $(native_image)
> $(native_image) $(native_image_opts) -cp $< org.example.$(MAIN_CLASS) $@

# TODO make generic when opts splits into params and opts and the native opts can be copied to jvm ones
run-jvm: $(jar)
> $(java) $(run_jvm_opts) -cp $< org.example.$(MAIN_CLASS)
.PHONY: run-jvm

gdb: $(binary)
> gdb --args $< $(opts)
.PHONY: gdb

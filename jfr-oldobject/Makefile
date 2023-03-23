app := .
jar = $(target)/jfr-oldobject.jar

include ../make/common/MakeBase.gmk
include ../make/common/GraalVM.gmk
include ../make/common/Maven.gmk

JFC_FILE ?= jfc/base.jfc
JFR_FILE ?= target/native.jfr
TEST_CLASS ?= PlainObjectLeak

binary := target/jfr-oldobject
native_image_opts += --enable-monitoring=jfr
native_image_opts += --no-fallback
native_image_opts += -H:+ReportExceptionStackTraces

ifdef VERBOSE
  jvm_opts += -Xlog:jfr+system=trace
  # binary_opts += -XX:FlightRecorderLogging=all=trace
  # native_image_opts += -H:+TraceVMOperations
  # mx += -V
endif

ifdef PATH_TO_GC_ROOTS
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc,path-to-gc-roots=true
  native_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE),path-to-gc-roots=true
else
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc
  native_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE)
endif

run-native: $(binary)
> $< $(native_opts)
.PHONY: run-native

$(binary): $(jar) $(native_image)
> $(native_image) $(native_image_opts) -cp $< jfr.oldobject.$(TEST_CLASS) $@

run-jvm: $(jar) clean-jfr
> $(java) $(jvm_opts) -cp $< jfr.oldobject.$(TEST_CLASS)
.PHONY: run-jvm

print:
> $(jfr) -J-Xlog:jfr+system+parser=trace print --events OldObjectSample --stack-depth 100 $(JFR_FILE)
.PHONY: print

clean-jfr:
> rm -f target/jvm.jfr
.PHONY: clean-jfr

app := .
jar = $(target)/jfr-oldobject.jar

include ../make/common/MakeBase.gmk
include ../make/common/GraalVM.gmk
include ../make/common/Maven.gmk

JFR_FILE ?= target/native.jfr
TEST_CLASS ?= PlainObjectLeak

ifdef VERBOSE
  jvm_opts += -Xlog:jfr+system=trace
  # binary_opts += -XX:FlightRecorderLogging=all=trace
  # native_image_opts += -H:+TraceVMOperations
  # mx += -V
endif

ifdef PATH_TO_GC_ROOTS
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc,path-to-gc-roots=true
  # binary_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE),path-to-gc-roots=true
else
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc
  # binary_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE)
endif

run-jvm: $(jar) clean-jfr
> $(java) $(jvm_opts) -cp $< jfr.oldobject.$(TEST_CLASS)
.PHONY: run-jvm

print:
> $(jfr) -J-Xlog:jfr+system+parser=trace print --events OldObjectSample --stack-depth 100 $(JFR_FILE)
.PHONY: print

clean-jfr:
> rm -f target/jvm.jfr
.PHONY: clean-jfr

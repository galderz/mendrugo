include ../make/common/MakeBase.gmk

APP_DIR := .
APP_NAME := jfr-oldobject
BOOT_JDK := $(opt)/java-21
GRAALVM_VERSION := 23.2.0
JDK_REPO := jdk21u
JFC_FILE ?= jfc/base.jfc
JFR_FILE ?= target/native.jfr
TEST_CLASS ?= PlainObjectLeak

include ../make/common/GraalDev.gmk
include ../make/common/Maven.gmk

binary := target/jfr-oldobject
native_image_opts += --enable-monitoring=jfr
native_image_opts += --no-fallback
native_image_opts += -H:+ReportExceptionStackTraces
native_image_opts += -H:+AllowVMInspection

jvm_opts += -XX:TLABSize=2k

ifdef VERBOSE
  jvm_opts += -Xlog:jfr+system=trace
  # For queue size, add -XX:FlightRecorderLogging=jfr=debug
  # binary_opts += -XX:FlightRecorderLogging=all=trace
  # native_image_opts += -H:+TraceVMOperations
  # mx += -V
endif

ifdef DEBUG
  native_image_opts += -H:GenerateDebugInfo=1
  native_image_opts += -H:+SourceLevelDebug
  native_image_opts += -H:-DeleteLocalSymbols
endif

ifdef PATH_TO_GC_ROOTS
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc,path-to-gc-roots=true
  native_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE),path-to-gc-roots=true
else
  jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./jfc/base.jfc
  native_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/native.jfr\,settings=$(JFC_FILE)
endif

ifdef TEST
  native_test_pattern := $(TEST)
else
  native_test_pattern := com.oracle.svm.test.jfr.oldobject
endif

mx_unittest += $(mx)
mx_unittest += unittest

ifdef REMOTE_DEBUG
  mx_unittest += -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
endif

#> DEBUG_RECORDING=true $(mx) native-unittest --run-args -XX:FlightRecorderLogging=jfr=debug,jfr+dcmd=debug --verbose --build-args --enable-monitoring=heapdump -H:+DumpThreadStacksOnSignal --preserve-image com.oracle.svm.test.jfr.oldobject
#> DEBUG_RECORDING=true $(mx) native-unittest --preserve-image --build-args --enable-monitoring=heapdump -H:+DumpThreadStacksOnSignal com.oracle.svm.test.jfr.oldobject
test: $(native_image)
> cd $(svm_home)
> DEBUG_RECORDING=true $(mx) native-unittest --preserve-image $(native_test_pattern) --build-args --enable-monitoring=heapdump,jvmstat -H:+UnlockExperimentalVMOptions -H:+DumpThreadStacksOnSignal -g
.PHONY: test

svm_test_jar = $(svm_home)/mxbuild/jdk17/dists/jdk17/svm-tests.jar
$(svm_test_jar): $(shell find $(svm_home)/src/com.oracle.svm.test -type f -name '*.java')
> cd $(svm_home)
> $(mx) build --only com.oracle.svm.test
> $(mx) archive @SVM_TESTS

svm_core_jar = $(svm_home)/mxbuild/jdk17/dists/jdk17/svm.jar
$(svm_core_jar): $(shell find $(svm_home)/src/com.oracle.svm.core -type f -name '*.java')
> cd $(svm_home)
> $(mx) build --only com.oracle.svm.core
> $(mx) archive @SVM

unittest: $(svm_core_jar) $(svm_test_jar)
> cd $(svm_home)
> $(mx_unittest) com.oracle.svm.test.jfr.oldobject.TestOldObjectProfiler
.PHONY: unittest

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

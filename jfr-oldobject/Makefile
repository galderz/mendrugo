SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

ifeq ($(origin .RECIPEPREFIX), undefined)
  $(error This Make does not support .RECIPEPREFIX. Please use GNU Make 4.0 or later)
endif
.RECIPEPREFIX = >

TEST_CLASS ?= DeepChain
JFR_FILE ?= binary.jfr

binary := target/jfr-oldobject
binary_opts += -XX:+FlightRecorder
binary_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/binary.jfr\,settings=./oldobject.jfc
graalbuilder_home := $(HOME)/1/jdk17u-dev
graalbuilder_java = $(graalbuilder_java_home)/bin/java
graalbuilder_java_home = $(graalbuilder_home)/build/graalbuilder/images/graal-builder-jdk
graalbuilder_jfr = $(graalbuilder_java_home)/bin/jfr
graalbuilder_sources += $(shell find $(graalbuilder_home)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_home := /opt/mandrel-mandrel/substratevm
graalvm_sources += $(shell find $(graalvm_home)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_version := 23.0.0
jar := target/jfr-oldobject.jar
jvm_opts += -XX:TLABSize=2k
jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./oldobject.jfc
mvn += JAVA_HOME=$(graalbuilder_java_home)
mvn += /opt/maven/bin/mvn
mx += JAVA_HOME=$(graalbuilder_java_home)
mx += /opt/mx/mx
native_image += /opt/mandrel-mandrel/sdk/latest_graalvm_home/bin/native-image
native_image_opts += --enable-monitoring=jfr
native_image_opts += --no-fallback
native_image_opts += -H:+ReportExceptionStackTraces
project_sources += $(shell find src/ -type f -name '*.java' | sed 's: :\\ :g')
project_sources += pom.xml

ifdef VERBOSE
  binary_opts += -XX:FlightRecorderLogging=all=trace
  native_image_opts += -H:+TraceVMOperations
endif

run-binary: $(binary)
> $< $(binary_opts)
.PHONY: run-binary

print:
> $(graalbuilder_jfr) -J-Xlog:jfr+system+parser=trace print --events OldObjectSample --stack-depth 100 target/$(JFR_FILE)
.PHONY: print

test: $(native_image)
> cd $(graalvm_home)
> $(mx) native-unittest com.oracle.svm.test.jfr
.PHONY: test

$(binary): $(jar) $(native_image)
> $(native_image) $(native_image_opts) -cp $< jfr.oldobject.$(TEST_CLASS) $@

run-jvm: $(jar)
> $(graalbuilder_java) $(jvm_opts) -cp $< jfr.oldobject.$(TEST_CLASS)
.PHONY: run-jvm

$(native_image): $(graalvm_sources) $(graalbuilder_java)
> cd $(graalvm_home)
> $(mx) build
> sed -i'' -e 's/EnableJVMCI/EnableJVMCI -Dorg.graalvm.version=$(graalvm_version)-dev/g' ../sdk/latest_graalvm_home/lib/svm/bin/native-image

$(graalbuilder_java): $(graalbuilder_sources)
> cd $(graalbuilder_home)
> bash configure \
>   --disable-warnings-as-errors \
>   --with-boot-jdk=/opt/java-17 \
>   --with-conf-name=graalbuilder \
>   --with-extra-cflags=-fcommon \
>   --with-jvm-variants=server
> make CONF=graalbuilder graal-builder-image

$(jar): $(project_sources)
> $(mvn) package

clean:
> $(mvn) clean
.PHONY: clean

clean-graalvm:
> cd $(graalvm_home)
> $(mx) clean
.PHONY: clean-graalvm

clean-all: clean clean-graalvm
> cd $(graalbuilder_home)
> make CONF=graalbuilder clean
.PHONY: clean-all

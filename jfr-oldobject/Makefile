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

binary := target/jfr-oldobject
binary_opts += -XX:+FlightRecorder
binary_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/binary.jfr\,settings=./oldobject.jfc
binary_opts += -XX:FlightRecorderLogging=all=trace
graalvm_home := /opt/mandrel-mandrel/substratevm
graalvm_sources += $(shell find $(graalvm_home)/ -type f -name '*.java' | sed 's: :\\ :g')
jar := target/jfr-oldobject.jar
java_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
jvm_opts += -XX:TLABSize=2k
jvm_opts += -XX:StartFlightRecording=dumponexit=true\,filename=target/jvm.jfr\,settings=./oldobject.jfc
graalvm_builder := /opt/graalbuilder-17
graalvm_version := 23.0.0
mvn += JAVA_HOME=$(graalvm_builder)
mvn += /opt/maven/bin/mvn
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
native_image += $(java_home)/bin/native-image
native_image_opts += --enable-monitoring=jfr
native_image_opts += --no-fallback
native_image_opts += -H:+ReportExceptionStackTraces
project_sources += $(shell find src/ -type f -name '*.java' | sed 's: :\\ :g')

run-binary: $(binary)
> $< $(binary_opts)
.PHONY: run-binary

$(binary): $(jar) $(native_image)
> $(native_image) $(native_image_opts) -cp $< jfr.oldobject.LargeObjects $@

run-jvm: $(jar)
> java $(jvm_opts) -cp $< jfr.oldobject.LargeObjects
.PHONY: run-jvm

$(native_image): $(graalvm_sources)
> cd $(graalvm_home)
> $(mx) build
> sed -i'' -e 's/EnableJVMCI/EnableJVMCI -Dorg.graalvm.version=$(graalvm_version)-dev/g' ../sdk/latest_graalvm_home/lib/svm/bin/native-image

$(jar): $(project_sources)
> $(mvn) package

clean:
> $(mvn) clean
> cd $(graalvm_home)
> $(mx) clean
.PHONY: clean

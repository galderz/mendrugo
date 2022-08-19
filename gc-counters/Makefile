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

graalvm_builder := /opt/graalbuilder-17
graalvm_home := /opt/mandrel-mandrel/substratevm
graalvm_sources += $(shell find $(graalvm_home)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_version := 22.3.0
java_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
mvn += JAVA_HOME=$(java_home)
mvn += /opt/maven/bin/mvn
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
native_image = $(java_home)/bin/native-image
project_sources += $(shell find src/ -type f -name '*.java' | sed 's: :\\ :g')
runner := ./target/gc-counters-1.0.0-SNAPSHOT-runner
wrk := /opt/wrk/wrk

run: $(runner)
> $< -XX:+VerboseGC -XX:+PrintHeapShape -XX:+TraceHeapChunks
.PHONY: run

wrk: $(wrk)
> $(wrk) -H 'Host: localhost' -H 'Accept: text/plain,text/html;q=0.9,application/xhtml+xml;q=0.9,application/xml;q=0.8,*/*;q=0.7' -H 'Connection: keep-alive' --latency -d 5 -c 16 --timeout 8 -t 5 http://localhost:8080/hello
.PHONY: wrk

$(runner): $(native_image) $(project_sources)
> $(mvn) package -DskipTests -Dnative

$(native_image): $(graalvm_sources)
> cd $(graalvm_home)
> $(mx) build
> sed -i'' -e 's/EnableJVMCI/EnableJVMCI -Dorg.graalvm.version=$(graalvm_version)-dev/g' ../sdk/latest_graalvm_home/lib/svm/bin/native-image

clean:
> $(mvn) clean
> cd $(graalvm_home)
> $(mx) clean
.PHONY: clean

clean-all:
> git clean -dfx
> cd $(graalvm_home)
> git clean -dfx
.PHONY: clean-all

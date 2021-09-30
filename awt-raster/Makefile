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

# Mandrel
MANDREL_BIN ?= /opt/mandrel-java11-21.2
MANDREL_BOOT ?= /opt/adopt-11.0.12+7
MANDREL_HOME ?= /opt/mandrel-21.2
MANDREL_PACKAGING ?= /opt/mandrel-packaging
MX_HOME ?= /opt/mx

bin = $(MANDREL_BIN)/bin
mx = $(MX_HOME)/mx
native_image = $(MANDREL_BIN)/bin/native-image
substrate := $(MANDREL_HOME)/substratevm
substrate_java_files = $(shell find $(substrate)/ -type f -name '*.java')

call_native += $(bin)/native-image
call_native += -H:+ReportExceptionStackTraces
call_native += --no-fallback

ifdef VERBOSE
  call_native += --verbose
endif

ifeq ($(DEBUG),driver)
  call_native += --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
else ifdef DEBUG
  call_native += --debug-attach=*:8000
endif

# Project
classfiles := $(patsubst %.java, %.class, $(wildcard *.java))
jar := example.jar
program := a.out

$(program): $(jar) $(native_image)
> $(native_image) -jar $(jar) $(program)
> ./$(program)
.PHONY: $(program)

$(jar): $(classfiles)
> $(bin)/jar cvfe $@ Example $(classfiles)
> $(bin)/java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $@
> $(bin)/jar cvfe $@ Example $(classfiles) META-INF
> $(bin)/java -jar $@

%.class: %.java
> $(bin)/javac -d . $<

clean:
> rm -f a.out
> rm -f *.jar
> rm -f *.class
.PHONY: clean

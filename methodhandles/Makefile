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

GRAALVM_HOME ?= /opt/graalvm-17
MVN_HOME ?= /opt/maven

mvn += JAVA_HOME=$(GRAALVM_HOME)
mvn += $(MVN_HOME)/bin/mvn

print = echo '$(1)'
comma := ,

bin = $(GRAALVM_HOME)/bin
native_image += $(bin)/native-image
native_image += --no-fallback
native_image += -H:+ReportExceptionStackTraces

ifdef VERBOSE
  native_image += --verbose
  mvn += -X
endif

ifeq ($(DEBUG),driver)
  native_image += --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
else ifdef DEBUG
  native_image += --debug-attach=*:8000
endif

# Project
sources += $(shell find src/ -type f -name '*.java')
jar := target/example.jar
program := target/a.out

$(program): $(jar) $(bin_native_image)
> $(native_image) -jar $(jar) $(program)
> ./$(program) name
.PHONY: $(program)

$(jar): pom.xml $(sources)
> $(mvn) package

clean:
> $(mvn) clean
.PHONY: clean

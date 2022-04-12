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

# Params
#BYTEMAN_HOME ?= /opt/byteman
GRAALVM_HOME ?= /opt/graalvm-17
MVN_HOME ?= /opt/maven

mvn += JAVA_HOME=$(GRAALVM_HOME)
mvn += $(MVN_HOME)/bin/mvn

print = echo '$(1)'
comma := ,

# GraalVM
bin = $(GRAALVM_HOME)/bin
#byteman_jar := $(BYTEMAN_HOME)/lib/byteman.jar
#byteman_scripts = script:$(resources_dir)/graalvm.btm
native_image += $(bin)/native-image
native_image += -H:+ReportExceptionStackTraces
#native_image += -H:+PrintAnalysisCallTree
#native_image += -H:NeverInline=numberOfArgs
native_image += --no-fallback
native_image += --initialize-at-build-time=org.example
#native_image += -J-javaagent:$(byteman_jar)=boot:$(byteman_jar)$(comma)$(byteman_scripts)
resources_dir := src/main/resources

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
sources += $(shell find src/ -type f -name '*.btm')
jar := target/example.jar
program := target/a.out

$(program): $(jar) $(bin_native_image)
> $(native_image) -jar $(jar) $(program)
> ./$(program)
.PHONY: $(program)

$(jar): pom.xml $(sources)
> $(mvn) package
 
clean:
> $(mvn) clean
.PHONY: clean

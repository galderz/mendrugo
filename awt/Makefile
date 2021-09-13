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

# GraalVM
#graalvm_home = $(MANDREL_HOME)/sdk/latest_graalvm_home
graalvm_home = /opt/graalvm-ce-java11-21.2.0
bin_jar = $(graalvm_home)/bin/jar
bin_javac = $(graalvm_home)/bin/javac
bin_native_image = $(graalvm_home)/bin/native-image
#image_agent = $(graalvm_home)/lib/libnative-image-agent.so

do_native_image += $(bin_native_image)
do_native_image += -H:+ReportExceptionStackTraces
do_native_image += --no-fallback

ifdef VERBOSE
  do_native_image += --verbose
endif

ifeq ($(DEBUG),driver)
  do_native_image += --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
else ifdef DEBUG
  do_native_image += --debug-attach=*:8000
endif

# Project
classfiles = $(patsubst %.java, %.class, $(wildcard *.java))
jar = example.jar
program = a.out

$(program): $(jar) $(bin_native_image)
> $(do_native_image) -jar $(jar) $(program)
> ./$(program)

$(jar): $(classfiles)
> $(bin_jar) cvfe $@ Example $(classfiles)

%.class: %.java
> $(bin_javac) -d . $<


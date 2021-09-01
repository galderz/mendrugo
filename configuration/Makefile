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

all: run

# Mandrel
MANDREL_BOOT ?= /opt/mandrel-boot
MANDREL_HOME ?= /opt/mandrel
MX_HOME ?= /opt/mx

mx = $(MX_HOME)/mx
substrate := $(MANDREL_HOME)/substratevm
substrate_java_files = $(shell find $(substrate)/ -type f -name '*.java')

graalvm_home = $(MANDREL_HOME)/sdk/latest_graalvm_home
bin_jar = $(graalvm_home)/bin/jar
bin_javac = $(graalvm_home)/bin/javac
bin_native_image = $(graalvm_home)/bin/native-image
imageagent = $(graalvm_home)/lib/libnative-image-agent.so

$(bin_native_image): $(substrate_java_files)
> cd $(substrate)
> JAVA_HOME=$(MANDREL_BOOT) $(mx) build

$(image_agent): $(bin_native_image)
> native-image --macro:native-image-agent-library

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

nativeproperties = META-INF/native-image/native-image.properties
reflectjson = META-INF/native-image/reflect-config.json

ifeq ($(SKIP),reflect)
  do_native_image += --exclude-config example.*.jar META-INF\/native-image\/.*.json
  do_native_image += -jar $(jar)
else ifeq ($(SKIP),args)
  do_native_image += -jar $(jar)
  do_native_image += --exclude-config example.*.jar META-INF\/native-image\/.*.properties
endif

run: $(jar) $(bin_native_image)
> $(do_native_image) -jar $(jar) $(program)
> ./$(program) getInstance
.PHONY: run

$(jar): $(classfiles) $(nativeproperties) $(reflectjson)
> $(bin_jar) cvfe $@ Example $(classfiles) $(nativeproperties) $(reflectjson)

%.class: %.java
> $(bin_javac) -d . $<

jvm-run: $(jar) $(image_agent)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jar)
.PHONY: jvm-run

clean:
> rm -f a.out
> rm -f *.jar
> rm -f *.class
.PHONY: clean

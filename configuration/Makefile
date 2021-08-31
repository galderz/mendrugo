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

MANDREL_HOME ?= /opt/mandrel
MX_HOME=/opt/mx
MANDREL_BOOT_JAVA_HOME=/opt/mandrel-boot-java

substrate := $(MANDREL_HOME)/substratevm
substratejavafiles = $(shell find $(substrate)/ -type f -name '*.java')
graalvmhome = $(MANDREL_HOME)/sdk/latest_graalvm_home

nativeimage = $(graalvmhome)/bin/native-image
ifdef VERBOSE
  nativeimage += --verbose
endif

imageagent = $(graalvmhome)/lib/libnative-image-agent.so
mx = $(MX_HOME)/mx
bin_jar = $(graalvmhome)/bin/jar
javac = $(graalvmhome)/bin/javac

classfiles = $(patsubst %.java, %.class, $(wildcard *.java))
jar = example.jar
program = a.out

invokenative += $(nativeimage)
invokenative += -H:+ReportExceptionStackTraces
invokenative += --no-fallback

nativeproperties = META-INF/native-image/native-image.properties
reflectjson = META-INF/native-image/reflect-config.json

run: $(program)
> ./$(program) getInstance
.PHONY: run

$(program): $(jar) $(nativeimage)
> $(invokenative) -jar $(jar) $(program)

$(jar): $(classfiles) $(nativeproperties) $(reflectjson)
> $(bin_jar) cvfe $@ Example $(classfiles) $(nativeproperties) $(reflectjson)

%.class: %.java
> $(javac) -d . $<

$(nativeimage): $(substratejavafiles)
> cd $(substrate)
> JAVA_HOME=$(MANDREL_BOOT_JAVA_HOME) $(mx) build

debug:
> $(invokenative) -jar $(jar) --debug-attach=*:8000 $(program)
.PHONY: debug

debug-driver:
> $(invokenative) -jar $(jar) --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000 $(program)
.PHONY: debug

$(imageagent): $(nativeimage)
> native-image --macro:native-image-agent-library

jvm-run: $(jar) $(imageagent)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jar)
.PHONY: jvm-run

clean:
> rm -f a.out
> rm -f *.jar
> rm -f *.class
.PHONY: clean

noreflect := $(invokenative)
noreflect += --exclude-config example.*.jar META-INF\/native-image\/.*.json
noreflect += -jar $(jar)

skip-reflect: $(jar) $(nativeimage)
> $(noreflect) $(program)
> ./$(program) getInstance
.PHONY: skip-reflect

skip-reflect-debug:
> $(noreflect) --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000 $(program)
> ./$(program) getInstance
.PHONY: skip-reflect-debug

no_args := $(invokenative)
no_args += --exclude-config example.*.jar META-INF\/native-image\/.*.properties
no_args += -jar $(jar)

skip-args: $(jar) $(nativeimage)
> $(no_args) $(program)
> ./$(program) getInstance
.PHONY: skip-args

skip-args-debug:
> $(no_args) --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000 $(program)
> ./$(program) getInstance
.PHONY: skip-args-debug

noboth := $(invokenative)
noboth += --exclude-config example.*.jar META-INF\/native-image\/.*.properties
noboth += --exclude-config example.*.jar META-INF\/native-image\/.*.json
noboth += -jar $(jar)

skip-both: $(jar) $(nativeimage)
> $(noboth) $(program)
> ./$(program) getInstance
.PHONY: skip-both

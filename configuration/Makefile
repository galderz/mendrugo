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

MANDREL_HOME=/opt/mandrel
MX_HOME=/opt/mx
MANDREL_BOOT_JAVA_HOME=/opt/mandrel-boot-java

substrate := $(MANDREL_HOME)/substratevm
substratejavafiles = $(shell find $(substrate)/ -type f -name '*.java')
graalvmhome = $(MANDREL_HOME)/sdk/latest_graalvm_home
nativeimage = $(graalvmhome)/bin/native-image
imageagent = $(graalvmhome)/lib/libnative-image-agent.so
mx = $(MX_HOME)/mx

classfiles = $(patsubst %.java, %.class, $(wildcard *.java))
jar = example.jar
program = a.out

invokenative += $(nativeimage)
invokenative += -jar $(jar)
invokenative += -H:+ReportExceptionStackTraces
invokenative += --no-fallback

nativeproperties = META-INF/native-image/native-image.properties
reflectjson = META-INF/native-image/reflect-config.json

run: $(program)
> ./$(program) getInstance
.PHONY: run

$(program): $(jar) $(nativeimage)
> $(invokenative) $(program)

$(jar): $(classfiles) $(nativeproperties) $(reflectjson)
> jar cvfe $@ Example $(classfiles) $(nativeproperties) $(reflectjson)

%.class: %.java
> javac -d . $<

$(nativeimage): $(substratejavafiles)
> cd $(substrate)
> JAVA_HOME=$(MANDREL_BOOT_JAVA_HOME) $(mx) build

debug:
> $(invokenative) --debug-attach=*:8000 $(program)
.PHONY: debug

$(imageagent): $(nativeimage)
> native-image --macro:native-image-agent-library

jvm-run: $(jar) $(imageagent)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jar)
.PHONY: jvm-run

clean:
> rm -f *.class
> rm -f *.jar
.PHONY: clean

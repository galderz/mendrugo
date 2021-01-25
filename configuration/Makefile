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

substrate = $(MANDREL_HOME)/substratevm
substratejavafiles = $(shell find $(substrate)/ -type f -name '*.java')
graalvmhome = $(MANDREL_HOME)/sdk/latest_graalvm_home
nativeimage = $(graalvmhome)/bin/native-image
imageagent = $(graalvmhome)/lib/libnative-image-agent.so
mx = $(MX_HOME)/mx

execnative = $(nativeimage) \
  -jar $(targetjarfile) \
  -H:+ReportExceptionStackTraces \
  --no-fallback
execnativeskip = $(execnative) \
  -H:+SkipConfigurationFiles
execnativeskipdebug =

currentdir = $(notdir $(shell pwd))
targetdir = /tmp/mendrugo/$(currentdir)
classfiles = $(patsubst %.java, %.class, $(wildcard *.java))
targetclassfiles = $(patsubst %.java, $(targetdir)/%.class, $(wildcard *.java))
jarfile = example.jar
targetjarfile = $(targetdir)/$(jarfile)
program = $(targetdir)/a.out

targetnativemetainf = $(targetdir)/META-INF/native-image
nativeproperties = META-INF/native-image/native-image.properties
targetnativeproperties = $(targetdir)/$(nativeproperties)
reflectjson = META-INF/native-image/reflect-config.json
targetreflectjson = $(targetdir)/$(reflectjson)

skip: $(program)
> $(program) getInstance
.PHONY: run

$(program): $(targetjarfile) $(nativeimage)
> $(execnativeskip) $(program)

$(nativeimage): $(substratejavafiles)
> cd $(substrate)
> JAVA_HOME=$(MANDREL_BOOT_JAVA_HOME) $(mx) build

$(targetjarfile): $(targetclassfiles) $(targetnativeproperties) $(targetreflectjson)
> cd $(targetdir)
> jar cvfe $@ Example $(classfiles) $(nativeproperties) $(reflectjson)

$(targetnativemetainf):
> mkdir -p $(targetnativemetainf)

$(targetnativeproperties): $(targetnativemetainf) $(nativeproperties)
> cp $(nativeproperties) $(targetnativeproperties)

$(targetreflectjson): $(targetnativemetainf) $(reflectjson)
> cp $(reflectjson) $(targetreflectjson)

$(targetdir)/%.class: %.java
> javac -d $(targetdir) $<

debug:
> $(execnativeskip) --debug-attach=*:8000 $(program)
.PHONY: debug

$(imageagent): $(nativeimage)
> native-image --macro:native-image-agent-library

jvm-run: $(targetjarfile) $(imageagent)
> cd $(targetdir)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jarfile)
.PHONY: run-jar

clean:
> rm -drf $(targetdir)
.PHONY: clean

default: $(targetjarfile) $(nativeimage)
> $(execnative) $(program)
> $(program) getInstance
.PHONY: default

alt-security: $(targetjarfile) $(nativeimage)
> $(execnativeskip) --enable-all-security-services $(program)
> $(program) getInstance
.PHONY: workaround

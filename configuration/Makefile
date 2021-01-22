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

currentdir = $(notdir $(shell pwd))
targetdir = /tmp/mendrugo/$(currentdir)
classfiles  = $(patsubst %.java, %.class, $(wildcard *.java))
targetclassfiles  = $(patsubst %.java, $(targetdir)/%.class, $(wildcard *.java))
jarfile = example.jar
targetjarfile = $(targetdir)/$(jarfile)
program = $(targetdir)/a.out
nativemetainf = $(targetdir)/META-INF/native-image
nativeproperties = META-INF/native-image/native-image.properties
targetnativeproperties = $(targetdir)/$(nativeproperties)

run: $(program)
> $(program)
.PHONY: run

$(program): $(targetjarfile)
> $(call native-image)

define native-image
native-image \
  -jar $(targetjarfile) \
  -H:+ReportExceptionStackTraces \
  $(program)
endef

$(targetjarfile): $(targetclassfiles) $(targetnativeproperties)
> cd $(targetdir)
> jar cvfe $@ Example $(classfiles) $(nativeproperties)

$(targetnativeproperties): $(nativeproperties)
> mkdir -p $(nativemetainf)
> cp $(nativeproperties) $(targetnativeproperties)

$(targetdir)/%.class: %.java
> javac -d $(targetdir) $<

jvm-run: $(targetjarfile)
> cd $(targetdir)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jarfile)
.PHONY: run-jar

clean:
> rm -drf $(targetdir)
.PHONY: clean

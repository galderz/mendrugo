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

GRAALVM_HOME ?= graalvm-21.2-dev

vm := /opt/$(GRAALVM_HOME)
jar := target/example.jar
exec := target/example

gen_sources_dir := target/generated-sources
sources = $(shell find . -type f -name '*.java')
classes = $(patsubst %.java,target/classes/%.class,$(notdir $(sources)))

all: $(exec)

$(exec): $(jar)
> $(vm)/bin/native-image -jar $< $@

reports: $(jar)
> $(vm)/bin/native-image -jar $< $(exec) -H:+PrintAnalysisCallTree
.PHONY: reports

$(jar): $(gen_sources_dir) make.java
> jbang ./make.java
> make -s phonyJar

phonyJar: $(sources)
> mvn package
.PHONY: phonyJar

$(gen_sources_dir):
> mkdir -p $@

clean:
> rm -drf target
.PHONY: clean

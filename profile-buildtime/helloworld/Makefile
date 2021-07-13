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

comma := ,

GRAALVM_HOME ?= graalvm-21.2-dev
AP_HOME ?= /opt/async-profiler
NAME ?= $(GRAALVM_HOME)
EXTRA_ARGS ?=

target_dir := target/$(NAME)
vm := /opt/$(GRAALVM_HOME)
main := HelloWorld
exec := $(shell echo $(main) | tr A-Z a-z)

comma := ,
space := $(subst ,, )
args := $(subst $(comma),$(space),$(EXTRA_ARGS))

javafiles := $(shell find . -type f -name '*.java')
classfiles := $(patsubst %.java,%.class,$(javafiles))

ap_agent := -J-agentpath:$(AP_HOME)/build/libasyncProfiler.so=start

# TODO use async profiler 2 instead of 1.8.x (see https://github.com/jvm-profiling-tools/async-profiler/releases/tag/v2.0)

$(target_dir)/$(exec): $(classfiles)
> cd $(target_dir)
> $(vm)/bin/native-image $(main) $(args)

%.class: %.java $(target_dir)
> $(vm)/bin/javac -d $(target_dir) -classpath . $<

$(target_dir):
> mkdir -p $(target_dir)

reports: $(classfiles)
> cd $(target_dir)
> $(vm)/bin/native-image $(main) $(args) -H:+PrintAnalysisCallTree
> wc -l reports/used_methods*.txt
> wc -l reports/used_classes*.txt
> wc -l reports/used_packages*.txt

profiling: $(classfiles)
> cd $(target_dir)
> $(vm)/bin/native-image $(ap_agent),file=profile-wall.svg,event=wall,threads,interval=10ms $(main) | tee console-wall.log
> $(vm)/bin/native-image $(ap_agent),file=profile-lock.svg,event=lock,total $(main) | tee console-lock.log
> $(vm)/bin/native-image $(ap_agent),file=profile-alloc.svg,event=alloc $(main) | tee console-alloc.log
> $(vm)/bin/native-image $(ap_agent),file=profile-cpu.svg,event=cpu $(main) | tee console-cpu.log
.PHONY: profiling

clean:
> rm -drf $(target_dir)
.PHONY: clean

clean-all:
> rm -drf target
.PHONY: clean-all

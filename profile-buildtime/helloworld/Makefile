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

NAME ?= graalvm-21.2-dev
AP_HOME ?= /opt/async-profiler

target_dir := target/$(NAME)
vm := /opt/$(NAME)
main := HelloWorld
exec := $(shell echo $(main) | tr A-Z a-z)

javafiles := $(shell find . -type f -name '*.java')
classfiles := $(patsubst %.java,%.class,$(javafiles))

ap_agent := -J-agentpath:$(AP_HOME)/build/libasyncProfiler.so=start

# TODO add tee to capture output (e.g. phase elapsed times)

profiling: $(classfiles)
> cd $(target_dir)
> $(vm)/bin/native-image $(ap_agent),file=profile-wall.svg,event=wall,threads,interval=10ms $(main)
> $(vm)/bin/native-image $(ap_agent),file=profile-lock.svg,event=lock,total $(main)
> $(vm)/bin/native-image $(ap_agent),file=profile-alloc.svg,event=alloc $(main)
> $(vm)/bin/native-image $(ap_agent),file=profile-cpu.svg,event=cpu $(main)

$(target_dir)/$(exec):
> cd $(target_dir)
> $(vm)/bin/native-image $(main)

%.class: %.java $(target_dir)
> $(vm)/bin/javac -d $(target_dir) -classpath . $<

$(target_dir):
> mkdir -p $(target_dir)

clean:
> rm -drf $(target_dir)
.PHONY: clean

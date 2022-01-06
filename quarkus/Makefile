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

EXTENSION ?= tika
QUICKSTART ?= $(EXTENSION)-quickstart
GRAALVM_HOME ?= /opt/graalvm-ce-java11-21.3.0-dev

all: extension quickstart
.PHONY: all

extension:
> GRAALVM_HOME=$(GRAALVM_HOME) EXTENSION=$(EXTENSION) make -C quarkus-extension
.PHONY: extension

quickstart:
> GRAALVM_HOME=$(GRAALVM_HOME) QUICKSTART=$(QUICKSTART) make -C quarkus-quickstart
.PHONY: quickstart

clean: clean-quickstart clean-extension

clean-extension:
> GRAALVM_HOME=$(GRAALVM_HOME) EXTENSION=$(EXTENSION) make -C quarkus-extension clean
.PHONY: clean-extension

clean-quickstart:
> GRAALVM_HOME=$(GRAALVM_HOME) QUICKSTART=$(QUICKSTART) make -C quarkus-quickstart clean
.PHONY: clean-quickstart

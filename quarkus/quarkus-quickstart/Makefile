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

QUICKSTART ?= tika-quickstart
GRAALVM_HOME ?= /opt/graalvm-ce-java11-21.3.0-dev

QUARKUS_VERSION ?= 999-SNAPSHOT

mvn = JAVA_HOME=$(GRAALVM_HOME)
mvn += ./mvnw
mvnw = $(project)/mvnw
project := /opt/quarkus-quickstarts
runner = $(project)/$(QUICKSTART)/$(target_dir)/$(QUICKSTART)-1.0.0-SNAPSHOT-runner
target_dir := target

ifdef VERBOSE
  mvn += -X
endif

test: $(mvnw)
> cd $(project)
> $(mvn) \
>   -Dquarkus-plugin.version=$(QUARKUS_VERSION) \
>   -Dquarkus.platform.version=$(QUARKUS_VERSION) \
>   -Pnative \
>   --projects $(QUICKSTART) \
>   install
.PHONY: test

clean:
> cd $(project)
> $(mvn) clean --projects $(QUICKSTART)
.PHONY: clean

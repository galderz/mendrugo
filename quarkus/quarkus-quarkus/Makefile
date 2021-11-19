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

GRAALVM_HOME ?= /opt/mandrel-java11-21.3.0.1-dev

current_dir = $(shell pwd)
project = /opt/quarkus-quarkus
target_dir = target
vm := $(GRAALVM_HOME)

sources += $(shell find $(project)/ -type f -name '*.java' | sed 's: :\\ :g')
sources += $(shell find $(project)/ -type f -name 'pom.xml' | sed 's: :\\ :g')

# Point to the last jar built
jar += $(project)/docs/target/quarkus-documentation-999-SNAPSHOT.jar

ifdef VERBOSE
  mvn += -X
endif

native_args += -Dquarkus.native.additional-build-args=$(NATIVE_ARGS)
# ifdef NATIVE_ARGS
# else
#   native_args = ""
# endif

test: $(jar) $(target_dir)
> cd $(project)/integration-tests
> JAVA_HOME=$(vm) ../mvnw \
>   -Dformat.skip \
>   -Denforcer.skip \
>   -Dnative \
>   $(native_args) \
>   --fail-at-end \
>   install \
>  | tee $(current_dir)/$(target_dir)/console-test.log

$(target_dir):
> mkdir $@

jars: $(jar)
.PHONY: jars

$(jar): $(mvnw) $(sources)
> cd $(project)
> JAVA_HOME=$(vm) ./mvnw \
>   -Dformat.skip \
>   -Denforcer.skip \
>   -DskipTests \
>   --also-make \
>   install
> touch $@

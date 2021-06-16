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

jar := target/example.jar
exec := target/example

$(jar):
> mvn install

build-time: $(jar)
> native-image \
> -jar $(jar) \
> --initialize-at-build-time \
> --no-fallback \
> -H:+PrintHeapHistogram \
> $(exec)
.PHONY: build-time

clean:
> mvn clean
.PHONY: clean

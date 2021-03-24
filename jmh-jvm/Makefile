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

bench-cmd += java
bench-cmd += -jar
bench-cmd += target/benchmarks.jar
bench-cmd += -wi
bench-cmd += 1
bench-cmd += -f
bench-cmd += 1

build:
> mvn verify
.PHONY: build

bench: build
> $(bench-cmd)
.PHONY: bench

bench-agent: build
> mkdir -p src/main/resources/META-INF/native-image
> $(bench-cmd) -jvmArgsAppend "-agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image"

clean:
> mvn clean
.PHONY: clean

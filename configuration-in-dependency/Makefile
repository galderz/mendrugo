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

JAVA_FILES = $(shell find src/ -type f -name '*.java')

NATIVEFLAGS += -ea
NATIVEFLAGS += --no-fallback
NATIVEFLAGS += --verbose

program := target/example
jar := target/example.jar

run: $(program)
> ./$(program)

$(program): $(jar)
> native-image -jar $(jar) ${NATIVEFLAGS} $(program)

$(jar): pom.xml $(JAVA_FILES)
> mvn package

clean:
> mvn clean
.PHONY: clean

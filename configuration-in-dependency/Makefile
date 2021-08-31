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

program := target/example
jar := target/example.jar

native_image += native-image

ifdef EXCLUDE
  native_image += --exclude-config netty-common-*.jar META-INF\/native-image\/.*.properties
endif

native_image += -jar
native_image += $(jar)
native_image += -ea
native_image += --no-fallback

ifdef VERBOSE
  native_image += --verbose
endif

run: $(program)
> ./$(program)

$(program): $(jar)
> $(native_image) $(program)

$(jar): pom.xml $(JAVA_FILES)
> mvn package

clean:
> mvn clean
.PHONY: clean

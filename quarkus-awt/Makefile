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

# Shared
mvn += ./mvnw

# Mandrel

# Quarkus

# Project
sources := $(shell find src -type f -name '*')
sources += pom.xml
target_dir = target

jar = $(target_dir)/quarkus-app/quarkus-run.jar

$(jar): $(sources)
> $(mvn) package
> touch $(jar)

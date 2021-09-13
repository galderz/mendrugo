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

# Mandrel

# Quarkus

# Project
java_home := /opt/graalvm-ce-java11-21.2.0
sources := $(shell find src -type f -name '*')
sources += pom.xml
target_dir = target

exec := $(target_dir)/quarkus-awt-1.0.0-SNAPSHOT-runner
jar := $(target_dir)/quarkus-app/quarkus-run.jar
java := $(java_home)/bin/java
mvn += JAVA_HOME=$(java_home)
mvn += ./mvnw

$(exec): $(sources)
> $(mvn) package -Dnative
> ./$(exec)

$(jar): $(sources)
> $(mvn) package
> touch $(jar)
> $(java) -jar $(jar)

clean:
> $(mvn) clean
.PHONY: clean

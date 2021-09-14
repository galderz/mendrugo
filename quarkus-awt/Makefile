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
JAVA_HOME ?= /opt/graalvm-ce-java11-21.2.0

# Mandrel

# Quarkus

# Project
exec = $(target_dir)/quarkus-awt-1.0.0-SNAPSHOT-runner
jar = $(target_dir)/quarkus-app/quarkus-run.jar
java = $(JAVA_HOME)/bin/java
mvn += JAVA_HOME=$(JAVA_HOME)
mvn += ./mvnw
sources += $(shell find src -type f -name '*')
sources += pom.xml
target_dir := target

$(exec): $(sources)
> $(mvn) package -Dnative
> ./$@

run-jar: $(jar)
> $(java) -jar $<
.PHONY: run-jar

$(jar): $(sources)
> $(mvn) package
> touch $@

clean:
> $(mvn) clean
.PHONY: clean

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
mvn += JAVA_HOME=$(JAVA_HOME)
mvn += ./mvnw

# Quarkus Variables
quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/runtime/target/quarkus-core-$(quarkus_version).jar
quarkus_overrides += -Dquarkus-plugin.version=$(quarkus_version)
quarkus_overrides += -Dquarkus.platform.version=$(quarkus_version)
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name '*.java' | sed 's: :\\ :g')
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')
quarkus_version := 999-SNAPSHOT

ifndef VALIDATE
  quarkus_args += -Denforcer.skip
  quarkus_args += -Dformat.skip
endif

# Mandrel

# Project
exec = $(target_dir)/quarkus-awt-1.0.0-SNAPSHOT-runner
jar = $(target_dir)/quarkus-app/quarkus-run.jar
java = $(JAVA_HOME)/bin/java
native_args := -Dnative
sources += $(shell find src -type f -name '*')
sources += pom.xml
target_dir := target

ifdef VERBOSE
  native_args += -Dquarkus.native.additional-build-args=-H:+PrintClassInitialization
endif

$(exec): $(sources) $(quarkus_jar_core)
> $(mvn) package $(quarkus_overrides) $(native_args)
> ./$@

run-jar: $(jar)
> $(java) -jar $<
.PHONY: run-jar

$(jar): $(sources) $(quarkus_jar_core) $(quarkus_jar_maven)
> $(mvn) package $(quarkus_overrides)
> touch $@

clean:
> $(mvn) clean
.PHONY: clean

# Quarkus Targets
$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -DskipTests -pl core/runtime,devtools/maven,extensions/arc/deployment -am $(quarkus_args)
> touch $@

clean-quarkus:
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean-quarkus

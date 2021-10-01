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

# GraalVM
VM_HOME ?= /opt/graalvm-ce-java11-21.2.0

native_image = $(VM_HOME)/bin/native-image

# Maven
maven_home := /opt/maven
mvn += JAVA_HOME=$(VM_HOME)
mvn += $(maven_home)/bin/mvn

# Quarkus Variables
quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/runtime/target/quarkus-core-$(quarkus_version).jar
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name '*.java' | sed 's: :\\ :g')
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')
quarkus_version := 999-SNAPSHOT

ifndef VALIDATE
  quarkus_args += -Denforcer.skip
  quarkus_args += -Dformat.skip
endif

ifdef NATIVE_ARGS
  quarkus_native_args += -Dquarkus.native.additional-build-args=$(NATIVE_ARGS)
endif

# Optaplanner
opta_home := /opt/optaplanner
opta_jar_it = $(opta_home)/optaplanner-quarkus-integration/optaplanner-quarkus/integration-test/target/optaplanner-quarkus-integration-test-8.12.0-SNAPSHOT-tests.jar
opta_sources += $(shell find $(opta_home)/ -type f -name '*.java' | sed 's: :\\ :g')
opta_sources += $(shell find $(opta_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')

test: $(native_image) $(opta_jar_it)
> cd $(opta_home)
> $(mvn) install -pl optaplanner-quarkus-integration/optaplanner-quarkus/integration-test -Dversion.io.quarkus=$(quarkus_version) -Dnative -Dquarkus.log.level=TRACE $(quarkaus_args) $(quarkus_native_args)
.PHONY: test

$(opta_jar_it): $(opta_sources) $(quarkus_jar_core)
> cd $(opta_home)
> $(mvn) install -pl optaplanner-quarkus-integration/optaplanner-quarkus/integration-test -Dversion.io.quarkus=$(quarkus_version) -DskipTests -am
> touch $@

clean:
> cd $(opta_home)
> $(mvn) clean
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean

# Quarkus Targets
$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -pl core/deployment,devtools/maven,extensions/arc/deployment,extensions/awt/deployment -DskipTests -am $(quarkus_args) $(quarkus_native_args)
> touch $@
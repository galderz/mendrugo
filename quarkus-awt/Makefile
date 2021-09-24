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
TEST ?= awt

quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/runtime/target/quarkus-core-$(quarkus_version).jar
quarkus_it_test = $(quarkus_home)/integration-tests/$(TEST)/target/quarkus-integration-test-$(TEST)-$(quarkus_version).jar
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
quarkus_native_args := -Dnative
sources += $(shell find src -type f -name '*')
sources += pom.xml
target_dir := target

# Double escape for native arguments containing `,`:
# E.g. NATIVE_ARGS=--trace-class-initialization=sun.awt.AWTAccessor\\\\,java.awt.Toolkit\\\\,java.awt.Color make test-tika
# Or: NATIVE_ARGS=-H:+PrintAnalysisCallTree
ifdef NATIVE_ARGS
  quarkus_native_args += -Dquarkus.native.additional-build-args=$(NATIVE_ARGS)
endif

$(exec): $(sources) $(quarkus_jar_core)
> $(mvn) package $(quarkus_overrides) $(quarkus_native_args)
> ./$@

run-jar: $(jar)
> $(java) -jar $<
.PHONY: run-jar

$(jar): $(sources) $(quarkus_jar_core) $(quarkus_jar_maven)
> $(mvn) package $(quarkus_overrides)
> touch $@

clean:
> $(mvn) clean $(quarkus_overrides)
.PHONY: clean

# Quarkus Targets
$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -pl core/runtime,devtools/maven,extensions/arc/deployment,extensions/awt/deployment -DskipTests -am $(quarkus_args) $(quarkus_native_args)
> touch $@

$(quarkus_it_test): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -pl integration-tests/$(TEST) -DskipTests -am $(quarkus_args)
> touch $@

test: $(quarkus_it_test)
> cd $(quarkus_home)
> $(mvn) install -pl integration-tests/$(TEST) -Dnative -Dquarkus.log.level=TRACE $(quarkus_args) $(quarkus_native_args)
.PHONY: test

clean-quarkus:
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean-quarkus

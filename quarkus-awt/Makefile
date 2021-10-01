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

# Mandrel
MANDREL_BIN ?= /opt/mandrel-java11-21.2
MANDREL_BOOT ?= /opt/adopt-11.0.12+7
MANDREL_HOME ?= /opt/mandrel-21.2
MANDREL_PACKAGING ?= /opt/mandrel-packaging
MX_HOME ?= /opt/mx

mx = $(MX_HOME)/mx
native_image = $(MANDREL_BIN)/bin/native-image
substrate := $(MANDREL_HOME)/substratevm
substrate_java_files = $(shell find $(substrate)/ -type f -name '*.java')

# Shared
#JAVA_HOME ?= /opt/graalvm-ce-java11-21.2.0
mvn += JAVA_HOME=$(MANDREL_BIN)
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

# Project
exec = $(target_dir)/quarkus-awt-1.0.0-SNAPSHOT-runner
jar = $(target_dir)/quarkus-app/quarkus-run.jar
java = $(JAVA_HOME)/bin/java
quarkus_native_args += -Dnative
quarkus_native_args += -Dquarkus.native.debug.enabled=true
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

# Mandrel Targets
# $(native_image): $(substrate_java_files)
# > cd /opt
# > JAVA_HOME=$(MANDREL_BOOT) \
# > $(MANDREL_BOOT)/bin/java \
# > -ea \
# > $(MANDREL_PACKAGING)/build.java \
# > --mx-home $(MX_HOME) \
# > --mandrel-repo $(MANDREL_HOME) \
# > --mandrel-home $(MANDREL_BIN) \
# > --skip-native-agents

# Quarkus Targets
$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -pl core/runtime,devtools/maven,extensions/arc/deployment,extensions/awt/deployment -DskipTests -am $(quarkus_args) $(quarkus_native_args)
> touch $@

$(quarkus_it_test): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) install -pl integration-tests/$(TEST) -DskipTests -am $(quarkus_args)
> touch $@

test: $(native_image) $(quarkus_it_test)
> cd $(quarkus_home)
> $(mvn) install -pl integration-tests/$(TEST) -Dnative -Dquarkus.log.level=TRACE $(quarkus_args) $(quarkus_native_args)
.PHONY: test

ifdef GDB
  runner += gdb
endif

exec-runner:
> $(runner) $(quarkus_home)/integration-tests/$(TEST)/target/quarkus-integration-test-$(TEST)-999-SNAPSHOT-runner
.PHONY: exec-runner

curl-tika-runner:
> curl \
> -X POST \
> -H "Content-Type: application/pdf" \
> -F "file=@/$(quarkus_home)/integration-tests/tika/src/main/resources/invoice.pdf" \
> http://localhost:8080/invoice/text
.PHONY: curl-tika-runner

clean-quarkus:
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean-quarkus

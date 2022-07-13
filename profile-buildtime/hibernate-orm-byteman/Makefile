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

BYTEMAN_HOME ?= /opt/byteman/byteman/target
GRAALVM_HOME ?= /opt/graalvm-ce-java17-22.1.0
MVN_HOME ?= /opt/maven

mvn += JAVA_HOME=$(GRAALVM_HOME)
mvn += $(MVN_HOME)/bin/mvn

print = echo '$(1)'
comma := ,

ifdef DEBUG
  args += -Dquarkus.native.additional-build-args=-J-javaagent:$(byteman_jar)=boot:$(byteman_jar)\\$(comma)$(byteman_scripts)\\$(comma)$(byteman_sysjar),-H:HeapVerifierVerbosity=1,--debug-attach=*:8000
else ifdef VERBOSE
  args += -Dquarkus.native.additional-build-args=-J-javaagent:$(byteman_jar)=boot:$(byteman_jar)\\$(comma)$(byteman_scripts)\\$(comma)$(byteman_sysjar)\\$(comma)prop:org.jboss.byteman.verbose=true,-H:HeapVerifierVerbosity=1
else
  args += -Dquarkus.native.additional-build-args=-J-javaagent:$(byteman_jar)=boot:$(byteman_jar)\\$(comma)$(byteman_scripts)\\$(comma)$(byteman_sysjar),-H:HeapVerifierVerbosity=1
endif

byteman_jar = $(BYTEMAN_HOME)/byteman-4.0.18.jar

#byteman_scripts = script:$(resources_dir)/graalvm.btm
byteman_scripts = script:$(resources_dir)/reflection-registry.btm

byteman_sysjar = sys:hibernate-orm-quickstart-1.0.0-SNAPSHOT-runner.jar
# Root for native-image invocation is `target/*-native-image-source-jar` folder
resources_dir := ../../src/main/resources

package:
> $(mvn) package -DskipTests -Pnative $(args)
.PHONY: package
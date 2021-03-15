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

BYTEMAN_HOME := /opt/byteman-4.0.14
JAVA8_HOME := /opt/graalvm-ce-java8-21.0.0.2
JAVA11_HOME := /opt/graalvm-ce-java11-21.0.0.2
QUARKUS_VERSION := 1.12.2.Final
QUICKSTARTS_HOME := /opt/quarkus-quickstarts

all: native-java8 native-java11

native-java8:
> $(call native,$(JAVA8_HOME))

native-java11:
> $(call native,$(JAVA11_HOME))

define native
cd $(QUICKSTARTS_HOME)/validation-quickstart
git checkout $(QUARKUS_VERSION)
for number in 1 2 3 4 5 ; do \
JAVA_HOME=$1 mvn clean install -DskipTests -Dnative ; \
done
endef

count-classes-java8:
> $(call native-byteman,$(JAVA8_HOME),count-classes)

count-classes-java11:
> $(call native-byteman,$(JAVA11_HOME),count-classes)

list-classes-java8:
> $(call native-byteman,$(JAVA8_HOME),list-classes)

list-classes-java11:
> $(call native-byteman,$(JAVA11_HOME),list-classes)

define native-byteman
cd $(QUICKSTARTS_HOME)/validation-quickstart
git checkout $(QUARKUS_VERSION)
JAVA_HOME=$(1) mvn clean install -DskipTests -Dnative -Dquarkus.native.additional-build-args="-J-javaagent:$(BYTEMAN_HOME)/lib/byteman.jar=script:/home/g/1/mendrugo/faster-builds/rules/$(2).btm\,boot:$(BYTEMAN_HOME)/lib/byteman.jar\,prop:org.jboss.byteman.verbose=true\,prop:org.jboss.byteman.transform.all=true"
endef

debug-attach:
> cd $(QUICKSTARTS_HOME)/validation-quickstart
> git checkout $(QUARKUS_VERSION)
> JAVA_HOME=$(JAVA11_HOME) mvn clean install -DskipTests -Dnative -Dquarkus.native.additional-build-args="--debug-attach=*:8000"


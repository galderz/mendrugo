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

EXTENSION ?= tika
GRAALVM_HOME ?= /opt/graalvm-ce-java11-21.3.0-dev

mvn = JAVA_HOME=$(GRAALVM_HOME)
mvn += $(mvnw)
mvnw = $(project)/mvnw
native_image := $(GRAALVM_HOME)/bin/native-image
project := /opt/quarkus-quarkus

sources += $(shell find $(project)/ -type f -name '*.java' | sed 's: :\\ :g')
sources += $(shell find $(project)/ -type f -name 'pom.xml' | sed 's: :\\ :g')

jar_modules = bom/application$\
,bom/test$\
,devtools/maven$\
,independent-projects/bootstrap/bom$\
,independent-projects/bootstrap/bom-test$\
,independent-projects/bootstrap/core$\
,integration-tests/$(EXTENSION)
test_modules = integration-tests/$(EXTENSION)

jar += $(project)/integration-tests/$(EXTENSION)/target/quarkus-integration-test-$(EXTENSION)-999-SNAPSHOT.jar

ifdef VERBOSE
  mvn += -X
endif  

native_args += -Dquarkus.native.additional-build-args=$(NATIVE_ARGS)

test: $(jar) $(native_image)
> cd $(project)
> $(mvn) \
>   -Dformat.skip \
>   -Denforcer.skip \
>   --projects $(test_modules) \
>   -Dnative \
>   $(native_args) \
>   install

$(native_image):
> $(GRAALVM_HOME)/bin/gu install native-image

jars: $(jar)
.PHONY: jars

$(jar): $(mvnw) $(sources)
> cd $(project)
> $(mvn) \
>   -Dformat.skip \
>   -Denforcer.skip \
>   --projects $(jar_modules) \
>   -DskipTests \
>   --also-make \
>   install
> touch $@

dependency-tree:
> cd $(project)
> $(mvn) \
>   --projects $(test_modules) \
>   dependency:tree
.PHONY: dependency-tree

clean:
> cd $(project)
> rm -drf integration-tests/$(EXTENSION)/target
> $(mvn) clean
.PHONY: clean

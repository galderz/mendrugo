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

GRAALVM_HOME ?= /opt/graalvm-ce-java11-21.3.0-dev

mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
project = /opt/quarkus-quarkus
vm := $(GRAALVM_HOME)

sources += $(shell find $(project)/ -type f -name '*.java' | sed 's: :\\ :g')
sources += $(shell find $(project)/ -type f -name 'pom.xml' | sed 's: :\\ :g')

jar_modules = bom/application$\
,devtools/maven$\
,independent-projects/bootstrap/bom$\
,independent-projects/bootstrap/bom-test$\
,independent-projects/bootstrap/core$\
,integration-tests/tika
test_modules = integration-tests/tika

jar += $(project)/integration-tests/tika/target/quarkus-integration-test-tika-999-SNAPSHOT.jar

ifdef VERBOSE
  mvn += -X
endif  

ifdef NATIVE_ARGS
  native_args += -Dquarkus.native.additional-build-args=$(NATIVE_ARGS)
endif

test: $(jar)
> cd $(project)
> $(mvn) \
>   -Dformat.skip \
>   -Denforcer.skip \
>   --projects $(test_modules) \
>   -Dnative \
>   $(native_args) \
>   install

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
> rm -drf integration-tests/tika/target
> $(mvn) clean
.PHONY: clean

poi := /opt/apache-poi

poi-snapshot:
> cd $(poi)
> JAVA_HOME=$(vm) ./gradlew :main:build -xtest
> cp ./build/main/build/libs/main-4.1.2-SNAPSHOT.jar ~/.m2/repository/org/apache/poi/poi/4.1.2/poi-4.1.2.jar
.PHONY: poi-snapshot

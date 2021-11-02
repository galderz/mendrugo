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

QUARKUS_VERSION ?= 2.4.0.CR1

base := /tmp
project = $(base)/mendrugo/$(project_name)
project_name = camel-quarkus-$(QUARKUS_VERSION)
app := tika-quickstart
mendrugo = $(base)/mendrugo
mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
mvnw = $(project)/mvnw
runner = $(project)/$(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
target_dir := target
vm := $(GRAALVM_HOME)

modules = integration-tests/tika
jar += $(project)/integration-tests/tika/target/camel-quarkus-integration-test-tika-2.5.0-SNAPSHOT.jar

test: $(jar)
> cd $(project)
> $(mvn) \
>   -Dquarkus.version=$(QUARKUS_VERSION) \
>   -Dformat.skip \
>   -Denforcer.skip \
>   --projects $(modules) \
>   -Pnative \
> install

$(jar): $(mvnw)
> cd $(project)
> $(mvn) \
>   -Dquarkus.version=$(QUARKUS_VERSION) \
>   -Dformat.skip \
>   -Denforcer.skip \
>   --projects $(modules) \
>   -DskipTests \
>   --also-make \
> install

$(mvnw):
> mkdir -p $(mendrugo)
> pushd $(mendrugo)
> git clone --depth 1 https://github.com/apache/camel-quarkus $(project_name) || true

clean:
> cd $(project)
> $(mvn) clean
.PHONY: clean

clean-all:
> rm -drf $(mendrugo)
.PHONY: clean-all

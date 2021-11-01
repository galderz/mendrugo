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

# Works with 2.3.0.Final
#QUARKUS_VERSION ?= 2.3.0.Final
# Doesn't work with 2.4.0.CR1
QUARKUS_VERSION ?= 2.4.0.CR1

base := /tmp
project = $(base)/mendrugo/$(project_name)
project_name = quarkus-quickstarts-$(QUARKUS_VERSION)
app := tika-quickstart
mendrugo = $(base)/mendrugo
mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
mvnw = $(project)/mvnw
runner = $(project)/$(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
target_dir := target
vm := $(GRAALVM_HOME)

ifdef VERBOSE
  mvn += -X
endif

$(runner): $(mvnw)
> cd $(project)/$(app)
> $(mvn) install \
> -Dquarkus-plugin.version=$(QUARKUS_VERSION) \
> -Dquarkus.platform.version=$(QUARKUS_VERSION) \
> -Pnative \
> -Dquarkus.native.add-all-charsets=false \
> -Dquarkus.native.enable-reports

$(mvnw):
> mkdir -p $(mendrugo)
> pushd $(mendrugo)
> git clone --depth 1 https://github.com/quarkusio/quarkus-quickstarts $(project_name)|| true

clean:
> cd $(project)
> $(mvn) clean
.PHONY: clean

clean-all:
> rm -drf $(mendrugo)
.PHONY: clean-all

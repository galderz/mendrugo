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

# Quarkus Tika Quickstart

app = $(target_dir)/tika-quickstart-$(QUARKUS_VERSION)
mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
mvnw = $(app)/mvnw
runner = $(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
target_dir := target
vm := $(GRAALVM_HOME)

ifdef VERBOSE
  mvn += -X
endif

$(runner): $(mvnw)
> cd $(app)
> $(mvn) install \
> -Dquarkus-plugin.version=$(QUARKUS_VERSION) \
> -Dquarkus.platform.version=$(QUARKUS_VERSION) \
> -Pnative \
> -Dquarkus.native.add-all-charsets=false \
> -Dquarkus.native.enable-reports \

$(mvnw):
> pushd /tmp
> git clone https://github.com/quarkusio/quarkus-quickstarts || true
> popd
> mkdir -p $(target_dir)
> cp -r /tmp/quarkus-quickstarts/tika-quickstart $(app)

clean:
> cd $(app)
> $(mvn) clean
.PHONY: clean

clean-all:
> rm -drf $(target_dir)
.PHONY: clean-all

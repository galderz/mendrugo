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

GRAALVM_HOME ?= graalvm-ce-java11-21.2.0
QUARKUS_VERSION ?= 2.1.2.Final

app := code-with-quarkus
mvnw := $(app)/mvnw
target_dir := target
runner := $(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
vm := /opt/$(GRAALVM_HOME)

mvn += JAVA_HOME=$(vm)
mvn += ./mvnw

$(runner): $(mvnw)
> cd $(app)
> $(mvn) package -DskipTests -Pnative

$(mvnw):
> true | mvn io.quarkus:quarkus-maven-plugin:$(QUARKUS_VERSION):create

bench:
> cd $(app)
> $(mvn) package -DskipTests -Pnative
.PHONY: bench

reports: $(mvnw)
> cd $(app)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.enable-reports
.PHONY: reports

analyse:
> rm -f target/*.csv
> jbang ../analyse.java
.PHONY: analyse

clean:
> rm -drf $(app)
.PHONY: clean

clean-all:
> rm -drf $(target_dir)
.PHONY: clean-all

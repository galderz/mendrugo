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

GRAALVM_HOME ?= graalvm-ce-java11-21.3.0-dev

app := spring-petclinic-data-jdbc
bench_args += -DbuildArgs=$(NATIVE_ARGS)
mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
mvnw = $(app)/mvnw
vm = /opt/$(GRAALVM_HOME)

bench: $(mvnw)
> cd $(app)
> $(mvn) package -DskipTests -Pnative $(bench_args)
.PHONY: bench

$(mvnw):
> git clone https://github.com/spring-petclinic/$(app)

clean:
> cd $(app)
> $(mvn) clean
.PHONY: clean

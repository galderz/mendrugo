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

GRAALVM_HOME ?= /opt/graalvm-21.3/graalvm_home

# To test:
# $ make build
# $ make datetime
# $ make number

java += java
java += -cp
java += main-project/target/main-project-0.0.1-SNAPSHOT.jar:project-with-optionals/target/project-with-optionals-0.0.1-SNAPSHOT.jar
java += org.example.main.Main

native_image += $(GRAALVM_HOME)/bin/native-image
native_image += -H:+ReportExceptionStackTraces
native_image += --no-fallback

ifdef DEBUG
  native_image += --debug-attach=*:8000
endif

build:
> mvn -DskipTests=true install
.PHONY: build

datetime:
> $(java) Europe/Paris,2015-05-05,10:15:30
.PHONY: datetime

number:
> $(java) 1234
.PHONY: number

native:
> $(native_image) \
> -cp \
> main-project/target/main-project-0.0.1-SNAPSHOT.jar:project-with-optionals/target/project-with-optionals-0.0.1-SNAPSHOT.jar \
> org.example.main.Main \
> target/a.out
.PHONY: native

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

java += java
java += -cp
java += main-project/target/main-project-0.0.1-SNAPSHOT.jar:project-with-optionals/target/project-with-optionals-0.0.1-SNAPSHOT.jar
java += org.example.main.Main

build:
> mvn -DskipTests=true install
.PHONY: build

datetime:
> $(java) Europe/Paris,2015-05-05,10:15:30
.PHONY: datetime

number:
> $(java) 1234
.PHONY: number

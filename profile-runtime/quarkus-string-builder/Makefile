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

mvn += JAVA_HOME=/opt/graalvm-ce-java17-22.3.0
mvn += ./mvnw
runner = target/quarkus-string-builder-1.0.0-SNAPSHOT-runner
sources += $(shell find . -type f -name '*.java')
sources += pom.xml

$(runner): $(sources)
> $(mvn) package -DskipTests -Dnative

clean:
> rm -drf target
.PHONY: clean

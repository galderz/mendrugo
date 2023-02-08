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

QUARKUS_VERSION=2.16.1
JAVA_HOME=/opt/graalvm-ce-java17-22.3.0

app := code-with-quarkus
mvn += JAVA_HOME=$(JAVA_HOME)
mvn += /opt/boot-maven-3/bin/mvn
pom = $(app)/pom.xml
runner = $(target)/code-with-quarkus-1.0.0-SNAPSHOT-runner
sources += $(shell find $(app) -type f -name '*.java' | sed 's: :\\ :g')
target = $(app)/target

$(runner): $(pom) $(sources)
> cd $(app)
> $(mvn) package -DskipTests -Dnative \
>   -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:+PreserveFramePointer

$(pom):
> $(mvn) io.quarkus.platform:quarkus-maven-plugin:$(QUARKUS_VERSION).Final:create \
>   -DprojectGroupId=org.acme \
>   -DprojectArtifactId=code-with-quarkus

clean:
> rm -drf $(target)
.PHONY: clean

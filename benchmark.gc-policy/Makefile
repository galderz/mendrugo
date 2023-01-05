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

JVM ?= /opt/graalvm-ce-java17-22.3.0
QUARKUS_VERSION ?= 2.15.2

mvn += JAVA_HOME=$(JVM)
mvn += /opt/boot-maven-3/bin/mvn

app-a := adaptive
app-s := spacetime
pom-a = $(app-a)/pom.xml
pom-s = $(app-s)/pom.xml
runner-a = $(app-a)/target/$(app-a)-1.0.0-SNAPSHOT-runner
runner-s = $(app-s)/target/$(app-s)-1.0.0-SNAPSHOT-runner

runner: $(runner-a) $(runner-s)

$(runner-a): $(pom-a)
> $(call build-native,$(app-a))

$(runner-s): $(pom-s)
> $(call build-native,$(app-s),-H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$$BySpaceAndTime)

define build-native
cd ${1}
$(mvn) package -DskipTests -Dnative -Dquarkus.native.additional-build-args=${2}
endef

pom: $(pom-a) $(pom-s)

$(pom-a):
> $(call get-pom,$(app-a))

$(pom-s):
> $(call get-pom,$(app-s))

define get-pom
$(mvn) io.quarkus.platform:quarkus-maven-plugin:$(QUARKUS_VERSION).Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=${1}
endef

clean:
> rm -drf $(app-a)
> rm -drf $(app-s)
.PHONY: clean

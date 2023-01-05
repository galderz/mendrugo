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

hyperfoil = /opt/hyperfoil/bin/cli.sh
mvn += JAVA_HOME=$(JVM)
mvn += /opt/boot-maven-3/bin/mvn
jvisualvm := $(JVM)/bin/jvisualvm

app_a := adaptive
app_s := spacetime
pom_a = $(app_a)/pom.xml
pom_s = $(app_s)/pom.xml
native_args_a = --enable-monitoring=jvmstat
native_args_s = -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$$BySpaceAndTime,--enable-monitoring=jvmstat
runner_a = $(app_a)/target/$(app_a)-1.0.0-SNAPSHOT-runner
runner_s = $(app_s)/target/$(app_s)-1.0.0-SNAPSHOT-runner

default: $(runner_a) $(runner_s)

run-a: $(runner_a)
> $(call run,$(runner_a))
.PHONY: run-a

run-s: $(runner_b)
> $(call run,$(runner_s))
.PHONY: run-s

define run
${1} -Xmx8g
endef

$(runner_a): $(pom_a)
> $(call build-native,$(app_a),$(native_args_a))

$(runner_s): $(pom_s)
> $(call build-native,$(app_s),$(native_args_s))

define build-native
cd ${1}
$(mvn) package -DskipTests -Dnative -Dquarkus.native.additional-build-args=${2}
endef

pom: $(pom_a) $(pom_s)

$(pom_a):
> $(call get-pom,$(app_a))

$(pom_s):
> $(call get-pom,$(app_s))

define get-pom
$(mvn) io.quarkus.platform:quarkus-maven-plugin:$(QUARKUS_VERSION).Final:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=${1}
endef

hyperfoil:
> $(hyperfoil)
.PHONY: hyperfoil

jvisualvm: $(jvisualvm)
> $<
.PHONY: jvisualvm

$(jvisualvm):
> $(JVM)/bin/gu install jvisualvm

clean:
> pushd $(app_a)
> $(mvn) clean
> popd
> pushd $(app_s)
> $(mvn) clean
> popd
.PHONY: clean

clean-all:
> rm -drf $(app_a)
> rm -drf $(app_s)
.PHONY: clean-all

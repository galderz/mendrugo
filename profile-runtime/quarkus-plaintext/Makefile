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

JVM ?= /opt/java-17

app := getting-started
jar = $(app)/target/quarkus-app/quarkus-run.jar
java = $(JVM)/bin/java
mvn += JAVA_HOME=$(JVM)
mvn += ./mvnw
mvnw = $(app)/mvnw

run: $(jar)
> $(java) -jar $(JVM_ARGS) $<
.PHONY: run

shenandoah: $(jar)
> $(java) -jar -XX:+UnlockExperimentalVMOptions -XX:+ShenandoahRegionSampling -XX:+UseShenandoahGC $<
.PHONY: shenandoah

$(jar): $(mvnw)
> cd $(app)
> $(mvn) package -DskipTests

$(mvnw):
> pushd /tmp
> git clone https://github.com/quarkusio/quarkus-quickstarts
> popd
> cp -r /tmp/quarkus-quickstarts/getting-started .

clean:
> rm -drf $(app)
> rm -drf /tmp/quarkus-quickstarts
.PHONY: clean

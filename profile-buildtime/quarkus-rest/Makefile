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

GRAALVM_HOME ?= graalvm-ce-java11-21.3-dev
QUARKUS_VERSION ?= 2.1.2.Final
AP_HOME ?= /opt/async-profiler

app := code-with-quarkus
mvnw := $(app)/mvnw
target_dir := target
runner := $(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
native_dir := $(target_dir)/$(app)-1.0.0-SNAPSHOT-native-image-source-jar
vm := /opt/$(GRAALVM_HOME)

ap_agent := -J-agentpath:$(AP_HOME)/build/libasyncProfiler.so=start

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

profiling: $(mvnw)
> mkdir -p target/$(GRAALVM_HOME)
> cd $(app)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.additional-build-args=$(ap_agent)\\,file=profile-wall.svg\\,event=wall\\,threads\\,interval=10ms | tee target/console-wall.log
> cp $(native_dir)/*.svg ../target/$(GRAALVM_HOME)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.additional-build-args=$(ap_agent)\\,file=profile-lock.svg\\,event=lock\\,total | tee target/console-lock.log
> cp $(native_dir)/*.svg ../target/$(GRAALVM_HOME)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.additional-build-args=$(ap_agent)\\,file=profile-alloc.svg\\,event=alloc | tee target/console-alloc.log
> cp $(native_dir)/*.svg ../target/$(GRAALVM_HOME)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.additional-build-args=$(ap_agent)\\,file=profile-cpu.svg\\,event=cpu | tee target/console-cpu.log
> cp $(native_dir)/*.svg ../target/$(GRAALVM_HOME)
.PHONY: profiling

clean:
> rm -drf $(app)
.PHONY: clean

clean-all:
> rm -drf $(target_dir)
.PHONY: clean-all

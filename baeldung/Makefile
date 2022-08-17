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

baeldung_home := /opt/baeldung-tutorials
graalvm_builder := /opt/graalbuilder-17
graalvm_home := /opt/mandrel-mandrel/substratevm
graalvm_sources += $(shell find $(graalvm_home)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_version := 22.3.0
java_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
java = $(java_home/bin/java
native_image = $(java_home)/bin/native-image
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
mvn += JAVA_HOME=$(java_home)
mvn += /opt/maven/bin/mvn
quarkus_runner = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project/target/quarkus-project-0.1-SNAPSHOT-runner

ifdef VERBOSE_GC
  native_args += -XX:+PrintGC
  native_args += -XX:+VerboseGC
  native_args += -XX:+PrintHeapShape
  native_args += -XX:+TraceHeapChunks
endif

ifeq ($(GC),serial)
  jvm_args += -XX:+UseSerialGC
  jvm_args += -verbose:gc
else ifeq ($(GC),parallel)
  jvm_args += -XX:+UseParallelGC
  jvm_args += -verbose:gc
endif

run-native: $(quarkus_runner)
> $< $(native_args)
.PHONY: run-native

run-jvm: $(native_image)
> $(java) $(jvm_args)
.PHONY: run-jvm

$(quarkus_runner): $(native_image)
> cd $(baeldung_home)
> $(mvn) package -DskipTests -Dnative

$(native_image): $(graalvm_sources)
> cd $(graalvm_home)
> $(mx) build
> sed -i'' -e 's/EnableJVMCI/EnableJVMCI -Dorg.graalvm.version=$(graalvm_version)-dev/g' ../sdk/latest_graalvm_home/lib/svm/bin/native-image

db:
> docker rm /mysqldb
> docker run --name mysqldb --network=host -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=baeldung -d mysql:5.7.38 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
.PHONY: db

clean:
> cd $(graalvm_home)
> $(mx) clean
.PHONY: clean

clean-all:
> cd $(graalvm_home)
> git clean -dfx
.PHONY: clean-all

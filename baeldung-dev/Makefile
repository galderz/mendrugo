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

args := ""
baeldung_home := /opt/baeldung-tutorials
baeldung_hyperfoil_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/hyperfoil
baeldung_quarkus_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project
baeldung_quarkus_runner = $(baeldung_quarkus_home)/target/quarkus-project-0.1-SNAPSHOT-runner
baeldung_wrk_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/wrk
graalvm_builder := /opt/graalbuilder-17
graalvm_home := /opt/mandrel-mandrel/substratevm
graalvm_sources += $(shell find $(graalvm_home)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_version := 22.3.0
hyperfoil_cli = /opt/hyperfoil/bin/cli.sh
java_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
native_image = $(java_home)/bin/native-image
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
mvn += JAVA_HOME=$(java_home)
mvn += /opt/maven/bin/mvn
wrk_home = /opt/wrk

ifdef VERBOSE_GC
  args += -XX:+PrintGC
  args += -XX:+VerboseGC
  args += -XX:+PrintHeapShape
  args += -XX:+TraceHeapChunks
endif

run: $(baeldung_quarkus_runner)
> $< $(args)
.PHONY: run

wrk:
> cd $(baeldung_wrk_home)
> wrk_home=$(wrk_home) ip=$(IP) ./run_test_wrk.sh
.PHONY: wrk

hyperfoil:
> cd $(baeldung_hyperfoil_home)
> $(hyperfoil_cli)
.PHONY: hyperfoil

$(baeldung_quarkus_runner): $(native_image)
> cd $(baeldung_quarkus_home)
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
> cd $(baeldung_quarkus_home)
> $(mvn) clean
> cd $(graalvm_home)
> $(mx) clean
.PHONY: clean

clean-all:
> cd $(graalvm_home)
> git clean -dfx
.PHONY: clean-all

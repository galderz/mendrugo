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

# The underlying java version in graalvm is aligned with the jvm version
graalvm_home := /opt/graalvm-ce-java17-22.2.0
java = /opt/adopt-17.0.4+8/bin/java

baeldung_home := /opt/baeldung-tutorials
baeldung_quarkus_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project
baeldung_quarkus_runner = $(baeldung_quarkus_home)/target/quarkus-project-0.1-SNAPSHOT-runner
jar = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project/target/quarkus-app/quarkus-run.jar
native_image = $(graalvm_home)/bin/native-image
mvn += JAVA_HOME=$(graalvm_home)
mvn += /opt/maven/bin/mvn

ifeq ($(GC),serial)
  jvm_args += -XX:+UseSerialGC
  jvm_args += -verbose:gc
else ifeq ($(GC),parallel)
  jvm_args += -XX:+UseParallelGC
  jvm_args += -verbose:gc
endif

run-native: $(baeldung_quarkus_runner)
> $< $(native_args)
.PHONY: run-native

$(baeldung_quarkus_runner): $(native_image)
> cd $(baeldung_quarkus_home)
> $(mvn) package -DskipTests -Dnative

db:
> docker rm /mysqldb
> docker run --name mysqldb --network=host -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=baeldung -d mysql:5.7.38 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
.PHONY: db

run-jvm: $(jar)
> $(java) $(jvm_args) -jar $<
.PHONY: run-jvm

$(jar): $(java)
> cd $(baeldung_home)
> $(mvn) package -DskipTests

clean:
> cd $(baeldung_quarkus_home)
> $(mvn) clean
.PHONY: clean

clean-all:
> cd $(baeldung_quarkus_home)
> git clean -dfx
.PHONY: clean-all

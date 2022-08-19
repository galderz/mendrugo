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
baeldung_hyperfoil_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/hyperfoil
baeldung_spring_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/spring-project
baeldung_spring_runner = $(baeldung_spring_home)/target/spring-project
baeldung_quarkus_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project
baeldung_quarkus_runner = $(baeldung_quarkus_home)/target/quarkus-project-0.1-SNAPSHOT-runner
baeldung_wrk_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/wrk
baeldung_quarkus_jar = $(baeldung_quarkus_home)/target/quarkus-app/quarkus-run.jar
native_image = $(graalvm_home)/bin/native-image
mvn += JAVA_HOME=$(graalvm_home)
mvn += /opt/maven/bin/mvn
wrk_home = /opt/wrk

ifeq ($(GC),serial)
  jvm_args += -XX:+UseSerialGC
  jvm_args += -verbose:gc
else ifeq ($(GC),parallel)
  jvm_args += -XX:+UseParallelGC
  jvm_args += -verbose:gc
endif

quarkus-native: $(baeldung_quarkus_runner)
> $<
.PHONY: quarkus-native

wrk:
> cd $(baeldung_wrk_home)
> wrk_home=$(wrk_home) ip=$(IP) ./run_test_wrk.sh
.PHONY: wrk

hyperfoil:
> cd $(baeldung_hyperfoil_home)
> ./docker_run.sh
.PHONY: hyperfoil

$(baeldung_quarkus_runner): $(native_image)
> cd $(baeldung_quarkus_home)
> $(mvn) package -DskipTests -Dnative

db:
> docker rm /mysqldb
> docker run --name mysqldb --network=host -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=baeldung -d mysql:5.7.38 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
.PHONY: db

quarkus-jvm: $(baeldung_quarkus_jar)
> $(java) $(jvm_args) -jar $<
.PHONY: quarkus-jvm

$(baeldung_quarkus_jar): $(java)
> cd $(baeldung_quarkus_home)
> $(mvn) package -DskipTests

clean:
> cd $(baeldung_quarkus_home)
> $(mvn) clean
> cd $(baeldung_spring_home)
> $(mvn) clean
.PHONY: clean

clean-all:
> cd $(baeldung_quarkus_home)
> git clean -dfx
.PHONY: clean-all

spring-native: $(baeldung_spring_runner)
> $<
.PHONY: spring-native

$(baeldung_spring_runner): $(native_image)
> cd $(baeldung_spring_home)
> $(mvn) package -DskipTests -Plocal-native

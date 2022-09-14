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
GRAALVM_HOME ?= /opt/graalvm-ce-java17-22.2.0
java = /opt/adopt-17.0.4+8/bin/java

baeldung_home := /opt/baeldung-tutorials
baeldung_hyperfoil_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/hyperfoil
baeldung_spring_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/spring-project
baeldung_spring_runner = $(baeldung_spring_home)/target/spring-project
baeldung_quarkus_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/quarkus-project
baeldung_quarkus_runner = $(baeldung_quarkus_home)/target/quarkus-project-0.1-SNAPSHOT-runner
baeldung_wrk_home = $(baeldung_home)/quarkus-modules/quarkus-vs-springboot/wrk
baeldung_quarkus_jar = $(baeldung_quarkus_home)/target/quarkus-app/quarkus-run.jar
hyperfoil_cli = /opt/hyperfoil/bin/cli.sh
native_image = $(GRAALVM_HOME)/bin/native-image
mvn += JAVA_HOME=$(GRAALVM_HOME)
mvn += /opt/maven/bin/mvn
quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/deployment/target/quarkus-core-$(quarkus_version).jar
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name '*.java' | sed 's: :\\ :g')
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')
quarkus_version := 999-SNAPSHOT
wrk_home = /opt/wrk

ifeq ($(GC),serial)
  jvm_args += -XX:+UseSerialGC
  jvm_args += -verbose:gc
else ifeq ($(GC),parallel)
  jvm_args += -XX:+UseParallelGC
  jvm_args += -verbose:gc
endif

ifeq ($(VERBOSE_GC),1)
  run_args += -XX:+PrintGC
else ifeq ($(VERBOSE_GC),2)
  run_args += -XX:+PrintGC
  run_args += -XX:+VerboseGC
else ifeq ($(VERBOSE_GC),3)
  run_args += -XX:+PrintGC
  run_args += -XX:+VerboseGC
  run_args += -XX:+PrintHeapShape
else ifeq ($(VERBOSE_GC),4)
  run_args += -XX:+PrintGC
  run_args += -XX:+VerboseGC
  run_args += -XX:+PrintHeapShape
  run_args += -XX:+TraceHeapChunks
endif

ifdef XMN
  run_args += -Xmn$(XMN)
endif

ifdef XMS
  run_args += -Xms$(XMS)
endif

ifdef XMX
  run_args += -Xmx$(XMX)
endif

ifdef BUILD_ARGS
  build_args += -Dquarkus.native.additional-build-args=$(BUILD_ARGS)
endif

native: $(baeldung_quarkus_runner)
> numactl --cpunodebind=2 --localalloc /usr/bin/time -v $< $(run_args)
.PHONY: native

wrk:
> cd $(baeldung_wrk_home)
> wrk_home=$(wrk_home) ip=$(IP) ./run_test_wrk.sh
.PHONY: wrk

hyperfoil:
> cd $(baeldung_hyperfoil_home)
> $(hyperfoil_cli)
.PHONY: hyperfoil

$(baeldung_quarkus_runner): $(native_image) $(quarkus_jar_core)
> cd $(baeldung_quarkus_home)
> $(mvn) package -DskipTests -Dnative $(build_args)

db:
> docker rm /mysqldb || true
> docker run --name mysqldb --network=host -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=baeldung -d mysql:5.7.38 --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
.PHONY: db

mariadb:
> podman run --rm=true --net=host --memory-swappiness=0 --tmpfs /var/lib/mysql:rw --tmpfs /var/log:rw -v /opt/custom-mariadbconfig:/etc/mysql/conf.d --name mariadb_demo -e MYSQL_DATABASE=baeldung -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 mariadb:10.4
.PHONY: mariadb

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
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean

clean-all:
> cd $(baeldung_quarkus_home)
> git clean -dfx
.PHONY: clean-all

spring-native: $(baeldung_spring_runner)
> numactl --cpunodebind=2 --localalloc /usr/bin/time -v $< $(run_args)
.PHONY: spring-native

$(baeldung_spring_runner): $(native_image)
> cd $(baeldung_spring_home)
> $(mvn) package -DskipTests -Plocal-native

$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) -Dquickly -T 4C
> touch $@

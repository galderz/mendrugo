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

QUARKUS ?= 2.13

quarkus_2_7 = 2.7.6.Final
quarkus_2_13 = 2.13.4.Final
graalvm_21_3 = graalvm-ce-java17-21.3.3.1
graalvm_22_3 = graalvm-ce-java17-22.3.0
quarkus_999 = 999-SNAPSHOT

ifeq ($(QUARKUS),2.13)
  branch = topic.1122.minor-tweaks
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-hd)
  branch = topic.1122.minor-tweaks.heapdump
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.7)
  branch = topic.1122.minor-tweaks
  graalvm_home = /opt/$(graalvm_21_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.13-21.3)
  branch = topic.1122.minor-tweaks.2.13-21.3
  graalvm_home = /opt/$(graalvm_21_3)
  maven_opts = -javaagent:/opt/byteman/byteman/target/byteman-4.0.18.jar=script:/opt/mendrugo/start-stop/target/2.13-21.3/quarkus-startstop/app-full-microprofile/src/main/resources/native-image-build-step.btm\,prop:org.jboss.byteman.verbose=true
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.7-22.3)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),999)
  branch = topic.1122.minor-tweaks.999
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_999)
else ifeq ($(QUARKUS),999-21.3)
  branch = topic.1122.minor-tweaks
  graalvm_home = /opt/$(graalvm_21_3)
  quarkus_version = $(quarkus_999)
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

ifdef XMX
  run_args += -Xmx$(XMX)
endif

app_home = $(target_dir)/quarkus-startstop
app_runner = $(app_home)/app-full-microprofile/target/quarkus-runner

mvn += JAVA_HOME=$(graalvm_home)
ifdef maven_opts
  mvn += MAVEN_OPTS=$(maven_opts)
endif
mvn += PATH=$(PATH):/opt/boot-maven-3/bin
mvn += mvn

pom_xml = $(target_dir)/quarkus-startstop/app-full-microprofile/pom.xml
quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/deployment/target/quarkus-core-$(quarkus_version).jar
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name '*.java' | sed 's: :\\ :g')
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')
target_dir = target/$(QUARKUS)

time: $(app_runner)
> /usr/bin/time -v $< $(run_args)
.PHONY: time

massif: $(app_runner)
> valgrind --tool=massif $< $(run_args)
#> valgrind --tool=massif --pages-as-heap=yes $<
.PHONY: massif

snapshot: $(quarkus_jar_core) $(app_runner)
> /usr/bin/time -v $(app_home)/app-full-microprofile/target/quarkus-runner $(run_args)
.PHONY: snapshot

heapdump: $(app_runner)
> $< $(run_args)
.PHONY: heapdump

$(app_runner): $(pom_xml)
> cd $(app_home)
> $(mvn) test -Ptestsuite -Dtest=StartStopTest#fullMicroProfileNative -Dquarkus.version=$(quarkus_version) -Dstart-stop.iterations=3

$(pom_xml): $(target_dir)
> cd $<
> git clone https://github.com/galderz/quarkus-startstop
> cd quarkus-startstop
> git checkout -b $(branch) origin/$(branch)

$(target_dir):
> mkdir -p $@

$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) -Dquickly -T 4C
> touch $@

test:
> ./test-endpoints.sh
.PHONY: test

clean:
> rm -drf $(target_dir)
.PHONY: clean

clean-all:
> rm -drf target
.PHONY: clean-all

clean-quarkus:
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean-quarkus

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

ifeq ($(QUARKUS),2.13)
  quarkus_version = 2.13.4.Final
  graalvm_home = /opt/graalvm-ce-java17-22.3.0
endif
ifeq ($(QUARKUS),2.7)
  quarkus_version = 2.7.6.Final
  graalvm_home = /opt/graalvm-ce-java17-21.3.3.1
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
mvn += PATH=$(PATH):/opt/boot-maven-3/bin
mvn += mvn
pom_xml = $(target_dir)/quarkus-startstop/app-full-microprofile/pom.xml
target_dir = target/$(QUARKUS)

time: $(app_runner)
> /usr/bin/time -v $< $(run_args)

massif: $(app_runner)
> valgrind --tool=massif $< $(run_args)
#> valgrind --tool=massif --pages-as-heap=yes $<

$(app_runner): $(pom_xml)
> cd $(app_home)
> $(mvn) test -Ptestsuite -Dtest=StartStopTest#fullMicroProfileNative -Dquarkus.version=$(quarkus_version) -Dstart-stop.iterations=3

$(pom_xml): $(target_dir)
> cd $<
> git clone https://github.com/galderz/quarkus-startstop
> cd quarkus-startstop
> git checkout -b topic.1122.minor-tweaks origin/topic.1122.minor-tweaks

$(target_dir):
> mkdir -p $@

test:
> ./test-endpoints.sh
.PHONY: test

clean:
> rm -drf $(target_dir)
.PHONY: clean

clean-all:
> rm -drf target
.PHONY: clean-all

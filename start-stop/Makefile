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

QUARKUS_VERSION ?=
GRAALVM_HOME ?=

app_home = $(target_dir)/quarkus-startstop
app_runner = $(app_home)/app-full-microprofile/target/quarkus-runner
mvn += JAVA_HOME=$(graalvm_home)
mvn += PATH=$(PATH):/opt/boot-maven-3/bin
mvn += mvn
pom_xml = $(target_dir)/quarkus-startstop/app-full-microprofile/pom.xml
target_dir = target/$(QUARKUS)

time: $(app_runner)
> /usr/bin/time -v $<

massif: $(app_runner)
> valgrind --tool=massif $<
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

clean:
> rm -drf $(target_dir)
.PHONY: clean

clean-all:
> rm -drf target
.PHONY: clean-all

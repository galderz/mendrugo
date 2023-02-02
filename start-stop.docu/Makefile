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

qdup_home = /opt/qDup
qdup_jar := $(qdup_home)/target/qDup-0.6.17-SNAPSHOT-uber.jar
qdup_pom := $(qdup_home)/pom.xml
qdup_sources += $(shell find $(qdup_home) -type f -name '*.java' | sed 's: :\\ :g')
qdup_sources += $(shell find $(qdup_home) -type f -name '*.xml' | sed 's: :\\ :g')
mvn += JAVA_HOME=/opt/java-17
mvn += /opt/maven/bin/mvn

$(qdup_jar): $(qdup_pom) $(qdup_sources)
> cd $(qdup_home)
> $(mvn) -DskipTests package
> touch $@

$(qdup_pom):
> cd /opt
> git clone https://github.com/Hyperfoil/qDup

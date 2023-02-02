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

java_home = /opt/java-17
java = $(java_home)/bin/java
mvn += JAVA_HOME=/opt/$(java_home)
mvn += /opt/maven/bin/mvn
qdup_args += -i
qdup_args += $(QDUP_ID)
qdup_args += -p
qdup_args += $(QDUP_PASS)
qdup_args += -S
qdup_args += _USER=$$(whoami)
qdup_args += -S
qdup_args += _HOST=$(QDUP_HOST)
qdup_home = /opt/qDup
qdup_jar := $(qdup_home)/target/qDup-*-uber.jar
qdup_pom := $(qdup_home)/pom.xml
qdup_sources += $(shell find $(qdup_home) -type f -name '*.java' | sed 's: :\\ :g')
qdup_sources += $(shell find $(qdup_home) -type f -name 'pom.xml' | sed 's: :\\ :g')
script_helloworld = helloworld.yaml

ifdef DEBUG
  jvm_args += -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
endif

helloworld: $(qdup_jar) $(script_helloworld)
> $(java) $(jvm_args) -jar $< $(qdup_args) $(script_helloworld)
.PHONY: helloworld

$(qdup_jar): $(qdup_pom) $(qdup_sources)
> cd $(qdup_home)
> $(mvn) -DskipTests package
> touch $@

$(qdup_pom):
> cd /opt
> git clone https://github.com/Hyperfoil/qDup

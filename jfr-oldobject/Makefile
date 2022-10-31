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

jar = target/jfr-oldobject.jar
mvn += JAVA_HOME=/opt/java-17
mvn += /opt/maven/bin/mvn
project_sources += $(shell find src/ -type f -name '*.java' | sed 's: :\\ :g')

run: $(jar)
> java \
> -XX:TLABSize=2k \
> -XX:StartFlightRecording=dumponexit=true\,filename=target/recording.jfr\,settings=./oldobject.jfc \
> -cp $< \
> jfr.oldobject.LargeObjects
.PHONY: run

$(jar): $(project_sources)
> $(mvn) package

clean:
> $(mvn) clean
.PHONY: clean

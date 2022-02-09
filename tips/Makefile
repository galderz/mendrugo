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

native_image += native-image
native_image += -H:+ReportExceptionStackTraces
native_image += --initialize-at-build-time=Converters
native_image += --no-fallback

$(APP).bin: $(APP).jar
> $(native_image) -jar $< $@
> ./$@

$(APP).jar:
> jbang export local $(APP).java
> java -jar $@

clean:
> rm -f *.bin*
> rm -f *.jar
.PHONY: clean

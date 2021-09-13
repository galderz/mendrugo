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

# GraalVM
bin = $(graalvm_home)/bin
#graalvm_home = $(MANDREL_HOME)/sdk/latest_graalvm_home
graalvm_home := /opt/graalvm-ce-java11-21.2.0

native_image += $(bin)/native-image
native_image += -H:+ReportExceptionStackTraces
native_image += --no-fallback

ifdef VERBOSE
  native_image += --verbose
endif

ifeq ($(DEBUG),driver)
  native_image += --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000
else ifdef DEBUG
  native_image += --debug-attach=*:8000
endif

# Project
classfiles := $(patsubst %.java, %.class, $(wildcard *.java))
jar := example.jar
program := a.out

$(program): $(jar) $(bin_native_image)
> $(native_image) -jar $(jar) $(program)
> ./$(program)
.PHONY: $(program)

# Even if environment is headless,
# default toolkit in JVM is set to sun.awt.X11.XToolkit,
# and that needs to be looked up by reflection,
# even if in the end it uses sun.awt.HeadlessToolkit.
$(jar): $(classfiles)
> $(bin)/jar cvfe $@ Example $(classfiles)
> $(bin)/java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $@
> rm -f img.png
> $(bin)/jar cvfe $@ Example $(classfiles) META-INF
> $(bin)/java -jar $@
> rm -f img.png

%.class: %.java
> $(bin)/javac -d . $<

clean:
> rm -f a.out
> rm -f *.jar
> rm -f *.class
> rm -f *.png
.PHONY: clean

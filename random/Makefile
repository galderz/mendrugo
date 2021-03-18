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

MANDREL_HOME=/opt/mandrel
MX_HOME=/opt/mx
MANDREL_BOOT_JAVA_HOME=/opt/mandrel-boot-java

classfiles = $(patsubst %.java, %.class, $(wildcard *.java))
jar = example.jar
program = a.out

native += native-image
native += -H:+ReportExceptionStackTraces
native += --initialize-at-build-time=Example
native += --no-fallback

run: $(program)
> ./$(program)
.PHONY: run

$(program): $(jar)
> $(native) -jar $(jar) $(program)

$(jar): $(classfiles)
> jar cvfe $@ Example $(classfiles)

%.class: %.java
> javac -d . $<

debug:
> $(native) -jar $(jar) --debug-attach=*:8000 $(program)
.PHONY: debug

debug-driver:
> $(invokenative) -jar $(jar) --vm.agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:8000 $(program)
.PHONY: debug

$(imageagent):
> native-image --macro:native-image-agent-library

jvm-run: $(jar) $(imageagent)
> java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -jar $(jar)
.PHONY: jvm-run

clean:
> rm -f a.out*
> rm -f *.jar
> rm -f *.class
.PHONY: clean

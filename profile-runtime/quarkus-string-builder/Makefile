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

fg_home = /opt/FlameGraph
mvn += JAVA_HOME=/opt/graalvm-ce-java17-22.3.0
mvn += /opt/maven/bin/mvn
runner = target/quarkus-string-builder-1.0.0-SNAPSHOT-runner
sources += $(shell find . -type f -name '*.java')
sources += pom.xml

$(runner): $(sources)
> $(mvn) package -DskipTests -Dnative \
> -Dquarkus.native.debug.enabled \
> -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:-OmitInlinedMethodDebugLineInfo

flamegraph:
> rm -f flamegraph.svg || true
> perf script -i perf.data | $(fg_home)/stackcollapse-perf.pl | $(fg_home)/flamegraph.pl > flamegraph.svg
.PHONY: flamegraph

record: $(runner)
> perf record -F 1009 --call-graph dwarf -a $<
.PHONY: record

stringbuilder:
> curl http://localhost:8080/string-builder
.PHONY: stringbuilder

clean:
> rm -drf target
.PHONY: clean

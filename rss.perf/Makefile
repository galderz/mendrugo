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

QUARKUS_VERSION=2.16.1
JAVA_HOME=/opt/graalvm-ce-java17-22.3.0

app := code-with-quarkus
mvn += JAVA_HOME=$(JAVA_HOME)
mvn += /opt/boot-maven-3/bin/mvn
pom = $(app)/pom.xml
runner = $(target)/code-with-quarkus-1.0.0-SNAPSHOT-runner
sources += $(shell find $(app) -type f -name '*.java' | sed 's: :\\ :g')
target = $(app)/target

$(runner): $(pom) $(sources)
> cd $(app)
> $(mvn) package -DskipTests -Dnative \
>   -Dquarkus.native.debug.enabled \
>   -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:-OmitInlinedMethodDebugLineInfo

$(pom):
> $(mvn) io.quarkus.platform:quarkus-maven-plugin:$(QUARKUS_VERSION).Final:create \
>   -DprojectGroupId=org.acme \
>   -DprojectArtifactId=code-with-quarkus

clean:
> rm -drf $(target)
.PHONY: clean

### perf specific targets below ###

mmap_stacks = $(stacks_dir)/mmap.stacks
mmap_svg = target/flamegraphs/mmap.svg
stacks_dir := target/flamegraphs/stacks
stackcollapse_perf = /opt/FlameGraph/stackcollapse-perf.pl
flamegraph = /opt/FlameGraph/flamegraph.pl

stack: $(runner)
> sudo perf record -e syscalls:sys_enter_mmap --call-graph dwarf -a -- $<
.PHONY: stack

flamegraph:
> rm -drf $(stacks_dir)
> mkdir -p $(stacks_dir)
> sudo chown $(USER) perf.data
> perf script > $(mmap_stacks)
> $(stackcollapse_perf) < $(mmap_stacks) | $(flamegraph) \
>   --color=mem \
>   --title="mmap Flame Graph" \
>   --countname="calls" > $(mmap_svg)
.PHONY: flamegraph

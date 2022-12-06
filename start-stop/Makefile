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
APP ?= full-microprofile

quarkus_2_7 = 2.7.6.Final
quarkus_2_13 = 2.13.4.Final
graalvm_21_3 = graalvm-ce-java17-21.3.3.1
graalvm_22_3 = graalvm-ce-java17-22.3.0
quarkus_999 = 999-SNAPSHOT

ifeq ($(APP),full-microprofile)
  test_name = fullMicroProfileNative
else ifeq ($(APP),jax-rs-minimal)
  test_name = jaxRsMinimalNative
endif

ifeq ($(QUARKUS),2.13)
  branch = topic.1122.minor-tweaks
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-debug)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-min)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-hd)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-netty-stats)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.13-n4174)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_maven_args += -Dnetty.version=4.1.74.Final
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.7)
  branch = topic.1122.minor-tweaks
  graalvm_home = /opt/$(graalvm_21_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.7-debug)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_21_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.13-21.3)
  branch = topic.1122.minor-tweaks.2.13-21.3
  graalvm_home = /opt/$(graalvm_21_3)
  maven_opts = -javaagent:/opt/byteman/byteman/target/byteman-4.0.18.jar=script:/opt/mendrugo/start-stop/target/2.13-21.3/quarkus-startstop/app-full-microprofile/src/main/resources/native-image-build-step.btm\,prop:org.jboss.byteman.verbose=true
  quarkus_version = $(quarkus_2_13)
else ifeq ($(QUARKUS),2.7-22.3)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.7-22.3-hd)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.7-22.3-debug)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.7-22.3-min)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),2.7-22.3-netty-stats)
  branch = topic.1122.minor-tweaks.$(QUARKUS)
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_2_7)
else ifeq ($(QUARKUS),999)
  branch = topic.1122.minor-tweaks.999
  graalvm_home = /opt/$(graalvm_22_3)
  quarkus_version = $(quarkus_999)
else ifeq ($(QUARKUS),999-21.3)
  branch = topic.1122.minor-tweaks.999
  graalvm_home = /opt/$(graalvm_21_3)
  quarkus_version = $(quarkus_999)
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

ifdef TRACE
  run_args += -Dquarkus.log.level=TRACE
  run_args += -Dquarkus.log.min-level=TRACE
endif

app_home = $(target_dir)/quarkus-startstop
app_runner = $(app_home)/app-$(APP)/target/quarkus-runner
app_jar = $(app_home)/app-$(APP)/target/quarkus-native-image-source-jar/quarkus-runner.jar

mvn += JAVA_HOME=$(graalvm_home)
ifdef maven_opts
  mvn += MAVEN_OPTS=$(maven_opts)
endif
mvn += PATH=$(PATH):/opt/boot-maven-3/bin
mvn += mvn

flamegraph = /opt/FlameGraph/flamegraph.pl
pom_xml = $(target_dir)/quarkus-startstop/app-$(APP)/pom.xml
quarkus_home := /opt/quarkus-quarkus
quarkus_jar_core = $(quarkus_home)/core/deployment/target/quarkus-core-$(quarkus_version).jar
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name '*.java' | sed 's: :\\ :g')
quarkus_sources += $(shell find $(quarkus_home)/ -type f -name 'pom.xml' | sed 's: :\\ :g')
stackcollapse = /opt/FlameGraph/stackcollapse.pl
stackcollapse_perf = /opt/FlameGraph/stackcollapse-perf.pl
target_dir = target/$(QUARKUS)

time: $(app_runner)
> /usr/bin/time -v $< $(run_args)
.PHONY: time

massif: $(app_runner)
> valgrind --tool=massif $< $(run_args)
#> valgrind --tool=massif --pages-as-heap=yes $<
.PHONY: massif

# Call `starti` to put a breakpoint in the very first instruction
debug:
> gdb $(app_runner)
.PHONY: debug

malloc:
> rm -f $(target_dir)/malloc.stacks
> sudo /usr/share/bcc/tools/stackcount -P -p $(shell pidof quarkus-runner) -U c:malloc > $(target_dir)/malloc.stacks
.PHONY: malloc

calloc:
> rm -f $(target_dir)/calloc.stacks
> sudo /usr/share/bcc/tools/stackcount -P -p $(shell pidof quarkus-runner) -U c:calloc > $(target_dir)/calloc.stacks
.PHONY: calloc

realloc:
> rm -f $(target_dir)/realloc.stacks
> sudo /usr/share/bcc/tools/stackcount -P -p $(shell pidof quarkus-runner) -U c:realloc > $(target_dir)/realloc.stacks
.PHONY: realloc

# malloc-bytes:
# > rm -f $(target_dir)/malloc-bytes.stacks
# > sudo /opt/bcc/examples/tracing/mallocstacks.py $(shell pidof quarkus-runner) > $(target_dir)/malloc-bytes.stacks
# .PHONY: malloc-bytes

brendan-malloc-bytes:
> rm -f $(target_dir)/malloc-bytes.stacks
> sudo /opt/BPF-tools/old/2017-12-23/mallocstacks.py -p $(shell pidof quarkus-runner) -f > $(target_dir)/brendan-malloc-bytes.stacks
.PHONY: brendan-malloc-bytes

mmap:
> sudo perf record -e syscalls:sys_enter_mmap -a -g -o $(target_dir)/perf-mmap.data -- $(app_runner)
.PHONY: mmap

pagefault:
> sudo perf record -e page-faults -a -g -o $(target_dir)/perf-pagefault.data -- $(app_runner)
.PHONY: mmap

flame-malloc:
> $(stackcollapse) < $(target_dir)/malloc.stacks | $(flamegraph) --color=mem --title="malloc() Flame Graph" --countname="calls" > $(target_dir)/malloc.svg
.PHONY: flame-malloc

flame-calloc:
> $(stackcollapse) < $(target_dir)/calloc.stacks | $(flamegraph) --color=mem --title="calloc() Flame Graph" --countname="calls" > $(target_dir)/calloc.svg
.PHONY: flame-calloc

flame-realloc:
> $(stackcollapse) < $(target_dir)/realloc.stacks | $(flamegraph) --color=mem --title="realloc() Flame Graph" --countname="calls" > $(target_dir)/realloc.svg
.PHONY: flame-realloc

# flame-malloc-bytes:
# > $(stackcollapse) < $(target_dir)/malloc-bytes.stacks | $(flamegraph) --color=mem --title="malloc() bytes Flame Graph" --countname="bytes" > $(target_dir)/malloc-bytes.svg
# .PHONY: flame-malloc-bytes

flame-brendan-malloc-bytes:
> cat $(target_dir)/brendan-malloc-bytes.stacks | $(flamegraph) --color=mem --title="malloc() bytes Flame Graph" --countname="bytes" > $(target_dir)/brendan-malloc-bytes.svg
.PHONY: flame-brendan-malloc-bytes

flame-mmap:
> sudo perf script -i $(target_dir)/perf-mmap.data > $(target_dir)/mmap.stacks
> $(stackcollapse_perf) < $(target_dir)/mmap.stacks | $(flamegraph) --color=mem --title="mmap() Flame Graph" --countname="calls" > $(target_dir)/mmap.svg
.PHONY: flame-mmap

flame-pagefault:
> sudo perf script -i $(target_dir)/perf-pagefault.data > $(target_dir)/pagefault.stacks
> $(stackcollapse_perf) < $(target_dir)/pagefault.stacks | $(flamegraph) --color=mem --title="Page Fault Flame Graph" --countname="pages" > $(target_dir)/pages.svg
.PHONY: flame-pagefault

memleak:
> sudo /usr/share/bcc/tools/memleak -c $(app_runner) -O /lib64/libc.so.6
.PHONY: memleak

snapshot: $(quarkus_jar_core) $(app_runner)
> /usr/bin/time -v $(app_runner) $(run_args)
.PHONY: snapshot

heapdump: $(app_runner)
> $< $(run_args)
.PHONY: heapdump

jvm: $(app_runner)
> /usr/bin/time -v java $(run_args) -jar $(app_jar)
.PHONY: jvm

$(app_runner): $(pom_xml)
> cd $(app_home)
> $(mvn) test -Ptestsuite -Dtest=StartStopTest#$(test_name) -Dquarkus.version=$(quarkus_version) -Dstart-stop.iterations=3

$(pom_xml): $(target_dir)
> cd $<
> git clone https://github.com/galderz/quarkus-startstop
> cd quarkus-startstop
> git checkout -b $(branch) origin/$(branch)

$(target_dir):
> mkdir -p $@

$(quarkus_jar_core): $(quarkus_sources)
> cd $(quarkus_home)
> $(mvn) -Dquickly -T 4C $(quarkus_maven_args)
> touch $@

test:
> ./test-endpoints.sh
.PHONY: test

clean:
> rm -drf $(target_dir)
.PHONY: clean

clean-all:
> rm -drf target
.PHONY: clean-all

clean-quarkus:
> cd $(quarkus_home)
> $(mvn) clean
.PHONY: clean-quarkus

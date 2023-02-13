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
jar = $(target)/quarkus-app/quarkus-run.jar
java = $(JAVA_HOME)/bin/java
jvm_args += -Xmx128m
mvn += JAVA_HOME=$(JAVA_HOME)
mvn += /opt/boot-maven-3/bin/mvn
pom = $(app)/pom.xml
runner = $(target)/code-with-quarkus-1.0.0-SNAPSHOT-runner
sources += $(shell find $(app) -type f -name '*.java' | sed 's: :\\ :g')
target = $(app)/target

ifdef JVM_DEBUG
  jvm_args += -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005
endif

run: $(runner)
> $< -Xmx128m
.PHONY: run

gdb: $(runner)
> gdb --args $< -Xmx128m
.PHONY: gdb

$(runner): $(pom) $(sources)
> cd $(app)
> $(mvn) package -DskipTests -Dnative \
>   -Dquarkus.native.additional-build-args=-H:-DeleteLocalSymbols,-H:+PreserveFramePointer

jvm: $(jar)
> $(java) $(jvm_args) -jar $<
.PHONY: jvm

$(jar): $(pom) $(sources)
> cd $(app)
> $(mvn) package -DskipTests

$(pom):
> $(mvn) io.quarkus.platform:quarkus-maven-plugin:$(QUARKUS_VERSION).Final:create \
>   -DprojectGroupId=org.acme \
>   -DprojectArtifactId=code-with-quarkus

clean:
> rm -drf $(target)
.PHONY: clean

### eBPF specific targets below ###

stack: $(runner)
> gdb --batch --command=gdb.$(STACK_NAME).script --args $< -Xmx128m
.PHONY: stack

mmap-munmap:
> sudo /usr/share/bcc/tools/stackcount -P -p $$(pidof $(app)-1.0.0-SNAPSHOT-runner) -U "t:syscalls:sys_enter_m*"
.PHONY: mmap-munmap

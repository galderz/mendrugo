APP_DIR := .

GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 0
GRAALVM_BUILD ?= 35.1

BOOT_JDK := $(opt)/java-21
JDK_REPO := jdk21u
GRAALVM_VERSION ?= 23.1

JAVA_HOME = $(HOME)/.mx/jdks/labsjdk-ce-latest-22+26-jvmci-b01/Contents/Home
#JAVA_HOME = $(HOME)/.mx/jdks/labsjdk-ce-21-jvmci-23.1-b22/Contents/Home

TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk

GRAALVM_RELEASE_HOME = $(opt)/graalvm-community-openjdk-$(GRAALVM_MAJOR)+$(GRAALVM_BUILD)/Contents/Home

runner = target/factory-methods

run: $(runner)
> $<
.PHONY: run

$(runner): $(shell find . -type f -name '*.java')
$(runner): $(shell find . -type f -name 'pom.xml')
$(runner): $(native_image)
> JAVA_HOME=$(GRAALVM_HOME) $(mvn) clean install

build-with-agent:
> JAVA_HOME=$(GRAALVM_HOME) $(mvn) -Pnative -Dagent=true -DskipTests package exec:exec@native
.PHONY: build-with-agent

run-with-agent:
> JAVA_HOME=$(GRAALVM_HOME) $(mvn) -Dagent=true -DskipTests -DskipNativeBuild=true clean package exec:exec@java-agent
.PHONY: run-with-agent

native-image-agent: $(native_image)
> $< -g --macro:native-image-agent-library
.PHONY: native-image-agent

release-run:
> JAVA_HOME=$(GRAALVM_RELEASE_HOME) $(mvn) clean install

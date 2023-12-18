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

ifdef NATIVE_BUILD_ARGS
  native_build_args += -Dquarkus.native.additional-build-args=-H:PrintAnalysisCallTreeType=TXT,$(NATIVE_BUILD_ARGS)
else
  native_build_args += -Dquarkus.native.additional-build-args=-H:PrintAnalysisCallTreeType=TXT
endif

default: test

include $(TOPDIR)/make/common/MakeBase.gmk
#include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk
include $(TOPDIR)/make/common/Colima.gmk

GRAALVM_RELEASE_HOME = $(opt)/graalvm-community-openjdk-$(GRAALVM_MAJOR)+$(GRAALVM_BUILD)/Contents/Home

test: $(native_image) $(quarkus_jackson_jar)
> cd $(QUARKUS_HOME)
> $(mvnw_cmd) -f integration-tests -pl jpa-postgresql -Dnative -Dtest-containers -Dstart-containers -Dnative.surefire.skip -Dnet.bytebuddy.experimental $(native_build_args) clean install
.PHONY: test

test-release:
> cd $(QUARKUS_HOME)
> JAVA_HOME=$(GRAALVM_RELEASE_HOME) ./mvnw -f integration-tests -pl jpa-postgresql -Dnative -Dtest-containers -Dstart-containers -Dnative.surefire.skip -Dnet.bytebuddy.experimental $(native_build_args) clean install
.PHONY: test-release

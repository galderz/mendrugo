TOPDIR = ..
include $(TOPDIR)/make/common/MakeBase.gmk

default: test

APP_DIR ?= project
JAVA_HOME = $(opt)/graal-21
include $(TOPDIR)/make/common/Maven.gmk

QUARKUS_HOME = $(HOME)/1/quarkus-quarkus-dev-services-agent
GRAALVM_HOME = $(JAVA_HOME)
include $(TOPDIR)/make/common/QuarkusDev.gmk

test_args += -DskipITs=false
test_args += -Dquarkus.test.integration-test-profile=test-with-native-agent
test_args += -Dmaven.failsafe.debug

test: $(quarkus_junit5_jar)
> cd $(APP_DIR)
> $(mvnw_cmd) verify $(test_args)
.PHONY: test

test-quarkus-maven: $(quarkus_junit5_jar)
> cd $(QUARKUS_HOME)
> $(mvnw_cmd) clean verify -f integration-tests/maven -Dit.test=NativeAgentIT -Dnative
.PHONY: test-quarkus-maven

native_args += -Dnative
native_args += -Dquarkus.native.container-build=true
native_args += -Dquarkus.container-image.build=true
native_args += -Dmaven.failsafe.debug
# To debug unit tests
# native_args += -Dmaven.surefire.debug
# To get extra logging
# native_args += -Dquarkus.log.level=TRACE
# native_args += -Dquarkus.log.min-level=TRACE

native:
> cd $(APP_DIR)
> $(mvnw_cmd) verify $(native_args)
.PHONY: native

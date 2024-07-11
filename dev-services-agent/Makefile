TOPDIR = ..
include $(TOPDIR)/make/common/MakeBase.gmk

default: test

APP_DIR ?= project
JAVA_HOME := $(opt)/java-21
include $(TOPDIR)/make/common/Maven.gmk

include $(TOPDIR)/make/common/QuarkusDev.gmk

test:
> cd $(APP_DIR)
> $(mvnw_cmd) verify -DskipITs=false
#> $(mvnw_cmd) verify -DskipITs=false -Dquarkus.test.integration-test-profile=test-with-native-agent
.PHONY: test

native_args += -Dnative
native_args += -Dquarkus.native.container-build=true
native_args += -Dquarkus.container-image.build=true

native:
> cd $(APP_DIR)
> $(mvnw_cmd) verify $(native_args)
.PHONY: native

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
.PHONY: test

APP ?= getting-started
JAVA_HOME ?= /opt/graalvm-ce-java17-22.3.0
QUICKSTARTS_BRANCH ?= development
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Podman.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusQuick.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

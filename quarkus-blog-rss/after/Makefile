APP ?= getting-started
QUARKUS_HOME ?= /opt/quarkus-rss-after
QUARKUS_VERSION ?= 999-RSS-AFTER
QUICKSTARTS_BRANCH ?= 2.13
TOPDIR = ../..

java_home := /opt/graalvm-ce-java17-22.3.0

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Podman.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusQuick.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

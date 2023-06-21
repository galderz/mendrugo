APP ?= getting-started
JAVA_HOME ?= /opt/graalvm-ce-java17-21.3.3.1
# JAVA_HOME ?= /opt/graalvm-ce-java17-22.3.0
QUARKUS_HOME ?= /opt/quarkus-rss-2.7.6
# QUARKUS_HOME ?= /opt/quarkus-rss-before
QUARKUS_VERSION ?= 2.7.6.Final
# QUARKUS_VERSION ?= 999-RSS-BEFORE
QUICKSTARTS_BRANCH ?= 2.7
# QUICKSTARTS_BRANCH ?= 2.13
TOPDIR = ../..


default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Podman.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusQuick.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

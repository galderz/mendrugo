APP ?= getting-started
JAVA_MAJOR ?= 17
GRAALVM_MAJOR ?= 22
GRAALVM_MINOR ?= 3
GRAALVM_MICRO ?= 0
QUARKUS_VERSION ?= 2.13.4.Final
QUICKSTARTS_BRANCH ?= 2.13
TOPDIR = ../..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/Quarkus.gmk

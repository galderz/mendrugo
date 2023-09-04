APP ?= getting-started
JAVA_MAJOR ?= 17
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 3
GRAALVM_MICRO ?= 3.1
QUARKUS_VERSION ?= 2.7.6.Final
QUICKSTARTS_BRANCH ?= 2.7
TOPDIR = ../..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk

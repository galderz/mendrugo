GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 7
GRAALVM_BUILD ?= 7.1

APP ?= rest-json-quickstart
QUARKUS_VERSION ?= 3.2.2.Final
QUICKSTARTS_BRANCH ?= main
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/Quarkus.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk

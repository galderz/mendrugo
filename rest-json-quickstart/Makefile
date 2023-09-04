GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 7
GRAALVM_BUILD ?= 7.1

APP ?= quickstart
QUICKSTART_BRANCH ?= 3.2.3.Final
QUICKSTART_NAME ?= rest-json-quickstart
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk
include $(TOPDIR)/make/common/AsyncProfiler.gmk

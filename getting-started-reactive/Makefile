GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 0
GRAALVM_BUILD ?= 35.1

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
QUICKSTART_BRANCH ?= 3.5.0
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk
include $(TOPDIR)/make/common/AsyncProfiler.gmk
include $(TOPDIR)/make/common/Numa.gmk
include $(TOPDIR)/make/common/Perf.gmk
include $(TOPDIR)/make/common/Pidstat.gmk

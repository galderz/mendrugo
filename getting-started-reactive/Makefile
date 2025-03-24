GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 2
GRAALVM_BUILD ?= 13.1

#GRAALVM_FLAVOUR ?= vm
#GRAALVM_MAJOR ?= 22
##GRAALVM_MINOR ?= 3
#GRAALVM_MINOR ?= 2
#GRAALVM_MICRO ?= 0
#GRAALVM_JAVA_MAJOR ?= 17

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
#QB ?= a.b.c
QB ?= development
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk

JAVA_HOME=$(GRAALVM_HOME)

include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk
include $(TOPDIR)/make/common/AsyncProfiler.gmk
include $(TOPDIR)/make/common/Numa.gmk
include $(TOPDIR)/make/common/Taskset.gmk
include $(TOPDIR)/make/common/Perf.gmk
include $(TOPDIR)/make/common/Pidstat.gmk
include $(TOPDIR)/make/common/Export.gmk
include $(TOPDIR)/make/common/Wrk.gmk
include $(TOPDIR)/make/common/JOL.gmk

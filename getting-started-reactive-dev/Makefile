TOPDIR = ..

include $(TOPDIR)/make/common/MakeBase.gmk

BOOT_GDK_HOME ?= $(HOME)/opt/labs-latest
GDK_HOME ?= $(HOME)/1/mandrel-mandrel

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
QUICKSTART_BRANCH ?= development

default: run

include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk

GRAALVM_HOME=$(JAVA_HOME)

include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk
include $(TOPDIR)/make/common/Numa.gmk
include $(TOPDIR)/make/common/Pidstat.gmk
include $(TOPDIR)/make/common/Wrk.gmk
include $(TOPDIR)/make/common/JOL.gmk

TOPDIR = ..

include $(TOPDIR)/make/common/MakeBase.gmk

BOOT_JDK := $(opt)/java-21
JDK_REPO := jdk21u
GRAALVM_VERSION ?= 24.0

JAVA_HOME = $(HOME)/.mx/jdks/labsjdk-ce-latest-22+26-jvmci-b01/Contents/Home

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
#QUICKSTART_BRANCH ?= development
QUICKSTART_BRANCH ?= 3.6.5

default: run

include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/Numa.gmk
include $(TOPDIR)/make/common/Pidstat.gmk
include $(TOPDIR)/make/common/Wrk.gmk
include $(TOPDIR)/make/common/JOL.gmk

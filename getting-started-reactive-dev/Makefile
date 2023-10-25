TOPDIR = ..

include $(TOPDIR)/make/common/MakeBase.gmk

BOOT_JDK := $(opt)/java-21
JDK_REPO := jdk21u
GRAALVM_VERSION ?= 23.1

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
QUICKSTART_BRANCH ?= development

default: run

include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk

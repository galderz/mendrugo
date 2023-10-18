GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 0
GRAALVM_BUILD ?= 35.1

APP_DIR ?= .
APP_NAME ?= metadata
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk

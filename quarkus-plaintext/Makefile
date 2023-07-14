JAVA_HOME ?= $(HOME)/opt/graalvm-community-openjdk-17.0.7+7.1
QUARKUS_VERSION ?= 3.2.0.Final
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk

extensions := resteasy-reactive

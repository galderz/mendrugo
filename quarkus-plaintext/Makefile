JAVA_HOME ?= /opt/graalvm-ce-java17-22.3.0
QUARKUS_VERSION ?= 3.0.1.Final
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk

extensions := resteasy-reactive

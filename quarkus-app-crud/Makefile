QUARKUS_VERSION ?= 2.16.4.Final
TOPDIR = ..

extensions := resteasy-reactive,hibernate-orm-panache,jdbc-postgresql,smallrye-openapi
java_home := /opt/graalvm-ce-java17-22.3.0

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk

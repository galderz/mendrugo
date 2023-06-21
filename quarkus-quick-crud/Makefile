APP ?= hibernate-orm-quickstart
JAVA_HOME ?= $(HOME)/opt/graalvm-community-openjdk-17.0.7+7.1
# Locally built GraalVM, with local built JDK fails with https://github.com/quarkusio/quarkus/issues/34191
# JAVA_HOME ?= $(HOME)/opt/graalvm-17
QUARKUS_VERSION ?= 3.1.2.Final
QUICKSTARTS_BRANCH ?= main
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Colima.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusQuick.gmk

db:
> docker run --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:13.3
.PHONY: db

upgrade:
> cd $(APP)
> $(mvn) io.quarkus.platform:quarkus-maven-plugin:3.0.1.Final:update -N -Dstream=3.0
.PHONY: upgrade

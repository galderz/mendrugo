APP ?= hibernate-orm-quickstart
JAVA_HOME ?= /opt/graalvm-ce-java17-22.3.0
QUARKUS_VERSION ?= 2.16.4.Final
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

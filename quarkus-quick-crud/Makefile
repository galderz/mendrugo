app := hibernate-orm-quickstart
quickstarts_branch := main

default: run

include ../make/common/MakeBase.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusQuick.gmk

java_home := /opt/graalvm-ce-java17-22.3.0
quarkus_version := 2.16.4.Final

colima:
> colima start
.PHONY: colima

stop:
> colima stop
.PHONY: stop

db:
> docker run --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 5432:5432 postgres:13.3
.PHONY: db

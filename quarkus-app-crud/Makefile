QUARKUS_VERSION ?= 2.16.4.Final

extensions := resteasy-reactive,hibernate-orm-panache,jdbc-postgresql,smallrye-openapi
java_home := /opt/graalvm-ce-java17-22.3.0

default: run

include ../make/common/MakeBase.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusApp.gmk

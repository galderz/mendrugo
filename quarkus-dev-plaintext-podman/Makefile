app := getting-started
java_home := /opt/java-17
quarkus_version := 999-SNAPSHOT
quickstarts_branch := development

default: run

include ../make/common/MakeBase.gmk
include ../make/common/Podman.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusQuick.gmk
include ../make/common/QuarkusDev.gmk

mvn_package += -Dquarkus.native.container-build=true
mvn_package += -Dquarkus.native.container-runtime=podman
#mvn_package += -Dquarkus.native.container-runtime=podman_rootless

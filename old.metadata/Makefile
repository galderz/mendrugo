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
include $(TOPDIR)/make/common/QuarkusDev.gmk

verify: $(quarkus_core_jar) $(pom) $(native_image)
> $(mvn_cmd) verify -Dnative-with-agent
.PHONY: verify

verify-with-agent-trace: $(pom) $(native_image)
> $(mvn_cmd) verify -Dnative-with-agent-trace
.PHONY: verify-with-agent-trace

new_project := new-project
new_project_pom := $(new_project)/pom.xml

new-project: $(new_project_pom)
.PHONY: new-project

$(new_project_pom): $(quarkus_core_jar)
> $(mvn_cmd) io.quarkus:quarkus-maven-plugin:999-SNAPSHOT:create \
>   -DprojectGroupId=org.acme \
>   -DprojectArtifactId=new-project

clean-new-project:
> rm -drf $(new_project)
.PHONY: clean-new-project

GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 0
GRAALVM_BUILD ?= 35.1

greeting_resource_java = $(project)/src/main/java/org/acme/GreetingResource.java
native_build_args =
project = new-project
project_pom = $(project)/pom.xml
runner = $(project)/target/$(project)-1.0.0-SNAPSHOT-native-image-source-jar/$(project)-1.0.0-SNAPSHOT-runner

ifdef NATIVE_BUILD_ARGS
  native_build_args += -Dquarkus.native.additional-build-args=$(NATIVE_BUILD_ARGS)
endif

APP_DIR ?= $(project)
APP_NAME ?= $(project)
TOPDIR = ..

default: $(runner)

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

$(runner): $(greeting_resource_java)
> cd $(APP_DIR)
> $(mvnw_cmd) verify -Dnative-with-agent $(native_build_args)

no-agent: $(greeting_resource_java)
> cd $(APP_DIR)
> $(mvnw_cmd) verify -Dnative $(native_build_args)
.PHONY: no-agent

all-config: reflect-config resource-config
> tar xfO ./new-project/target/quarkus-app/quarkus/generated-bytecode.jar META-INF/native-image/jni-config.json | jq
> tar xfO ./new-project/target/quarkus-app/quarkus/generated-bytecode.jar META-INF/native-image/proxy-config.json | jq
> tar xfO ./new-project/target/quarkus-app/quarkus/generated-bytecode.jar META-INF/native-image/serialization-config.json | jq
.PHONY: all-config

resource-config:
> tar xfO ./new-project/target/quarkus-app/quarkus/generated-bytecode.jar META-INF/native-image/resource-config.json | jq -s '.[].resources.includes |= sort_by(.pattern)' > $(project)/target/resource-config.json
.PHONY: resource-config

reflect-config:
> tar xfO ./new-project/target/quarkus-app/quarkus/generated-bytecode.jar META-INF/native-image/reflect-config.json | jq -s '.[] |= sort_by(.name)' > $(project)/target/reflect-config.json
.PHONY: reflect-config

$(greeting_resource_java): $(project_pom)
> cp -r src-main/* $(project)/src/main/java
> mkdir -p $(project)/src/test/java
> cp -r src-test/* $(project)/src/test/java

$(project_pom): $(quarkus_core_jar)
> rm -drf $(project)
> $(mvn_cmd) io.quarkus:quarkus-maven-plugin:999-SNAPSHOT:create \
>   -DprojectGroupId=org.acme \
>   -DprojectArtifactId=$(project) \
>   -Dextensions='resteasy-reactive' \
>   -DplatformVersion=999-SNAPSHOT \
>   -DnoCode
> awk '/<dependencyManagement>/{f=1} /<\/dependencyManagement>/{f=0} !f && /<\/dependencies>/{print "    <dependency>"; print "      <groupId>io.rest-assured</groupId>"; print "      <artifactId>rest-assured</artifactId>"; print "      <scope>test</scope>"; print "    </dependency>";} 1' $@ > pom_new.xml && mv pom_new.xml $@

clean-project:
> rm -drf $(project)
.PHONY: clean-project

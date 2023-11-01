GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 21
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 0
GRAALVM_BUILD ?= 35.1

greeting_resource_java = $(project)/src/main/java/org/acme/GreetingResource.java
project = new-project
project_pom = $(project)/pom.xml
reflect_config_json = $(project)/target/generated-native-config/reflect-config.json

APP_DIR ?= $(project)
APP_NAME ?= $(project)
TOPDIR = ..

default: $(reflect_config_json)

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

$(reflect_config_json): $(greeting_resource_java)
> cd $(APP_DIR)
> $(mvnw_cmd) verify -Dnative-with-agent

$(greeting_resource_java): $(project_pom)
> cp -r src-main/* $(project)/src/main/java
> mkdir -p $(project)/src/test/java
> cp -r src-test/* $(project)/src/test/java

$(project_pom): $(quarkus_core_jar)
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

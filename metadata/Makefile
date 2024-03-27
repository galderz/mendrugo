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
RUN_ARGS ?=
TOPDIR = ..

default: $(runner)

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

ifdef MAVEN_VERBOSE
  mvnw_cmd += -Dquarkus.log.min-level=TRACE
  mvnw_cmd += -Dquarkus.log.category.\"org.jboss.resteasy.reactive.client.logging\".level=TRACE
  mvnw_cmd += -Dquarkus.log.category.\"org.apache.http\".level=TRACE
  mvnw_cmd += -Dquarkus.log.category.\"io.restassured\".level=TRACE
endif

$(runner): $(greeting_resource_java)
> cd $(APP_DIR)
> $(mvnw_cmd) verify -DskipITs=false -Dquarkus.test.integration-test-profile=test-with-native-agent
#> $(mvnw_cmd) verify -Dnative -Dquarkus.native.agent-configuration-apply $(native_build_args)

trace-output: $(greeting_resource_java)
> cd $(APP_DIR)
> JAVA_HOME=$(opt)/java-21 ./mvnw -X verify -DskipITs=false -Dquarkus.test.integration-test-profile=test-with-native-agent \
>  -Dquarkus.test.native.agent.output.property.name=trace-output \
>  -Dquarkus.test.native.agent.output.property.value=native-image-agent-trace-file.json
.PHONY: trace-output

origins: $(greeting_resource_java)
> cd $(APP_DIR)
> JAVA_HOME=$(opt)/java-21 ./mvnw -X verify -DskipITs=false -Dquarkus.test.integration-test-profile=test-with-native-agent \
>  -Dquarkus.test.native.agent.additional.args=experimental-configuration-with-origins
.PHONY: origins

run: $(greeting_resource_java)
> cd $(APP_DIR)
> $(mvnw_cmd) package quarkus:run -Drun-with-agent $(RUN_ARGS)
.PHONY: run

help:
> cd $(APP_DIR)
> $(mvnw_cmd) quarkus:help -Ddetail -Dgoal=test
> $(mvnw_cmd) quarkus:help -Ddetail -Dgoal=run
> $(mvnw_cmd) help:describe -Dcmd=test
.PHONY: help

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

$(project_pom): $(quarkus_core_jar) $(quarkus_devtools_jar) $(quarkus_tools_jar) $(quarkus_junit5_jar)
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

test:
> cd $(QUARKUS_HOME)
> $(mvnw_cmd) clean verify -f integration-tests/maven -Dit.test=CreateProjectMojoIT
> $(mvnw_cmd) clean verify -f independent-projects/tools/devtools-testing
.PHONY: test

test-quarkus-maven:
> cd $(QUARKUS_HOME)
> $(mvnw_cmd) clean verify -f integration-tests/maven -Dit.test=NativeAgentIT -Dnative
.PHONY: test-quarkus-maven

# Manually add -Dorg.gradle.debug=true in the gradle invocation
# to debug Quarkus build process with IDE,
# then connect with IDE on port 5005.
test-quarkus-gradle:
> cd $(QUARKUS_HOME)
> GRADLE_JAVA_HOME=$(HOME)/opt/graalvm-community-openjdk-17.0.9+9.1/Contents/Home $(mvnw_cmd) verify -f integration-tests/gradle -Dtest=NativeAgentIntegrationTestIT#runGeneratesNativeImageConfig
.PHONY: test-quarkus-gradle

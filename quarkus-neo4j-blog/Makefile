SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

ifeq ($(origin .RECIPEPREFIX), undefined)
  $(error This Make does not support .RECIPEPREFIX. Please use GNU Make 4.0 or later)
endif
.RECIPEPREFIX = >

NEO_HOME ?= /opt/neo4j-home
NEO_USER ?= neo4j
NEO_PASS ?= test

GRAALVM_HOME ?= /opt/graalvm-ce-java11-21.3.0-dev

app := hibernate-orm-quickstart
native_dir = $(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-native-image-source-jar
mvn += JAVA_HOME=$(vm)
mvn += ./mvnw
mvnw = $(app)/mvnw
reports = $(native_dir)/reports
runner = $(app)/$(target_dir)/$(app)-1.0.0-SNAPSHOT-runner
target_dir := target
vm := $(GRAALVM_HOME)

import: $(runner)
> docker cp $(reports) testneo4j:/var/lib/neo4j/import
> docker cp import.cypher testneo4j:/var/lib/neo4j
> docker exec testneo4j /var/lib/neo4j/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "MATCH(n) DETACH DELETE n" || true
> docker exec testneo4j /var/lib/neo4j/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "DROP CONSTRAINT unique_vm_id" || true
> docker exec testneo4j /var/lib/neo4j/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "DROP CONSTRAINT unique_method_id" || true
> docker exec testneo4j /bin/bash -c "time cat import.cypher | /var/lib/neo4j/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)"
.PHONY:import

$(runner): $(mvnw)
> cd $(app)
> $(mvn) package -DskipTests -Pnative -Dquarkus.native.enable-reports

$(mvnw):
> pushd /tmp
> git clone https://github.com/quarkusio/quarkus-quickstarts || true
> popd
> cp -r /tmp/quarkus-quickstarts/hibernate-orm-quickstart .

#import:
#> cp reports/*.csv ${NEO_HOME}/import
#> time cat $(shell find reports -name '*.cypher') | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)

run: $(NEO_HOME)
> docker run \
> --name testneo4j \
> -p7474:7474 -p7687:7687 \
> -d \
> --env NEO4J_AUTH=$(NEO_USER)/$(NEO_PASS) \
> neo4j:latest
.PHONY: run

$(NEO_HOME):
> mkdir -p $(NEO_HOME)/import

stop:
> docker stop testneo4j
> docker rm testneo4j
.PHONY: stop

browser:
> open http://$(IP):7474/browser
.PHONY: browser

clean:
> cd $(app)
> $(mvn) clean
.PHONY: clean

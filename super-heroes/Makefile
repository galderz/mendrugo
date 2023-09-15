GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 8
GRAALVM_BUILD ?= 7.1

APP_DIR ?= quarkus-super-heroes
TOPDIR = ..

default: run-villains-dev

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Colima.gmk

QUARKUS_VERSION ?= 3.3.2

villains_dir = $(super_heroes_dir)/rest-villains
villains_pom = $(villains_dir)/pom.xml
super_heroes_dir := $(APP_DIR)/super-heroes
super_heroes_mvnw := $(super_heroes_dir)/mvnw
zip := quarkus-super-heroes-workshop.zip

villains:
> cp villains/Villain.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainService.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainResource.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainResourceTest.java $(villains_dir)/src/test/java/io/quarkus/workshop/superheroes/villain
> cp villains/application.properties $(villains_dir)/src/main/resources
> cp villains/import.sql $(villains_dir)/src/main/resources
.PHONY: villains

run-villains-dev: $(villains_pom)
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw quarkus:dev
.PHONY: run-dev

$(villains_pom): $(super_heroes_mvnw)
> cd $(super_heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw io.quarkus:quarkus-maven-plugin:$(QUARKUS_VERSION):create \
>   -DplatformVersion=$(QUARKUS_VERSION) \
>   -DprojectGroupId=io.quarkus.workshop.super-heroes \
>   -DprojectArtifactId=rest-villains \
>   -DclassName="io.quarkus.workshop.superheroes.villain.VillainResource" \
>   -Dpath="api/villains" \
>   -Dextensions="resteasy-reactive-jackson"
> touch ./mvnw
> cd rest-villains
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw quarkus:add-extension -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator"

$(super_heroes_mvnw): $(zip)
> unzip $<

$(zip):
> wget https://raw.githubusercontent.com/quarkusio/quarkus-workshops/refs/heads/main/quarkus-workshop-super-heroes/dist/$(zip)

clean:
> rm -drf $(APP_DIR)

clean-all: clean
> rm -drf $(zip)

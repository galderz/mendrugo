GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 8
GRAALVM_BUILD ?= 7.1

APP_DIR ?= quarkus-super-heroes
TOPDIR = ..

default: run-fights-dev

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Colima.gmk

QUARKUS_VERSION ?= 3.3.2

fights_pom = $(fights_dir)/pom.xml
fights_dir = $(super_heroes_dir)/rest-fights
heroes_pom = $(heroes_dir)/pom.xml
heroes_dir = $(super_heroes_dir)/rest-heroes
heroes_jar = $(heroes_dir)/target/quarkus-app/quarkus-run.jar
villains_dir = $(super_heroes_dir)/rest-villains
villains_jar = $(villains_dir)/target/quarkus-app/quarkus-run.jar
villains_pom = $(villains_dir)/pom.xml
villains_runner = $(villains_dir)/target/rest-villains-1.0.0-SNAPSHOT-runner
super_heroes_dir := $(APP_DIR)/super-heroes
super_heroes_mvnw := $(super_heroes_dir)/mvnw
ui_dir = $(super_heroes_dir)/ui-super-heroes
ui_runner = $(super_heroes_dir)/ui-super-heroes/target/ui-super-heroes-1.0.0-SNAPSHOT-runner
zip := quarkus-super-heroes-workshop.zip

run-ui-native: $(ui_runner)
> $<

$(ui_runner): $(super_heroes_mvnw)
> cd $(ui_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw package -Pnative

run-ui-dev: $(super_heroes_mvnw)
> cd $(ui_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw quarkus:dev
.PHONY: run-ui-dev

run-fights-dev: $(fights_pom) fights
> cd $(fights_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw quarkus:dev
.PHONY: run-fights-dev

test-fights: $(fights_pom) fights
> cd $(fights_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw test
.PHONY: test-fights

fights:
> cp fights/Fight.java $(fights_dir)/src/main/java/io/quarkus/workshop/superheroes/fight
> cp fights/Fighters.java $(fights_dir)/src/main/java/io/quarkus/workshop/superheroes/fight
> cp fights/FightResource.java $(fights_dir)/src/main/java/io/quarkus/workshop/superheroes/fight
> cp fights/FightResourceTest.java $(fights_dir)/src/test/java/io/quarkus/workshop/superheroes/fight
> cp fights/FightService.java $(fights_dir)/src/main/java/io/quarkus/workshop/superheroes/fight
> cp fights/application.properties $(fights_dir)/src/main/resources
> cp fights/import.sql $(fights_dir)/src/main/resources
> cp fights/pom.xml $(fights_dir)
> cp -r fights/client $(fights_dir)/src/main/java/io/quarkus/workshop/superheroes/fight
> mkdir -p $(fights_dir)/src/test/java/io/quarkus/workshop/superheroes/fight/client
> cp -r fights/test/client $(fights_dir)/src/test/java/io/quarkus/workshop/superheroes/fight/client
.PHONY: fights

$(fights_pom): $(super_heroes_mvnw)
> cd $(super_heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw io.quarkus:quarkus-maven-plugin:$(QUARKUS_VERSION):create \
>   -DplatformVersion=$(QUARKUS_VERSION) \
>   -DprojectGroupId=io.quarkus.workshop.super-heroes \
>   -DprojectArtifactId=rest-fights \
>   -DclassName="io.quarkus.workshop.superheroes.fight.FightResource" \
>   -Dpath="api/fights" \
>   -Dextensions="jdbc-postgresql,hibernate-orm-panache,hibernate-validator,quarkus-resteasy-reactive-jackson,smallrye-openapi,kafka,quarkus-rest-client-reactive-jackson,smallrye-fault-tolerance"
> cd rest-fights
> touch ./mvnw

run-heroes-dev: $(heroes_pom) heroes
> cd $(heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw quarkus:dev
.PHONY: run-heroes-dev

run-heroes: $(heroes_jar)
> $(GRAALVM_HOME)/bin/java -jar $<
.PHONY: run-heroes

$(heroes_jar): $(heroes_pom) heroes
> cd $(heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw package

heroes:
> cp heroes/Hero.java $(heroes_dir)/src/main/java/io/quarkus/workshop/superheroes/hero
> cp heroes/HeroResource.java $(heroes_dir)/src/main/java/io/quarkus/workshop/superheroes/hero
> cp heroes/HeroResourceTest.java $(heroes_dir)/src/test/java/io/quarkus/workshop/superheroes/hero
> cp heroes/application.properties $(heroes_dir)/src/main/resources
> cp heroes/import.sql $(heroes_dir)/src/main/resources
.PHONY: heroes

$(heroes_pom): $(super_heroes_mvnw)
> cd $(super_heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw io.quarkus:quarkus-maven-plugin:$(QUARKUS_VERSION):create \
>   -DplatformVersion=$(QUARKUS_VERSION) \
>   -DprojectGroupId=io.quarkus.workshop.super-heroes \
>   -DprojectArtifactId=rest-heroes \
>   -DclassName="io.quarkus.workshop.superheroes.hero.HeroResource" \
>   -Dpath="api/heroes" \
>   -Dextensions="resteasy-reactive-jackson,quarkus-hibernate-validator,quarkus-smallrye-openapi,quarkus-hibernate-reactive-panache,quarkus-reactive-pg-client"
> cd rest-heroes
> touch ./mvnw

run-villains-dev: $(villains_pom) villains
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ../mvnw quarkus:dev
.PHONY: run-villains-dev

build-native-villains-container: $(villains_pom) villains
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw package  -Dquarkus.container-image.build=true -Pnative -Dquarkus.native.container-build=true
.PHONY: build-native-villains-container

build-villains-container: $(villains_pom) villains
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw package -Dquarkus.container-image.build=true
.PHONY: build-villains-container

run-native-villains: $(villains_runner)
> $<
.PHONY: run-native-villains

$(villains_runner):
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw package -Pnative

villains:
> cp villains/Villain.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainApplication.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainApplicationLifeCycle.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainService.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainResource.java $(villains_dir)/src/main/java/io/quarkus/workshop/superheroes/villain
> cp villains/VillainResourceTest.java $(villains_dir)/src/test/java/io/quarkus/workshop/superheroes/villain
> cp villains/application.properties $(villains_dir)/src/main/resources
> cp villains/import.sql $(villains_dir)/src/main/resources
.PHONY: villains

run-villains: $(villains_jar)
> $(GRAALVM_HOME)/bin/java -jar $<
.PHONY: run-villains

$(villains_jar): $(villains_pom) villains
> cd $(villains_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw package

$(villains_pom): $(super_heroes_mvnw)
> cd $(super_heroes_dir)
> JAVA_HOME=$(GRAALVM_HOME) ./mvnw io.quarkus:quarkus-maven-plugin:$(QUARKUS_VERSION):create \
>   -DplatformVersion=$(QUARKUS_VERSION) \
>   -DprojectGroupId=io.quarkus.workshop.super-heroes \
>   -DprojectArtifactId=rest-villains \
>   -DclassName="io.quarkus.workshop.superheroes.villain.VillainResource" \
>   -Dpath="api/villains" \
>   -Dextensions="resteasy-reactive-jackson,smallrye-openapi,jib,jdbc-postgresql,hibernate-orm-panache,hibernate-validator"
> cd rest-villains
> touch ./mvnw

$(super_heroes_mvnw): $(zip)
> unzip $<
> touch $@

$(zip):
> wget https://raw.githubusercontent.com/quarkusio/quarkus-workshops/refs/heads/main/quarkus-workshop-super-heroes/dist/$(zip)

compose:
> cd $(super_heroes_dir)/infrastructure
> docker compose -f docker-compose.yaml up -d
.PHONY: compose

compose-down:
> cd $(super_heroes_dir)/infrastructure
> docker compose -f docker-compose.yaml down
.PHONY: compose-down

clean:
> rm -drf $(APP_DIR)

clean-all: clean
> rm -drf $(zip)

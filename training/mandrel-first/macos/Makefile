quarkus-version := 1.13.7.Final
project := code-with-quarkus
zip := $(project).zip
artifact := ./$(project)/target/quarkus-artifact.properties
pom := $(project)/pom.xml

mvn-native += ./mvnw
mvn-native += package
mvn-native += -DskipTests
mvn-native += -Pnative
mvn-native += -Dquarkus.native.container-build=true
mvn-native += -Dquarkus.container-image.build=true

# Then head to http://localhost:8080/hello-resteasy
run: $(artifact)
	docker run -i --rm -p 8080:8080 g/$(project):1.0.0-SNAPSHOT
.PHONY: run

# On mac, using the default 2gb ram for docker vm will fail to build the image.
# Raising the docker vm memory to 4gb should be enough.
$(artifact): $(pom)
	cd $(project) && ./mvnw quarkus:add-extension -Dextensions="container-image-docker"
	cd $(project) && $(mvn-native)

$(pom):
	true | mvn io.quarkus:quarkus-maven-plugin:$(quarkus-version):create
	touch $(pom)

clean:
	cd $(project) && mvn clean
.PHONY: clean

clean-all:
	rm -drf $(project)
	rm -f $(zip)
.PHONY: clean-all

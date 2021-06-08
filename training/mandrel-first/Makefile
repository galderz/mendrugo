project := code-with-quarkus
zip := $(project).zip
executable := ./$(project)/target/$(project)-1.0.0-SNAPSHOT-runner
pom := $(project)/pom.xml

mvn-native += ./mvnw
mvn-native += package
mvn-native += -DskipTests
mvn-native += -Pnative
mvn-native += -Dquarkus.native.container-build=true
mvn-native += -Dquarkus.native.container-runtime=podman
mvn-native += -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel:21.0-java11

run: $(executable)
	./$(executable)
.PHONY: run

# Inspect sections of the binary. SVM heap and text sections contributing the most to the size of the binary.
readelf: $(executable)
	readelf -SW $(executable)
.PHONY: readelf

# Find out some interesting strings inside the binary, e.g. GraalVM version, static libraries...etc.
strings-vm: $(executable)
	strings $(executable) | grep core.VM
.PHONY: strings-vm

# Find the printed message inside the executable.
strings-hello: $(executable)
	strings $(executable) | grep Hello
.PHONY: strings-hello

# Find out shared library dependencies of an executable.
ldd: $(executable)
	ldd $(executable)
.PHONY: ldd

# Use --verbose to get a better understanding of how native-image works.
# It's a 2-step process.
# The first is a short java process, executed as native code.
# The second is where the real stuff happens.
# A normal java process for which you can see all the parameter invocations.
# You can take that and run it manually.
verbose: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.additional-build-args=--verbose
.PHONY: verbose

# Getting native-image info while building executables.
info: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.additional-build-args=--native-image-info
.PHONY: info

hello:
	curl http://localhost:8080/hello-resteasy
.PHONY: hello

$(executable): $(pom)
	cd $(project) && $(mvn-native)

$(pom): $(zip)
	unzip $(zip)
	touch $(pom)

$(zip):
	curl -O -J https://code.quarkus.io/api/download

clean:
	rm -drf $(project)
	rm -f $(zip)
.PHONY: clean

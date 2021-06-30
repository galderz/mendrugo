quarkus-version := 1.13.7.Final
mandrel-version := 21.0
project := code-with-quarkus
zip := $(project).zip
targetdir := $(project)/target
artifact := $(targetdir)/quarkus-artifact.properties
executable := $(targetdir)/$(project)-1.0.0-SNAPSHOT-runner
pom := $(project)/pom.xml
image := quay.io/quarkus/ubi-quarkus-mandrel:$(mandrel-version)-java11

mvn-native += ./mvnw
mvn-native += package
mvn-native += -DskipTests
mvn-native += -Pnative
mvn-native += -Dquarkus.native.container-build=true
mvn-native += -Dquarkus.container-image.build=true
mvn-native += -Dquarkus.native.builder-image=$(image)

ldd_cmd += ldd
ldd_cmd += /data/$(executable)

dnd_install += dnf
dnd_install += install
dnd_install += -y
dnd_install += binutils

strings += strings
strings += /data/$(executable)
strings += |
strings += grep
strings_hello += $(strings)
strings_hello += Hello
strings_vm += $(strings)
strings_vm += core.VM

# Then head to http://localhost:8080/hello-resteasy
run: $(artifact)
	docker run -i --rm -p 8080:8080 g/$(project):1.0.0-SNAPSHOT
.PHONY: run

# Enter a fedora container for entering commands
fedora: $(executable)
	docker run -t -i -v $(PWD)/$(targetdir):/data --rm fedora:34 /bin/bash
.PHONY: fedora

# Inspect native binary
inspect: $(executable)
	docker run -i -v $(PWD):/data --rm fedora:34 /bin/bash -c \
		"$(dnd_install); $(ldd_cmd); $(strings_hello); $(strings_vm)"
.PHONY: inspect

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

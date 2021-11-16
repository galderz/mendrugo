quarkus-version := 2.2.3.Final
mandrel-version := 21.3
project := code-with-quarkus
zip := $(project).zip
executable := ./$(project)/target/$(project)-1.0.0-SNAPSHOT-runner
pom := $(project)/pom.xml
image := quay.io/quarkus/ubi-quarkus-mandrel:$(mandrel-version)-java11

mvn-native += ./mvnw
mvn-native += package
mvn-native += -DskipTests
mvn-native += -Pnative
mvn-native += -Dquarkus.native.container-build=true
mvn-native += -Dquarkus.native.container-runtime=podman
mvn-native += -Dquarkus.native.builder-image=$(image)

FG_HOME ?= /opt/FlameGraph

run: $(executable)
	$(executable)
.PHONY: run

run-jvm: $(executable)
	java -jar code-with-quarkus/target/code-with-quarkus-1.0.0-SNAPSHOT-native-image-source-jar/code-with-quarkus-1.0.0-SNAPSHOT-runner.jar
.PHONY: run-jvm

preserve-fp: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.additional-build-args=-H:+PreserveFramePointer,-H:-DeleteLocalSymbols
.PHONY: preserve-fp

gdb-run: $(executable)
	gdb $(executable)
.PHONY: gdb-run

flamegraph-fixed:
	perf script | sed -E "s/thread-[0-9]*/thread/" | $(FG_HOME)/stackcollapse-perf.pl > out.perf-folded
	$(FG_HOME)/flamegraph.pl out.perf-folded > flamegraph.svg
.PHONY: flamegraph-fixed

perf-report-src:
	perf report --stdio -F+srcline
.PHONY: perf-report-src

# Swap -g for --call-graph dwarf
record-dwarf: $(executable)
	perf record -F 1009 --call-graph dwarf -a $(executable)
.PHONY: perf-record-dwarf

debug-info: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.debug.enabled
.PHONY: debug-info

query-symbols: $(executable)
	objdump -t $(executable) | grep GreetingResource || true
	objdump -t $(executable) | grep StringBuilder || true
.PHONY: query-symbols

flamegraph:
	perf script -i perf.data | $(FG_HOME)/stackcollapse-perf.pl > out.perf-folded
	$(FG_HOME)/flamegraph.pl out.perf-folded > flamegraph.svg
.PHONY: flamegraph

# -g to enable call graph recording.executable
# -a for all cpus
record: $(executable)
	perf record -F 1009 -g -a ./$(executable)

trace-object-instantiation: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.additional-build-args=--trace-object-instantiation=java.security.SecureRandom
.PHONY: trace-object-instantiation

# There are other reporting flags, but not used as much as the call tree one.
# Find all available flags calling:
expert-options:
	podman run $(image) --expert-options-all
.PHONY: expert-options

NEO_PASS ?= test

import:
	cd $(project) && \
	podman cp \
		target/*-native-image-source-jar/reports \
		testneo4j:/var/lib/neo4j/import
	podman cp import.cypher testneo4j:/var/lib/neo4j
	podman exec testneo4j bin/cypher-shell -u neo4j -p $(NEO_PASS) -f import.cypher
.PHONY: import

neo4j-clear:
	podman exec testneo4j bin/cypher-shell -u neo4j -p $(NEO_PASS) "MATCH(n) DETACH DELETE n" || true
	podman exec testneo4j bin/cypher-shell -u neo4j -p $(NEO_PASS) "DROP CONSTRAINT unique_vm_id" || true
	podman exec testneo4j bin/cypher-shell -u neo4j -p $(NEO_PASS) "DROP CONSTRAINT unique_method_id" || true
.PHONY: neo4j-clear

neo4j:
	podman run \
		--detach \
		--rm \
		--name testneo4j \
		-p7474:7474 -p7687:7687 \
		--env NEO4J_AUTH=neo4j/$(NEO_PASS) \
		neo4j:latest
.PHONY: neo4j

# Generate and inspect reports.
# Notice how reports are generated in a folder inside the container that is mapped locally.
# call_tree report useful for getting an approximation on why something is included.
# used_packages, used_classes and used_methods when comparing different versions of the application.
# Example: why does the image take longer to build? why is the image bigger now?
reports: $(pom)
	cd $(project) && $(mvn-native) -Dquarkus.native.enable-reports
.PHONY: reports

# Inspect sections of the binary.
# SVM heap and text sections contributing the most to the size of the binary.
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

$(pom):
	yes '' | mvn io.quarkus:quarkus-maven-plugin:$(quarkus-version):create
	touch $(pom)

clean:
	cd $(project) && mvn clean
.PHONY: clean

clean-all:
	rm -drf $(project)
	rm -f $(zip)
.PHONY: clean-all

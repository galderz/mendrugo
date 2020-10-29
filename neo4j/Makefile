NEO_HOME ?= /opt/neo4j

all: build call-tree neo4j

clean: clean-reports clean-neo4j
	rm -f helloworld || true
	rm -f *.class || true

clean-reports:
	rm -drf reports || true

clean-neo4j:
	cat clean.cypher | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) || true

build:
	javac HelloWorld.java

native:
	native-image HelloWorld

native-verbose:
	native-image --verbose HelloWorld

native-debug-attach:
	native-image --debug-attach=8000 HelloWorld

native-debug-remote-attach:
	native-image --debug-attach=*:8000 HelloWorld

run:
	./helloworld

call-tree: build
	native-image -H:+PrintAnalysisCallTree HelloWorld

jar:
	jar cvfm helloworld.jar Manifest.txt HelloWorld.class

run-jar:
	java -jar helloworld.jar

method-histogram:
	native-image -H:+PrintMethodHistogram HelloWorld

neo4j:
	time cat $(shell find reports -name '*.cypher') | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)

neo4j-start:
	${NEO_HOME}/bin/neo4j console


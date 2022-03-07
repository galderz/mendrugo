NEO_HOME ?= /opt/neo4j

all: build call-tree neo4j

clean: clean-reports clean-neo4j
	rm -f helloworld || true
	rm -f *.class || true

clean-reports:
	rm -drf reports || true

clean-neo4j:
	rm -f ${NEO_HOME}/import/*
	$(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "MATCH(n) DETACH DELETE n" || true
	$(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "DROP CONSTRAINT unique_vm_id" || true
	$(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS) "DROP CONSTRAINT unique_method_id" || true

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
	native-image -H:+PrintAnalysisCallTree -H:PrintAnalysisCallTreeType=CSV HelloWorld

jar:
	jar cvfe helloworld.jar HelloWorld HelloWorld.class

run-jar:
	java -jar helloworld.jar

method-histogram:
	native-image -H:+PrintMethodHistogram HelloWorld

neo4j:
	cp reports/*.csv ${NEO_HOME}/import
	time cat $(shell find reports -name '*.cypher') | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)

neo4j-stop:
	${NEO_HOME}/bin/neo4j stop || true

neo4j-start: neo4j-stop
	${NEO_HOME}/bin/neo4j console


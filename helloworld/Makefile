NEO_HOME ?= /opt/neo4j

all: clean build native run

clean: clean-reports
	rm -f helloworld
	rm -f *.class

clean-reports:
	rm -drf reports

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

native-dashboard:
	native-image -H:DashboardDump=dashboard -H:+DashboardPointsTo HelloWorld

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

neo4j-call-tree: call-tree
	$(call load-cypher )

neo4j-time-call-tree: call-tree
	time $(call load-cypher)

define load-cypher
cat $(shell find reports -name '*.cypher') | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)
endef

neo4j-start:
	${NEO_HOME}/bin/neo4j console

neo4j-clean:
	cat clean.cypher | $(NEO_HOME)/bin/cypher-shell -u $(NEO_USER) -p $(NEO_PASS)

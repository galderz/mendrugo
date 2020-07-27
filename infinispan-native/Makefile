# Examples:
# -Xms64m -Xmx512m
# -XX:+VerboseGC -XX:+PrintGC -XX:+PrintGCSummary -XX:+PrintHeapShape
EXTRA_ARGS ?= ""

ADDRESS ?= 127.0.0.1
NUM_ENTRIES ?= 500000

FG_HOME ?= /opt/FlameGraph
INFINISPAN_VERSION ?= 11.0.2-SNAPSHOT
Q_HOME ?= ${HOME}/.qollider/cache/latest
Q_JAVA_HOME ?= ${Q_HOME}/java_home
SERVER_HOME = ${Q_HOME}/infinispan/server/runtime/target/infinispan-server-${INFINISPAN_VERSION}
RUNNER_NAME = infinispan-quarkus-server-runner-${INFINISPAN_VERSION}-runner
DEBUG_NAME = ${RUNNER_NAME}.debug
RUNNER_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${RUNNER_NAME}
DEBUG_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${DEBUG_NAME}

install-servers: clean install-jvm install-native

clean:
	rm -drf servers
	mkdir servers || true
	mkdir graphs || true

install-jvm:
	$(call make-jvm,1)
	$(call make-jvm,2)

define make-jvm
mkdir -p servers/jvm${1} || true
cp -r ${SERVER_HOME}/* servers/jvm${1}
./servers/jvm${1}/bin/cli.sh user create foo -p "bar"
endef

install-native:
	$(call make-native,1)
	$(call make-native,2)

define make-native
mkdir -p servers/native${1} || true
mkdir -p servers/native${1}/server || true
cp ${RUNNER_PATH} servers/native${1}/${RUNNER_NAME}
cp ${DEBUG_PATH} servers/native${1}/${DEBUG_NAME}
cp -r servers/jvm1/server/conf servers/native${1}/server
cp -r servers/jvm1/server/conf servers/native${1}
endef

start-jvm1:
	JAVA_HOME=${Q_JAVA_HOME} ./servers/jvm1/bin/server.sh -Dinfinispan.bind.address=${ADDRESS}

start-jvm2:
	JAVA_HOME=${Q_JAVA_HOME} ./servers/jvm2/bin/server.sh -o 100 -Dinfinispan.bind.address=${ADDRESS}

start-native1:
	cd servers/native1 && $(call run-native,11222)

start-native2:
	cd servers/native2 && $(call run-native,11322)

define run-native
./${RUNNER_NAME} \
  ${EXTRA_ARGS} \
  -Dquarkus.infinispan-server.bind-port=${1} \
  -Dinfinispan.bind.address=${ADDRESS}
endef

record-native1:
	cd servers/native1 && \
		${call perf-record} -- $(call run-native,11222)

record-native2:
	cd servers/native2 && \
		${call perf-record} -- $(call run-native,11322)

define perf-record
perf record -F 99 -a --call-graph dwarf
endef

gen-graphs:
	${call run-graph,1}
	${call run-graph,2}

define run-graph
perf script -i servers/native${1}/perf.data | ${FG_HOME}/stackcollapse-perf.pl > graphs/out.perf-folded
${FG_HOME}/flamegraph.pl graphs/out.perf-folded > graphs/native${1}-perf-kernel.svg
endef

load-data:
	mvn clean compile exec:java \
		-Dexec.mainClass="svm.infinispan.LoadData" \
		-Daddress=${ADDRESS} \
		-Dnum.entries=${NUM_ENTRIES}

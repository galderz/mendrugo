# Examples:
# -Xms64m -Xmx512m
# -XX:+VerboseGC -XX:+PrintGC -XX:+PrintGCSummary -XX:+PrintHeapShape
EXTRA_ARGS ?= ""

NUM_ENTRIES ?= 500000

FG_HOME ?= /opt/FlameGraph
PERF_HOME ?= /usr/bin

INFINISPAN_VERSION ?= 12.0.0-SNAPSHOT
Q_HOME ?= ${HOME}/.qollider/cache/latest
Q_JAVA_HOME ?= ${Q_HOME}/java_home
SERVER_HOME = ${Q_HOME}/infinispan/server/runtime/target/infinispan-server-${INFINISPAN_VERSION}

RUNNER_NAME = infinispan-quarkus-server-runner-${INFINISPAN_VERSION}-runner
DEBUG_NAME = ${RUNNER_NAME}.debug
TRACING_RUNNER_NAME = tracing-${RUNNER_NAME}
TRACING_DEBUG_NAME = tracing-${DEBUG_NAME}

RUNNER_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${RUNNER_NAME}
DEBUG_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${DEBUG_NAME}
TRACING_RUNNER_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${TRACING_RUNNER_NAME}
TRACING_DEBUG_PATH = ${Q_HOME}/infinispan-quarkus/server-runner/target/${TRACING_DEBUG_NAME}

TMP_DIR = /tmp/servers-$$(date +%d%m)
GRAPHS_DIR = $(TMP_DIR)/graphs

install-servers: clean mkdirs install-jvm install-native

clean:
	rm -drf $(TMP_DIR)

mkdirs:
	mkdir -p $(TMP_DIR)
	mkdir -p $(TMP_DIR)/graphs

install-jvm:
	$(call make-jvm,1)
	$(call make-jvm,2)

define make-jvm
mkdir -p $(TMP_DIR)/jvm${1} || true
cp -r ${SERVER_HOME}/* $(TMP_DIR)/jvm${1}
JAVA_HOME=${Q_JAVA_HOME} $(TMP_DIR)/jvm${1}/bin/cli.sh user create foo -p "bar"
endef

install-native:
	$(call make-native,1)
	$(call make-native,2)

define make-native
mkdir -p $(TMP_DIR)/native${1} || true
mkdir -p $(TMP_DIR)/native${1}/server || true
cp ${RUNNER_PATH} $(TMP_DIR)/native${1}/${RUNNER_NAME}
cp ${TRACING_RUNNER_PATH} $(TMP_DIR)/native${1}/${TRACING_RUNNER_NAME}
cp ${TRACING_DEBUG_PATH} $(TMP_DIR)/native${1}/${TRACING_DEBUG_NAME}
cp -r $(TMP_DIR)/jvm1/server/conf $(TMP_DIR)/native${1}/server
cp -r $(TMP_DIR)/jvm1/server/conf $(TMP_DIR)/native${1}
endef

start-jvm1:
	JAVA_HOME=${Q_JAVA_HOME} $(TMP_DIR)/jvm1/bin/server.sh -Dinfinispan.bind.address=${ADDRESS}

start-jvm2:
	JAVA_HOME=${Q_JAVA_HOME} $(TMP_DIR)/jvm2/bin/server.sh -o 100 -Dinfinispan.bind.address=${ADDRESS}

start-native1:
	cd $(TMP_DIR)/native1 && $(call run-native,$(RUNNER_NAME),11222)

start-native2:
	cd $(TMP_DIR)/native2 && $(call run-native,$(RUNNER_NAME),11322)

define run-native
./${1} \
  ${EXTRA_ARGS} \
  -Dquarkus.infinispan-server.bind-port=${2} \
  -Dinfinispan.bind.address=${ADDRESS}
endef

debug-jvm1:
	JAVA_HOME=${Q_JAVA_HOME} \
	JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,address=${ADDRESS}:8787,server=y,suspend=n" \
	$(TMP_DIR)/jvm1/bin/server.sh -Dinfinispan.bind.address=${ADDRESS}

record-native1:
		${call perf-record} -- $(call run-native,$(TRACING_RUNNER_NAME),11222)
	cd $(TMP_DIR)/native1 && \

record-native2:
	cd $(TMP_DIR)/native2 && \
		${call perf-record} -- $(call run-native,$(TRACING_RUNNER_NAME),11322)

define perf-record
${PERF_HOME}/perf record -F 99 -a --call-graph dwarf
endef

report-native1:
	cd $(TMP_DIR)/native1 && \
		perf report --stdio

gen-graphs:
	${call run-graph,1}
	${call run-graph,2}

define run-graph
${PERF_HOME}/perf script -i $(TMP_DIR)/native${1}/perf.data | ${FG_HOME}/stackcollapse-perf.pl > $(GRAPHS_DIR)/out.perf-folded
${FG_HOME}/flamegraph.pl $(GRAPHS_DIR)/out.perf-folded > $(GRAPHS_DIR)/native${1}-perf-kernel.svg
endef

load-data:
	mvn clean compile exec:java \
		-Dexec.mainClass="svm.infinispan.LoadData" \
		-Daddress=${ADDRESS} \
		-Dnum.entries=${NUM_ENTRIES}

# Examples:
# -Xms64m -Xmx512m
# -XX:+VerboseGC -XX:+PrintGC -XX:+PrintGCSummary -XX:+PrintHeapShape
EXTRA_ARGS ?= ""

FG_HOME ?= /opt/FlameGraph
INFINISPAN_VERSION ?= 11.0.2-SNAPSHOT
Q_HOME ?= ${HOME}/.qollider/cache/latest
Q_JAVA_HOME ?= ${Q_HOME}/java_home
INFINISPAN_SERVER_HOME = ${Q_HOME}/infinispan/server/runtime/target/infinispan-server-${INFINISPAN_VERSION}
INFINISPAN_SERVER_RUNNER = ${Q_HOME}/infinispan-quarkus/server-runner/target/infinispan-quarkus-server-runner-${INFINISPAN_VERSION}-runner

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
cp -r ${INFINISPAN_SERVER_HOME}/* servers/jvm${1}
./servers/jvm${1}/bin/cli.sh user create foo -p "bar"
endef

install-native:
	$(call make-native,1)
	$(call make-native,2)

define make-native
mkdir -p servers/native${1} || true
mkdir -p servers/native${1}/server || true
cp ${INFINISPAN_SERVER_RUNNER} servers/native${1}/server-runner
cp -r servers/jvm1/server/conf servers/native${1}/server
cp -r servers/jvm1/server/conf servers/native${1}
endef

start-jvm1:
	JAVA_HOME=${Q_JAVA_HOME} ./servers/jvm1/bin/server.sh

start-jvm2:
	JAVA_HOME=${Q_JAVA_HOME} ./servers/jvm2/bin/server.sh -o 100

start-native1:
	cd servers/native1 && $(call run-native,11222)

start-native2:
	cd servers/native2 && $(call run-native,11322)

define run-native
./server-runner \
  ${EXTRA_ARGS} \
  -Dquarkus.infinispan-server.bind-port=${1}
endef

record-native1:
	cd servers/native1 && \
		${call perf-record} -- $(call run-native,11222)

record-native2:
	cd servers/native2 && \
		${call perf-record} -- $(call run-native,11322)

define perf-record
perf record -F 99 -a -g
endef

gen-graphs:
	${call run-graph,1}
	${call run-graph,2}

define run-graph
perf script -i servers/native${1}/perf.data | ./stackcollapse-perf.pl > out.perf-folded
${FG_HOME}/flamegraph.pl out.perf-folded > graphs/native${1}-perf-kernel.svg
endef

copy-graphs:
	scp sheldon:~/1/mendrugo/infinispan-native/graphs .

load-data:
	mvn clean compile exec:java -Dexec.mainClass="svm.infinispan.LoadData"

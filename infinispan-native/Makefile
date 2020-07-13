INFINISPAN_VERSION ?= 11.0.2-SNAPSHOT
Q_HOME ?= ${HOME}/.qollider/cache/latest
Q_JAVA_HOME ?= ${Q_HOME}/java_home
INFINISPAN_SERVER_HOME = ${Q_HOME}/infinispan/server/runtime/target/infinispan-server-${INFINISPAN_VERSION}
INFINISPAN_SERVER_RUNNER = ${Q_HOME}/infinispan-quarkus/server-runner/target/infinispan-quarkus-server-runner-${INFINISPAN_VERSION}-runner

install-servers: clean install-jvm install-native

clean:
	rm -drf target
	mkdir target || true

install-jvm:
	$(call make-jvm,1)
	$(call make-jvm,2)

define make-jvm
mkdir -p target/jvm${1} || true
cp -r ${INFINISPAN_SERVER_HOME}/* target/jvm${1}
./target/jvm${1}/bin/cli.sh user create foo -p "bar"
endef

install-native:
	$(call make-native,1)
	$(call make-native,2)

define make-native
mkdir -p target/native${1} || true
mkdir -p target/native${1}/server || true
cp ${INFINISPAN_SERVER_RUNNER} target/native${1}/server-runner
cp -r target/jvm1/server/conf target/native${1}/server
cp -r target/jvm1/server/conf target/native${1}
endef

start-jvm1:
	JAVA_HOME=${Q_JAVA_HOME} ./target/jvm1/bin/server.sh

start-jvm2:
	JAVA_HOME=${Q_JAVA_HOME} ./target/jvm2/bin/server.sh -o 100

start-native1:
	cd target/native1 && ./server-runner \
		-Dquarkus.infinispan-server.bind-port=11222

start-native2:
	cd target/native2 && ./server-runner -o 100 \
		-Dquarkus.infinispan-server.bind-port=11322

load-data:
	mvn clean compile exec:java -Dexec.mainClass="svm.infinispan.LoadData"

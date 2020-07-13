INFINISPAN_VERSION ?= 11.0.2-SNAPSHOT
Q_HOME ?= ${HOME}/.qollider/cache/latest
JAVA_HOME ?= ${Q_HOME}/java_home
INFINISPAN_SERVER_HOME = ${Q_HOME}/infinispan/server/runtime/target/infinispan-server-${INFINISPAN_VERSION}

install-servers: clean install-jvm install-native

clean:
	rm -drf target
	mkdir target || true

install-jvm:
	$(call make-jvm,1)
	$(call make-jvm,2)

define make-jvm
mkdir -p target/jvm${1} || true
cp -r ${INFINISPAN_SERVER_HOME} target/jvm${1}
./target/jvm${1}/bin/cli.sh user create foo -p "bar"
endef

install-native:
	$(call make-native,1)
	$(call make-native,2)

define make-native
mkdir -p target/native${1}/bin || true
mkdir -p target/native${1}/server || true
cp ${Q_HOME}/infinispan-quarkus/server-runner/target/infinispan-quarkus-server-runner-${INFINISPAN_VERSION}-runner target/native${1}/bin/server-runner
cp -r ${INFINISPAN_SERVER_HOME}/server/conf target/native${1}/server
endef

start-jvm1:
	JAVA_HOME=${JAVA_HOME} ./target/jvm1/bin/server.sh

start-jvm2:
	JAVA_HOME=${JAVA_HOME} ./target/jvm2/bin/server.sh -o 100

start-native1:
	./target/native1/bin/server-runner

start-native2:
	./target/native2/bin/server-runner -o 100

load-data:
	mvn compile exec:java -Dexec.mainClass="svm.infinispan.LoadData" 

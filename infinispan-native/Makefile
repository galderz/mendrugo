INFINISPAN_VERSION ?= 11.0.1.Final

install-servers: clean install-jvm install-native

clean:
	rm -drf target
	mkdir target || true

install-jvm:
	curl -o target/infinispan-jvm.zip https://downloads.jboss.org/infinispan/${INFINISPAN_VERSION}/infinispan-server-${INFINISPAN_VERSION}.zip
	unzip target/infinispan-jvm.zip -d target
	mkdir -p target/jvm1 || true
	mkdir -p target/jvm2 || true
	cp -r target/infinispan-server-${INFINISPAN_VERSION}/* target/jvm1
	cp -r target/infinispan-server-${INFINISPAN_VERSION}/* target/jvm2
	./target/jvm1/bin/cli.sh user create foo -p "bar"
	./target/jvm2/bin/cli.sh user create foo -p "bar"

install-native:
	curl -Lo target/infinispan-native.tar.gz https://github.com/infinispan/infinispan-quarkus/archive/${INFINISPAN_VERSION}.tar.gz
	mkdir -p target/infinispan-native || true
	tar -xzvpf target/infinispan-native.tar.gz -C target/infinispan-native --strip-components 1
	cd target/infinispan-native \
		&& mvn clean install -Dnative -DskipTests
	mkdir -p target/native1 || true
	mkdir -p target/native2 || true
	tar -xzvpf target/infinispan-native.tar.gz -C target/native1 --strip-components 1
	tar -xzvpf target/infinispan-native.tar.gz -C target/native2 --strip-components 1

start-jvm1:
	./target/jvm1/bin/server.sh

start-jvm2:
	./target/jvm2/bin/server.sh -o 100

start-native1:
	./target/native1/bin/server.sh

start-native2:
	./target/native2/bin/server -o 100

load-data:
	mvn compile exec:java -Dexec.mainClass="svm.infinispan.LoadData" 

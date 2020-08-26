M2_REPO=${HOME}/.m2/repository

all: clean build

build:
	mvn install

clean:
	mvn clean

run-jvm:
	mvn exec:java \
		-Dexec.mainClass="mendrugo.infinispan.embedded.EmbeddedStress"

native:
	$(call make-native)

native-debug:
	$(call make-native,-g)

run:
	./target/perf-infinispan-embedded

define make-native
native-image ${1} \
	-jar target/perf-infinispan-embedded-1.0-SNAPSHOT.jar \
	-cp ${M2_REPO}/org/infinispan/infinispan-commons/11.0.1.Final/infinispan-commons-11.0.1.Final.jar:${M2_REPO}/org/infinispan/infinispan-core/11.0.1.Final/infinispan-core-11.0.1.Final.jar:${M2_REPO}/org/jgroups/jgroups/4.2.1.Final/jgroups-4.2.1.Final.jar:${M2_REPO}/org/reactivestreams/reactive-streams/1.0.3/reactive-streams-1.0.3.jar:${M2_REPO}/org/jboss/logging/jboss-logging/3.4.1.Final/jboss-logging-3.4.1.Final.jar:${M2_REPO}/org/jboss/spec/javax/transaction/jboss-transaction-api_1.2_spec/1.1.1.Final/jboss-transaction-api_1.2_spec-1.1.1.Final.jar:${M2_REPO}/io/reactivex/rxjava3/rxjava/3.0.4/rxjava-3.0.4.jar:${M2_REPO}/org/infinispan/protostream/protostream/4.3.3.Final/protostream-4.3.3.Final.jar:${M2_REPO}/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar:${M2_REPO}/org/jboss/slf4j/slf4j-jboss-logging/1.2.0.Final/slf4j-jboss-logging-1.2.0.Final.jar:${M2_REPO}/com/github/ben-manes/caffeine/caffeine/2.8.4/caffeine-2.8.4.jar \
	target/perf-infinispan-embedded \
	-H:+ReportExceptionStackTraces \
	-H:+TraceClassInitialization \
	-H:IncludeResources='.*\.properties$\' \
	-H:IncludeResources='.*\.proto$\' \
	-H:IncludeResourceBundles=sun.security.util.Resources \
	--no-fallback \
	--no-server \
	--allow-incomplete-classpath
endef

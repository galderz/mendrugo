runtime=org.acme.getting.started.commandmode.graal.RuntimeIsTraceAccessor

additional-build-args="--initialize-at-run-time=$(runtime)"

additional-build-args-debug="--debug-attach=*:8000"

all: native run

native:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args)

native-debug:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-debug)

run:
	./target/quarkus-logging-1.0-SNAPSHOT-runner

run-info:
	./target/quarkus-logging-1.0-SNAPSHOT-runner -Dquarkus.log.category.\"org.acme.getting.started.commandmode\".level=INFO

run-trace:
	./target/quarkus-logging-1.0-SNAPSHOT-runner -Dquarkus.log.category.\"org.acme.getting.started.commandmode\".level=TRACE

clean:
	./mvnw clean

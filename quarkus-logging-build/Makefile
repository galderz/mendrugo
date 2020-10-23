runtime=org.acme.getting.started.commandmode.graal.RuntimeIsTraceAccessor

additional-build-args="-H:+AllowFoldMethods"

additional-build-args-debug="$(additional-build-args),--debug-attach=*:8000"

runner="quarkus-logging-build-1.0-SNAPSHOT-runner"

all: native run

native:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args)

native-debug:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-debug)

run:
	./target/$(runner)

run-info:
	./target/$(runner) -Dquarkus.log.category.\"org.acme.getting.started.commandmode\".level=INFO

run-trace:
	./target/$(runner) -Dquarkus.log.category.\"org.acme.getting.started.commandmode\".level=TRACE

clean:
	./mvnw clean

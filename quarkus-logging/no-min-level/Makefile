additional-build-args-debug="$(additional-build-args),--debug-attach=*:8000"

runner="quarkus-logging-runner"

all: native run run-warn run-info run-trace

native:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args)

native-debug:
	./mvnw install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-debug)

run:
	./target/$(runner)

run-warn:
	./target/$(runner) -Dquarkus.log.category.\"org.example.house\".level=WARN

run-info:
	./target/$(runner) -Dquarkus.log.category.\"org.example.house\".level=INFO

run-trace:
	./target/$(runner) -Dquarkus.log.category.\"org.example.house\".level=TRACE

clean:
	./mvnw clean

additional-build-args-debug="$(additional-build-args),--debug-attach=*:8000"
additional-build-args="-H:+RuntimeAssertions"

runner=quarkus-logging-runner

all: native run run-warn run-info run-trace

native:
	mvn install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args)

native-debug:
	mvn install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-debug)

run:
	./target/$(runner)

run-warn:
	./target/$(runner) -Dquarkus.log.category.\"org.example.iron\".level=WARN

run-info:
	./target/$(runner) -Dquarkus.log.category.\"org.example.iron\".level=INFO

run-trace:
	./target/$(runner) -Dquarkus.log.category.\"org.example.iron\".level=TRACE

clean:
	mvn clean

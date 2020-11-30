additional-build-args-debug="$(additional-build-args),--debug-attach=*:8000"

#additional-build-args="-H:+RuntimeAssertions"
additional-build-args="-ea"

additional-build-args-graph="-ea,-H:Dump=:2,-H:MethodFilter=tryTraceOnTheFly"

runner=quarkus-logging-runner

all: native run run-warn run-info run-trace

native:
	mvn install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args)

native-debug:
	mvn install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-debug)

# Can be rendered with seafoam call, e.g.
# seafoam ./SubstrateHostedCompilation-260762\[Iron.tryTraceOnTheFly\(\)void\].bgv:0 render --out graph_tryTraceOnTheFly_3.pdf
native-graph:
	mvn install -Dnative -Dquarkus.native.additional-build-args=$(additional-build-args-graph)

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

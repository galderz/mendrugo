all: build native bench

build:
	mvn clean package -DskipTests

native:
	$(call make-native)

native-verbose:
	$(call make-native,--verbose -H:UnsafeAutomaticSubstitutionsLogLevel=3)

define make-native
native-image \
    -jar target/benchmarks.jar \
    --no-fallback \
    --no-server \
    -H:IncludeResources=META-INF/BenchmarkList \
    -H:+ReportExceptionStackTraces \
    ${1} \
    target/benchmarks
endef

bench-jvm:
	java -jar target/benchmarks.jar -v EXTRA

bench:
	target/benchmarks \
	-v EXTRA \
	--jvm target/benchmarks

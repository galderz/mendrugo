all: clean build run-jvm native run

all-debug: clean build run-jvm native-debug

clean:
	rm -drf reports

build:
	mvn clean package

#run-jvm:
#	mkdir -p META-INF/native-image
#	java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -ea -jar target/example.jar

run-jvm:
	java -ea -jar target/example.jar

native:
	$(call make-native,n)

native-debug:
	$(call make-native,y)

# Without --no-fallback it'd fail
define make-native
native-image \
    -ea \
    -jar target/example.jar \
    -H:+ReportExceptionStackTraces \
    -H:+PrintAnalysisCallTree \
    -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=${1},address=8000 \
    --no-fallback \
    target/example
endef

run:
	target/example

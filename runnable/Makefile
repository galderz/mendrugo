all: build native

clean:
	mvn clean
	rm -drf reports

build:
	mvn package

native:
	native-image --initialize-at-build-time= -H:+PrintAnalysisCallTree -jar target/runnable.jar target/runnable

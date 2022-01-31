all: build native run

clean:
	mvn clean
	rm -drf reports

build:
	mvn package

native:
	native-image --no-fallback --initialize-at-build-time= -H:+PrintAnalysisCallTree -jar target/tostring.jar target/tostring

run:
	./target/tostring

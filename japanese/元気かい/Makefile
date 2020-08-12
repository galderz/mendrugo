all: clean build native run

clean:
	rm -f helloworld
	rm -f *.class

build:
	javac HelloWorld.java

native:
	native-image HelloWorld

native-verbose:
	native-image --verbose HelloWorld

native-debug-attach:
	native-image --debug-attach=8000 HelloWorld

native-debug-remote-attach:
	native-image --debug-attach=*:8000 HelloWorld

run:
	./helloworld

call-tree:
	native-image -H:+PrintAnalysisCallTree HelloWorld

method-histogram:
	native-image -H:+PrintMethodHistogram HelloWorld

build-container:
	docker build -t graalvm .

run-container:
	docker run --rm -it -v $$(pwd):/data graalvm

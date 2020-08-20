sourcefiles = \
Stress.java

classfiles  = $(sourcefiles:.java=.class)

all: dependencies $(classfiles)

dependencies:
	mkdir lib || true
	curl https://repo1.maven.org/maven2/org/graalvm/nativeimage/svm/20.1.0/svm-20.1.0.jar -o lib/svm.jar

%.class: %.java
	javac -d . -classpath .:lib/svm.jar $<

clean:
	rm -f *.class
	rm -f stress

run-jvm:
	java Stress

native:
	native-image -g Stress

record:
	perf record -F 99 -a --call-graph dwarf -- ./stress

report:
	perf report --stdio

run:
	./stress

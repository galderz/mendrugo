sourcefiles = \
Stress.java

classfiles  = $(sourcefiles:.java=.class)

all: $(classfiles)

%.class: %.java
	javac -d . -classpath . $<

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

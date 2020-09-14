FG_HOME ?= /opt/FlameGraph

sourcefiles = \
Sockets.java

main = Sockets
exec = $(shell echo $(main) | tr A-Z a-z)

classfiles  = $(sourcefiles:.java=.class)

all: $(classfiles)

%.class: %.java
	javac -d . -classpath . $<

clean:
	rm -f *.class
	rm -f ${exec}

run-jvm:
	java $(main)

native:
	$(call make-native,-g)

native-nodebug:
	$(call make-native)

define make-native
native-image \
    ${1} \
    -H:-DeleteLocalSymbols \
    -H:+PreserveFramePointer \
    $(main)
endef

record:
	perf record -F 1009 -a --call-graph dwarf -- ./$(exec)

report:
	perf report --stdio

report-log:
	rm -f perf-report.log && perf report --stdio > perf-report.log

verbose-report-log:
	rm -f verbose-perf-report.log && perf report --verbose --stdio > verbose-perf-report.log 2>&1

gen-graph:
	perf script -i perf.data | $(FG_HOME)/stackcollapse-perf.pl > graphs/out.perf-folded
	$(FG_HOME)/flamegraph.pl graphs/out.perf-folded > graphs/profile.svg

run:
	./$(exec)

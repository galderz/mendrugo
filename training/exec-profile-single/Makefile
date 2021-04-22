executable := stringbuilders
jar := $(executable).jar
source := $(executable).java

FG_HOME ?= /opt/FlameGraph

all: $(jar)

flamegraph:
	perf script -i perf.data | $(FG_HOME)/stackcollapse-perf.pl > out.perf-folded
	$(FG_HOME)/flamegraph.pl out.perf-folded > flamegraph.svg
.PHONY: flamegraph

$(executable): $(jar)
	native-image -jar $(jar) $(executable)

$(jar): $(source)
	jbang export portable --force $(source)

intellij-idea:
	jbang edit --open=idea $(source)
.PHONY: intellij-idea

profile-fp:
	native-image -H:-DeleteLocalSymbols -H:+PreserveFramePointer -jar $(jar) $(executable)
	perf record -F 1009 -g -a -- ./$(executable)
.PHONY: profile-fp

profile-dwarf:
	native-image -g -jar $(jar) $(executable)
	perf record -F 1009 --call-graph dwarf -a -- ./$(executable)
.PHONY: profile-dwarf

profile-report:
	perf report --stdio -F+srcline
.PHONY: profile-report

clean:
	rm -f *.jar
	rm -drf libs
	rm -f $(executable)
	rm -f perf.data*
	rm -f *.svg
	rm -f .*.svg
	rm -f out*
	rm -drf sources
.PHONY: clean

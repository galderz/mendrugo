executable := maps
jar := $(executable).jar
source := $(executable).java

FG_HOME ?= /opt/FlameGraph

all: $(executable)

$(executable): $(jar)
	native-image -g -jar $(jar) $(executable)

$(jar): $(source)
	jbang export portable --force $(source)

$(source):
	jbang init --template=hello $(source)

intellij-idea:
	jbang edit --open=idea $(source)
.PHONY: intellij-idea

in.txt: $(jar)
	time java -jar $(jar) gen

perf.data:
	perf mem record -F 1009 --call-graph dwarf -a -- ./$(executable)

profile-report:
	perf report --stdio -F+srcline
.PHONY: profile-report

flamegraph.svg:
	perf script | $(FG_HOME)/stackcollapse-perf.pl > out.perf-folded
	$(FG_HOME)/flamegraph.pl out.perf-folded > flamegraph.svg

clean:
	rm -f *.jar
	rm -drf libs
	rm -f *.java
	rm -f $(executable)
.PHONY: clean

sourcefiles = \
StringBuilders.java

main = StringBuilders
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
native-image ${1} $(main)
endef

record:
	perf record -F 1009 -a --call-graph dwarf -- ./$(exec)

report:
	perf report --stdio

run:
	./$(exec)

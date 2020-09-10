sourcefiles = \
DayTwo.java

main = DayTwo
exec = $(shell echo $(main) | tr A-Z a-z)

classfiles  = $(sourcefiles:.java=.class)

all: $(classfiles) native run

%.class: %.java
	javac -d . -classpath . $<

clean:
	rm -f *.class
	rm -f *.jar
	rm -f ${exec}

run-jvm:
	java $(main)

native:
	$(call make-native)

native-debug:
	$(call make-native,-g)

native-tree:
	$(call make-native,-H:+PrintAnalysisCallTree)

define make-native
native-image ${1} $(main)
endef

run:
	./$(exec)

jar:
	jar cvfm daytwo.jar Manifest.txt DayTwo.class

run-jar:
	java -jar daytwo.jar

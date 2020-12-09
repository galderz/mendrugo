sourcefiles = \
reflection_basics.java

main = reflection_basics
exec = $(shell echo $(main) | tr A-Z a-z)

classfiles = $(sourcefiles:.java=.class)

all: $(classfiles) run-jvm native run

clean:
	rm -drf reports
	rm -f *.class
	rm -f ${exec}

%.class: %.java
	javac -d . -classpath . $<

run-jvm:
	mkdir -p META-INF/native-image
	java -ea -agentlib:native-image-agent=config-output-dir=META-INF/native-image $(main)

native:
	$(call make-native,n)

# If reflection becoming a pain, make sure you add:
# --no-fallback
define make-native
native-image \
    -ea \
    $(main) \
    -H:+ReportExceptionStackTraces \
    -H:+PrintAnalysisCallTree \
    -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=${1},address=8000 \
    $(exec)
endef

run:
	./$(exec)

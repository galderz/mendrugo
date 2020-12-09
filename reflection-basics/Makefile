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

define make-native
native-image \
    -ea \
    $(main) \
    -H:+ReportExceptionStackTraces \
    -H:+PrintAnalysisCallTree \
    -J-agentlib:jdwp=transport=dt_socket,server=y,suspend=${1},address=8000 \
    --no-fallback \
    $(exec)
endef

run:
	./$(exec)

# all: clean build run-jvm native run

# all-debug: clean build run-jvm native-debug

# clean:
# 	rm -drf reports

# build:
# 	mvn clean package

#run-jvm:
#	mkdir -p META-INF/native-image
#	java -agentlib:native-image-agent=config-output-dir=META-INF/native-image -ea -jar target/example.jar

# run-jvm:
# 	java -ea -jar target/example.jar

# native:
# 	$(call make-native,n)

# native-debug:
# 	$(call make-native,y)

# run:
# 	target/example

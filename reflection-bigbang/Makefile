all: gen build run-jvm native run-native

small: gen-small build run-jvm native run-native

clean:
	mvn clean

gen:
	jbang ./reflection_poet.java

gen-small:
	jbang ./reflection_poet.java \
		--number-classes 200 \
		--number-fields 30

build:
	mvn package

run-jvm:
	java -ea -jar ./target/reflection-bigbang.jar

native:
	native-image \
		-ea \
		-jar ./target/reflection-bigbang.jar \
		--no-fallback \
		-H:+PrintAnalysisCallTree \
		-H:+ReportExceptionStackTraces \
		target/reflection-bigbang

run-native:
	./target/reflection-bigbang

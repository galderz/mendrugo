all: gen build run-jvm

clean:
	mvn clean

gen:
	jbang ./reflection_poet.java

build:
	mvn package

run-jvm:
	java -ea -jar ./target/reflection-bigbang.jar

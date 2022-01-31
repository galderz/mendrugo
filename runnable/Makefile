clean:
	mvn clean

build:
	mvn package

native:
	native-image -H:+PrintAnalysisCallTree -jar target/runnable.jar target/runnable

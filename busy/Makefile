all: clean build native

clean:
	mvn clean

build:
	mvn package

native:
	native-image -jar target/busy-0.1.jar target/busy

run:
	./target/busy

project = throwables

all: build native run

clean:
	rm -drf reports || true

build: clean
	mvn clean package -DskipTests

native: clean build
	native-image -jar target/$(project)-1.0-SNAPSHOT.jar target/$(project)

run:
	./target/$(project)

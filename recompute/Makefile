all: clean build native run

clean:
	rm -drf reports

build:
	mvn clean package

native:
	native-image \
	-jar target/recompute-1.0-SNAPSHOT.jar \
	--initialize-at-build-time=recompute \
	-H:+PrintImageObjectTree \
	target/recompute

run:
	target/recompute

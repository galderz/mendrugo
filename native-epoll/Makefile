project = native-epoll

all: build native run

clean:
	rm -drf reports || true

build: clean
	mvn clean package dependency:copy-dependencies -DskipTests

native: clean build
	native-image \
	-cp target/dependency/netty-transport-native-epoll-4.1.49.Final-linux-x86_64.jar:target/dependency/netty-native-epoll-1.0-SNAPSHOT.jar:target/dependency/netty-common-4.1.49.Final.jar:target/dependency/netty-transport-native-unix-common-4.1.49.Final.jar \
	--no-fallback \
	-H:+ReportExceptionStackTraces \
	-jar target/$(project)-1.0-SNAPSHOT.jar \
	target/$(project)

run:
	./target/$(project)

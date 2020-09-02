project = native-epoll

all: build native run

clean:
	rm -drf reports || true

build: clean
	mvn clean package dependency:copy-dependencies -DskipTests

native: clean build
	native-image \
	-jar target/$(project)-1.0-SNAPSHOT.jar \
	-cp target/dependency/netty-transport-native-epoll-4.1.49.Final-linux-x86_64.jar:target/dependency/netty-native-epoll-1.0-SNAPSHOT.jar:target/dependency/netty-common-4.1.49.Final.jar:target/dependency/netty-transport-native-unix-common-4.1.49.Final.jar \
	--initialize-at-build-time= \
	--enable-all-security-services \
	--no-fallback \
	--no-server \
	-J-Dfile.encoding=UTF-8 \
	-J-Dio.netty.allocator.maxOrder=1 \
	-J-Dio.netty.leakDetection.level=DISABLED \
	-J-Dsun.nio.ch.maxUpdateArraySize=100 \
	-J-Duser.language=en \
	-H:EnableURLProtocols=http,https \
	-H:FallbackThreshold=0 \
	-H:+JNI \
	-H:+ReportExceptionStackTraces \
	-H:+StackTrace \
	-H:-AddAllCharsets \
	-H:-UseServiceLoaderFeature \
	target/$(project)

run:
	./target/$(project)

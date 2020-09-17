netty = 4.1.52.Final-SNAPSHOT
project = epoll-jni

all: build native run

clean:
	rm -drf reports || true

build: clean
	mvn clean package dependency:copy-dependencies -DskipTests

native: clean build
	native-image \
	-jar target/$(project)-1.0-SNAPSHOT.jar \
	-cp target/dependency/netty-transport-native-epoll-$(netty)-linux-x86_64.jar:target/dependency/netty-native-epoll-$(netty).jar:target/dependency/netty-common-$(netty).jar:target/dependency/netty-transport-native-unix-common-$(netty).jar:target/dependency/netty-transport-$(netty).jar:target/dependency/netty-buffer-$(netty).jar \
	--initialize-at-build-time= \
	--enable-all-security-services \
	--no-fallback \
	--no-server \
	--verbose \
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
	-H:+TraceClassInitialization \
	-H:-AddAllCharsets \
	-H:-UseServiceLoaderFeature \
	target/$(project)

run:
	./target/$(project)
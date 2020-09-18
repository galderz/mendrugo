# SNAPSHOT required to make AbstractReferenceCounted available at build time
# This is needed because in this case the JNI library,
# and related classes, are loaded at build time.
NETTY ?= 4.1.52.Final-SNAPSHOT

project = epoll-jni-buildtime

classpath=target/dependency/netty-transport-native-epoll-$(NETTY)-linux-x86_64.jar:$\
	target/dependency/netty-common-$(NETTY).jar:$\
	target/dependency/netty-transport-native-unix-common-$(NETTY).jar:$\
	target/dependency/netty-transport-$(NETTY).jar:$\
	target/dependency/netty-buffer-$(NETTY).jar

all: build native run

clean:
	rm -drf reports || true
	mvn clean

build:
	mvn clean package dependency:copy-dependencies -DskipTests -Dnetty.version=$(NETTY)

native:
	native-image \
	-jar target/$(project)-1.0-SNAPSHOT.jar \
	-cp $(classpath) \
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

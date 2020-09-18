# SNAPSHOT required to make AbstractReferenceCounted available at build time
# This is needed to be able to make the required JNI registrations at build time
NETTY ?= 4.1.52.Final-SNAPSHOT

project = epoll-jni-runtime

classpath=target/dependency/netty-transport-native-epoll-$(NETTY)-linux-x86_64.jar:$\
	target/dependency/netty-common-$(NETTY).jar:$\
	target/dependency/netty-transport-native-unix-common-$(NETTY).jar:$\
	target/dependency/netty-transport-$(NETTY).jar:$\
	target/dependency/netty-buffer-$(NETTY).jar

# Push loading of epoll and related classes to runtime
runtime=io.netty.channel.epoll.Epoll,$\
	io.netty.channel.epoll.Native,$\
	io.netty.channel.unix.Errors,$\
	io.netty.channel.unix.Socket

all: build native run

clean:
	mvn clean

build:
	mvn clean package dependency:copy-dependencies -DskipTests -Dnetty.version=$(NETTY)

native:
	native-image \
	-jar target/$(project)-1.0-SNAPSHOT.jar \
	-cp $(classpath) \
	--initialize-at-build-time= \
	--initialize-at-run-time=$(runtime) \
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
	-H:IncludeResources=META-INF/native/libnetty_transport_native_epoll_x86_64.so \
	-H:+JNI \
	-H:+ReportExceptionStackTraces \
	-H:+StackTrace \
	-H:+TraceClassInitialization \
	-H:-AddAllCharsets \
	-H:-UseServiceLoaderFeature \
	target/$(project)

run:
	./target/$(project)

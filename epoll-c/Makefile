# SNAPSHOT required to access non-static native method definitions
NETTY ?= 4.1.52.Final-SNAPSHOT
NETTY_LIB_PATH ?= $(HOME)/1/netty/transport-native-epoll/target/static-libs

project = epoll-c

classpath=target/dependency/netty-buffer-$(NETTY).jar:$\
	target/dependency/netty-common-$(NETTY).jar:$\
	target/dependency/netty-transport-$(NETTY).jar:$\
	target/dependency/netty-transport-native-epoll-$(NETTY)-linux-x86_64.jar:$\
	target/dependency/netty-transport-native-unix-common-$(NETTY).jar

runtime=io.netty.channel.epoll.Epoll,$\
	io.netty.channel.epoll.Native,$\
	io.netty.channel.unix.Errors,$\
	io.netty.channel.unix.Socket

all: build native run

build:
	mvn clean package dependency:copy-dependencies -DskipTests -Dnetty.version=$(NETTY)

native: build
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
	-H:CLibraryPath=$(NETTY_LIB_PATH) \
	-H:EnableURLProtocols=http,https \
	-H:FallbackThreshold=0 \
	-H:+DumpTargetInfo \
	-H:+ReportExceptionStackTraces \
	-H:+StackTrace \
	-H:+TraceClassInitialization \
	-H:-AddAllCharsets \
	-H:-UseServiceLoaderFeature \
	target/$(project)

run:
	./target/$(project)

# SNAPSHOT required to access non-static native method definitions
NETTY ?= 4.1.52.Final-SNAPSHOT
NETTY_HOME = $(HOME)/shared/netty

TCPKALI = /opt/tcpkali/src/tcpkali
ECHO_BENCH = /opt/rust_echo_bench/target/release/echo_bench

nettylibs ?= $(NETTY_HOME)/transport-native-epoll/target/static-libs

project = epoll-c

classpath=target/dependency/netty-buffer-$(NETTY).jar:$\
	target/dependency/netty-common-$(NETTY).jar:$\
	target/dependency/netty-resolver-$(NETTY).jar:$\
	target/dependency/netty-transport-$(NETTY).jar:$\
	target/dependency/netty-transport-native-epoll-$(NETTY)-linux-x86_64.jar:$\
	target/dependency/netty-transport-native-unix-common-$(NETTY).jar

runtime=io.netty.channel.epoll.Epoll,$\
	io.netty.channel.epoll.EpollEventArray,$\
	io.netty.channel.epoll.EpollEventLoop,$\
	io.netty.channel.epoll.Native,$\
	io.netty.channel.unix.Errors,$\
	io.netty.channel.unix.IovArray,$\
	io.netty.channel.unix.Limits,$\
	io.netty.channel.unix.Socket

all: build native run-epoll-example

all-nio: build native run-nio-example

all-netty: build-netty build native run-epoll-example

all-jvm: build run-jvm-epoll-example run-jvm-nio-example

epoll-server: build-netty build native run-epoll-server

nio-server: build native run-nio-server

build-netty:
	pushd $(NETTY_HOME) && \
	mvn -DskipTests=true clean install -pl :netty-transport-native-epoll,:netty-transport-native-unix-common

build-netty-all:
	pushd $(NETTY_HOME) && \
	mvn -DskipTests=true clean install

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
	-H:CLibraryPath=$(nettylibs) \
	-H:EnableURLProtocols=http,https \
	-H:FallbackThreshold=0 \
	-H:+DumpTargetInfo \
	-H:+PrintAnalysisCallTree \
	-H:+ReportExceptionStackTraces \
	-H:+StackTrace \
	-H:+TraceClassInitialization \
	-H:-AddAllCharsets \
	-H:-UseServiceLoaderFeature \
	target/$(project)

run-epoll-example:
	./target/$(project) -Drun=epoll-example

run-nio-example:
	./target/$(project) -Drun=nio-example

run-epoll-server:
	./target/$(project) -Drun=epoll-server

run-nio-server:
	./target/$(project) -Drun=nio-server

run-jvm-epoll-example:
	java -cp $(classpath):target/$(project)-1.0-SNAPSHOT.jar -Drun=epoll-example epoll.c.Main

run-jvm-epoll-server:
	java -cp $(classpath):target/$(project)-1.0-SNAPSHOT.jar -Drun=epoll-server epoll.c.Main

run-jvm-nio-example:
	java -cp $(classpath):target/$(project)-1.0-SNAPSHOT.jar -Drun=nio-example epoll.c.Main

run-jvm-nio-server:
	java -cp $(classpath):target/$(project)-1.0-SNAPSHOT.jar -Drun=nio-server epoll.c.Main

perf-tcpkali:
	sudo sysctl -w net.ipv4.tcp_tw_reuse=1
	$(TCPKALI) \
	-m xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx \
	--connect-rate=200 \
	-c 500 \
	-T 30 \
	127.0.0.1:8081

perf-echo-bench:
	$(ECHO_BENCH) --address 'localhost:8081' --number 500 --duration 30 --length 128

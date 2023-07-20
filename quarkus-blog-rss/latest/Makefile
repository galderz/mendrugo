GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 7
GRAALVM_BUILD ?= 7.1

APP ?= getting-started
QUARKUS_VERSION ?= 3.2.1.Final
QUICKSTARTS_BRANCH ?= main
TOPDIR = ../..

JFR ?= true
JFR_FILE ?= $(APP)/target/recording.jfr
JFC_FILE ?= $(APP)/newtlab.jfc
RUN_ARGS += -XX:+FlightRecorder
RUN_ARGS += -XX:StartFlightRecording=settings=$(JFC_FILE),filename=$(JFR_FILE)

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/Quarkus.gmk

run: $(JFC_FILE)

$(JFC_FILE):
> $(jfr) configure jdk.ObjectAllocationInNewTLAB#enabled=true --output $@

print:
> $(jfr) print --events ObjectAllocationInNewTLAB --stack-depth 100 $(JFR_FILE)
.PHONY: print

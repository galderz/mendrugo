APP ?= code-with-quarkus
JAVA_HOME ?= /opt/graalvm-ce-java17-22.3.0
NB_ARGS ?= -H:+UseNewExperimentalClassInitialization,--initialize-at-run-time=org.acme.B_InitAtRunTime
QUARKUS_VERSION ?= 3.0.1.Final
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusBase.gmk

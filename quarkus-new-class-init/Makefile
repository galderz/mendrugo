APP ?= code-with-quarkus
NB_ARGS ?= -H:+UseNewExperimentalClassInitialization,--initialize-at-run-time=org.acme.B_InitAtRunTime
QUARKUS_VERSION ?= 3.0.1.Final

default: run

include ../make/common/MakeBase.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusBase.gmk

java_home := /opt/graalvm-ce-java17-22.3.0

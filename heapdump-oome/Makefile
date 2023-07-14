APP := .
jar := target/heapdump-oome.jar
pom = $(APP)/pom.xml

include ../make/common/MakeBase.gmk
include ../make/common/GraalVM.gmk
include ../make/common/Maven.gmk

jvm_opts += -XX:+HeapDumpOnOutOfMemoryError
jvm_opts += -XX:HeapDumpPath=target

# TODO move this to Maven makefile
$(jar): $(pom) $(sources)
> cd $(APP)
> $(mvn_package)

# TODO move this to Maven makefile
run-jvm: $(jar)
> $(java) $(jvm_opts) -cp $< org.example.Main
.PHONY: run-jvm

GRAALVM_FLAVOUR ?= jdk
GRAALVM_MAJOR ?= 17
GRAALVM_MINOR ?= 0
GRAALVM_MICRO ?= 8
GRAALVM_BUILD ?= 7.1

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
QUICKSTART_BRANCH ?= 3.2.3.Final
TOPDIR = ..

default: run

include $(TOPDIR)/make/common/MakeBase.gmk
include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/Quarkus.gmk
include $(TOPDIR)/make/common/Hyperfoil.gmk
include $(TOPDIR)/make/common/AsyncProfiler.gmk
include $(TOPDIR)/make/common/Numa.gmk
include $(TOPDIR)/make/common/Perf.gmk

greeting_resource_file = $(APP_DIR)/src/main/java/org/acme/getting/started/ReactiveGreetingResource.java
nonblocking_fix_marker = $(APP_DIR)/.nonblocking-fix.marker

$(jar): $(nonblocking_fix_marker)

$(nonblocking_fix_marker): $(greeting_resource_file)
> @sed -i.bak "/@Produces(MediaType.TEXT_PLAIN)/{N;N;/public String hello() {/s/@Produces(MediaType.TEXT_PLAIN)/@io.smallrye.common.annotation.NonBlocking\n    &/}" $<
> @echo "Differences between original and modified files:"
> @diff -U2 "$<.bak" $< || true
> touch $@

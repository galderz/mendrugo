TOPDIR = ..

include $(TOPDIR)/make/common/MakeBase.gmk

BOOT_JDK := $(opt)/java-21
JDK_REPO := jdk21u
GRAALVM_VERSION ?= 23.1

APP_DIR ?= quickstart
APP_NAME ?= getting-started-reactive
QUICKSTART_BRANCH ?= 3.2.3.Final

default: run

include $(TOPDIR)/make/common/GraalDev.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusApp.gmk

greeting_resource_file = $(APP_DIR)/src/main/java/org/acme/getting/started/ReactiveGreetingResource.java
nonblocking_fix_marker = $(APP_DIR)/.nonblocking-fix.marker

$(runner): $(nonblocking_fix_marker)

$(jar): $(nonblocking_fix_marker)

$(nonblocking_fix_marker): $(greeting_resource_file)
> @sed -i.bak "/@Produces(MediaType.TEXT_PLAIN)/{N;N;/public String hello() {/s/@Produces(MediaType.TEXT_PLAIN)/@io.smallrye.common.annotation.NonBlocking\n    &/}" $<
> @echo "Differences between original and modified files:"
> @diff -U2 "$<.bak" $< || true
> touch $@

TOPDIR = ..
include $(TOPDIR)/make/common/MakeBase.gmk

APP_DIR ?= quickstart
APP_NAME ?= getting-started
GRAALVM_HOME ?= $(JAVA_HOME)
JAVA_HOME ?= $(opt)/graalvm-25
RUN_ARGS ?=
QUARKUS_HOME ?= $(HOME)/src/quarkus-future-defaults
QUICKSTARTS_BRANCH ?= development

run_jar = $(target)/quarkus-app/quarkus-run.jar
project_pom = $(APP_DIR)/pom.xml
runner = $(target)/$(APP_NAME)-1.0.0-SNAPSHOT-runner
target = $(APP_DIR)/target

ifdef NATIVE_BUILD_ARGS
  native_build_args += -Dquarkus.native.additional-build-args=$(NATIVE_BUILD_ARGS)
endif

ifdef RUN_ARGS
  run_args += $(foreach arg,$(RUN_ARGS),$(arg))
endif

ifdef TMPDIR
  tmp_dir = $(TMPDIR)
else
  tmp_dir = /tmp
endif

default: run

include $(TOPDIR)/make/common/Graal.gmk
include $(TOPDIR)/make/common/Maven.gmk
include $(TOPDIR)/make/common/QuarkusDev.gmk

run: $(runner)
> $< $(run_args)
.PHONY: run

run-jvm: $(run_jar)
> $(GRAALVM_HOME)/bin/java -jar $<
.PHONY: run-jvm

build: $(runner)
.PHONY: build

$(runner): $(project_pom)
$(runner): $(quarkus_core_jar)
$(runner): $(quarkus_netty_jar)
$(runner): $(quarkus_resteasy_reactive_jar)
$(runner): $(quarkus_undertow_jar)
> cd $(APP_DIR)
> $(mvnw_package) -Dnative $(native_build_args)

quickstart: $(project_pom)
.PHONY: quickstart

$(project_pom):
> pushd $(tmp_dir)
> rm -drf quarkus-quickstarts || true
> git clone https://github.com/quarkusio/quarkus-quickstarts
> cd quarkus-quickstarts
> git checkout $(QUICKSTARTS_BRANCH)
> popd
> cp -r $(tmp_dir)/quarkus-quickstarts/$(APP_NAME) .
> mv $(APP_NAME) $(APP_DIR)

$(run_jar): $(project_pom)
> cd $(APP_DIR)
> $(mvnw_package)

clean-app:
> rm -drf $(APP_DIR)
.PHONY: clean-app

app := getting-started
java_home := /opt/crac-17
quarkus_version := 2.16.999-SNAPSHOT
quickstarts_branch := 2.16

default: build-quarkus run-jvm

include ../make/common/MakeBase.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusQuick.gmk
include ../make/common/QuarkusDev.gmk

crac_options += -Dquarkus.package.type=crac
ifdef CRAC_DEBUG
  crac_options += -Dquarkus.crac.checkpoint.debug=true
endif

build-crac: build-quarkus $(pom) $(sources)
> cd $(app)
> $(mvn_package) -Dquarkus.package.quiltflower.enabled $(crac_options)
.PHONY: build-crac

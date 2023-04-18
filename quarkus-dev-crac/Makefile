app := getting-started
java_home := /opt/crac-17
quarkus_version := 2.16.999-SNAPSHOT
quickstarts_branch := 2.16

default: checkpoint

include ../make/common/MakeBase.gmk
include ../make/common/Maven.gmk
include ../make/common/QuarkusQuick.gmk
include ../make/common/QuarkusDev.gmk

crac_options += -Dquarkus.package.type=crac
ifdef CRAC_DEBUG
  crac_options += -Dquarkus.crac.checkpoint.debug=true
endif

checkpoint_dir = $(target)/checkpoint
checkpoint_log = $(checkpoint_dir)/dump4.log

checkpoint: $(checkpoint_log)
.PHONY: checkpoint

restore: $(checkpoint_log)
> sudo $(java) -XX:CRaCRestoreFrom=$(checkpoint_dir)
.PHONY: restore

$(checkpoint_log): $(quarkus_jar_core) $(pom) $(sources)
> rm -drf $(checkpoint_dir)
> cd $(app)
> $(mvn_package) -Dquarkus.package.quiltflower.enabled $(crac_options)

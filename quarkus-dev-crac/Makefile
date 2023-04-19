app := getting-started
java_home := /opt/crac-17
quarkus_version := 2.16.999-SNAPSHOT
quickstarts_branch := 2.16

default: checkpoint

include ../make/common/MakeBase.gmk
include ../make/common/Colima.gmk
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

# sudo not needed, as long as the JDK was extracted as sudo:
# https://github.com/CRaC/docs#jdk
restore: $(checkpoint_log)
> $(java) -XX:CRaCRestoreFrom=$(checkpoint_dir)
.PHONY: restore

$(checkpoint_log): $(quarkus_jar_core) $(pom) $(sources)
> rm -drf $(checkpoint_dir)
> cd $(app)
> $(mvn_package) -Dquarkus.package.quiltflower.enabled $(crac_options)

crac-image:
> docker build -t crac:v1 .
.PHONY: crac-image

crac: crac-image
> docker run -t -i -v $(PWD)/$(app):/data --rm -p 8080:8080 crac:v1 /bin/bash
.PHONY: crac

get-crac:
> cd /opt
> wget https://github.com/CRaC/openjdk-builds/releases/download/17-crac%2B5/openjdk-17-crac+5_linux-x64.tar.gz
> sudo tar vxzf openjdk-17-crac+5_linux-x64.tar.gz
> ln -s openjdk-17-crac+5_linux-x64 crac-17
.PHONY: get-crac

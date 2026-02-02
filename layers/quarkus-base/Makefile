ID ?= layers
TOPDIR = ../..

default: run

include $(TOPDIR)/make/MakeBase.gmk
include $(TOPDIR)/make/Quarkus.gmk

app_dir = getting-started
app_pom = $(app_dir)/pom.xml
base_nil = target/libquarkusbaselayer.nil
base_so = target/libquarkusbaselayer.so
build_layer_base = build-layer-base.sh
build_non_layered = build-non-layered.sh
runner_dir = $(app_dir)/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar
runner_jar = $(runner_dir)/getting-started-1.0.0-SNAPSHOT-runner.jar

run: $(base_nil)

$(base_nil): $(runner_jar)
$(base_nil): $(build_layer_base)
> ./$(build_layer_base)

$(runner_jar): $(app_pom)
> cd $(app_dir)
> ../$(build_non_layered)

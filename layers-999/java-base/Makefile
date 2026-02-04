ID ?= layers
TOPDIR = ../..

default: run

include $(TOPDIR)/make/MakeBase.gmk
include $(TOPDIR)/make/Quarkus.gmk

app_dir = getting-started
app_pom = $(app_dir)/pom.xml
base_nil = target/libjavabaselayer.nil
base_so = target/libjavabaselayer.so
build_layer_app = build-layer-app.sh
build_layer_base = build-layer-base.sh
build_non_layered = build-non-layered.sh
java_home = $(HOME)/src/mandrel/sdk/latest_graalvm_home
runner_base_so = $(runner_dir)/libjavabaselayer.so
runner_dir = $(app_dir)/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar
runner_jar = $(runner_dir)/getting-started-1.0.0-SNAPSHOT-runner.jar
runner_name = getting-started-1.0.0-SNAPSHOT-runner
runner = $(runner_dir)/$(runner_name)

run: $(runner_base_so)
run: $(runner)
> cd $(runner_dir)
> ./$(runner_name)
.PHONY: run-layers

$(runner): $(runner_jar)
$(runner): $(base_nil)
$(runner): $(build_app)
> cd $(runner_dir)
> ../../../$(build_layer_app)

$(runner_base_so): $(base_so)
> cp $< $@

$(base_nil): $(build_layer_base)
> ./$(build_layer_base)

$(runner_jar): $(app_pom)
> cd $(app_dir)
> ../$(build_non_layered)

ID ?= layers
TOPDIR = ../..

default: run

include $(TOPDIR)/make/MakeBase.gmk
include $(TOPDIR)/make/Quarkus.gmk

app_dir = getting-started
app_pom = $(app_dir)/pom.xml
base_nil = target/libnettybaselayer.nil
build_layer_app = build-layer-app.sh
build_layer_base = build-layer-base.sh
build_non_layered = build-non-layered.sh
runner = target/getting-started-1.0.0-SNAPSHOT-runner
runner_dir = $(app_dir)/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar
runner_jar = $(runner_dir)/getting-started-1.0.0-SNAPSHOT-runner.jar

run: $(runner)
> LD_LIBRARY_PATH=target $<

$(runner): $(base_nil)
$(runner): $(build_layer_app)
> ./$(build_layer_app)

$(base_nil): $(runner_jar)
$(base_nil): $(build_layer_base)
> ./$(build_layer_base)

$(runner_jar): $(app_pom)
> cd $(app_dir)
> ../$(build_non_layered)

clean:
> rm -drf target
> cd $(app_dir)
> rm -drf target
.PHONY: clean

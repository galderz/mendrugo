ID ?= layers
TOPDIR = ../..

default: run

include $(TOPDIR)/make/MakeBase.gmk
include $(TOPDIR)/make/Quarkus.gmk

app_dir = getting-started
app_pom = $(app_dir)/pom.xml
base_nil = target/libquarkusbaselayer.nil
base_so = target/libquarkusbaselayer.so
build_layer_app = build-layer-app.sh
build_layer_base = build-layer-base.sh
build_non_layered = build-non-layered.sh
extracted_jar = $(runner_dir)/extracted-classes.jar
layer_packages = target/layer-packages.txt
runner_dir = $(app_dir)/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar
runner_jar = $(runner_dir)/getting-started-1.0.0-SNAPSHOT-runner.jar
runner = $(target)/getting-started-1.0.0-SNAPSHOT-runner
scan_packages = scan-packages.sh
split_jar = split-jar.sh

run: $(runner)

$(runner): $(base_nil)
$(runner): $(build_layer_app)
> ./$(build_layer_app)

$(base_nil): $(layer_packages)
$(base_nil): $(build_layer_base)
> ./$(build_layer_base)

$(layer_packages): $(runner_jar)
$(layer_packages): $(scan_packages)
> ./$(scan_packages) -p io.netty,io.vertx,io.quarkus,jakarta -e io.netty.handler.ssl

$(extracted_jar): $(split_jar)
$(extracted_jar): $(runner_jar)
> ./$(split_jar)

$(runner_jar): $(app_pom)
> cd $(app_dir)
> ../$(build_non_layered)

clean:
> rm -drf target
> cd $(app_dir)
> rm -drf target
.PHONY: clean

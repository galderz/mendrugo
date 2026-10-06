TOPDIR = ..

default: build

include $(TOPDIR)/make/MakeBase.gmk

app_dir = native/getting-started
app_pom = $(app_dir)/pom.xml
base_nil = target/libquarkusbaselayer.nil
base_so = target/libquarkusbaselayer.so
build_layer_app = build-layer-app.sh
build_layer_base = build-layer-base.sh
build_non_layered = build-non-layered.sh
runner = target/getting-started-1.0.0-SNAPSHOT-runner
runner_dir = $(app_dir)/target/getting-started-1.0.0-SNAPSHOT-native-image-source-jar
runner_jar = $(runner_dir)/getting-started-1.0.0-SNAPSHOT-runner.jar

build_args =

ifeq ($(DEBUG),true)
  build_args += --with-debug=true
endif

build: $(runner)

run: $(runner)
> LD_LIBRARY_PATH=target $<

$(runner): $(base_nil)
$(runner): $(build_layer_app)
> ./$(build_layer_app) $(build_args)

$(base_nil): $(runner_jar)
$(base_nil): $(build_layer_base)
> ./$(build_layer_base) $(build_args)

$(runner_jar): $(app_pom)
> ./$(build_non_layered) $(build_args)

clean:
> rm -drf target
> cd $(app_dir)
> rm -drf target
.PHONY: clean

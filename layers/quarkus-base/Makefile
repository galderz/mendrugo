ID ?= layers
TOPDIR = ../..

default: $(base_nil)

include $(TOPDIR)/make/MakeBase.gmk
include $(TOPDIR)/make/Quarkus.gmk

base_nil = target/libquarkusbaselayer.nil
base_so = target/libquarkusbaselayer.so
build_layer_base = build-layer-base.sh

$(base_nil): $(build_layer_base)
> ./$(build_layer_base)

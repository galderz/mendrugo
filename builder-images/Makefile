SHELL := bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c
.DELETE_ON_ERROR:
MAKEFLAGS += --warn-undefined-variables
MAKEFLAGS += --no-builtin-rules

ifeq ($(origin .RECIPEPREFIX), undefined)
  $(error This Make does not support .RECIPEPREFIX. Please use GNU Make 4.0 or later)
endif
.RECIPEPREFIX = >

graalvm_builder_dockerfile = Dockerfile.graalvm
graalvm_sources += $(shell find $(mandrel)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
graalvm_image_name = galderz/graalvm-builder
graalvm_version := 23.0.0
mandrel := /opt/mandrel-mandrel
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
native_image = $(graalvm_home)/bin/native-image
quarkus_builder_dockerfile = Dockerfile.builder
quarkus_builder_name = galderz/ubi-quarkus-graalvmce-builder-image:23.0.0-java17
quarkus_builder_stamp = target/quarkus-builder.stamp
target_graalvm_home := target/latest_graalvm_home

$(quarkus_builder_stamp): $(target_graalvm_home) $(quarkus_builder_dockerfile)
> docker build -f Dockerfile.quarkus-builder -t $(quarkus_builder_name) .
> docker run $(quarkus_builder_name) --expert-options-all
> touch $@

$(target_graalvm_home): $(native_image) target
> sudo chown -R g $(graalvm_home)
> mkdir -p target/latest_graalvm_home
> cp -r $(graalvm_home)/* target/latest_graalvm_home

graalvm-builder:
> docker build -f Dockerfile.graalvm-builder -t $(graalvm_image_name) .
> docker run -t -i --rm -v $(HOME):/data $(graalvm_image_name)
.PHONY: graalvm-builder

target:
> mkdir -p target

docker-daemon:
> colima start --cpu 4 --memory 8
.PHONY: docker-daemon

clean:
> rm -drf target
.PHONY: clean

clean-all:
> cd $(mandrel)
> git clean -dfx
.PHONY: clean-all

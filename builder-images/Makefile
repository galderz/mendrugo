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

docker_builder_stamp = target/builder.stamp
docker_image_name = galderz/ubi-quarkus-graalvmce-builder-image:23.0.0-java17
dockerfile_builder = Dockerfile.builder
graalvm_builder := /opt/graalbuilder-17
graalvm_sources += $(shell find $(mandrel)/ -type f -name '*.java' | sed 's: :\\ :g')
graalvm_home := /opt/mandrel-mandrel/sdk/latest_graalvm_home
graalvm_version := 23.0.0
mandrel := /opt/mandrel-mandrel
mx += JAVA_HOME=$(graalvm_builder)
mx += /opt/mx/mx
native_image = $(graalvm_home)/bin/native-image
target_graalvm_home := target/latest_graalvm_home

$(docker_builder_stamp): $(target_graalvm_home) $(dockerfile_builder)
> docker build -f Dockerfile.builder -t $(docker_image_name) .
> docker run $(docker_image_name) --expert-options-all
> touch $@

$(target_graalvm_home): $(native_image) target
> mkdir -p target/latest_graalvm_home
> cp -r $(graalvm_home)/* target/latest_graalvm_home

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

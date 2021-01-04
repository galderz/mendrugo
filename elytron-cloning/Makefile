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

JAVA_FILES = $(shell find src/ -type f -name '*.java')

NATIVEFLAGS += -ea
NATIVEFLAGS += --allow-incomplete-classpath
NATIVEFLAGS += --enable-all-security-services
NATIVEFLAGS += --no-fallback

run-native: target/main
> ./target/main

target/main: run-jar
> native-image -jar target/main.jar ${NATIVEFLAGS} target/main

run-jar: target/main.jar
> java -ea -jar target/main.jar

target/main.jar: pom.xml $(JAVA_FILES)
> mvn package

clean:
> mvn clean

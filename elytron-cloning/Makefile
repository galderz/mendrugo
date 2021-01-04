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

NATIVEFLAGS += --allow-incomplete-classpath
NATIVEFLAGS += --no-fallback

target/main: run-jar
> native-image -jar target/main.jar ${NATIVEFLAGS} target/main

run-jar: target/main.jar
> java -ea -jar target/main.jar

target/main.jar: pom.xml src/main/java/org/example/elytron/Main.java
> mvn package

clean:
> mvn clean

#!/usr/bin/env bash

set -e

mvn clean package
java -jar target/resources-1.0-SNAPSHOT.jar
native-image --no-fallback -cp target/resources-1.0-SNAPSHOT.jar org.sample.resources.ReadResource target/resources

#!/usr/bin/env bash

set -e

(/usr/bin/time -v $(dirname $0)/native-image.link "$@") |& tee -a /tmp/times.log

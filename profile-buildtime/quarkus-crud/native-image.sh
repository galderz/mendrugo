#!/usr/bin/env bash

(/usr/bin/time -v $(dirname $0)/native-image.real "$@") |& tee -a /tmp/times.log

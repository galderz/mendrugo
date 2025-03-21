#!/bin/bash

set -eux

ID=mandrel-base QB=3.19.3 APP_DIR=compiler-base make clean-graal clean-app
ID=mandrel-base QB=3.19.3 APP_DIR=compiler-base make build
NIR_EVENT=branches,branch-misses,instructions,cycles,stalled-cycles-frontend ID=mandrel-base QB=3.19.3 APP_DIR=compiler-base make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel-base QB=3.19.3 APP_DIR=compiler-base make ni-record

ID=mandrel QB=3.19.3 APP_DIR=compiler-patch make clean-graal clean-app
ID=mandrel QB=3.19.3 APP_DIR=compiler-patch make build
NIR_EVENT=branches,branch-misses,instructions,cycles,stalled-cycles-frontend ID=mandrel QB=3.19.3 APP_DIR=compiler-patch make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel QB=3.19.3 APP_DIR=compiler-patch make ni-record

#!/bin/bash

set -eux

ID=mandrel QB=3.19.3 make clean-graal clean-app
ID=mandrel QB=3.19.3 make build
NIR_EVENT=branches,branch-misses,instructions,cycles,stalled-cycles-frontend ID=mandrel QB=3.19.3 make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel QB=3.19.3 make ni-record

ID=mandrel-base QB=3.19.3 make clean-graal clean-app
ID=mandrel-base QB=3.19.3 make build
NIR_EVENT=branches,branch-misses,instructions,cycles,stalled-cycles-frontend ID=mandrel-base QB=3.19.3 make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel-base QB=3.19.3 make ni-record

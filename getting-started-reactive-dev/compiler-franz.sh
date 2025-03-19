#!/bin/bash

set -eux

ID=mandrel make clean
ID=mandrel make
NIR_EVENT=branch-misses:pp ID=mandrel make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel make ni-record

ID=mandrel-base make clean
ID=mandrel-base make
NIR_EVENT=branch-misses:pp ID=mandrel-base make ni-stat
NIR_EVENT=branch-misses:pp ID=mandrel-base make ni-record

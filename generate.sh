#!/bin/bash

SRCLOC="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm -rf "$SRCLOC/java"
mkdir -p "$SRCLOC/java/net/osmand/core"

rm -rf "$SRCLOC/src"
mkdir -p "$SRCLOC/src"

swig -java -package net.osmand.core -outdir "$SRCLOC/java/net/osmand/core" -o "$SRCLOC/src/swig.cpp" -I"$SRCLOC/../core/include" -c++ -v "$SRCLOC/../core/core.swig"

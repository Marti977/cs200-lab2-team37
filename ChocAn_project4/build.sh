#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ -n "${JAVA_HOME:-}" ]; then compiler="$JAVA_HOME/bin/javac"; else compiler=javac; fi
mkdir -p build/classes
"$compiler" --release 17 -Xlint:all -d build/classes src/chocan/*.java

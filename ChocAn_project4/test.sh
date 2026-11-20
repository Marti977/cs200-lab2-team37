#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
if [ -n "${JAVA_HOME:-}" ]; then compiler="$JAVA_HOME/bin/javac"; runtime="$JAVA_HOME/bin/java"; else compiler=javac; runtime=java; fi
mkdir -p build/test-classes
"$compiler" --release 17 -Xlint:all -cp build/classes -d build/test-classes tests/chocan/*.java
"$runtime" -cp build/classes:build/test-classes chocan.RegressionTest

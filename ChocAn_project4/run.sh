#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
if [ -n "${JAVA_HOME:-}" ]; then runtime="$JAVA_HOME/bin/java"; else runtime=java; fi
exec "$runtime" -cp build/classes chocan.Main

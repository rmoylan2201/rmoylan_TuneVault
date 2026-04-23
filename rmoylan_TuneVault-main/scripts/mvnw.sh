#!/usr/bin/env bash
# Run Maven Wrapper with JAVA_HOME set on macOS when the JDK is installed but not on PATH
# (e.g. some IDE/agent shells). Usage: ./scripts/mvnw.sh compile
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [[ -z "${JAVA_HOME:-}" ]] && [[ -x /usr/libexec/java_home ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21 2>/dev/null || /usr/libexec/java_home 2>/dev/null || true)"
fi
exec ./mvnw "$@"

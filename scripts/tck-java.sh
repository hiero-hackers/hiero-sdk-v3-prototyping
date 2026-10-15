#!/usr/bin/env bash
# Runs the Hiero TCK against the generated Java TCK server.
#
# Generates the API and the server, builds both and hands over to tooling/tck/run-tck.sh, which
# starts the server and runs the TCK against a local Solo network. Everything after the options is
# passed on as the list of TCK test files; without one the complete suite runs.
#
# usage: scripts/tck-java.sh [--skip-build] [TCK test file ...]
#
# environment:
#   TCK_DIR          a clone of https://github.com/hiero-ledger/hiero-sdk-tck with 'npm install' done (required)
#   TCK_ENV          the TCK configuration (default: tooling/tck/solo.env, a local Solo network)
#   TCK_SERVER_LOG   error (default) | debug | off - how much the server logs about failed calls

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

skip_build=0
if [[ "${1:-}" == "--skip-build" ]]; then
  skip_build=1
  shift
fi

# checked here and not only in run-tck.sh, so that a missing clone does not surface after the build
[[ -n "${TCK_DIR:-}" && -f "$TCK_DIR/package.json" ]] \
  || fail "TCK_DIR must point to a clone of hiero-sdk-tck (with 'npm install' done)"

if (( ! skip_build )); then
  "$(dirname "${BASH_SOURCE[0]}")/generate-java.sh"

  log "generating the Java TCK contract and server into sdk-java/tck/generated"
  metalang tck generate --language=java --fail-on=never \
    --config="$repository/sdk-java/generator.properties" \
    --bindings="$repository/tooling/tck/bindings" \
    --output="$repository/sdk-java/tck/generated" \
    "$repository/spec"

  JAVA_HOME="$(jdk_home "$repository/sdk-java")"
  export JAVA_HOME
  cd "$repository/sdk-java"

  log "building the TCK contract, the runtime and the server"
  ./mvnw -f tck/generated/contract install
  ./mvnw -f tck/runtime install
  # clean: an incremental package keeps the Class-Path of the previous manifest
  ./mvnw -f tck/generated/server clean package
fi

log "running the TCK"
exec "$repository/tooling/tck/run-tck.sh" java "$@"

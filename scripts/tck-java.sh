#!/usr/bin/env bash
# Runs the Hiero TCK against the generated Java TCK server.
#
# Generates the API and the server, builds both and hands over to tooling/tck/run-tck.sh, which
# starts the server and runs the TCK against a local Solo network. Everything after the options is
# passed on as the list of TCK test files; without one the complete suite runs.
#
# usage: scripts/tck-java.sh [--skip-build | --skip-api] [--ignore-spec-errors] [TCK test file ...]
#
#   --skip-build  build nothing, run the TCK against the server built before
#   --skip-api    do not generate the Java API and do not install sdk-java/impl (the Maven profile `tck` of
#                 sdk-java/impl does that itself), only generate and build the TCK contract, runtime and server
#
# environment:
#   TCK_DIR          a clone of https://github.com/hiero-ledger/hiero-sdk-tck with 'npm install' done (required)
#   TCK_ENV          the TCK configuration (default: tooling/tck/solo.env, a local Solo network)
#   TCK_SERVER_LOG   error (default) | debug | off - how much the server logs about failed calls
#   TCK_IGNORE_FAILURES  true: failed TCK tests do not fail the script (see tooling/tck/run-tck.sh)

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

skip_build=0
skip_api=0
generate_options=""
while (( $# > 0 )); do
  case "$1" in
    --skip-build) skip_build=1 ;;
    --skip-api) skip_api=1 ;;
    --ignore-spec-errors) fail_on="never"; generate_options="--ignore-spec-errors" ;;
    *) break ;;
  esac
  shift
done

# checked here and not only in run-tck.sh, so that a missing clone does not surface after the build
[[ -n "${TCK_DIR:-}" && -f "$TCK_DIR/package.json" ]] \
  || fail "TCK_DIR must point to a clone of hiero-sdk-tck (with 'npm install' done)"
# absolute: the build below changes the working directory before run-tck.sh reads TCK_DIR
TCK_DIR="$(cd "$TCK_DIR" && pwd)"
export TCK_DIR

if (( ! skip_build )); then
  if (( ! skip_api )); then
    "$(dirname "${BASH_SOURCE[0]}")/generate-java.sh" $generate_options
  fi

  log "generating the Java TCK contract and server into sdk-java/tck/generated"
  metalang tck generate --language=java --fail-on="$fail_on" \
    --config="$repository/sdk-java/generator.properties" \
    --bindings="$repository/tooling/tck/bindings" \
    --output="$repository/sdk-java/tck/generated" \
    "$repository/spec"

  JAVA_HOME="$(jdk_home "$repository/sdk-java")"
  export JAVA_HOME
  cd "$repository/sdk-java"

  if (( ! skip_api )); then
    # sdk-java/impl has the coordinates of the generated modules; installed after them, it replaces the stubs
    log "building the implementation (sdk-java/impl)"
    ./mvnw -f impl install -DskipTests
  fi

  log "building the TCK contract, the runtime and the server"
  ./mvnw -f tck/generated/contract install
  ./mvnw -f tck/runtime install
  # clean: an incremental package keeps the Class-Path of the previous manifest
  ./mvnw -f tck/generated/server clean package
fi

log "running the TCK"
exec "$repository/tooling/tck/run-tck.sh" java "$@"

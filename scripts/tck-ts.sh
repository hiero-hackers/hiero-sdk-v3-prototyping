#!/usr/bin/env bash
# Runs the Hiero TCK against the generated TypeScript TCK server.
#
# Generates the API and the server, builds both and hands over to tooling/tck/run-tck.sh, which
# starts the server and runs the TCK against a local Solo network. Everything after the options is
# passed on as the list of TCK test files; without one the complete suite runs.
#
# usage: scripts/tck-ts.sh [--skip-build] [--ignore-spec-errors] [TCK test file ...]
#
# environment:
#   TCK_DIR          a clone of https://github.com/hiero-ledger/hiero-sdk-tck with 'npm install' done (required)
#   TCK_ENV          the TCK configuration (default: tooling/tck/solo.env, a local Solo network)
#   TCK_SERVER_LOG   error (default) | debug | off - how much the server logs about failed calls

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

skip_build=0
generate_options=""
while (( $# > 0 )); do
  case "$1" in
    --skip-build) skip_build=1 ;;
    --ignore-spec-errors) fail_on="never"; generate_options="--ignore-spec-errors" ;;
    *) break ;;
  esac
  shift
done

# checked here and not only in run-tck.sh, so that a missing clone does not surface after the build
[[ -n "${TCK_DIR:-}" && -f "$TCK_DIR/package.json" ]] \
  || fail "TCK_DIR must point to a clone of hiero-sdk-tck (with 'npm install' done)"

if (( ! skip_build )); then
  "$(dirname "${BASH_SOURCE[0]}")/generate-ts.sh" $generate_options

  log "generating the TypeScript TCK contract and server into sdk-ts/tck/generated"
  metalang tck generate --language=ts --fail-on="$fail_on" \
    --config="$repository/sdk-ts/generator.properties" \
    --bindings="$repository/tooling/tck/bindings" \
    --output="$repository/sdk-ts/tck/generated" \
    "$repository/spec"

  # the generated contract and server are workspaces of sdk-ts and have to be linked before tsc runs
  log "installing the npm workspace sdk-ts"
  npm --prefix "$repository/sdk-ts" install

  log "building the TCK runtime and the server"
  npm --prefix "$repository/sdk-ts" run build:tck-ts
fi

log "running the TCK"
exec "$repository/tooling/tck/run-tck.sh" ts "$@"

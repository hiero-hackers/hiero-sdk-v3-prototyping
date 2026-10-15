#!/usr/bin/env bash
# Generates the TypeScript API from the specs into sdk-ts/generated and builds it.
#
# `npm install` runs in the workspace root sdk-ts, which links the hand-written @hiero/support
# package to the generated packages; the build itself is the `build` script of the generated
# workspace root (tsc --build).
#
# usage: scripts/generate-ts.sh [--no-build] [--test]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the TypeScript API into sdk-ts/generated"
metalang generate --language=ts --fail-on=never \
  --config="$repository/sdk-ts/generator.properties" \
  --output="$repository/sdk-ts/generated" \
  "$repository/spec"

if (( build )); then
  command -v npm >/dev/null || fail "npm is needed (Node.js 22, see the toolchain section of README.md)"

  log "installing the npm workspace sdk-ts"
  npm --prefix "$repository/sdk-ts" install

  log "compiling the generated packages"
  npm --prefix "$repository/sdk-ts/generated" run build

  if (( run_tests )); then
    log "running the generated tests (stubs fail by design)"
    npm --prefix "$repository/sdk-ts/generated" test || true
  fi
fi

log "done: sdk-ts/generated"

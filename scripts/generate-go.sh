#!/usr/bin/env bash
# Generates the Go API from the specs into sdk-go/generated and checks it.
#
# The Go generator emits the declared types only - no methods, functions or tests yet - so there is
# nothing to run; what has to hold is that the module builds, vets clean and is already formatted
# the way gofmt would format it. `--test` is accepted and does nothing.
#
# usage: scripts/generate-go.sh [--no-build] [--test] [--ignore-spec-errors]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the Go API into sdk-go/generated"
generate_api go sdk-go

if (( build )); then
  command -v go >/dev/null || fail "go is needed (Go 1.27 or newer, see https://go.dev/dl/)"
  cd "$repository/sdk-go/generated"

  log "go build ./..."
  go build ./...

  log "go vet ./..."
  go vet ./...

  log "gofmt -l . (has to print nothing)"
  unformatted="$(gofmt -l .)"
  [[ -z "$unformatted" ]] || fail "not formatted as gofmt would:"$'\n'"$unformatted"
fi

log "done: sdk-go/generated"

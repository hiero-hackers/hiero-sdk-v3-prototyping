#!/usr/bin/env bash
# Generates the Python API from the specs into sdk-python/generated and checks it.
#
# Like the Go one the generator emits the declared types only, so there is nothing to run; the check
# is `mypy --strict`. PYTHON=<interpreter> selects one, the default is python3.
#
# usage: scripts/generate-python.sh [--no-build] [--test]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the Python API into sdk-python/generated"
metalang generate --language=python --fail-on=never \
  --config="$repository/sdk-python/generator.properties" \
  --output="$repository/sdk-python/generated" \
  "$repository/spec"

if (( build )); then
  python="${PYTHON:-python3}"
  command -v "$python" >/dev/null || fail "$python not found (Python 3.12 or newer is needed; set PYTHON=<interpreter>)"
  if ! "$python" -m mypy --version >/dev/null 2>&1; then
    fail "$python has no mypy ('$python -m pip install mypy', or set PYTHON=<interpreter with mypy>)"
  fi

  log "mypy --strict src"
  cd "$repository/sdk-python/generated"
  "$python" -m mypy --strict src
fi

log "done: sdk-python/generated"

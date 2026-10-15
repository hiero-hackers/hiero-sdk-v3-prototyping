#!/usr/bin/env bash
# Generates the Python API from the specs into sdk-python/generated and checks it.
#
# Like the Go one the generator emits the declared types only, so there is nothing to run; the check
# is `mypy --strict`. mypy is not expected to be installed: if the interpreter has none, the script
# creates the virtual environment sdk-python/.venv (git-ignored) and installs it there once.
#
# usage: scripts/generate-python.sh [--no-build] [--test]
#
# environment:
#   PYTHON             the interpreter (default: python3); Python 3.12 or newer
#   SKIP_TYPE_CHECK=1  generate and skip mypy

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the Python API into sdk-python/generated"
metalang generate --language=python --fail-on=never \
  --config="$repository/sdk-python/generator.properties" \
  --output="$repository/sdk-python/generated" \
  "$repository/spec"

if (( build )); then
  python="${PYTHON:-python3}"
  command -v "$python" >/dev/null \
    || fail "$python not found. Install Python 3.12 or newer, or set PYTHON=<interpreter>."
  "$python" -c 'import sys; sys.exit(0 if sys.version_info >= (3, 12) else 1)' \
    || fail "$python is $("$python" -c 'import platform; print(platform.python_version())'), the API needs Python 3.12 or newer (PEP 695 generics)"

  if [[ "${SKIP_TYPE_CHECK:-0}" == "1" ]]; then
    log "skipping mypy (SKIP_TYPE_CHECK=1)"
  else
    if ! "$python" -m mypy --version >/dev/null 2>&1; then
      venv="$repository/sdk-python/.venv"
      if [[ ! -x "$venv/bin/python" ]]; then
        log "$python has no mypy - creating the virtual environment sdk-python/.venv"
        "$python" -m venv "$venv" \
          || fail "could not create sdk-python/.venv. Install mypy yourself, set PYTHON=<interpreter with mypy> or skip the check with SKIP_TYPE_CHECK=1."
      fi
      if ! "$venv/bin/python" -m mypy --version >/dev/null 2>&1; then
        log "installing mypy into sdk-python/.venv"
        "$venv/bin/python" -m pip install --quiet mypy \
          || fail "could not install mypy into sdk-python/.venv (no network?). Install it yourself, set PYTHON=<interpreter with mypy> or skip the check with SKIP_TYPE_CHECK=1."
      fi
      python="$venv/bin/python"
    fi

    log "mypy --strict src"
    cd "$repository/sdk-python/generated"
    "$python" -m mypy --strict src
  fi
fi

log "done: sdk-python/generated"

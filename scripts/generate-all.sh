#!/usr/bin/env bash
# Generates the API of every target language and builds it.
#
# Every language is attempted even if an earlier one failed; the summary at the end names what
# failed and the exit code is 1 in that case. The options are passed on to the single scripts.
#
# usage: scripts/generate-all.sh [--no-build] [--test]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

ensure_cli   # build it once instead of in the first language

scripts_directory="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
languages=(java ts rust go python)
failed=""   # a string, not an array: bash 3.2 (macOS) trips over an empty array under "set -u"

for language in "${languages[@]}"; do
  log "### $language"
  if "$scripts_directory/generate-$language.sh" "$@"; then
    continue
  fi
  failed="$failed $language"
done

log "summary"
for language in "${languages[@]}"; do
  if [[ " $failed " == *" $language "* ]]; then
    printf '  %-7s FAILED\n' "$language"
  else
    printf '  %-7s ok\n' "$language"
  fi
done

[[ -z "$failed" ]] || exit 1

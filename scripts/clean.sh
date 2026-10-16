#!/usr/bin/env bash
# Removes the generated code: every sdk-<lang>/generated tree and the two generated TCK parts.
#
# Nothing of it is under version control, and scripts/generate-*.sh produces all of it again, so
# this is always safe. The hand-written modules (sdk-java/support, sdk-java/protobuf, sdk-ts/support,
# the two TCK runtimes) are never touched.
#
# usage: scripts/clean.sh [--build-output] [--caches] [--all] [--maven-repository] [--dry-run]
#
#   --build-output      also the compiled output of the hand-written modules and the metalang CLI jar
#   --caches            also sdk-ts/node_modules, sdk-ts/package-lock.json and sdk-python/.venv
#                       (restoring them needs network access)
#   --all               --build-output and --caches
#   --maven-repository  also the artifacts installed into ~/.m2/repository/org/hiero. Outside the
#                       repository and therefore not part of --all.
#   --dry-run           only print what would be removed

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

build_output=0
caches=0
maven_repository=0
dry_run=0
usage="usage: $(basename "$0") [--build-output] [--caches] [--all] [--maven-repository] [--dry-run]"

while (( $# > 0 )); do
  case "$1" in
    --build-output) build_output=1 ;;
    --caches) caches=1 ;;
    --all) build_output=1; caches=1 ;;
    --maven-repository) maven_repository=1 ;;
    --dry-run | -n) dry_run=1 ;;
    -h | --help) echo "$usage"; exit 0 ;;
    *) fail "unknown option '$1'. $usage" ;;
  esac
  shift
done

targets=""
add() {
  local path
  for path in "$@"; do
    [[ -e "$path" ]] || continue
    targets="$targets$path"$'\n'
  done
}

# the generated code
add "$repository"/sdk-*/generated "$repository"/sdk-*/tck/generated

if (( build_output )); then
  add "$repository"/tooling/metalang/target "$repository"/tooling/metalang/metalang-*/target
  add "$repository"/sdk-java/support/target "$repository"/sdk-java/protobuf/target \
      "$repository"/sdk-java/tck/runtime/target
  add "$repository"/sdk-ts/support/dist "$repository"/sdk-ts/support/tsconfig.tsbuildinfo \
      "$repository"/sdk-ts/test-support/dist "$repository"/sdk-ts/test-support/tsconfig.tsbuildinfo \
      "$repository"/sdk-ts/tck/runtime/dist "$repository"/sdk-ts/tck/runtime/*.tsbuildinfo
fi

if (( caches )); then
  add "$repository"/sdk-ts/node_modules "$repository"/sdk-ts/package-lock.json \
      "$repository"/sdk-python/.venv
fi

if (( maven_repository )); then
  add "$HOME"/.m2/repository/org/hiero/sdk
fi

if [[ -z "$targets" ]]; then
  log "nothing to remove"
  exit 0
fi

while IFS= read -r path; do
  [[ -n "$path" ]] || continue
  size="$(du -sh "$path" 2>/dev/null | cut -f1 | tr -d ' ')"
  printf '  %-6s %s\n' "${size:-?}" "${path#"$repository"/}"
  (( dry_run )) || rm -rf "$path"
done <<< "$targets"

if (( dry_run )); then
  log "dry run - nothing was removed"
else
  log "removed"
fi

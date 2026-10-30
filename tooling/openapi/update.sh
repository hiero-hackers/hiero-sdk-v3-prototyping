#!/usr/bin/env bash
# Vendors the OpenAPI definitions of the Hiero node types into this directory.
#
# Usage: openapi/update.sh [mirror-node ...]
#        (no argument updates all of them)
#
# The versions are pinned in openapi/sources.json. To move to a newer release, change the "tag"
# there and run this script; it replaces the vendored file and records the resolved commit.
set -euo pipefail

directory="$(cd "$(dirname "$0")" && pwd)"
sources="$directory/sources.json"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT

command -v jq >/dev/null || { echo "jq is required" >&2; exit 1; }

names="$*"
if [[ -z "$names" ]]; then
  names="$(jq -r '.sources | keys | join(" ")' "$sources")"
fi

for name in $names; do
  repository="$(jq -r --arg n "$name" '.sources[$n].repository' "$sources")"
  tag="$(jq -r --arg n "$name" '.sources[$n].tag' "$sources")"
  path="$(jq -r --arg n "$name" '.sources[$n].path' "$sources")"
  if [[ "$repository" == "null" ]]; then
    echo "Unknown source '$name'; known: $(jq -r '.sources | keys | join(", ")' "$sources")" >&2
    exit 2
  fi

  echo "==> $name: $repository @ $tag"
  clone="$work/$name"
  git -c advice.detachedHead=false clone --quiet --depth 1 --branch "$tag" --filter=blob:none --sparse "$repository" "$clone"
  git -C "$clone" sparse-checkout set --no-cone "/$path"
  commit="$(git -C "$clone" rev-parse HEAD)"

  if [[ ! -f "$clone/$path" ]]; then
    echo "The file '$path' does not exist in $repository at $tag" >&2
    exit 1
  fi

  mkdir -p "$directory/$name"
  cp "$clone/$path" "$directory/$name/$(basename "$path")"
  echo "    $(basename "$path"), commit $commit"

  tmp="$(mktemp)"
  jq --arg n "$name" --arg c "$commit" \
     '.sources[$n].commit = $c | .updated = (now | todate)' \
     "$sources" > "$tmp" && mv "$tmp" "$sources"
done

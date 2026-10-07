#!/usr/bin/env bash
# Vendors the protobuf definitions of the Hiero node types into this directory.
#
# Usage: protobuf/update.sh [consensus-node|block-node|mirror-node ...]
#        (no argument updates all three)
#
# The versions are pinned in protobuf/sources.json. To move to a newer release, change the "tag"
# there and run this script; it rewrites the vendored tree and records the resolved commit.
# Afterwards run protobuf/verify.sh — a newer release may add imports that do not resolve.
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
  git clone --quiet --depth 1 --branch "$tag" --filter=blob:none --sparse "$repository" "$clone"
  git -C "$clone" sparse-checkout set --no-cone "/$path"
  commit="$(git -C "$clone" rev-parse HEAD)"

  if [[ ! -d "$clone/$path" ]]; then
    echo "The path '$path' does not exist in $repository at $tag" >&2
    exit 1
  fi

  rm -rf "${directory:?}/$name"
  mkdir -p "$directory/$name"
  # only .proto files; everything else in the upstream tree is build configuration
  (cd "$clone/$path" && find . -name '*.proto' | while read -r file; do
      mkdir -p "$directory/$name/$(dirname "$file")"
      cp "$file" "$directory/$name/$file"
  done)

  count="$(find "$directory/$name" -name '*.proto' | wc -l | tr -d ' ')"
  echo "    $count .proto file(s), commit $commit"

  tmp="$(mktemp)"
  jq --arg n "$name" --arg c "$commit" --argjson f "$count" \
     '.sources[$n].commit = $c | .sources[$n].files = $f | .updated = (now | todate)' \
     "$sources" > "$tmp" && mv "$tmp" "$sources"
done

echo
echo "Pinned versions are in protobuf/sources.json. Verify with protobuf/verify.sh."

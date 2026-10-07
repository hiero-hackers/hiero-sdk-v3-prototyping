#!/usr/bin/env bash
# Compiles every vendored .proto with protoc and writes one descriptor set per source root.
# This is the language-neutral proof that the vendored tree is complete and consistent: protoc
# resolves every import, so any generator can be pointed at these roots.
#
# Usage: protobuf/verify.sh [--keep]
#        --keep  leaves the descriptor sets in protobuf/target/ instead of a temporary directory
#
# protoc and the well-known types are fetched through the repository's Maven wrapper, so no
# protobuf installation is needed.
set -euo pipefail

directory="$(cd "$(dirname "$0")" && pwd)"
repository="$(cd "$directory/.." && pwd)"
version="$(jq -r '.protocVersion' "$directory/sources.json")"

case "$(uname -s)-$(uname -m)" in
  Darwin-arm64)  classifier="osx-aarch_64" ;;
  Darwin-x86_64) classifier="osx-x86_64" ;;
  Linux-aarch64) classifier="linux-aarch_64" ;;
  Linux-x86_64)  classifier="linux-x86_64" ;;
  *) echo "Unsupported platform $(uname -s)-$(uname -m)" >&2; exit 2 ;;
esac

out="$directory/target"
if [[ "${1:-}" != "--keep" ]]; then
  out="$(mktemp -d)"
  trap 'rm -rf "$out"' EXIT
fi
mkdir -p "$out"

local_repository="$("$repository/sdk-java/mvnw" -q help:evaluate -Dexpression=settings.localRepository -DforceStdout 2>/dev/null | tail -1)"
protoc="$local_repository/com/google/protobuf/protoc/$version/protoc-$version-$classifier.exe"
runtime="$local_repository/com/google/protobuf/protobuf-java/$version/protobuf-java-$version.jar"

if [[ ! -f "$protoc" ]]; then
  echo "==> fetching protoc $version ($classifier)"
  "$repository/sdk-java/mvnw" -q dependency:get \
      -Dartifact="com.google.protobuf:protoc:$version:exe:$classifier" >/dev/null
fi
if [[ ! -f "$runtime" ]]; then
  echo "==> fetching protobuf-java $version"
  "$repository/sdk-java/mvnw" -q dependency:get \
      -Dartifact="com.google.protobuf:protobuf-java:$version" >/dev/null
fi
chmod +x "$protoc"

# the well-known types (google/protobuf/*.proto) ship inside protobuf-java, not inside protoc
wellknown="$out/well-known"
mkdir -p "$wellknown"
(cd "$wellknown" && unzip -qo "$runtime" 'google/protobuf/*.proto')

status=0
for root in $(jq -r '.sources | keys[]' "$directory/sources.json"); do
  includes=(-I "$directory/$root" -I "$wellknown")
  # every root but the consensus node imports from it
  if [[ "$root" != "consensus-node" ]]; then
    includes+=(-I "$directory/consensus-node")
  fi

  files=()
  while IFS= read -r file; do
    files+=("$file")
  done < <(cd "$directory/$root" && find . -name '*.proto' | sed 's|^\./||' | sort)

  printf '==> %-16s %3d file(s) ... ' "$root" "${#files[@]}"
  log="$out/$root.log"
  if (cd "$directory/$root" && "$protoc" "${includes[@]}" \
        --include_imports --descriptor_set_out="$out/$root.desc" "${files[@]}") 2>"$log"; then
    printf 'ok (%s bytes)\n' "$(wc -c < "$out/$root.desc" | tr -d ' ')"
  else
    printf 'FAILED\n'
    status=1
  fi
  # upstream warnings (unused imports); they are reported, not fixed - the tree is vendored as it is
  sed 's|^|    |' "$log"
done

if [[ "${1:-}" == "--keep" ]]; then
  echo
  echo "Descriptor sets in $out"
fi
exit $status

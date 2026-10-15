#!/usr/bin/env bash
# Shared helpers of the scripts in this directory. Sourced, never run on its own.
#
# It locates the repository, resolves the JDK a Maven build needs (from the .sdkmanrc of that build,
# so the version stays pinned in one place), builds the metalang CLI on demand and parses the two
# options every generate script understands.

if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
  echo "common.sh is a library; run one of the other scripts instead." >&2
  exit 2
fi

set -euo pipefail

repository="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cli_jar=""

log() { printf '\n==> %s\n' "$*"; }
fail() { printf 'error: %s\n' "$*" >&2; exit 1; }

# The feature version of a JDK, e.g. 25 for "25.0.1".
java_feature_version() {
  "$1/bin/java" -version 2>&1 | head -1 | sed -E 's/^[^"]*"([0-9]+).*/\1/'
}

# jdk_home <directory with a .sdkmanrc>
#
# Prints a JAVA_HOME that satisfies the pin of that directory: $JAVA_HOME if it is new enough, the
# pinned SDKMAN! candidate, any newer one, or what /usr/libexec/java_home knows (macOS).
jdk_home() {
  local sdkmanrc="$1/.sdkmanrc" pinned required candidate version
  [[ -f "$sdkmanrc" ]] || fail "no .sdkmanrc in $1"
  pinned="$(sed -nE 's/^java=(.*)$/\1/p' "$sdkmanrc" | tail -1)"
  required="${pinned%%.*}"

  if [[ -n "${JAVA_HOME:-}" && -x "${JAVA_HOME}/bin/java" ]]; then
    version="$(java_feature_version "$JAVA_HOME")"
    if [[ "$version" =~ ^[0-9]+$ ]] && (( version >= required )); then
      printf '%s' "$JAVA_HOME"
      return 0
    fi
  fi

  candidate="$HOME/.sdkman/candidates/java/$pinned"
  if [[ -x "$candidate/bin/java" ]]; then
    printf '%s' "$candidate"
    return 0
  fi

  for candidate in "$HOME"/.sdkman/candidates/java/*; do
    [[ -x "$candidate/bin/java" ]] || continue
    version="$(java_feature_version "$candidate")"
    [[ "$version" =~ ^[0-9]+$ ]] || continue
    (( version >= required )) || continue
    printf '%s' "$candidate"
    return 0
  done

  if candidate="$(/usr/libexec/java_home -v "$required" 2>/dev/null)"; then
    printf '%s' "$candidate"
    return 0
  fi

  fail "no JDK $required or newer found. Install the pinned one with 'cd ${1#"$repository"/} && sdk env install'."
}

# Builds the metalang CLI unless the jar is there; REBUILD_CLI=1 forces a rebuild. Sets $cli_jar.
ensure_cli() {
  cli_jar="$(ls "$repository"/tooling/metalang/target/metalang-*-cli.jar 2>/dev/null | head -1 || true)"
  if [[ -n "$cli_jar" && "${REBUILD_CLI:-0}" != "1" ]]; then
    return 0
  fi
  log "building the metalang CLI"
  (cd "$repository/tooling/metalang" \
    && JAVA_HOME="$(jdk_home "$repository/tooling/metalang")" ./mvnw -q package -DskipTests)
  cli_jar="$(ls "$repository"/tooling/metalang/target/metalang-*-cli.jar 2>/dev/null | head -1 || true)"
  [[ -n "$cli_jar" ]] || fail "the CLI jar was not built"
}

# Runs the CLI. The jar targets release 21, so any JDK of the repository runs it.
metalang() {
  ensure_cli
  "$(jdk_home "$repository/tooling/metalang")/bin/java" -jar "$cli_jar" "$@"
}

# Options of the generate scripts: --no-build generates only, --test also runs the generated tests
# (they fail for every method that is only a stub), --ignore-spec-errors generates although the
# specs have findings at or above 'error'.
build=1
run_tests=0
fail_on="error"
parse_generate_options() {
  local usage="usage: $(basename "$0") [--no-build] [--test] [--ignore-spec-errors]"
  while (( $# > 0 )); do
    case "$1" in
      --no-build) build=0 ;;
      --test) run_tests=1 ;;
      --ignore-spec-errors) fail_on="never" ;;
      -h | --help) echo "$usage"; exit 0 ;;
      *) fail "unknown option '$1'. $usage" ;;
    esac
    shift
  done
}

# generate_api <language> <language folder>
#
# Generates into <language folder>/generated. The generator writes nothing while the specs have a
# finding at or above 'error', so that a broken spec cannot silently produce an SDK with the
# affected declarations missing; this prints the findings and how to override it.
generate_api() {
  local language="$1" folder="$2"
  if metalang generate --language="$language" --fail-on="$fail_on" --show-deferred \
      --config="$repository/$folder/generator.properties" \
      --output="$repository/$folder/generated" \
      "$repository/spec"; then
    return 0
  fi
  log "the findings of the specs"
  metalang validate --min-severity=error "$repository/spec" || true
  fail "nothing was generated. Fix the findings above, or generate anyway with --ignore-spec-errors."
}

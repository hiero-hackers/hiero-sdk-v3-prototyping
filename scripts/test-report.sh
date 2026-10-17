#!/usr/bin/env bash
# Renders the results of the last test runs as one HTML page per area, from the Open Test Reporting files the Maven
# builds write next to their Surefire reports (ADR-0012). The tests are not run: build first (e.g.
# `./mvnw -f impl install` in sdk-java, `./mvnw verify` in tooling/metalang).
#
# usage: scripts/test-report.sh [sdk|tck|generated|tooling|all] [--open]
#
#   sdk        the Java SDK: sdk-java/impl, sdk-java/test-support and the last TCK run against it (scripts/tck-java.sh)
#                                                                   -> sdk-java/impl/target/test-report/index.html
#   tck        only the last TCK run of the Java SDK                -> sdk-java/tck/target/test-report/index.html
#   generated  the generated Java API (stubs): sdk-java/generated   -> sdk-java/generated/target/test-report/index.html
#   tooling    the tests of tooling/metalang                        -> tooling/metalang/target/test-report/index.html
#   all        all three (the default)
#   --open     open the reports in the browser
#
# Environment:
#   TEST_REPORT_FAIL_ON_FAILURES  true: exit with 1 after rendering if a test failed (not counting the TCK, whose failures
#                                 tck-java.sh governs); the module sdk-java/impl/test-report sets it
#
# The renderer is the CLI of Open Test Reporting, fetched once into the local Maven repository. It names every result
# file by its path relative to the page, so the files are copied next to the page under the name of what they test (the
# module, `test-support`, `TCK`).

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

readonly cli_version="0.2.7"
area="all"
open=""
for argument in "$@"; do
  case "$argument" in
    sdk|tck|generated|tooling|all) area="$argument" ;;
    --open) open="--open" ;;
    *) fail "unknown argument '$argument' (usage: scripts/test-report.sh [sdk|tck|generated|tooling|all] [--open])" ;;
  esac
done

JAVA_HOME="$(jdk_home "$repository/sdk-java")"
export JAVA_HOME

otr_cli="$HOME/.m2/repository/org/opentest4j/reporting/open-test-reporting-cli/$cli_version/open-test-reporting-cli-$cli_version-standalone.jar"
if [[ ! -f "$otr_cli" ]]; then
  log "fetching the Open Test Reporting CLI $cli_version"
  (cd "$repository/sdk-java" && ./mvnw -q dependency:get \
    -Dartifact="org.opentest4j.reporting:open-test-reporting-cli:$cli_version:jar:standalone")
fi

# render <name> <target directory> <label>=<report file> ...: the page is <target directory>/index.html
render() {
  local name="$1" target="$2"
  shift 2
  rm -rf "$target"
  local files=()
  for entry in "$@"; do
    local label="${entry%%=*}" file="${entry#*=}"
    if [[ -f "$file" ]]; then
      mkdir -p "$target"
      cp "$file" "$target/$label"
      files+=("$target/$label")
    fi
  done
  if (( ${#files[@]} == 0 )); then
    log "$name: no results (run the tests first)"
    return 0
  fi
  log "$name: ${#files[@]} result file(s)"
  "$JAVA_HOME/bin/java" -jar "$otr_cli" html-report --output "$target/index.html" $open "${files[@]}"
  local file count
  for file in "${files[@]}"; do
    [[ "${file##*/}" == "TCK" ]] && continue
    count="$( (grep -Eo '<result status="(FAILED|ERRORED)"' "$file" || true) | wc -l | tr -d ' ')"
    if (( count > 0 )); then
      failures+=("$name/${file##*/}: $count failed")
    fi
  done
}

# <label>=<report file> for the OTR file of every Maven module below <directory>, labelled by the module's directory
modules() {
  local file module
  for file in "$1"/*/target/surefire-reports/open-test-report.xml; do
    module="${file%/target/surefire-reports/open-test-report.xml}"
    echo "${module##*/}=$file"
  done
}

failures=()
shopt -s nullglob
if [[ "$area" == "sdk" || "$area" == "all" ]]; then
  # shellcheck disable=SC2046 # one label=file per line, no spaces
  render "sdk" "$repository/sdk-java/impl/target/test-report" $(modules "$repository/sdk-java/impl") \
    "test-support=$repository/sdk-java/test-support/target/surefire-reports/open-test-report.xml" \
    "TCK=$repository/sdk-java/tck/target/open-test-report.xml"
fi
if [[ "$area" == "tck" ]]; then
  render "tck" "$repository/sdk-java/tck/target/test-report" "TCK=$repository/sdk-java/tck/target/open-test-report.xml"
fi
if [[ "$area" == "generated" || "$area" == "all" ]]; then
  # shellcheck disable=SC2046
  render "generated" "$repository/sdk-java/generated/target/test-report" $(modules "$repository/sdk-java/generated")
fi
if [[ "$area" == "tooling" || "$area" == "all" ]]; then
  # shellcheck disable=SC2046
  render "tooling" "$repository/tooling/metalang/target/test-report" $(modules "$repository/tooling/metalang")
fi

if (( ${#failures[@]} > 0 )) && [[ "${TEST_REPORT_FAIL_ON_FAILURES:-false}" == "true" ]]; then
  for failure in "${failures[@]}"; do
    log "$failure (tests and containers)"
  done
  fail "tests failed, see the report"
fi

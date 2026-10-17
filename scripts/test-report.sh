#!/usr/bin/env bash
# Renders the results of the last test runs as one HTML page per area, from the Open Test Reporting files the Maven
# builds write next to their Surefire reports (ADR-0012). The tests are not run: build first (e.g.
# `./mvnw -f impl install` in sdk-java, `./mvnw verify` in tooling/metalang).
#
# usage: scripts/test-report.sh [sdk|generated|tooling|all] [--open]
#
#   sdk        the Java SDK: sdk-java/impl and sdk-java/test-support -> sdk-java/impl/target/test-report.html
#   generated  the generated Java API (stubs): sdk-java/generated   -> sdk-java/generated/target/test-report.html
#   tooling    the tests of tooling/metalang                        -> tooling/metalang/target/test-report.html
#   all        all three (the default)
#   --open     open the reports in the browser
#
# The renderer is the CLI of Open Test Reporting, fetched once into the local Maven repository.

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"

readonly cli_version="0.2.7"
area="all"
open=""
for argument in "$@"; do
  case "$argument" in
    sdk|generated|tooling|all) area="$argument" ;;
    --open) open="--open" ;;
    *) fail "unknown argument '$argument' (usage: scripts/test-report.sh [sdk|generated|tooling|all] [--open])" ;;
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

# render <name> <target html> <report file ...>
render() {
  local name="$1" target="$2"
  shift 2
  local files=()
  for file in "$@"; do
    [[ -f "$file" ]] && files+=("$file")
  done
  if (( ${#files[@]} == 0 )); then
    log "$name: no results (run the tests first)"
    return 0
  fi
  log "$name: ${#files[@]} result file(s)"
  mkdir -p "$(dirname "$target")"
  "$JAVA_HOME/bin/java" -jar "$otr_cli" html-report --output "$target" $open "${files[@]}"
}

shopt -s nullglob
if [[ "$area" == "sdk" || "$area" == "all" ]]; then
  render "sdk" "$repository/sdk-java/impl/target/test-report.html" \
    "$repository"/sdk-java/impl/*/target/surefire-reports/open-test-report.xml \
    "$repository/sdk-java/test-support/target/surefire-reports/open-test-report.xml"
fi
if [[ "$area" == "generated" || "$area" == "all" ]]; then
  render "generated" "$repository/sdk-java/generated/target/test-report.html" \
    "$repository"/sdk-java/generated/*/target/surefire-reports/open-test-report.xml
fi
if [[ "$area" == "tooling" || "$area" == "all" ]]; then
  render "tooling" "$repository/tooling/metalang/target/test-report.html" \
    "$repository"/tooling/metalang/*/target/surefire-reports/open-test-report.xml
fi

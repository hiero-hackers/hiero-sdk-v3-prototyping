#!/usr/bin/env bash
# Generates the Java API from the specs into sdk-java/generated and builds it.
#
# The hand-written modules the generated code depends on (support, test-support, protobuf) and the generated
# modules themselves are installed into the local Maven repository, so that the TCK server and any
# other consumer can resolve them.
#
# usage: scripts/generate-java.sh [--no-build] [--test] [--ignore-spec-errors]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the Java API into sdk-java/generated"
generate_api java sdk-java

if (( build )); then
  # a plain assignment, so that a missing JDK fails the script instead of exporting nothing
  JAVA_HOME="$(jdk_home "$repository/sdk-java")"
  export JAVA_HOME
  cd "$repository/sdk-java"

  log "building hiero-sdk-support, hiero-sdk-test-support and hiero-sdk-protobuf"
  ./mvnw -f support install
  # the tests generated from the specs' `tests` blocks depend on it (test scope)
  ./mvnw -f test-support install
  ./mvnw -f protobuf install

  log "building the generated modules"
  ./mvnw -f generated install -DskipTests

  if (( run_tests )); then
    log "running the generated tests (stubs fail by design)"
    ./mvnw -f generated test -Dmaven.test.failure.ignore=true
  fi
fi

log "done: sdk-java/generated"

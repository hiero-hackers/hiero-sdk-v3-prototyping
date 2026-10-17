#!/usr/bin/env bash
# Runs the Hiero TCK (https://github.com/hiero-ledger/hiero-sdk-tck) against a generated V3 TCK server. By default the
# network is a local Solo network (tooling/tck/solo.env); start it first with `solo one-shot single deploy`.
#
# Usage:   tooling/tck/run-tck.sh java|ts [TCK test file ...]
# Example: TCK_DIR=../hiero-sdk-tck tooling/tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
#
# The results are written as Open Test Reporting to sdk-<language>/tck/target/open-test-report.xml (ADR-0012; mocha
# runs with tooling/tck/otr-mocha-reporter.cjs instead of the TCK's mochawesome; every test carries its stdout, stderr
# and the server's log of its calls, see tooling/tck/otr-test-output.cjs), the console output also to
# tooling/tck/last-run-<language>.log (both git-ignored); the summary of the run is printed at the very end, after the
# summary of the server. `scripts/test-report.sh sdk` renders the results together with those of the SDK's tests.
#
# Environment:
#   TCK_DIR   the clone of hiero-sdk-tck with installed dependencies (required)
#   TCK_ENV   the TCK configuration (default: tooling/tck/solo.env); it is copied to $TCK_DIR/.env, an existing .env is
#             saved as .env.before-v3
#   JAVA_HOME a JDK 25 (Java server only; `sdk env` sets it from .sdkmanrc)
#   TCK_IGNORE_FAILURES  true: failed TCK tests do not fail this script (exit code 0 after the summary); an unreachable
#             network or a server that cannot start still does
#   TCK_SERVER_LOG  how much the server logs about failed calls: 'error' (default) logs the first call of each
#             distinct failure and a summary at the end, 'debug' adds the stack trace of every call, 'off' is silent.
#             The TCK itself only shows "Internal error", never the exception behind it.
#
# The server must be built before (see tooling/metalang/README.md, "Generate the TCK server"):
#   java: sdk-java/tck/generated/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar
#   ts:   sdk-ts/tck/generated/server/dist/main.js (npm install && npm run build:tck-ts in sdk-ts)
set -euo pipefail

repository="$(cd "$(dirname "$0")/../.." && pwd)"
language="${1:-}"
if [[ "$language" != "java" && "$language" != "ts" ]]; then
  echo "Usage: tooling/tck/run-tck.sh java|ts [TCK test file ...]" >&2
  exit 2
fi
shift
if [[ -z "${TCK_DIR:-}" || ! -f "${TCK_DIR}/package.json" ]]; then
  echo "TCK_DIR must point to a clone of hiero-sdk-tck (with 'npm install' done)" >&2
  exit 2
fi
TCK_DIR="$(cd "$TCK_DIR" && pwd)"
env_file="${TCK_ENV:-$repository/tooling/tck/solo.env}"

# the configuration, also for the server (MIRROR_NODE_REST_URL)
set -a
# shellcheck disable=SC1090
source "$env_file"
set +a

# the network must be running
node_host="${NODE_IP%:*}"
node_port="${NODE_IP##*:}"
if ! (exec 3<>"/dev/tcp/$node_host/$node_port") 2>/dev/null; then
  echo "No consensus node at $NODE_IP. Start Solo with 'solo one-shot single deploy' (or set TCK_ENV)." >&2
  exit 1
fi

# the output of the server, also attached to the results of the run
server_log="$repository/tooling/tck/last-run-$language-server.log"
# the results as Open Test Reporting (ADR-0012), next to the TCK server of the language; scripts/test-report.sh renders
# them together with the results of the SDK's own tests. The output of every single test (its stdout and stderr, and
# the server's log of its calls) is attached to it: tooling/tck/otr-test-output.cjs and the server write it to
# OTR_TEST_OUTPUT_DIR, the reporter reads it from there.
results="$repository/sdk-$language/tck/target/open-test-report.xml"
export OTR_TEST_OUTPUT_DIR="$repository/sdk-$language/tck/target/test-output"
rm -rf "$OTR_TEST_OUTPUT_DIR" "$results" "$results.summary.json"

# the server
port="$(echo "$JSON_RPC_SERVER_URL" | sed -E 's#.*:([0-9]+)/?$#\1#')"
if [[ "$language" == "java" ]]; then
  jar="$repository/sdk-java/tck/generated/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar"
  [[ -f "$jar" ]] || { echo "Build the Java TCK server first: $jar is missing" >&2; exit 1; }
  "${JAVA_HOME:+$JAVA_HOME/bin/}java" -jar "$jar" "$port" > >(tee "$server_log") 2>&1 &
else
  main="$repository/sdk-ts/tck/generated/server/dist/main.js"
  [[ -f "$main" ]] || { echo "Build the TypeScript TCK server first: $main is missing" >&2; exit 1; }
  node "$main" "$port" > >(tee "$server_log") 2>&1 &
fi
server=$!
trap 'kill "$server" 2>/dev/null || true' EXIT
for _ in $(seq 1 50); do
  if curl -s -o /dev/null -d '{"jsonrpc":"2.0","id":0,"method":"reset","params":{}}' "$JSON_RPC_SERVER_URL"; then
    break
  fi
  sleep 0.2
done

# the TCK
if [[ -f "$TCK_DIR/.env" ]] && ! cmp -s "$env_file" "$TCK_DIR/.env"; then
  cp "$TCK_DIR/.env" "$TCK_DIR/.env.before-v3"
fi
cp "$env_file" "$TCK_DIR/.env"
cd "$TCK_DIR"
log="$repository/tooling/tck/last-run-$language.log"
reporter="$repository/tooling/tck/otr-mocha-reporter.cjs"
export OTR_OUTPUT="$results" TCK_SDK="$language" OTR_GIT_DIR="$repository" OTR_JAVA_HOME="${JAVA_HOME:-}" \
  TCK_ENV_FILE="$env_file" OTR_ATTACHMENTS="$server_log:$log"
test_output="$repository/tooling/tck/otr-test-output.cjs"
# The arguments of the TCK's npm scripts `test` and `test:file` (revision a3ea777), with our reporter instead of
# mochawesome: an npm script cannot be given another reporter. Keep them in sync when the TCK changes them.
set +e
if [[ $# -gt 0 ]]; then
  npx mocha --require ts-node/register --require tsconfig-paths/register --require src/preflight.ts \
    --require "$test_output" \
    --timeout 90000 --slow 30000 --reporter "$reporter" --exit "$@" 2>&1 | tee "$log"
else
  npx mocha --parallel --jobs 7 --node-option require=ts-node/register --node-option require=tsconfig-paths/register \
    --require src/preflight.ts --require "$test_output" --recursive 'src/tests/**/*.ts' --timeout 90000 --slow 30000 \
    --reporter "$reporter" --exit 2>&1 | tee "$log"
fi
status=${PIPESTATUS[0]}
set -e

# stop the server first: it prints the summary of its failed calls when it stops, the result of the TCK comes last
kill "$server" 2>/dev/null || true
wait "$server" 2>/dev/null || true
trap - EXIT

echo
echo "==> TCK result ($language)"
if [[ -f "$results.summary.json" ]]; then
  node -e '
    const s = JSON.parse(require("fs").readFileSync(process.argv[1], "utf8"));
    console.log(`   ${s.tests} tests: ${s.passed} passed, ${s.failed} failed, ${s.skipped} skipped`
        + `; ${s.hooksFailed} failed hooks (before/after; mocha counts them as failures, the tests of their suite`
        + ` did not run)`);
  ' "$results.summary.json"
else
  echo "   no results: the run ended before the reporter finished"
fi
echo "   results: $results (scripts/test-report.sh sdk renders them as HTML)"
echo "   console: $log"
if [[ "${TCK_IGNORE_FAILURES:-false}" == "true" && "$status" != "0" ]]; then
  echo "   (failed tests ignored: TCK_IGNORE_FAILURES=true)"
  exit 0
fi
exit "$status"

#!/usr/bin/env bash
# Runs the Hiero TCK (https://github.com/hiero-ledger/hiero-sdk-tck) against a generated V3 TCK server. By default the
# network is a local Solo network (tooling/tck/solo.env); start it first with `solo one-shot single deploy`.
#
# Usage:   tooling/tck/run-tck.sh java|ts [TCK test file ...]
# Example: TCK_DIR=../hiero-sdk-tck tooling/tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
#
# The console output of the TCK is also written to tooling/tck/last-run-<language>.log (git-ignored); the summary of the
# run, the HTML report of the TCK and that log are printed at the very end, after the summary of the server.
#
# Environment:
#   TCK_DIR   the clone of hiero-sdk-tck with installed dependencies (required)
#   TCK_ENV   the TCK configuration (default: tooling/tck/solo.env); it is copied to $TCK_DIR/.env, an existing .env is
#             saved as .env.before-v3
#   JAVA_HOME a JDK 25 (Java server only; `sdk env` sets it from .sdkmanrc)
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

# the server
port="$(echo "$JSON_RPC_SERVER_URL" | sed -E 's#.*:([0-9]+)/?$#\1#')"
if [[ "$language" == "java" ]]; then
  jar="$repository/sdk-java/tck/generated/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar"
  [[ -f "$jar" ]] || { echo "Build the Java TCK server first: $jar is missing" >&2; exit 1; }
  "${JAVA_HOME:+$JAVA_HOME/bin/}java" -jar "$jar" "$port" &
else
  main="$repository/sdk-ts/tck/generated/server/dist/main.js"
  [[ -f "$main" ]] || { echo "Build the TypeScript TCK server first: $main is missing" >&2; exit 1; }
  node "$main" "$port" &
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
# not in mochawesome-report: `npm test` deletes that directory when it starts
log="$repository/tooling/tck/last-run-$language.log"
set +e
if [[ $# -gt 0 ]]; then
  npm run test:file -- "$@" 2>&1 | tee "$log"
else
  npm test 2>&1 | tee "$log"
fi
status=${PIPESTATUS[0]}
set -e

# stop the server first: it prints the summary of its failed calls when it stops, the result of the TCK comes last
kill "$server" 2>/dev/null || true
wait "$server" 2>/dev/null || true
trap - EXIT

echo
echo "==> TCK result ($language)"
node -e '
  const fs = require("fs");
  const file = "mochawesome-report/mochawesome.json";
  if (!fs.existsSync(file)) { console.log("   no report: the run crashed before mochawesome wrote it"); process.exit(0); }
  const s = JSON.parse(fs.readFileSync(file, "utf8")).stats;
  console.log(`   ${s.tests} tests: ${s.passes} passed, ${s.failures} failed, ${s.pending} pending (skipped)`
      + ` in ${Math.round(s.duration / 1000)} s`);
'
echo "   report:  $TCK_DIR/mochawesome-report/mochawesome.html"
echo "   console: $log"
exit "$status"

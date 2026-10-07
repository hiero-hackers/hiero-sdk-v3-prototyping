#!/usr/bin/env bash
# Runs the Hiero TCK (https://github.com/hiero-ledger/hiero-sdk-tck) against a generated V3 TCK server. By default the
# network is a local Solo network (tck/solo.env); start it first with `solo one-shot single deploy`.
#
# Usage:   tck/run-tck.sh java|ts [TCK test file ...]
# Example: TCK_DIR=../hiero-sdk-tck tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
#
# Environment:
#   TCK_DIR   the clone of hiero-sdk-tck with installed dependencies (required)
#   TCK_ENV   the TCK configuration (default: tck/solo.env); it is copied to $TCK_DIR/.env, an existing .env is
#             saved as .env.before-v3
#   JAVA_HOME a JDK 25 (Java server only; `sdk env` sets it from .sdkmanrc)
#
# The server must be built before (see tooling/metalang/README.md, "Generate the TCK server"):
#   java: generated/java-tck/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar
#   ts:   generated/ts-tck/server/dist/main.js (npm install && npm run build:tck-ts in the repository root)
set -euo pipefail

repository="$(cd "$(dirname "$0")/.." && pwd)"
language="${1:-}"
if [[ "$language" != "java" && "$language" != "ts" ]]; then
  echo "Usage: tck/run-tck.sh java|ts [TCK test file ...]" >&2
  exit 2
fi
shift
if [[ -z "${TCK_DIR:-}" || ! -f "${TCK_DIR}/package.json" ]]; then
  echo "TCK_DIR must point to a clone of hiero-sdk-tck (with 'npm install' done)" >&2
  exit 2
fi
env_file="${TCK_ENV:-$repository/tck/solo.env}"

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
  jar="$repository/generated/java-tck/server/target/hiero-sdk-tck-0.1.0-SNAPSHOT.jar"
  [[ -f "$jar" ]] || { echo "Build the Java TCK server first: $jar is missing" >&2; exit 1; }
  "${JAVA_HOME:+$JAVA_HOME/bin/}java" -jar "$jar" "$port" &
else
  main="$repository/generated/ts-tck/server/dist/main.js"
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
if [[ $# -gt 0 ]]; then
  npm run test:file -- "$@"
else
  npm test
fi

#!/usr/bin/env bash
# Generates the Rust API from the specs into sdk-rust/generated and builds it.
#
# `cargo build --all-targets` compiles the crates and their integration tests. `--test` runs the
# tests with `--tests`, which leaves the doctests out: the protobuf code carries the HAPI comment of
# ExchangeRate, whose fenced code block rustdoc compiles as Rust and which therefore always fails
# (see the Rust section of README.md).
#
# usage: scripts/generate-rust.sh [--no-build] [--test]

source "$(dirname "${BASH_SOURCE[0]}")/common.sh"
parse_generate_options "$@"

log "generating the Rust API into sdk-rust/generated"
metalang generate --language=rust --fail-on=never \
  --config="$repository/sdk-rust/generator.properties" \
  --output="$repository/sdk-rust/generated" \
  "$repository/spec"

if (( build )); then
  command -v cargo >/dev/null || fail "cargo is needed (Rust 1.85 or newer, see https://rustup.rs)"

  log "building the Cargo workspace"
  cargo build --manifest-path "$repository/sdk-rust/generated/Cargo.toml" --all-targets

  if (( run_tests )); then
    log "running the generated tests (stubs fail by design)"
    cargo test --manifest-path "$repository/sdk-rust/generated/Cargo.toml" --no-fail-fast --tests || true
  fi
fi

log "done: sdk-rust/generated"

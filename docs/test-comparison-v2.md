# Tests of the V3 prototype compared with the V2 SDKs

How many tests the V3 prototype and the seven SDKs of the current generation ("V2") have, how many of them fail, how
long a run takes, and which parts of an SDK they cover. Measured on 2026-11-06 on one machine (macOS, Apple Silicon)
with a local Solo network.

## Scope and method

| SDK | Repository state | Measured |
|---|---|---|
| V3 Java (`sdk-java/impl`) | this repository, branch `deterministic-generator` (after `834bf781`) | unit tests run, code coverage measured, full TCK run |
| V2 Java | `hiero-sdk-java` f917e67b3, v2.76.0 (2026-08-17) | unit tests run, JaCoCo coverage measured |
| V2 JavaScript | `hiero-sdk-js` 83dd15c (2026-04-02) | unit tests run (Node and browser), coverage measured |
| V2 Go | `hiero-sdk-go` eb2a9bd, v2.78.0 (2026-04-06) | unit tests run, coverage measured |
| Rust | `hiero-sdk-rust` 9ae3b3f, v0.44.1 (2026-04-06) | unit and doc tests run |
| Python | `hiero-sdk-python` 56c884a, v0.2.0 (2026-02-13) | unit tests run, coverage measured |
| Swift | `hiero-sdk-swift` f8e0a10, v0.49.0 (2026-04-02) | unit tests run in a Linux container (`swift:6.0-jammy`; the Mac has no XCTest) |
| C++ | `hiero-sdk-cpp` f1537cc (2026-04-07) | counted only (no CMake toolchain) |

- The V2 repositories are local clones that were not updated for this report; newer releases may have more tests.
- Only **offline** tests were run for V2. Their integration tests need a network and are counted, not run. The TCK was
  run against V3 only; for V2 the report lists the TCK methods each server implements.
- **Counts are not comparable one to one.** A "test" is a JUnit invocation (Java), a Vitest case (JS), a top-level
  `func Test` (Go, sub-tests listed separately), a runtime `#[test]` (Rust), a collected pytest item (Python), an XCTest
  method (Swift) or a gtest macro (C++). The V3 contract tests are generated per type (see below) and are many small
  checks, as are the getter/setter and snapshot tests of the V2 SDKs.
- Times are wall clock on this machine. "Test execution" is the time the test framework reports; "full run" includes
  compilation where noted.

## Summary

| | V3 Java | V2 Java | V2 JS | V2 Go | Rust | Python | Swift | C++ |
|---|---|---|---|---|---|---|---|---|
| **Unit tests run** | 3,954 | 1,598 | 1,503 (Node) + 1,277 (browser) | 1,185 (+960 sub-tests) | 998 (+17 doc tests) | 2,150 | 866 | ≈1,230 (not run) |
| Passed / failed / skipped | 3,954 / 0 / 0 | 1,596 / 0 / 2 | all passed | 1,183 / 0 / 2 | 998 / 0 / 0 | 2,148 / **2** / 0 | 866 / 0 / 0 | – |
| Test execution | 6 s | 3 min 17 s | 26 s + 37 s | 4–9 s | 0.15 s | 23 s | 7.6 s | – |
| Full run incl. build | ≈20 s (incremental) | ≈5 min 20 s (cold) | ≈4 min with install and build | ≈70 s (cold) | ≈2 min 50 s (cold) | 28 s (+1 min venv setup) | ≈5 min 40 s (build 314 s) | – |
| **Integration tests** (network) | none – the TCK takes this role | 721 (93 disabled) | 548 (19 skipped) | 642 | 380 (22 ignored) | 368 | 356 | ≈595 instances (22 disabled) |
| **Line coverage of the unit tests** | 69.1 % (branches 40.0 %); with the TCK 73.4 % (46.7 %) | 81.7 % (branches 72 %) | 68.2 % (branches 60.6 %) | 65.8 % (statements) | not configured | 93 % | not configured | not configured |
| **TCK server** | generated: 54 of 64 methods bound | 61 methods | 60 methods | 48 methods | 45 methods | none | 48 methods | 40 methods |
| **TCK run** (2,327 tests) | 1,591 passed, 360 failed, 376 skipped, 7 min 13 s | not run | not run | not run | not run | – | not run | not run |

Notes on the failures and skips:

- **Python:** both failures are `topic_info_test.py::test_repr_and_str` / `test_str_formatting`: `TopicInfo.__str__`
  formats the expiry with the local time zone (`consensus/topic_info.py:143`). They pass with `TZ=UTC`, which is what
  CI uses.
- **Go:** the run is **flaky** on this machine: the test binary often aborts with `panic: grpc: the server has been
  stopped` (`sdk/mock_test.go:948`) — `NewMockServer` panics when a parallel test stops the server before `Serve` has
  started. Without `-race` only 1 of 10 runs completed; no assertion ever failed. Two tests skip themselves as
  "currently broken".
- **V2 Java:** two `@Disabled` tests. Most of the run time is spent in retry/backoff tests with real delays
  (`ExecutableTest` 98 s, `MockingTest` 26 s, `ClientTest` 20 s).
- **Rust:** 354 of the 380 e2e tests return `Ok` without a configured operator, so an e2e run without a network
  "passes" silently.
- **Disabled integration tests in V2:** Java disables every batch (10), hooks (36) and fee-estimation (15) test and 10
  of 14 schedule tests; C++ disables all batch tests and the only Ethereum, Freeze, NodeCreate and NodeDelete tests.

## The V3 tests

| Kind | Count | Run | What it checks |
|---|---|---|---|
| Spec tests (`<Spec>SpecTest`) | 441 (51 spec files, 122 of them check that an invalid value is rejected) | `./mvnw -f impl verify` | The `tests` blocks of the specs, written once in the test language and generated per language; expected values come from the spec or a published vector (EIP-55, the Solidity ABI specification, EIP-165, RFC 8032, …). Every test has a stable test id. |
| Contract tests (`<Type>Test`) | 3,513 (372 classes with the spec tests) | same | Generated per type from the specs: constructors, required values, defaults, validation annotations, defensive copies, value equality. |
| TCK | 2,327 tests of the Hiero TCK | `./mvnw -f impl verify -Ptck` against Solo | The network behaviour, through a TCK server generated from `tooling/tck/bindings` |
| Tooling tests | 1,244 (`tooling/metalang`) | `./mvnw install` in `tooling/metalang` | Parser, validator, generators of five languages, conformance checks, TCK bindings |

A full run of the SDK's tests and the TCK (`./mvnw -f impl verify -Ptck`) takes 7 min 41 s, of which the TCK takes
7 min 13 s. The tests of the SDK alone need about 20 s, 6 s of which are test execution.

The TypeScript side has no implementation yet: `sdk-ts/generated` contains the generated tests, which fail on the
stubs, and its TCK server builds but has not been run.

### Code coverage of V3 (JaCoCo, without generated protobuf code)

The build records the coverage itself (module `impl/coverage`, reports in `sdk-java/impl/target/coverage`): of the
unit tests, of the TCK run and of both together. The low branch coverage of the clients shows where the unit tests
stop: the network paths (execution, retry, receipt polling, REST calls) are exercised by the TCK, not by the unit
tests, and the enterprise services and the mirror node repositories are tested only up to their parameter types.

| Module | Unit tests: lines / branches | TCK | Unit tests and TCK |
|---|---|---|---|
| base | 85.9 % / 76.7 % | 34.8 % / 19.6 % | 89.0 % / 77.8 % |
| consensus-node-client | 75.0 % / 38.6 % | 46.9 % / 34.3 % | 79.2 % / 47.5 % |
| consensus-node-admin-client | 72.9 % / 41.9 % | 23.9 % / 29.1 % | 75.4 % / 47.1 % |
| mirror-node-client | 58.1 % / 17.9 % | 9.2 % / 10.0 % | 64.7 % / 27.6 % |
| enterprise | 44.2 % / 19.1 % | 0 % / 0 % | 44.2 % / 19.1 % |
| **total** | **69.1 % / 40.0 %** | **30.2 % / 23.2 %** | **73.4 % / 46.7 %** |

The TCK adds 4.3 points of lines and 6.7 points of branches over the unit tests, almost all in the consensus node
client (execution, receipts) and the mirror node client (`getAddressBook`); it never reaches the enterprise services.

### V3 TCK results by area (full run, 2026-11-06)

| Area | Passed | Failed | Skipped |
|---|---:|---:|---:|
| Accounts, transfers, allowances | 305 | 62 | 22 |
| Tokens and airdrops | 622 | 158 | 177 |
| Topics | 114 | 62 | 38 |
| Files | 115 | 18 | 7 |
| Schedules | 75 | 13 | 1 |
| Contracts and Ethereum | 236 | 16 | 94 |
| Nodes and address book | 114 | 31 | 11 |
| Receipts | 10 | 1 | 0 |
| Mirror node balance queries, ping | 0 | 2 | 29 |
| **total** | **1,591** | **360** + 3 hooks | **376** |

The skipped tests belong mostly to methods without a binding (`ContractCallQuery`, `ContractInfoQuery`,
`ContractByteCodeQuery`, `TopicMessageSubmit`, the mirror node balance queries, `ping`) or to suites whose setup
fails (163 of the 177 skipped token tests are `TokenAirdropTransaction`). The failures are dominated by values the V3
SDK rejects before sending them where the TCK expects a status of the network (104 × "Internal error", about 200 ×
"expected undefined to equal '<STATUS>'", e.g. `CUSTOM_FEE_MUST_BE_POSITIVE` 28×, `INVALID_TOKEN_ID` 13×,
`INVALID_ACCOUNT_ID` 11×) and by gaps recorded in the bindings files; 10 tests time out.

## Coverage by area

Unit / integration tests per area. For V3 the first number is spec tests, the second contract tests; V3 has no
integration tests (see the TCK table above). Each V2 test file is assigned to one area by its name, so the numbers show
where the tests are, not exact feature coverage. C++ counts include disabled and not-compiled tests.

| Area | V3 (spec / contract) | V2 Java | V2 JS (Node) | V2 Go | Rust | Python | Swift | C++ |
|---|---|---|---|---|---|---|---|---|
| Accounts, transfers, allowances | 30 / 304 | 70 / 59 | 139 / 87 | 113 / 94 | 98 / 36 | 283 / 58 | 67 / 55 | 140 / 66 |
| Tokens and airdrops | 64 / 720 | 331 / 186 | 119 / 185 | 217 / 155 | 260 / 155 | 266 / 118 | 150 / 138 | 229 / 140 |
| Files | 11 / 149 | 26 / 24 | 14 / 28 | 44 / 23 | 40 / 22 | 59 / 30 | 32 / 22 | 31 / 18 |
| Topics (HCS) | 10 / 106 | 98 / 27 | 71 / 34 | 52 / 50 | 69 / 30 | 76 / 37 | 62 / 31 | 73 / 35 |
| Schedules | 11 / 112 | 16 / 17 | 8 / 9 | 25 / 12 | 25 / 23 | 67 / 23 | 20 / 19 | 29 / 12 |
| Smart contracts and Ethereum | 17 / 175 | 145 / 253 | 135 / 73 | 90 / 203 | 109 / 26 | 162 / 43 | 121 / 40 | 121 / 41 |
| EVM / contract ABI | 36 / 152 | in contracts | in contracts | in contracts | in contracts (14 ABI) | in contracts | in contracts | in contracts |
| Node and admin (freeze, system, nodes, address book) | 39 / 268 | 240 / 34 | 114 / 22 | 77 / 20 | 93 / 13 | 110 / 3 | 51 / 16 | 84 / 17 |
| Batch (HIP-551) | 5 / 5 | 24 / 10 | 9 / 10 | 21 / 9 | 21 / 8 | 24 / 13 | 6 / 8 | 0 / 9 |
| PRNG | 4 / 33 (utility) | 2 / 0 | 1 / 0 | 4 / 1 | 4 / 1 | 10 / 3 | 6 / 1 | 3 / 2 |
| Hooks (HIP-1195) | not specified | 107 / 36 | 277 / 4 | 121 / 15 | 45 / 37 | no API | 78 / 7 | 97 / 13 |
| Keys and crypto (incl. authority) | 49 / 92 | 119 / 0 | 82 / 3 | 157 / 2 | 60 / 0 | 102 / 0 | 79 / 0 | 71 / 0 |
| IDs, addresses, checksums, Hbar | 36 / 114 | 105 / 6 | 91 / 6 | 95 / 0 | 80 / 3 | 207 / 3 | 104 / 0 | 85 / 0 |
| Client, network, execution, retry | 4 / 93 | 122 / 13 | 149 / 27 | 64 / 8 | 13 / 6 | 202 / 19 | 16 / 1 | 26 / 12 |
| Transaction base, receipts, records, serialization | 15 / 113 | 73 / 27 | 181 / 41 | 59 / 27 | 36 / 4 | 88 / 18 | 23 / 4 | 163 / 11 |
| Mirror node client | 59 / 816 (REST, every endpoint) + 11 / 39 HTTP | ≈50 (gRPC topic query, contract call) | 11 / 6 | 32 / 11 | gRPC only, in topics | no API | 9 / 3 | 11 / 8 |
| Fee estimation (HIP-1261) | not specified | 6 / 15 (all disabled) | no API | 10 / 12 | no API | no API | no API | no API |
| Flows | not specified (enterprise services instead) | 2 / 13 | 8 / 3 | (3 / 12) | 14 / 6 | no API | 0 / 7 | 16 / 4 |
| Enterprise services | 40 / 222 | – | – | – | – | – | – | – |

## What V3 covers and what it does not

Covered:

- **The whole API surface of the specs**, type by type: every transaction and query type, the base types, the mirror
  node REST client (every endpoint of the pinned OpenAPI definition) and the enterprise services. Each type has
  generated contract tests; every spec file has spec tests written once for all languages.
- **Published test vectors** wherever they exist, where many V2 tests compare with snapshots of their own earlier
  output: EIP-55 addresses, the examples of the Solidity ABI specification and EIP-165 for the contract ABI, RFC 8032
  for Ed25519 and the key encodings of RFC 8410, RFC 5480 and SEC 1.
- **Network behaviour through the TCK**: 1,591 of 2,327 TCK tests pass against Solo; this is the only place where
  V3 runs against a network.

Not covered (or only thinly):

| Gap | Why |
|---|---|
| Integration tests of its own | By design the TCK is the integration test suite. Everything the TCK does not exercise is never run against a network: the mirror node REST client (apart from `getAddressBook`), the enterprise services, offline signing flows, batches, PRNG, the admin client except nodes. |
| Hooks (HIP-1195), fee estimation (HIP-1261), `TopicMessageSubmit`, the contract queries (`ContractCallQuery`, `ContractInfoQuery`, `ContractBytecodeQuery`), live hash | not specified yet (see `missing-features.md`) |
| Execution paths in unit tests (retry, failover, receipt polling) | 39–42 % branch coverage of the clients; the execution and receipt helpers have 28 hand-written tests (`ExecutionTest`, `DefaultExecutionTest`, `ReceiptsTest`), the paths through gRPC are exercised only by the TCK |
| Enterprise services and mirror repositories behind the network | 44 % / 58 % line coverage; their tests stop at the parameter types |
| TypeScript, Rust, Go, Python implementations | only the Java SDK is implemented; the other languages have generated APIs and (TS, Rust) generated tests that fail on stubs |
| 360 failing TCK tests | mostly values the SDK rejects before sending them (validation annotations, required values) where the TCK expects a status of the network, and the gaps listed in the bindings files |

## What the V2 SDKs cover and what they do not

Common to all V2 SDKs:

- **Unit tests are mostly data tests**: getters/setters and frozen-state checks, snapshot or `toBytes`/`fromBytes`
  round trips, protobuf conversion. In Rust about 365 getter/setter, 86 snapshot and 94 `to_from_bytes` tests; in Java
  about 237 getter/setter, 113 snapshot and 198 bytes tests. The behaviour is tested by the integration tests, which the CI
  of every SDK runs against Solo.
- **Weakest areas in every SDK**: PRNG (1–10 tests), flows (`AccountInfoFlow` has no test in any SDK,
  `EthereumFlow` none in Rust and Swift), the mirror node (only the topic message query and the contract call/estimate
  queries), and retry, backoff and node failover (no behavioural tests in Rust, Swift and C++).
- **Feature differences between the SDKs**: Python has no hooks, no mirror node API, no fee estimation, no flows and
  no key list type; Rust, JS, Swift and C++ have no fee estimation; only Java and Go have fee estimation tests (Java's
  integration tests for it are all disabled).

Per SDK:

| SDK | Strongest | Weakest |
|---|---|---|
| V2 Java | 81.7 % line coverage of the hand-written code; 207 ABI round trips against a deployed contract | slow unit run (3.5 min of real retry delays); all batch, hooks and fee-estimation integration tests disabled; `AccountInfoFlow` untested, `TokenRejectFlow` 0 % unit coverage; queries 40–45 % covered |
| V2 JS | the same unit tests in Node and the browser; widest TCK server (60 methods) | schedules 22.8 % covered and `ScheduleDeleteTransaction` referenced by no test; `SystemUndelete`, `AccountAllowanceAdjust` and `AccountInfoFlow` untested; most queries only integration-tested |
| V2 Go | many table-driven tests over all transaction types (812 sub-tests); the only SDK with fee-estimation tests that run | flaky unit run (race in the mock server); 65.8 % coverage; no e2e tests for freeze, system delete, node delete |
| Rust | fastest unit run; hooks and node/admin well unit-tested | no code coverage; execution path (retry, node selection, chunked execution) has no unit tests; e2e "passes" without a network |
| Python | 93 % line coverage, mock gRPC server through the real execution path | several features missing (hooks, mirror node, fee estimation, flows); topic message query 38 % covered; `query_payment.py` 0 % |
| Swift | no failing, skipped or disabled test | unit tests cannot run on macOS without Xcode; 1,454 compiler warnings in the build; no coverage; transfers only 4 unit tests |
| C++ | test vectors for BIP-39, SLIP-10, RFC 8032 and the mnemonics | not runnable without the vcpkg/CMake toolchain; batch untested (no unit tests, all integration tests disabled); 2 test files not compiled; no coverage |

## Conclusions

1. **V3 has more unit tests than any V2 SDK (3,954) and the fastest run of a Java SDK (6 s against 3 min 17 s)**, but
   most of them are generated contract tests. Measured by line coverage of the unit tests, V3 (69 %) is in the range
   of V2 JS (68 %) and Go (66 %) and below V2 Java (82 %) and Python (93 %); its branch coverage (40 %) is the lowest
   measured, because the network paths are left to the TCK. With the TCK it reaches 73 % of the lines and 47 % of the
   branches.
2. **The TCK replaces the integration tests of the V2 SDKs.** That gives one network test suite for all languages
   instead of one per SDK (the V2 SDKs have 356–721 integration tests each, written separately), but whatever the TCK
   does not exercise is never tested against a network in V3 — in particular the mirror node REST client and the
   enterprise services.
3. **The weak spots of V2 and V3 overlap**: PRNG, flows, retry and failover behaviour and the mirror node are thinly
   tested everywhere. The V3 specs could close some of these with spec tests that every language inherits.
4. **Before V3 replaces a V2 SDK**, the gaps above need an answer: hooks, fee estimation, `TopicMessageSubmit` and the
   contract queries in the specs; network tests (TCK or otherwise) for the mirror node client and the enterprise
   services; and the 360 failing TCK tests, most of which are decisions about validating before sending.

## Reproducing

- V3: `cd sdk-java && ./mvnw -f impl verify` (SDK tests and their coverage), `./mvnw -f impl verify -Ptck` (with
  the TCK against Solo and the coverage of the TCK and of both; the TCK clone is expected at `../hiero-sdk-tck`).
- V2 Java: `./gradlew :sdk:test` (JDK 21), coverage `:sdk:jacocoTestReport`.
- V2 JS: `task test:unit` (here by hand with `npx pnpm@9.15.5`: Vitest with `test/vitest-node.config.ts` and
  `test/vitest-browser.config.ts`).
- V2 Go: `go test ./sdk -tags=unit -race -coverprofile=... -covermode=atomic`.
- Rust: `cargo test --workspace --lib --locked` (needs `protoc` and the `protobufs/services` submodule).
- Python: `pytest tests/unit` (CI sets `TZ=UTC` implicitly through its runners).
- Swift: `HIERO_PROFILE=ciUnit swift test --filter HieroUnitTests` (Linux or Xcode).
- C++: CMake preset with `-DBUILD_TESTS=ON`, then `ctest` (Linux; unit, integration and TCK unit tests run together).

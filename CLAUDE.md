# CLAUDE.md

Guidance for Claude Code (and other agents) when working in this repository.

## What this repository is

This is a **proof-of-concept (PoC) for the next generation ("V3") of client SDKs/libraries** for the
[Hiero](https://hiero.org) project under Linux Foundation Decentralized Trust (LFDT). Because Hedera and other
networks build on Hiero, V3 targets all of them — it is explicitly broader than just Hedera.

The current SDKs are all at version 2; **V3 is the codename for the new generation** that replaces them. V3 is
designed from scratch with **no backward-compatibility constraints** with V2.

**This repo contains specifications, not a shippable SDK.** The specs themselves have no build system and do not
compile. What builds is the spec tooling under `tooling/metalang` (Maven, Java 21) and the hand-written modules of
each language (`sdk-<lang>/support`, ...); the code of every language is **generated** from the specs on demand and
is not part of the repository. The API is defined once in a **language-agnostic meta-language** and is meant to be
translated into idiomatic implementations per language (Java, JavaScript/TypeScript, Go, Rust, Python, C++, Swift). The `.java`/`.js`
files under `guidelines/` are reference snippets, not a buildable module — except `guidelines/rust-files/`: the
single source of the Rust support types (`BoxFuture`, `BoxStream`, `StreamItem`, `InvalidArgumentError`), which the
generator copies 1:1 into `sdk-rust/generated`, so they must always compile. The Java and TypeScript support types are not
copied: they are the hand-written modules `sdk-java/support` (Maven `hiero-sdk-support`, JPMS module
`org.hiero.sdk.support`: `@ThreadSafe`, `HieroStream`, `StreamItem`, `HieroPublisher`, `HieroSubscription`) and
`sdk-ts/support` (npm `@hiero/support`: `Duration`, `StreamItem`, `AbstractConstructor`), on which the generated
modules depend (ADR-0007).

## Repository structure

```
guidelines/
  api-guideline.md              # THE meta-language: syntax + cross-cutting API best practices. Read this first.
  test-language.md              # DRAFT: the language of `tests` blocks in a spec's `## Testing`, from which unit
                                #   tests are generated per language (analysis: docs/spec-defined-tests.md). Parsed,
                                #   resolved (test.* rules) and generated for Java (`<Spec>SpecTest`) and TypeScript
                                #   (`<spec>.spec.test.ts`) so far
  api-best-practices-java.md    # How meta-language concepts map to idiomatic Java
  api-best-practices-rust.md    # ... Rust (the mapping of the Rust generator)
  api-best-practices-go.md      # ... Go (the mapping of the Go generator)
  api-best-practices-python.md  # ... Python (the mapping of the Python generator)
  api-best-practices-js.md      # ... JavaScript
  api-best-practices-ts.md      # ... TypeScript (the mapping of the TypeScript generator)
  rust-files/                   # Rust support types (BoxFuture, InvalidArgumentError, ...); copied 1:1 into sdk-rust/generated
  js-files/                     # Illustrative JS reference snippets

# One folder per target language. Each holds its generator configuration, its hand-written modules and
# its generated modules - the per-language view (ADR-0008). A module stays either wholly generated or
# wholly hand-written (ADR-0007); `generated/` in the path is what marks the generated ones.
# Every `generated/` tree is git-ignored and produced on demand: scripts/generate-<lang>.sh (or
# generate-all.sh) generates and builds it, scripts/clean.sh removes it. Regenerating is always safe.

sdk-java/
  generator.properties          # Java generator configuration (e.g. java.interfaces), see tooling/metalang/README.md
  support/                      # Hand-written Java support types (streaming, thread-safety) as Maven module
                                #   hiero-sdk-support; sdk-java/generated depends on it (install it first)
  test-support/                 # Hand-written Maven module hiero-sdk-test-support (JPMS org.hiero.sdk.test.support):
                                #   the built-in functions and assertions of the test language (class Testing) and
                                #   assertValue of the contract tests; every generated module with tests depends on
                                #   it in test scope (install it first)
  protobuf/                     # Hand-written Maven module hiero-sdk-protobuf (JPMS org.hiero.sdk.protobuf). Has no
                                #   sources of its own: it compiles /tooling/protobuf into Java at build time. NOT public
                                #   API - every package is exported only with `exports ... to <sdk module>`.
                                #   No gRPC codegen; the client builds its io.grpc.MethodDescriptor by hand.
  generated/                    # Generated Java API as Maven project, one sub-module/JAR per Java module
                                #   (scripts/generate-java.sh). `metalang check` verifies that it (or an
                                #   implementation based on it) provides the API of the specs. src/test/java holds
                                #   the generated JUnit tests: the contract tests per type (<Type>Test) and the tests
                                #   of the specs' `tests` blocks (<Spec>SpecTest); tests of method stubs fail until
                                #   the methods are implemented.
  impl/                         # THE Java SDK: a copy of sdk-java/generated (same Maven coordinates and JPMS modules,
                                #   tests included) without generator header and @Generated, maintained BY HAND -
                                #   stub bodies are filled in place, helpers live in non-exported `internal`
                                #   packages. Tracked in git. See docs/rebuilding-the-spike.md.
  tck/generated/                # Generated from tooling/tck/bindings and the converter catalogue (`metalang tck
                                #   generate`, scripts/tck-java.sh). contract/: interfaces the runtime implements;
                                #   server/: the TCK server, compiled against API + contract only.
  tck/runtime/                  # Hand-written runtime of the Java TCK server (JSON-RPC, converters, setup); Maven
                                #   module hiero-sdk-tck-runtime, implements sdk-java/tck/generated/contract

sdk-ts/
  generator.properties          # TypeScript generator configuration (npm scope, version, support package location)
  buf.gen.yaml                  # TypeScript protobuf codegen (buf + protoc-gen-es); `npm run gen:proto-ts`
  package.json                  # THE npm workspace of every TypeScript module below it - a package resolves its
                                #   imports from its real path, so they need a common parent node_modules
  openspec/                     # TypeScript-only OpenSpec root
  support/                      # Hand-written TypeScript support types (Duration, StreamItem, AbstractConstructor)
                                #   as npm package @hiero/support
  test-support/                 # Hand-written npm package @hiero/test-support: the built-in functions and assertions
                                #   of the test language (spec tests, <spec>.spec.test.ts) and assertValue of the
                                #   contract tests; a development dependency of every generated package with tests
  generated/                    # Generated TypeScript API, one package per spec folder, with generated node:test
                                #   tests (scripts/generate-ts.sh)
  tck/generated/                # Generated TCK contract and server (@hiero/tck-contract, @hiero/tck-server)
  tck/runtime/                  # Hand-written TypeScript runtime (@hiero/tck-runtime), implements the contract

sdk-rust/
  generator.properties          # Rust generator configuration (crate prefix, version)
  generated/                    # Generated Rust API as Cargo workspace, one crate per spec folder, with a generated
                                #   integration test per crate (scripts/generate-rust.sh; `cargo test` builds and
                                #   tests it). Support types are copied from guidelines/rust-files.

sdk-go/
  generator.properties          # Go generator configuration (go.module, go.version)
  generated/                    # Generated Go API as ONE Go module (a spec folder is no artifact boundary in Go),
                                #   one package per namespace in a directory per segment (scripts/generate-go.sh).
                                #   `go build ./... && go vet ./... && gofmt -l .` must be clean; the generator
                                #   produces gofmt's alignment itself, nothing in the build needs a Go toolchain.
                                #   Incomplete: no setters, error types, constants, tests, protobuf or TCK part.

sdk-python/
  generator.properties          # Python generator configuration (python.distribution, python.package, python.version)
  generated/                    # Generated Python API as ONE distribution (hiero-sdk, import package hiero_sdk), one
                                #   package per namespace re-exporting its types, one private module per type
                                #   (scripts/generate-python.sh). Every package must import and `mypy --strict src` must be
                                #   clean (Python 3.12+). Incomplete: no constants, errors, tests, protobuf or
                                #   TCK part; types, methods and functions are generated.

# There are exactly two Maven builds - sdk-java/ and tooling/metalang/ - and each carries its own
# Maven wrapper (mvnw, mvnw.cmd, .mvn/) and its own .sdkmanrc (JDK 25). Build from inside one of
# them (`cd sdk-java && ./mvnw -f support install`); never call a locally installed `mvn`. Under
# sdk-java/ every module is built on its own with -f: support, test-support and protobuf before
# generated (scripts/generate-java.sh does it in that order).
# `sdk env` does not search parent directories, which is why the pin is per build and not at the
# root. The root needs none: nothing is built there, and the CLI jar targets release 21.

spec/                           # The actual V3 public-API specifications, written in the meta-language; one spec
                                #   folder is one module/package/crate of the generated code
  base/                         # Foundational namespaces shared by everything
    common.md (common)          #   Page<$$T> and other shared types
    ledger.md (ledger)          #   Ledger, Address, AccountId, TransactionId, ConsensusNode, MirrorNode
    ledger-config.md (ledger.config)  # network settings
    keys.md (keys)              #   Keys + key import/export (PKCS#8, SPKI, DER, PEM); has the first `tests` block
    authority.md (authority)    #   Authorization model (HAPI Key): Authority sum type (public key / contract / m-of-n)
    native-token.md (nativeToken)  # NativeToken abstraction (amount + unit), ExchangeRate
    hedera.md (hedera)          #   Hedera mainnet/testnet settings, Hbar and HbarUnit (the Hedera native token)
    solo.md (solo)              #   the local Solo network as network setting
    token.md (token)            #   HTS token classifier enums (TokenType, TokenSupplyType)
    grpc.md, http.md, proto.md  #   (grpc, http, proto)
  consensus-node-client/        # Talking to the consensus node (namespaces consensusnode.*)
    client.md                   #   HieroClient, Account, TransactionSigner
    transactions.md, queries.md #   Transaction<$$Receipt, $$Self>, Response, Receipt, Record; FreeQuery, PaidQuery
    transactions-*.md, queries-*.md  # accounts, files, schedules, tokens, topics, batch, utility
    transactions-spi.md         #   SPI for custom services/transaction types
    proto.md, proto-accounts.md #   (consensusnode.proto[.account]) the protobuf messages the SPI exposes
  consensus-node-admin-client/  # Network administration (consensusnode.admin.*): freeze, nodes, system, network
  mirror-node-client/           # Querying the Hiero Mirror Node REST API (mirrornode.*), paginated Page<$$T>
  enterprise/                   # High-level "service" layer on top of the raw SDK for app/framework integration
                                #   (enterprise.service.*: account, contract, file, nft, token, topic)

scripts/                        # generate-<lang>.sh / generate-all.sh (generate + build), clean.sh, tck-java.sh,
                                #   tck-ts.sh; documented in scripts/README.md
docs/                           # ADRs (docs/adr), analyses and runbooks (spec-defined-tests.md,
                                #   rebuilding-the-spike.md, tck-binding.md, ...)

tooling/                        # Everything that is neither a specification nor one of the language SDKs

tooling/protobuf/               # THE source of the protobuf definitions of all three node types, vendored at
                                #   pinned versions from their upstream repositories (see its README.md).
                                #   consensus-node/ is the base; block-node/ and mirror-node/ import from it, so
                                #   each directory is one include root and the latter two also need
                                #   consensus-node/ on the path. Never edit by hand: update.sh re-fetches,
                                #   sources.json pins versions + commits, verify.sh compiles every root with
                                #   protoc as the language-neutral proof that the tree resolves.

tooling/tck/                    # The language-NEUTRAL parts of the TCK spike (see docs/tck-binding.md); everything
                                #   language-specific lives in sdk-<lang>/tck/
  bindings/                     #   bindings of TCK methods to the API (`bindings` code blocks in Markdown)
  solo.env, run-tck.sh          #   TCK configuration for a local Solo network (the default) and the script that runs
                                #   the TCK against a generated server

tooling/metalang/               # Prototype tooling, multi-module Maven build (see its README):
  metalang-core/                #   ANTLR grammar, parser, semantic model, validator, shared generator support,
                                #   default instances and tests of the specs (InstanceResolver, TestResolver, the
                                #   built-in functions in resources/.../testing.md), TCK bindings
  metalang-java/                #   Java generator (incl. the spec tests) + Java conformance check + TCK server generator
  metalang-typescript/          #   TypeScript generator + TypeScript conformance check
  metalang-rust/                #   Rust generator + Rust conformance check (rs-api, a syn-based helper)
  metalang-go/                  #   Go generator (types, methods, functions; no tests, no conformance check yet)
  metalang-python/              #   Python generator (types, methods, functions; no tests, no conformance check yet)
  metalang-cli/                 #   command line tool; builds tooling/metalang/target/metalang-*-cli.jar
```

### How the layers relate

- **`base`** — primitives every layer depends on (`ledger`, `keys`, `nativeToken`, `common`, `proto`, `grpc`).
  The native token is modeled as an abstraction (`nativeToken`) with `Hbar` as one concrete implementation (namespace
  `hedera`), so the SDK is not bound to Hedera.
- **`consensusnode.*`** — the low-level client: build/sign/execute transactions against the consensus node. The
  `spi` namespace exists because the consensus node is service-oriented and supports *custom* services and transaction
  types, so the SDK must be extensible (hence `TransactionStatus` is an abstraction with an `int32` code, not a closed
  enum).
- **`mirrornode.*`** — read-side: query historical/state data from a Mirror Node over REST, returning paginated
  `Page<$$T>` results.
- **`enterprise.service.*`** — a higher-level convenience/service layer designed for easy use and deep framework
  integration (cf. Hiero Enterprise Java / JS). Provides factory methods today; real deployments use dependency
  injection.

## The spec meta-language (most important convention)

**Before writing or editing any `spec/*.md`, read `guidelines/api-guideline.md`** — it is the source of truth. Key
points to keep specs valid and consistent:

- Each spec file follows the same skeleton: `# Title` → `## Description` → optional `## Design Notes` →
  `## API Schema` (a fenced code block) → optional `## Default Instances for Testing` → optional `## Examples` → optional
  `## Testing` → `## Questions & Comments`.
- `## Testing` may hold `tests` code blocks in the test language of `guidelines/test-language.md`
  (`test "name" { let key = DEFAULT(PrivateKey) ... assertEquals(expected: ..., actual: ...) }`). The validator
  resolves them against the model (`test.*` rules); the Java generator turns them into `<Spec>SpecTest` classes.
  Expected values must come from the spec itself or a published test vector, never from a value computed for the
  test (see the guideline's `## Questions & Comments`). Every test has a stable `@@testId("kebab-id")`, unique per
  namespace (a tooling test checks it): it identifies the test across languages in the test results.
- `## Default Instances for Testing` records the standard way to obtain an instance of the spec's types through the API
  (`instance PublicKey = DEFAULT(PrivateKey).createPublicKey()`), with concrete values where implementations check them
  (keys, addresses). Generated tests use them first; the validator warns (`instance.missing`) about types that cannot
  be obtained at all. See "Default instances" in `guidelines/api-guideline.md`. The same block declares **named test
  values** that only the tests of the same spec share (`values MALFORMED_ENTITY_IDS: list<string> = [...]`, read as
  `for text in MALFORMED_ENTITY_IDS`); see "Named test values" in `guidelines/test-language.md`.
- `## Description` and the comments directly above declarations become the **public API documentation** (Javadoc,
  rustdoc, …): write them for SDK users. Spec-author rationale, links to other spec files, ADR references and
  meta-language details go to `## Design Notes` (resolved) or `## Questions & Comments` (open). The validator warns
  about leaks (`doc.internal-reference`).
- The `## API Schema` block opens with `namespace <name>`. Types from other namespaces are imported explicitly, one
  statement per source namespace: `requires {Address, Ledger} from ledger`. Import only what is used; `requires {*}
  from ns` imports everything.
- **Imported (and same-namespace) types are referenced by their simple name** (`Address`, `Page`) — like an ES6/Java
  import. The qualified form `namespace.Type` is only used to disambiguate a name collision between two namespaces.
- **Naming:** Types `PascalCase`; fields & methods `lowerCamelCase`; enum values `UPPER_SNAKE_CASE`; namespaces
  `lowerCamelCase` (dots for sub-namespaces, no hyphens); constants `UPPER_SNAKE_CASE`; `@@throws` error ids
  `lowercase-kebab-case` (e.g. `not-found-error`).
- **Generic type parameters are prefixed with `$$`** (e.g. `Page<$$T>`, `Transaction<$$Receipt extends Receipt>`).
- **Annotations use a `@@` prefix.** Common ones: `@@immutable`, `@@nullable`, `@@deprecated`, `@@default(v)`,
  `@@static`, `@@async`,
  `@@streaming`, `@@throws(...)`, `@@threadSafe[(group)]`, validation (`@@min/@@max/@@minLength/...`), and type-level
  `@@oneOf(...)`, `@@oneOrNoneOf(...)`, `@@finalType`.
- Abstract types use `abstraction`; inheritance uses `extends`; enums use `enum`.

### Cross-cutting design principles (enforce these in specs and reviews)

- **Immutability first:** annotate fields `@@immutable` by default; introduce mutability only with a clear reason.
- **Never define nullable collections** (`list`/`set`/`map`) — return empty instead of null.
- **Avoid `ANY` as a standalone type** — prefer a generic `$$T`, a concrete base type, a `@@oneOf` union, or `bytes`
  with a documented schema. (`ANY` as a *wildcard type argument* like `ContractParam<ANY>` is fine.)
- `@@async` returns a future/promise; `@@streaming` returns a pull-based async stream of items (`streamResult<TYPE>`
  for per-item errors). They are mutually exclusive.

## Protobuf is public API only where the SPI exposes it

All three languages generate protobuf code from `tooling/protobuf` at build time, and almost none of
it is part of the public API - the wire format changes with every network release, so exposing it
would make every protocol change a breaking change of the SDK. Nothing generated is committed.

| | Generated into | Kept internal by |
|---|---|---|
| Java | `sdk-java/protobuf` | `exports ... to ...` in `module-info.java`, naming only the SDK modules |
| Rust | `OUT_DIR`, included by `crates/consensus-node-client/src/proto.rs` | `mod proto;` without `pub` |
| TypeScript | `packages/consensus-node-client/src/internal/proto` | subpath absent from `exports` in `package.json` |

Java is the only one that needs a separate module: JPMS forbids split packages, so the protobuf
packages must have exactly one owner. Rust and TypeScript keep the code inside the consuming crate
resp. package. See `tooling/protobuf/README.md`, "Language bindings".

**The exception is the SPI** (`consensusnode.transactions.spi`): `TransactionSupport` hands the raw
transaction body, response, receipt and record to whoever adds a custom service, so those four
messages *are* API. The specs name them in the namespace `consensusnode.proto`; a generator that
supports it maps that namespace to the generated protobuf code instead of generating placeholders
beside it - `java.externalNamespace.consensusnode.proto = com.hederahashgraph.api.proto.java`. In
Java this makes `com.hederahashgraph.api.proto.java` an unqualified export and the
`requires org.hiero.sdk.protobuf` of the client module `transitive`; the generator derives both from
the configuration, so a regeneration preserves it. TypeScript, Rust and Go have no key yet and still
generate the placeholders. See [ADR-0009](docs/adr/0009-protobuf-messages-in-the-spi.md).

**The generators own the rest of this wiring** too: `java.protobuf`, `ts.protobuf` and
`rust.protobuf` in the three `generator.properties` list the spec folders that need it (Rust also has
`rust.protobufRoot`). Without the key nothing protobuf-related is generated.

## The Java implementation (`sdk-java/impl`)

The generators produce the API only (stub bodies). The Java SDK itself is `sdk-java/impl`: the generated project was
copied once, the generator header and `@Generated` were removed, and since then it is ordinary hand-written code -
including the copied tests. Generation is an aid to bring spec changes correctly into the API, not the SDK. **How
later spec changes reach `impl` is not decided yet** (today: regenerate, compare, carry over by hand;
`metalang check --project=sdk-java/impl` reports where `impl` no longer provides the API of the specs). Do not edit
`sdk-java/generated` to implement something, and do not resolve the update workflow silently.

`impl` started from the `createAccount` spike of the Hiero TCK (keys -> protobuf -> gRPC -> signing -> receipt
polling), which had been written into the generated code in place and reached 34 of the 42 tests of
`test-account-create-transaction.ts` against a local Solo network, in Java and TypeScript; that spike exists only in
git history (`3b5305d~1`, introduced in `3caad63 "Durchstich :)"`), and the TypeScript one has not been ported. Today
`impl` implements the **whole API**: every transaction type (one `TransactionKind` per type in an area class such as
`TokenKinds`, all sharing `Packing`; the admin client registers its types as `TransactionSupport` via the
`ServiceLoader`), every query (`QueryKind` + `Querying`, with query payment), the mirror node REST client, the base
utilities and the enterprise services. The specs have 386 spec tests (45 spec files, 88 of them check that an invalid
value is rejected); **all 386 and all 2,787 contract tests pass** against `impl`, and `metalang check` reports that it
provides the API. What the specs cannot express is recorded in their `## Questions & Comments`. It has the same
Maven coordinates as `generated`, so install it after `scripts/generate-java.sh` (`scripts/tck-java.sh` does). The
TCK binds 47 of its 65 methods (`tooling/tck/bindings`) but has not been run against `impl` yet (it needs Solo, i.e.
Docker). [`docs/rebuilding-the-spike.md`](docs/rebuilding-the-spike.md) is the
runbook: how the copy was made, file inventory, filled stubs, build and verify, the TCK, and the traps.

Four spec problems made the implementation harder than it should be; they are recorded in the `## Questions &
Comments` of the respective spec files and are its real output:

1. `HieroClient` is a record and `Network` carries no consensus nodes, so a client cannot reach its
   network - the `NetworkSetting` passed to `ClientFactory` is dropped.
2. `Response` is a record holding only a `TransactionId`, yet `queryReceipt()` has to reach the network.
3. `TransactionId.generateTransactionId` takes an `Address`, but `TransactionId` holds an `AccountId`.
4. `BasicTransactionStatus` has no `SUCCESS`; the protocol value is 22.

`ClientRuntime` (in `impl`) bridges (1) and (2) with process-wide registries - a workaround, not a design.

## How to make changes

- **Editing/adding a spec:** keep the section skeleton, declare the `namespace` and import external types with
  `requires {Type} from ns`, reference them by simple name, and follow the naming + annotation rules above. Match the
  style of neighboring spec files. Validate the result with the spec tooling (see `tooling/metalang/README.md`):
  `./mvnw -q package -DskipTests` then
  `java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar validate spec`. A change must not introduce
  new `syntax.error` or ERROR findings.
- **Changing the meta-language itself** (guideline syntax, new annotation): update the grammar
  (`MetaLang.g4`), `KnownAnnotation`, the `Rule` catalog and the tests in `tooling/metalang` in the same change. Every
  new rule needs a fixture in `tooling/metalang/metalang-core/src/test/resources/rule-fixtures/<rule-id>/` (enforced by
  `RuleFixturesTest`).
- **Generated vs. hand-written:** every module is either completely generated (under `sdk-<lang>/generated/`, every file carries
  the generator header, never edited by hand) or completely hand-written; generated code never contains copies of
  hand-written code. Hand-written code that generated code needs is a support module (`sdk-java/support`,
  `sdk-java/test-support`, `sdk-ts/support`) or implements a generated contract — see
  [ADR-0007](docs/adr/0007-separate-generated-and-hand-written-modules.md). To look at the output of a generator
  change without touching `sdk-<lang>/generated`, generate into a scratch directory (`--output=<dir>`).
- **Changing the test language** (`guidelines/test-language.md`): the grammar (start rule `tests`), `TestResolver` /
  the test mode of `InstanceResolver`, the `test.*` rules with fixtures, and — for a built-in function — the schema in
  the guideline **and** its copy `metalang-core/src/main/resources/org/hiero/sdk/v3/metalang/testing.md` (a test keeps
  both equal) plus its implementation in `sdk-java/test-support` (`Testing`) and the Java mapping in
  `SpecTestGenerator` / `api-best-practices-java.md`, and the same for TypeScript (`sdk-ts/test-support`,
  `TsSpecTestGenerator`, `api-best-practices-ts.md`). Java and TypeScript generate spec tests so far.
- **Test results** are written as Open Test Reporting next to the usual reports
  ([ADR-0012](docs/adr/0012-open-test-reporting-as-common-test-result-format.md)): by the Java builds
  (`open-test-report.xml` in `target/surefire-reports`, every test tagged `testId:<id>`) and by the TCK runs
  (`tooling/tck/otr-mocha-reporter.cjs`, into `sdk-<lang>/tck/target/`); `scripts/test-report.sh` renders them as
  HTML, separately for the SDK (with its last TCK run), the generated API and the tooling. TypeScript and Rust
  follow; the report with the matrix spec test × language and the classification passed / stub / network / other is
  a required follow-up.
- **Open design questions** belong under each file's `## Questions & Comments` (often attributed to a GitHub handle).
  Don't silently resolve them; surface them.
- **Language best-practice docs** (`api-best-practices-*.md`) describe how a meta-language concept maps to one
  language — when you add a new meta-language feature, consider whether each language guide needs a mapping (the
  guideline lists per-language mappings for varargs, wildcards, streaming cancellation, `streamResult`, etc.).
- Note: `api-guideline.md` references a `proposals/` folder, but in this repo the specs live under `spec/`.
- Some language guides referenced by `api-guideline.md` (cpp, swift) do not exist yet — that's
  expected; Java, TypeScript, Rust, JS, Go and Python guides are present so far. Five of them are implemented by a
  generator in `tooling/metalang`: `generate --language=java|ts|rust|go|python`, and `check --language=java|ts|rust`
  (there is no Go or Python conformance check yet, so `check` rejects both — asserted by tests in `metalang-cli`).
- **The Go generator is the youngest and is incomplete.** It emits the module, one package per namespace, the
  declared types (structs with constructor and getters, interfaces for abstractions, both enum shapes) and their
  methods and functions (value receiver, `panic("Not implemented yet: …")` body, `(T, error)` for `@@throws`,
  `ctx context.Context` for `@@async`, `iter.Seq2` for `@@streaming`, package-level functions for `@@static` and
  for a namespace); setters, the declared error identifiers, namespace constants, generated tests, the protobuf
  wiring and the TCK part are still missing. A declaration it
  cannot express is **left out** and reported by `--show-deferred`, so `sdk-go/generated` always compiles; on the
  current specs nothing is deferred. Writing it corrected four decisions in `api-best-practices-go.md` that the
  Go compiler rejected (no inherited state via an embedded base struct, self types dropped, concrete type
  parameter bounds widened to `any`, maps copied like slices) — see "Guideline decisions driven by the tooling"
  in `tooling/metalang/README.md`.
- **The Python generator is as young as the Go one**, with the same scope: the distribution, one package per
  namespace, the declared types (frozen dataclasses, `typing.Protocol` for abstractions, union aliases for
  `@@sealed`, both enum shapes) and their methods and functions (`...` in a protocol, `NotImplementedError`
  elsewhere, `async def`, `AsyncIterator`, `@staticmethod`, `_functions.py` per namespace, `typing.overload` for an
  overload set). It follows `api-best-practices-python.md` (Python 3.12, PEP 695 generics,
  `kw_only` dataclasses, `async def` only). Abstractions are protocols because a dataclass cannot inherit a property
  of the same name as one of its attributes. Verify a change with `mypy --strict src` in `sdk-python/generated` — the
  generator tests run it when `-Dpython.executable=<python with mypy>` is given, and always import every package.
- **`api-best-practices-js.md` and `api-best-practices-ts.md` describe the same runtime** and must not
  contradict each other: TypeScript output is consumed by JavaScript callers. Where they disagree today, the
  `## Questions & Comments` of the TypeScript guide records it.

## Relevant skills

When working here, these skills are particularly useful:

- **`java-api-design`** and **`java-best-pratices`** — when reasoning about API surface, SPI, interfaces vs. records,
  module boundaries, breaking changes.
- **`modern-java`** — when writing or reviewing the Java support types in `sdk-java/support/` (use modern idioms:
  records, sealed types, pattern matching, etc.).
- **`adr-create`** — when a genuine architectural decision is made, record it as an ADR.
- **`hiero-info`** / **`hedera-info`** — background when writing prose about Hiero/Hedera concepts.
- **`grill-me`** — to stress-test an API design before committing to it.

## Audience & language

The repository (README, guidelines, specs) is written in **English** for an international open-source audience — keep
new docs in English. Commit messages and PR descriptions in English as well.

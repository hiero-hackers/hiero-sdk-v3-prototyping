# CLAUDE.md

Guidance for Claude Code (and other agents) when working in this repository.

## What this repository is

This is a **proof-of-concept (PoC) for the next generation ("V3") of client SDKs/libraries** for the
[Hiero](https://hiero.org) project under Linux Foundation Decentralized Trust (LFDT). Because Hedera and other
networks build on Hiero, V3 targets all of them — it is explicitly broader than just Hedera.

The current SDKs are all at version 2; **V3 is the codename for the new generation** that replaces them. V3 is
designed from scratch with **no backward-compatibility constraints** with V2.

**This repo contains specifications, not a shippable SDK.** The specs themselves have no build system and do not
compile; the only buildable module is the spec tooling under `tooling/metalang` (Maven, Java 21). The API is defined once in a **language-agnostic meta-language** and is meant to be translated into
idiomatic implementations per language (Java, JavaScript/TypeScript, Go, Rust, Python, C++, Swift). The `.java`/`.js`
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
  api-best-practices-java.md    # How meta-language concepts map to idiomatic Java
  api-best-practices-rust.md    # ... Rust (the mapping of the Rust generator)
  api-best-practices-go.md      # ... Go (the mapping of the Go generator)
  api-best-practices-js.md      # ... JavaScript
  api-best-practices-ts.md      # ... TypeScript (the mapping of the TypeScript generator)
  rust-files/                   # Rust support types (BoxFuture, InvalidArgumentError, ...); copied 1:1 into sdk-rust/generated
  js-files/                     # Illustrative JS reference snippets

protobuf/                       # THE source of the protobuf definitions of all three node types, vendored at
                                #   pinned versions from their upstream repositories (see protobuf/README.md).
                                #   consensus-node/ is the base; block-node/ and mirror-node/ import from it, so
                                #   each directory is one include root and the latter two also need
                                #   consensus-node/ on the path. Never edit by hand: update.sh re-fetches,
                                #   sources.json pins versions + commits, verify.sh compiles every root with
                                #   protoc as the language-neutral proof that the tree resolves.

# One folder per target language. Each holds its generator configuration, its hand-written modules and
# its generated modules - the per-language view (ADR-0008). A module stays either wholly generated or
# wholly hand-written (ADR-0007); `generated/` in the path is what marks the generated ones.

sdk-java/
  generator.properties          # Java generator configuration (e.g. java.interfaces), see tooling/metalang/README.md
  support/                      # Hand-written Java support types (streaming, thread-safety) as Maven module
                                #   hiero-sdk-support; sdk-java/generated depends on it (install it first)
  protobuf/                     # Hand-written Maven module hiero-sdk-protobuf (JPMS org.hiero.sdk.protobuf). Has no
                                #   sources of its own: it compiles /protobuf into Java at build time. NOT public
                                #   API - every package is exported only with `exports ... to <sdk module>`.
                                #   No gRPC codegen; the client builds its io.grpc.MethodDescriptor by hand.
  generated/                    # Generated Java API as Maven project, one sub-module/JAR per Java module (tracked in
                                #   git; regenerate after spec or generator changes, commands in
                                #   tooling/metalang/README.md). !! CURRENTLY HAND-EDITED, DO NOT REGENERATE - see
                                #   "The AccountCreateTransaction spike" below. `metalang check` verifies
                                #   that it (or an implementation based on it) provides the API of the specs.
                                #   src/test/java holds generated JUnit tests of the spec contract; the tests of
                                #   method stubs fail until the methods are implemented.
  tck/generated/                # Generated from tck/bindings and the converter catalogue (tracked in git; `metalang
                                #   tck generate`). Never edit by hand. contract/: interfaces the runtime
                                #   implements; server/: the TCK server, compiled against API + contract only.
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
  generated/                    # Generated TypeScript API, one package per spec folder, with generated node:test
                                #   tests (tracked in git). !! ALSO HAND-EDITED by the spike, DO NOT REGENERATE.
  tck/generated/                # Generated TCK contract and server (@hiero/tck-contract, @hiero/tck-server)
  tck/runtime/                  # Hand-written TypeScript runtime (@hiero/tck-runtime), implements the contract

sdk-rust/
  generator.properties          # Rust generator configuration (crate prefix, version)
  generated/                    # Generated Rust API as Cargo workspace, one crate per spec folder, with a generated
                                #   integration test per crate (tracked in git; `cargo test` builds and tests it).
                                #   Never edit by hand. Support types are copied from guidelines/rust-files.

sdk-go/
  generator.properties          # Go generator configuration (go.module, go.version)
  generated/                    # Generated Go API as ONE Go module (a spec folder is no artifact boundary in Go),
                                #   one package per namespace in a directory per segment. Never edit by hand.
                                #   `go build ./... && go vet ./... && gofmt -l .` must be clean; the generator
                                #   produces gofmt's alignment itself, nothing in the build needs a Go toolchain.
                                #   Incomplete: types only - no methods, functions, tests, protobuf or TCK part.

tck/                            # The language-NEUTRAL parts of the TCK spike (see docs/tck-binding.md); everything
                                #   language-specific lives in sdk-<lang>/tck/
  bindings/                     #   bindings of TCK methods to the API (`bindings` code blocks in Markdown)
  solo.env, run-tck.sh          #   TCK configuration for a local Solo network (the default) and the script that runs
                                #   the TCK against a generated server

# There are exactly two Maven builds - sdk-java/ and tooling/metalang/ - and each carries its own
# Maven wrapper (mvnw, mvnw.cmd, .mvn/) and its own .sdkmanrc (JDK 25). Build from inside one of
# them (`cd sdk-java && ./mvnw -f support install`); never call a locally installed `mvn`.
# `sdk env` does not search parent directories, which is why the pin is per build and not at the
# root. The root needs none: nothing is built there, and the CLI jar targets release 21.

spec/                           # The actual V3 public-API specifications, written in the meta-language
  base/                         # Foundational namespaces shared by everything
    common.md (common)          #   Page<$$T> and other shared types
    ledger.md (ledger)          #   Ledger, Address, ConsensusNode, MirrorNode
    ledger-config.md (ledger.config)
    keys.md (keys)              #   Keys + key import/export (PKCS#8, SPKI, DER, PEM)
    authority.md (authority) #  Authorization model (HAPI Key): Authority sum type (public key / contract / m-of-n)
    native-token.md (nativeToken + hbar)  # NativeToken abstraction + Hedera-specific HBAR implementation
    token.md (token)              #   HTS token classifier enums (TokenType, TokenSupplyType); future home for TokenId / NftId
    grpc.md (grpc)
    proto.md (proto)
  consensus-node-client/        # Talking to the consensus node
    client.md (consensusnode.client)              # HieroClient, Account, TransactionSigner
    transactions.md (consensusnode.transactions)  # Transaction<$$Receipt, $$Self>, Response, Receipt, Record
    transactions-accounts.md (consensusnode.transactions.accounts)
    transactions-spi.md (consensusnode.transactions.spi)   # SPI for custom services/transaction types
    proto.md / proto-accounts.md (consensusnode.proto[.account])
  mirror-node-client/           # Querying the Hiero Mirror Node REST API
    mirror-node.md (mirrornode) # MirrorNodeClient with per-domain repositories
    mirror-node-{account,contract,network,nft,token,topic,transaction,common}.md
  enterprise/                   # High-level "service" layer on top of the raw SDK for app/framework integration
    service.md (enterprise.service)
    service-account.md (enterprise.service.account)
    service-contract.md (enterprise.service.contract)

tooling/metalang/               # Prototype tooling, multi-module Maven build (see its README):
  metalang-core/                #   ANTLR grammar, parser, semantic model, validator, shared generator support,
                                #   TCK bindings (parser, resolver, coverage check)
  metalang-java/                #   Java generator + Java conformance check + Java TCK server generator
  metalang-typescript/          #   TypeScript generator + TypeScript conformance check
  metalang-rust/                #   Rust generator + Rust conformance check (rs-api, a syn-based helper)
  metalang-go/                  #   Go generator (types only so far; no conformance check yet)
  metalang-cli/                 #   command line tool; builds tooling/metalang/target/metalang-*-cli.jar
```

### How the layers relate

- **`base`** — primitives every layer depends on (`ledger`, `keys`, `nativeToken`/`hbar`, `common`, `proto`, `grpc`).
  The native token is modeled as an abstraction (`nativeToken`) with HBAR as one concrete implementation (`hbar`), so
  the SDK is not bound to Hedera.
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
  `## API Schema` (a fenced code block) → optional `## Default Instances` → optional `## Examples` → optional
  `## Testing` → `## Questions & Comments`.
- `## Default Instances` records the standard way to obtain an instance of the spec's types through the API
  (`instance PublicKey = DEFAULT(PrivateKey).createPublicKey()`), with concrete values where implementations check them
  (keys, addresses). Generated tests use them first; the validator warns (`instance.missing`) about types that cannot
  be obtained at all. See "Default instances" in `guidelines/api-guideline.md`.
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

## Protobuf is never public API

All three languages generate protobuf code from `/protobuf` at build time, and in none of them is the
result part of the public API - the wire format changes with every network release, so exposing it
would make every protocol change a breaking change of the SDK. Nothing generated is committed.

| | Generated into | Kept internal by |
|---|---|---|
| Java | `sdk-java/protobuf` | `exports ... to ...` in `module-info.java`, naming only the SDK modules |
| Rust | `OUT_DIR`, included by `crates/consensus-node-client/src/proto.rs` | `mod proto;` without `pub` |
| TypeScript | `packages/consensus-node-client/src/internal/proto` | subpath absent from `exports` in `package.json` |

Java is the only one that needs a separate module: JPMS forbids split packages, so the protobuf
packages must have exactly one owner. Rust and TypeScript keep the code inside the consuming crate
resp. package. See `protobuf/README.md`, "Language bindings".

**The generators own this wiring**, so it survives a regeneration: `java.protobuf`, `ts.protobuf` and
`rust.protobuf` in the three `generator.properties` list the spec folders that need it (Rust also has
`rust.protobufRoot`). Without the key nothing protobuf-related is generated.

## The AccountCreateTransaction spike

**`sdk-java/generated` AND `sdk-ts/generated` are hand-edited and must not be regenerated.** Running
`metalang generate` over either overwrites every file that carries the generator header, so all method
bodies of the spike are lost. The hand-written files in the `internal` packages carry no header and
survive - orphaned, and the module no longer compiles.

`sdk-ts/generated` is affected exactly like `sdk-java/generated`: the TypeScript spike filled method bodies in
generated files (e.g. `packages/base/src/keys/functions.ts`) and added
`consensusnode/transactions/HapiTransactionStatus.ts` **with the generator header**, which makes the
generator delete it as stale. A regeneration reintroduces ~36 `Not implemented yet` stubs. Only
`sdk-rust/generated` and `sdk-go/generated` are safe.

To verify a generator change, generate into a throwaway directory
(`--output=/tmp/ts-out`) and inspect that, instead of regenerating in place.

**If it is gone, rebuild it with
[`docs/rebuilding-the-spike.md`](docs/rebuilding-the-spike.md)** - a runbook with the file
inventory, the stubs to fill, the build order and the 14 traps that cost time the first time. Start
there, not from scratch: the spike is in git (`3caad63 "Durchstich :)"`) and restoring beats
retyping.

To find out what implementing the V3 API actually costs, the `createAccount` path of the Hiero TCK was
implemented end to end (keys -> protobuf -> gRPC -> signing -> receipt polling), in **Java and
TypeScript**. Both pass 34 of the 42 tests of `test-account-create-transaction.ts` against a local
Solo network, with the same eight failures. Rust has no slice: `metalang tck generate` supports only
`--language=java|ts`, so there is nothing to measure one against yet.

ADR-0007 leaves open where an implementation of the API may live, and the generated shapes rule out
every option except editing in place: `AccountCreateTransaction` is `final` with stubbed `pack`/`sign`
overrides, `ClientFactory`/`KeysFactory`/`AuthorityFactory` are `final` with private constructors and
static stubs. The decision for this spike was therefore to edit the generated classes directly and to
settle the architecture afterwards, informed by what the spike found.

The conformance check stays green throughout: `metalang check` allows method bodies, additional types
and additional `requires`/`exports`, so only bodies were filled and types were added, never changed.

What the spike added inside `sdk-java/generated` (everything else is untouched):

- `org.hiero.keys.internal` - Ed25519 and ECDSA secp256k1 over BouncyCastle, PKCS#8/SPKI DER by OID
- `org.hiero.ledger.internal.DefaultTransactionId`, `org.hiero.nativeToken.internal.DefaultExchangeRate`
- `org.hiero.consensusnode.client.internal` - `ClientRuntime` (gRPC channels, node selection, receipt
  polling), `Protobuf`, `Grpc`, `DefaultPackedTransaction`
- `org.hiero.consensusnode.transactions.HapiTransactionStatus` - 350 constants from `response_code.proto`

Four spec problems made this harder than it should be; they are recorded in the `## Questions &
Comments` of the respective spec files and are the real output of the spike:

1. `HieroClient` is a record and `Network` carries no consensus nodes, so a client cannot reach its
   network - the `NetworkSetting` passed to `ClientFactory` is dropped.
2. `Response` is a record holding only a `TransactionId`, yet `queryReceipt()` has to reach the network.
3. `TransactionId.generateTransactionId` takes an `Address`, but `TransactionId` holds an `AccountId`.
4. `BasicTransactionStatus` has no `SUCCESS`; the protocol value is 22.

(1) and (2) are bridged by a process-wide registry in `ClientRuntime` - a workaround, not a design.

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
  hand-written code. Hand-written code that generated code needs is a support module or implements a generated
  contract — see [ADR-0007](docs/adr/0007-separate-generated-and-hand-written-modules.md).
- **Open design questions** belong under each file's `## Questions & Comments` (often attributed to a GitHub handle).
  Don't silently resolve them; surface them.
- **Language best-practice docs** (`api-best-practices-*.md`) describe how a meta-language concept maps to one
  language — when you add a new meta-language feature, consider whether each language guide needs a mapping (the
  guideline lists per-language mappings for varargs, wildcards, streaming cancellation, `streamResult`, etc.).
- Note: `api-guideline.md` references a `proposals/` folder, but in this repo the specs live under `spec/`.
- Some language guides referenced by `api-guideline.md` (cpp, python, swift) do not exist yet — that's
  expected; Java, TypeScript, Rust, JS and Go guides are present so far. Four of them are implemented by a
  generator in `tooling/metalang`: `generate --language=java|ts|rust|go`, and `check --language=java|ts|rust`
  (there is no Go conformance check yet, so `check` rejects `go` — asserted by tests in `metalang-cli`).
- **The Go generator is the youngest and is incomplete.** It emits the module, one package per namespace and the
  declared types (structs with constructor and getters, interfaces for abstractions, both enum shapes); methods,
  namespace functions, generated tests, the protobuf wiring and the TCK part are still missing. A declaration it
  cannot express is **left out** and reported by `--show-deferred`, so `sdk-go/generated` always compiles; on the
  current specs nothing is deferred. Writing it corrected four decisions in `api-best-practices-go.md` that the
  Go compiler rejected (no inherited state via an embedded base struct, self types dropped, concrete type
  parameter bounds widened to `any`, maps copied like slices) — see "Guideline decisions driven by the tooling"
  in `tooling/metalang/README.md`.
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

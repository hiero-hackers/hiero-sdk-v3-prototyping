# hiero-sdk-v3-prototyping

A prototyping ground for the **next generation of client SDKs ("V3")** for the
[Hiero](https://hiero.org) project under Linux Foundation Decentralized Trust (LFDT). Since Hedera and other networks
build on Hiero, V3 is designed for the whole ecosystem — not just Hedera.

This repository does **not** contain a shippable SDK. It holds the **language-agnostic API specification** for V3,
the conventions used to write that specification, and the per-language best-practice guides that translate it into
idiomatic implementations.

## Why V3?

The current Hiero SDKs were originally built for Hedera and the Hedera network. With the move of the Hedera codebase
to Hiero, the project has become much broader in scope. The existing SDKs (all at version 2) carry limitations that no
longer align with where the project is heading:

- **Scoped to Hedera** — they were designed for the Hedera network and don't reflect Hiero's broader scope.
- **Language idioms not fully embraced** — some SDKs feel foreign to developers experienced in that language, causing
  avoidable friction.
- **Outdated architecture** — they have not kept pace with the modularization of the consensus node and support for
  custom services and transactions.
- **Developer experience** — developers deserve SDKs that are intuitive, consistent across languages, and aligned with
  modern API design principles.

## Vision

V3 is an opportunity to design the ideal SDK from the ground up, without backward-compatibility constraints. The goal
is a public API that is:

- **Language-idiomatic** — each SDK feels natural in its language (generics, type inference, idiomatic error handling).
- **Consistent across languages** — concrete APIs adapt per language, but the structure, concepts, and workflows stay
  consistent.
- **Future-proof** — the architecture accommodates Hiero's evolution (custom services, modular consensus nodes, new
  transaction types) without breaking changes.
- **Accessible and well-documented** — clear guidelines, comprehensive docs, and example-driven design.
- **Broader than Hedera** — works with any Hiero-based network.

Beyond the consensus node, V3 also targets client APIs for the **Hiero Mirror Node** and **Hiero Block Node**, and is
designed with **framework integration** (e.g. Hiero Enterprise Java / JS) in mind from the start.

## Approach

- **Language-agnostic API definition** — the public API is specified once in a
  [meta-language](guidelines/api-guideline.md) and translated into concrete implementations per language.
- **Prototype-driven design** — rather than designing purely on paper, we draft the API and validate it through
  prototypes to ensure the design works in practice.
- **No backward-compatibility constraints** — V3 is designed as an ideal API. Migration paths from V2 will be
  addressed once concrete MVPs are available and the design has stabilized.

## Repository layout

| Path | Contents |
|------|----------|
| [`guidelines/api-guideline.md`](guidelines/api-guideline.md) | The meta-language: syntax + cross-cutting API best practices. **Start here.** |
| [`guidelines/api-best-practices-java.md`](guidelines/api-best-practices-java.md) | How the meta-language maps to idiomatic Java |
| [`guidelines/api-best-practices-rust.md`](guidelines/api-best-practices-rust.md) | ... Rust |
| [`guidelines/api-best-practices-go.md`](guidelines/api-best-practices-go.md) | ... Go (the mapping, written ahead of its generator) |
| [`guidelines/api-best-practices-python.md`](guidelines/api-best-practices-python.md) | ... Python (the mapping of the Python generator) |
| [`guidelines/api-best-practices-ts.md`](guidelines/api-best-practices-ts.md) | ... TypeScript (the mapping of the TypeScript generator) |
| [`guidelines/api-best-practices-js.md`](guidelines/api-best-practices-js.md) | ... using the resulting API from JavaScript |
| `guidelines/js-files/` | Illustrative reference snippets (not a buildable module) |
| `sdk-java/`, `sdk-ts/`, `sdk-rust/`, `sdk-go/`, `sdk-python/` | One folder per target language: its generator configuration and its hand-written modules; the generated ones are produced into `generated/` (see [ADR-0008](docs/adr/0008-one-folder-per-target-language.md)) |
| `sdk-<lang>/generated/` | Not part of the repository: the generated API is produced from the specs on demand, see [Generating the native API code](#generating-the-native-api-code) |
| `sdk-java/support/`, `sdk-ts/support/` | Hand-written support types the generated API depends on (`@ThreadSafe`, streaming; `Duration`, `StreamItem`, `AbstractConstructor`) |
| `sdk-<lang>/tck/` | The language-specific TCK parts: `runtime/` (hand-written); `generated/` (contract + server) is produced by the generator like the API |
| `tooling/tck/` | The language-neutral TCK parts: the bindings, the Solo configuration and `run-tck.sh` |
| [`scripts/`](scripts) | One script per task: generate (and build) one language or all of them, run the TCK against the Java or the TypeScript server |
| [`docs/pipeline.html`](docs/pipeline.html) | How the generator, the protobuf tree and the TCK server fit together — folder map and diagrams (open it in a browser) |
| [`docs/missing-features.md`](docs/missing-features.md) | What the V3 specs do not cover yet, measured against the v2 SDKs — the feature backlog |
| [`docs/TODO.md`](docs/TODO.md) | Open follow-up tasks on the guides, the meta-language and the specs |
| [`docs/TCK.md`](docs/TCK.md), [`docs/tck-binding.md`](docs/tck-binding.md), [`docs/tck-ideas.md`](docs/tck-ideas.md) | Three approaches to testing V3 against the Hiero TCK; `tck-binding.md` is the one with a spike |
| [`docs/adr/`](docs/adr) | Architecture decision records |
| [`tooling/metalang/`](tooling/metalang) | The spec tooling: parser, validator and the generators for Java, TypeScript, Rust, Go and Python, plus the `metalang` CLI |
| [`tooling/protobuf/`](tooling/protobuf) | The protobuf definitions of consensus, block and mirror node, vendored at pinned versions — one source for every generator |
| `spec/base/` | Foundational namespaces: `ledger`, `keys`, `hbar`, `common`, `proto`, `grpc` |
| `spec/consensus-node-client/` | Low-level client: build, sign, and execute transactions (incl. an SPI for custom services) |
| `spec/mirror-node-client/` | Querying the Hiero Mirror Node REST API |
| `spec/enterprise/` | High-level service layer for easy use and framework integration |

## Writing specifications

Every spec under `spec/` is written in the meta-language defined in
[`guidelines/api-guideline.md`](guidelines/api-guideline.md). In short:

- Each file follows the skeleton: `## Description` → `## API Schema` → optional `## Examples` →
  `## Questions & Comments`.
- The API schema declares a `namespace` and imports the types it uses with `requires {Type} from namespace`
  (one statement per source namespace); imported types are then referenced by their simple name (e.g. `Address`).
- Fields are **immutable by default** (`@@immutable`); collections are never nullable; the `ANY` top type is avoided
  as a standalone type.
- Naming: types `PascalCase`, fields/methods `lowerCamelCase`, enum values & constants `UPPER_SNAKE_CASE`, namespaces
  `lowerCamelCase`, error ids `lowercase-kebab-case`. Generic type parameters are prefixed with `$$`.

See [`CLAUDE.md`](CLAUDE.md) for a fuller orientation aimed at contributors and AI coding agents.

## Toolchain

The repository pins its build tools so that every contributor builds with the same versions:

| Tool | Version | Pinned in |
|------|---------|-----------|
| JDK | 25 (Temurin) | [`sdk-java/.sdkmanrc`](sdk-java/.sdkmanrc), [`tooling/metalang/.sdkmanrc`](tooling/metalang/.sdkmanrc) |
| Maven | 3.9.11 | `sdk-java/.mvn/wrapper/maven-wrapper.properties`, `tooling/metalang/.mvn/wrapper/maven-wrapper.properties` |
| Node.js | 22 (LTS) | — (the runtime the TypeScript mapping targets, [`api-best-practices-ts.md`](guidelines/api-best-practices-ts.md)) |
| TypeScript | 6.0.3 | [`sdk-ts/package.json`](sdk-ts/package.json) (`devDependencies`, together with `@types/node`) |
| Rust | 1.85 or newer, edition 2024 | the `Cargo.toml` the Rust generator writes, [`api-best-practices-rust.md`](guidelines/api-best-practices-rust.md) |
| Go | 1.27 or newer | [`sdk-go/generator.properties`](sdk-go/generator.properties) (`go.version`) |
| Python | 3.12 or newer | the `pyproject.toml` the Python generator writes (`requires-python`), [`api-best-practices-python.md`](guidelines/api-best-practices-python.md) |
| protoc | 4.31.1 | [`tooling/protobuf/sources.json`](tooling/protobuf/sources.json) (`protocVersion`) |

Only the JDK and Maven are needed to build this repository: the spec tooling in `tooling/metalang` is a Maven build
and carries its own wrapper. The four other toolchains are needed only to *verify* a generated SDK — the generator
tests skip what is not installed (`cargo` missing, `node` or `sdk-ts/node_modules` missing, a `python3` below 3.12 or
without mypy), so `./mvnw verify` stays green on a machine that has none of them.

**Java** — install [SDKMAN!](https://sdkman.io), then once per machine:

```bash
cd sdk-java && sdk env install   # installs the JDK listed in that directory's .sdkmanrc
```

and set `sdkman_auto_env=true` in `~/.sdkman/etc/config` (the file `sdk config` opens):

```bash
sed -i.bak 's/^sdkman_auto_env=false/sdkman_auto_env=true/' ~/.sdkman/etc/config
```

With `sdkman_auto_env=true`, SDKMAN! switches to the pinned JDK automatically whenever you `cd` into a build
directory — that is what makes `.sdkmanrc` take effect without setting `JAVA_HOME` by hand. **Without it,
`.sdkmanrc` is inert** and you have to run `sdk env` there before building. `sdk env` does *not* search parent
directories, which is why each of the two Maven builds carries its own `.sdkmanrc`.

JDK 25 is required, not just recommended: it builds every Maven module — the generated Java API, the support types
and the TCK modules (all `release` 25) as well as the spec tooling (`release` 21). Building on an older JDK fails in
the compiler plugin, typically with `Unsupported major.minor version 69.0` — that message means the active JDK is not
25, so run `sdk env` and build again.

**Maven** — there are two Maven builds, and each carries its own wrapper and its own `.sdkmanrc`:
`tooling/metalang/` (the spec tooling) and `sdk-java/` (the Java SDK). Build from inside one of them; the wrapper
downloads the pinned Maven version on first use, so a locally installed `mvn` is not needed:

```bash
cd tooling/metalang
sdk env            # not needed with sdkman_auto_env=true, which switches on cd
./mvnw verify
```

On Windows use `mvnw.cmd`. The repository root carries no toolchain pin on purpose: nothing is built there, and the
CLI jar the tooling produces targets `release` 21, so it runs on any JDK 21 or newer.

**Node.js and TypeScript** — install Node.js 22 (e.g. with [nvm](https://github.com/nvm-sh/nvm) or
[fnm](https://github.com/Schniz/fnm)); the TypeScript compiler is not installed globally but comes from the npm
workspace, so a single install provides it for every TypeScript module:

```bash
npm --prefix sdk-ts install
```

That links the hand-written `sdk-ts/support` package into the workspace and puts `typescript` and `@types/node` at the
pinned versions into `sdk-ts/node_modules`. The generator tests of `tooling/metalang` use the same installation; without
it they skip the compile and run steps.

**Rust** — install the toolchain with [rustup](https://rustup.rs); edition 2024 requires Rust 1.85 or newer:

```bash
rustup toolchain install stable
```

Cargo is needed twice: to build and test a generated workspace (`cargo test --manifest-path
sdk-rust/generated/Cargo.toml --no-fail-fast`) and for `metalang check --language=rust`, which builds its `syn`-based
helper `rs-api` once (`--cargo=<executable>` selects a Cargo that is not on the `PATH`). A generated crate that
compiles protobuf needs **no** protobuf installation: its build script takes `protoc` from the `protoc-bin-vendored`
crate.

**Go** — install Go 1.27 or newer ([go.dev/dl](https://go.dev/dl/)); the version is the minimum for `iter.Seq2`
(streaming) and generic methods on concrete types. Nothing in the build depends on a Go toolchain being present — the
generator produces the column alignment `gofmt` would — so Go is only needed to check a generated module:

```bash
cd sdk-go/generated && go build ./... && go vet ./... && gofmt -l .
```

**Python** — Python 3.12 or newer (PEP 695 generics) plus [mypy](https://mypy-lang.org); there is nothing to run yet,
the check is that every package imports and the strict type check is clean:

```bash
cd sdk-python/generated && python3 -m mypy --strict src
```

**protoc** — never installed by hand. Every build fetches it at the pinned version: `tooling/protobuf/verify.sh` and
`sdk-java/protobuf` through the Maven wrapper, the Rust crates through `protoc-bin-vendored`, TypeScript through
[`buf`](https://buf.build) from the `devDependencies` of `sdk-ts`.

The commands above assume the respective `sdk-<lang>/generated` tree exists — see
[Generating the native API code](#generating-the-native-api-code).

## Generating the native API code

The specifications under `spec/` are the only source of the API: the code of every target language is produced from
them by the `metalang` generator, and the generated trees are **not** part of this repository. They are produced on
demand, and the output is reproducible — a diff of two runs shows exactly what a spec or generator change did.

The generators produce the **API only**: every method body is a stub that throws "Not implemented yet", which is why
the generated tests fail for each method that is not implemented. The `AccountCreateTransaction` spike that filled
those bodies in Java and TypeScript by hand lives in the git history; [`docs/rebuilding-the-spike.md`](docs/rebuilding-the-spike.md)
is the runbook for it.

### Run a script

[`scripts/`](scripts) holds one script per language and one for all of them. They can be run from anywhere, build
the `metalang` CLI themselves if its jar is missing, and resolve the JDK from the `.sdkmanrc` of the Maven build they
drive instead of taking whatever is on the `PATH`:

```bash
scripts/generate-all.sh
```

| Script | Generates into | Then builds with |
|---|---|---|
| [`scripts/generate-java.sh`](scripts/generate-java.sh) | `sdk-java/generated` | Maven — `support`, `protobuf` and the generated modules into the local repository |
| [`scripts/generate-ts.sh`](scripts/generate-ts.sh) | `sdk-ts/generated` | `npm install` in the workspace, then `tsc --build` |
| [`scripts/generate-rust.sh`](scripts/generate-rust.sh) | `sdk-rust/generated` | `cargo build --all-targets` |
| [`scripts/generate-go.sh`](scripts/generate-go.sh) | `sdk-go/generated` | `go build`, `go vet` and the `gofmt -l` check |
| [`scripts/generate-python.sh`](scripts/generate-python.sh) | `sdk-python/generated` | `mypy --strict` |
| [`scripts/generate-all.sh`](scripts/generate-all.sh) | all five | each of the above; every language is attempted even if an earlier one failed |

Each of them takes `--no-build` (generate only) and `--test` (also run the generated tests, which fail for every
method that is still a stub). `REBUILD_CLI=1` forces a rebuild of the CLI, `PYTHON=<interpreter>` selects the
interpreter for the Python check and `SKIP_TYPE_CHECK=1` leaves that check out. mypy does not have to be installed:
`generate-python.sh` creates the git-ignored virtual environment `sdk-python/.venv` and installs it there if the
interpreter has none. [`scripts/README.md`](scripts/README.md) documents them in full.

### Do it by hand

Build the CLI once; it is a self-contained jar and needs the JDK of [Toolchain](#toolchain):

```bash
cd tooling/metalang && ./mvnw -q package -DskipTests
```

All commands below are run from the **repository root**. Each writes into the `generated` directory of its language
folder and reads the decisions the specs cannot express from that folder's `generator.properties`.
`--fail-on=never` generates even while the specs still have validation findings; without it nothing is written as
long as an `error` finding exists. `--show-deferred` lists the declarations a generator cannot express yet, with the
reason; `--output` takes any directory, so a throwaway target is the way to inspect a generator change. Details on
every generator are in [`tooling/metalang/README.md`](tooling/metalang/README.md).

#### Java

A Maven project: a parent `pom.xml` with one sub-module per Java module, each with its JUnit tests.

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=java --fail-on=never --config=sdk-java/generator.properties --output=sdk-java/generated spec
```

The generated modules depend on the hand-written support module (`sdk-java/support`) and, for the consensus node
client, on `sdk-java/protobuf`, so install those first (JDK 25, `sdk env`):

```bash
cd sdk-java && sdk env
```

```bash
./mvnw -f support install && ./mvnw -f protobuf install && ./mvnw -f generated package -DskipTests
```

`-Dmaven.test.failure.ignore=true` instead of `-DskipTests` runs the generated tests through all modules and shows
the implementation status. Whether a tree still provides the API of the specs — the generated one or an
implementation that started from it — is answered by the conformance check:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar check --language=java --fail-on=never --config=sdk-java/generator.properties --project=sdk-java/generated spec
```

#### TypeScript

One npm package per spec folder, with `node:test` tests, inside the npm workspace `sdk-ts`:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=ts --fail-on=never --config=sdk-ts/generator.properties --output=sdk-ts/generated spec
```

`npm install` links the hand-written `@hiero/support` package into the workspace and `tsc --build` builds it first.
The `test` script of the generated workspace root compiles and then runs the tests with `node --test`:

```bash
npm --prefix sdk-ts install && npm --prefix sdk-ts/generated test
```

#### Rust

A Cargo workspace with one crate per spec folder and an integration test per crate; the support types are copied
1:1 from [`guidelines/rust-files`](guidelines/rust-files):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=rust --fail-on=never --config=sdk-rust/generator.properties --output=sdk-rust/generated spec
```

```bash
cargo test --manifest-path sdk-rust/generated/Cargo.toml --no-fail-fast
```

Besides the failures of the method stubs, that run reports one failing **doctest**. The HAPI comment of
`ExchangeRate` in [`exchange_rate.proto`](tooling/protobuf/consensus-node/services/exchange_rate.proto) ends in a
fenced code block with the formula `amountInTinybar = (amountInTinycent * hbarEquiv) / centEquiv`, `prost` copies the
comment into the generated protobuf code, and rustdoc compiles an untagged fenced block as Rust. It therefore comes
from the vendored definitions, not from the API generator. `--tests` runs the integration tests without the doctests:

```bash
cargo test --manifest-path sdk-rust/generated/Cargo.toml --no-fail-fast --tests
```

#### Go

One Go module for everything (a spec folder is no artifact boundary in Go), one package per namespace:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=go --fail-on=never --config=sdk-go/generator.properties --output=sdk-go/generated spec
```

The Go generator is still incomplete — it emits the declared types, but no methods, namespace functions, tests,
protobuf wiring or TCK part. A declaration it cannot express is left out rather than emitted broken, so the module
always builds. There is nothing to run yet; what has to hold is that it builds, vets clean and is already formatted:

```bash
cd sdk-go/generated && go build ./... && go vet ./... && gofmt -l .
```

#### Python

One distribution (`hiero-sdk`, import package `hiero_sdk`), one package per namespace:

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar generate --language=python --fail-on=never --config=sdk-python/generator.properties --output=sdk-python/generated spec
```

As incomplete as the Go generator and with the same scope. The check is that every package imports and the strict
type check is clean:

```bash
cd sdk-python/generated && python3 -m mypy --strict src
```

## Running the TCK

The [Hiero TCK](https://github.com/hiero-ledger/hiero-sdk-tck) can be run against a TCK server generated from the
bindings in [`tooling/tck/bindings`](tooling/tck/bindings) (see [`tck-binding.md`](docs/tck-binding.md)). The default network is a local
[Solo](https://solo.hiero.org) network (Solo 0.63+). As long as the API is only generated stubs, every bound method
fails with `-32603` (already `setup` calls stubs) and the TCK skips the methods without binding (`-32601`); a run
shows whether server, network and TCK work together.

Steps 3 to 5 are also one script: `TCK_DIR=../hiero-sdk-tck scripts/tck-java.sh` (or
[`scripts/tck-ts.sh`](scripts/tck-ts.sh)) generates, builds and runs. Solo and the TCK clone of steps 1 and 2 are
needed either way.

**1. Start Solo** (requires Docker and the Solo CLI, see the
[Solo quickstart](https://solo.hiero.org/docs/simple-solo-setup/quickstart/)):

```bash
solo one-shot single deploy
```

**2. Clone and install the TCK** (once, e.g. next to this repository):

```bash
git clone https://github.com/hiero-ledger/hiero-sdk-tck.git ../hiero-sdk-tck
```

```bash
npm --prefix ../hiero-sdk-tck install
```

**3. Generate the API and the server.** The server is generated from the bindings against the generated API, so
both have to exist first — the API as described in
[Generating the native API code](#generating-the-native-api-code), the server with `tck generate` (from the
repository root, `--language=ts` and the `sdk-ts` paths for the TypeScript server):

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar tck generate --language=java --fail-on=never --config=sdk-java/generator.properties --bindings=tooling/tck/bindings --output=sdk-java/tck/generated spec
```

```bash
java -jar tooling/metalang/target/metalang-0.1.0-SNAPSHOT-cli.jar tck generate --language=ts --fail-on=never --config=sdk-ts/generator.properties --bindings=tooling/tck/bindings --output=sdk-ts/tck/generated spec
```

**4. Build the server.** Java, from `sdk-java`, in this order — the `sdk env` activates the JDK 25 pinned in
[`sdk-java/.sdkmanrc`](sdk-java/.sdkmanrc) and can be skipped if you set `sdkman_auto_env=true` (see
[Toolchain](#toolchain)):

```bash
cd sdk-java && sdk env
```

```bash
./mvnw -f support install
```

```bash
./mvnw -f protobuf install
```

```bash
./mvnw -f generated install -DskipTests
```

```bash
./mvnw -f tck/generated/contract install
```

```bash
./mvnw -f tck/runtime install
```

```bash
./mvnw -f tck/generated/server package
```

TypeScript (Node.js 22, from the repository root):

```bash
npm --prefix sdk-ts install && npm --prefix sdk-ts run build:tck-ts
```

**5. Run the TCK** — one test file first, then the complete suite (`ts` instead of `java` for the TypeScript server):

```bash
TCK_DIR=../hiero-sdk-tck tooling/tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
```

```bash
TCK_DIR=../hiero-sdk-tck tooling/tck/run-tck.sh java
```

[`tooling/tck/run-tck.sh`](tooling/tck/run-tck.sh) checks that Solo is reachable at `127.0.0.1:35211`, starts the server on port 8544,
copies [`tooling/tck/solo.env`](tooling/tck/solo.env) as `.env` into the TCK clone (an existing `.env` is saved as `.env.before-v3`)
and runs the tests. The HTML report is in `../hiero-sdk-tck/mochawesome-report`. `TCK_ENV=<file>` selects another
configuration.

### Why a test fails

The TCK only prints the `message` of a JSON-RPC error — a bound method that fails always reads `Internal error`,
never the exception behind it. The server therefore logs the cause itself: the first call of each distinct failure
plus a summary of all of them when it stops, which during the stub phase is the list of API methods to implement
next.

```
[tck-server] setup -> -32603 UnsupportedOperationException: Not implemented yet: AccountId.fromString
[tck-server] failed calls by cause (2 distinct):
[tck-server]   1x setup -> -32603 UnsupportedOperationException: Not implemented yet: AccountId.fromString
[tck-server]   1x version -> -32601 no binding for this TCK method
```

`TCK_SERVER_LOG` selects the detail — `debug` logs every call with the stack trace of its exception, `off` is
silent:

```bash
TCK_SERVER_LOG=debug TCK_DIR=../hiero-sdk-tck tooling/tck/run-tck.sh java
```

Possible stumbling blocks:

- The TCK's preflight checks that the mirror node REST API, the consensus node and the JSON-RPC server are reachable.
  If it reports the mirror node, the port in `tooling/tck/solo.env` (`MIRROR_NODE_REST_URL`, `38081`) does not match.
- Solo 0.62 and earlier use the ports `50211` and `8081`: adjust `tooling/tck/solo.env` or pass your own file with
  `TCK_ENV=…`.
- If a port is taken, Solo uses another one and logs `Using available port …`; adjust `tooling/tck/solo.env` accordingly.

## Target languages

V3 covers all Hiero SDKs: **Java, JavaScript / TypeScript, Go, Rust, Python, C++, and Swift.**

## Status

Early prototype. The API surface, namespaces, and open questions (tracked in each spec's
`## Questions & Comments` section) are still evolving and subject to change.
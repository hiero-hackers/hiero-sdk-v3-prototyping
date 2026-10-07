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
| [`guidelines/api-best-practices-ts.md`](guidelines/api-best-practices-ts.md) | ... TypeScript (the mapping of the TypeScript generator) |
| [`guidelines/api-best-practices-js.md`](guidelines/api-best-practices-js.md) | ... using the resulting API from JavaScript |
| `guidelines/js-files/` | Illustrative reference snippets (not a buildable module) |
| `sdk-java/`, `sdk-ts/`, `sdk-rust/`, `sdk-go/` | One folder per target language: its generator configuration, its hand-written modules and its generated ones (see [ADR-0008](docs/adr/0008-one-folder-per-target-language.md)) |
| `sdk-<lang>/generated/` | The generated API. Never edited by hand — except the Java and TypeScript ones, which currently carry the `AccountCreateTransaction` spike |
| `sdk-java/support/`, `sdk-ts/support/` | Hand-written support types the generated API depends on (`@ThreadSafe`, streaming; `Duration`, `StreamItem`, `AbstractConstructor`) |
| `sdk-<lang>/tck/` | The language-specific TCK parts: `generated/` (contract + server) and `runtime/` (hand-written) |
| `tck/` | The language-neutral TCK parts: the bindings, the Solo configuration and `run-tck.sh` |
| [`docs/pipeline.html`](docs/pipeline.html) | How the generator, the protobuf tree and the TCK server fit together — folder map and diagrams (open it in a browser) |
| [`docs/missing-features.md`](docs/missing-features.md) | What the V3 specs do not cover yet, measured against the v2 SDKs — the feature backlog |
| [`docs/TODO.md`](docs/TODO.md) | Open follow-up tasks on the guides, the meta-language and the specs |
| [`docs/TCK.md`](docs/TCK.md), [`docs/tck-binding.md`](docs/tck-binding.md), [`docs/tck-ideas.md`](docs/tck-ideas.md) | Three approaches to testing V3 against the Hiero TCK; `tck-binding.md` is the one with a spike |
| [`docs/adr/`](docs/adr) | Architecture decision records |
| [`protobuf/`](protobuf) | The protobuf definitions of consensus, block and mirror node, vendored at pinned versions — one source for every generator |
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
| Node.js | 22 | — |

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

## Running the TCK

The [Hiero TCK](https://github.com/hiero-ledger/hiero-sdk-tck) can be run against a TCK server generated from the
bindings in [`tck/bindings`](tck/bindings) (see [`tck-binding.md`](docs/tck-binding.md)). The default network is a local
[Solo](https://solo.hiero.org) network (Solo 0.63+). As long as the API is only generated stubs, every bound method
fails with `-32603` (already `setup` calls stubs) and the TCK skips the methods without binding (`-32601`); a run
shows whether server, network and TCK work together.

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

**3. Build the server.** Java, from `sdk-java`, in this order — the `sdk env` activates the JDK 25 pinned in
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

**4. Run the TCK** — one test file first, then the complete suite (`ts` instead of `java` for the TypeScript server):

```bash
TCK_DIR=../hiero-sdk-tck tck/run-tck.sh java src/tests/crypto-service/test-account-create-transaction.ts
```

```bash
TCK_DIR=../hiero-sdk-tck tck/run-tck.sh java
```

[`tck/run-tck.sh`](tck/run-tck.sh) checks that Solo is reachable at `127.0.0.1:35211`, starts the server on port 8544,
copies [`tck/solo.env`](tck/solo.env) as `.env` into the TCK clone (an existing `.env` is saved as `.env.before-v3`)
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
TCK_SERVER_LOG=debug TCK_DIR=../hiero-sdk-tck tck/run-tck.sh java
```

Possible stumbling blocks:

- The TCK's preflight checks that the mirror node REST API, the consensus node and the JSON-RPC server are reachable.
  If it reports the mirror node, the port in `tck/solo.env` (`MIRROR_NODE_REST_URL`, `38081`) does not match.
- Solo 0.62 and earlier use the ports `50211` and `8081`: adjust `tck/solo.env` or pass your own file with
  `TCK_ENV=…`.
- If a port is taken, Solo uses another one and logs `Using available port …`; adjust `tck/solo.env` accordingly.

## Target languages

V3 covers all Hiero SDKs: **Java, JavaScript / TypeScript, Go, Rust, Python, C++, and Swift.**

## Status

Early prototype. The API surface, namespaces, and open questions (tracked in each spec's
`## Questions & Comments` section) are still evolving and subject to change.
# Protobuf definitions

The single source of the protobuf definitions used in this repository. Hiero has three node types
with a gRPC/protobuf interface — consensus node, block node and mirror node — and their definitions
live in three different repositories. This directory vendors them at pinned versions, so that every
language generator in this repository works from one tree instead of its own copy.

Everything here is **vendored, never edited by hand**. [`update.sh`](update.sh) re-fetches it,
[`sources.json`](sources.json) pins the versions, [`verify.sh`](verify.sh) proves the tree compiles.

## Provenance

| Directory | Upstream | Version | Path in the repository | Files |
|---|---|---|---|---|
| [`consensus-node/`](consensus-node) | [hiero-consensus-node](https://github.com/hiero-ledger/hiero-consensus-node) | `v0.77.2` | `hapi/hedera-protobuf-java-api/src/main/proto` | 208 |
| [`block-node/`](block-node) | [hiero-block-node](https://github.com/hiero-ledger/hiero-block-node) | `v0.44.2` | `protobuf-sources/src/main/proto` | 18 |
| [`mirror-node/`](mirror-node) | [hiero-mirror-node](https://github.com/hiero-ledger/hiero-mirror-node) | `v0.164.0` | `protobuf/src/main/proto` | 2 |

All three are Apache-2.0. The exact commit of every pin is recorded in
[`sources.json`](sources.json); only `.proto` files are copied, the upstream build configuration is
not.

## The hierarchy

The three are **not** independent. The consensus node is the base, the other two build on it:

```
  consensus-node/                       the base, imports nothing outside itself
    services/      127 files            transactions, queries, basic_types, response_code — HAPI
    block/stream/                       the block stream
    streams/         8 files            the record stream
    platform/                           consensus internals (events, state)
    mirror/          1 file             an address-book service (see "Duplication" below)
    fees/, sdk/      2 files
          ▲                    ▲
          │                    │
  block-node/              mirror-node/
    block-node/api/          com/hedera/mirror/api/proto/
      imports                  imports
      block/stream/*.proto     services/basic_types.proto
                               services/timestamp.proto
                               services/consensus_submit_message.proto
    internal/                9 files, block node state — not a public API
```

Concretely:

- **`services/basic_types.proto` is the root of everything.** It declares `AccountID`, `Key`,
  `NodeAddress` and the other shared types; the mirror node imports it directly, the block node
  reaches it through `block/stream/`.
- **Import paths are root-relative and directory-scoped** (`import "services/basic_types.proto"`,
  `import "block/stream/block.proto"`). The proto root is therefore the directory *containing*
  `services/`, not `services/` itself.
- **Each directory here is one include root.** The block node and the mirror node additionally need
  `consensus-node/` on the include path — `verify.sh` adds it automatically. They are deliberately
  not merged into one tree; see the next section.

Protobuf has no inheritance. "Hierarchy" here means the import graph only: the dependent definitions
reference messages of the base, they do not extend them.

## Findings worth knowing

**The address-book service exists twice.** `consensus-node/mirror/mirror_network_service.proto` and
`mirror-node/com/hedera/mirror/api/proto/network_service.proto` both declare `package
com.hedera.mirror.api.proto` with a `message AddressBookQuery` and a `service NetworkService` that
has the same `getNodes` rpc. The two files differ only in comments, formatting and one unused
import — they are the same contract maintained in two repositories. Merging the roots would make
them collide, which is one reason they stay separate. Which repository owns this contract is a
question for upstream.

**The block node pins an older consensus node than we do.** `protobuf-sources/build.gradle.kts` of
`hiero-block-node v0.44.2` sets `cnVersion = "0.76.1"`, while this directory vendors `v0.77.2`.
`verify.sh` proves the combination resolves, so the newer base is safe to use here; a future
upgrade should re-check it.

**The block node's `proto-overrides` are not vendored.** Upstream keeps one overriding copy of
`block/stream/record_file_item.proto` to add fields before they reach a consensus node release.
Against `v0.77.2` that override differs from the consensus node file only in its import list, and
the messages are identical, so it is not applied here.

**`block-node/internal/` is not a public API.** Those nine files are the block node's own state
messages. They are vendored because they live in the same upstream tree, not because a client
should use them.

## Verifying

```bash
protobuf/verify.sh
```

Compiles every `.proto` of every root with `protoc` and writes one `FileDescriptorSet` per root.
That is the language-neutral proof that the tree is complete and that every import resolves — any
generator can be pointed at these roots. `--keep` leaves the descriptor sets in `protobuf/target/`.

`protoc` and the well-known types (`google/protobuf/*.proto`, which ship inside `protobuf-java` and
not inside `protoc`) are fetched through the repository's Maven wrapper, so no protobuf
installation is required. The version is `protocVersion` in [`sources.json`](sources.json).

The remaining warnings are unused imports in the upstream files. They are reported, not fixed — the
tree is vendored as it is.

## Updating

```bash
protobuf/update.sh                 # all three
protobuf/update.sh consensus-node  # or one of them
```

Change the `tag` in [`sources.json`](sources.json) first; the script rewrites the vendored tree and
records the resolved commit and file count. Afterwards run `verify.sh` — a newer release can add
imports that do not resolve. It needs `git` and `jq`.

## Language bindings

The three target languages generate from this tree at build time. **In none of them is the result
part of the public API** — protobuf is how the SDK talks to a node, and exposing it would turn every
protocol change into a breaking change of the SDK. Each language expresses that with its own
mechanism:

| | Generated into | Kept internal by | Proven by |
|---|---|---|---|
| Java | `sdk-java/protobuf` (module `org.hiero.sdk.protobuf`) | `exports … to …` in `module-info.java` naming only the SDK modules | a foreign module gets *package … is not visible* |
| Rust | `OUT_DIR` of `hiero-consensus-node-client`, included by `src/proto.rs` | `mod proto;` without `pub` in `lib.rs` | an integration test gets *module `proto` is private* |
| TypeScript | `packages/consensus-node-client/src/internal/proto` | the subpath is absent from `exports` in `package.json` | an importer gets `ERR_PACKAGE_PATH_NOT_EXPORTED` |

All three compile the same 207 files: the consensus node root without `mirror/`, which duplicates
the mirror node's own definitions.

**Java needs a separate module, the other two do not.** JPMS forbids a package from being split
across modules, so the protobuf packages must have exactly one owner — hence one module plus
qualified exports. Rust and TypeScript have no such constraint, so the generated code lives inside
the crate resp. package that uses it and never becomes visible at all.

None of the generated code is committed; it is build output in all three languages:

```bash
./mvnw -f sdk-java/protobuf install        # Java    (protobuf-maven-plugin)
cargo build -p hiero-consensus-node-client # Rust    (prost-build in build.rs, vendored protoc)
npm run gen:proto-ts                       # TypeScript (buf + protoc-gen-es, see buf.gen.yaml)
```

The TypeScript step is explicit because `tsc` cannot generate sources itself; Java and Rust run
their generator as part of the normal build.

## Not covered here

- **The mirror node REST API** is specified in OpenAPI, not protobuf, and is not part of this tree.
- **gRPC service stubs.** Only messages are generated, in every language. The Java client builds its
  `io.grpc.MethodDescriptor` from the service and method name by hand; when the Rust and TypeScript
  clients are implemented they will need the equivalent decision (`tonic` resp. `@grpc/grpc-js`).
- **The block node definitions.** Vendored in `block-node/`, but no language generates from them
  yet — there is no block node client in the specs.

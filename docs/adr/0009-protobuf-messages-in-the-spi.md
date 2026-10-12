# ADR-0009: The SPI exposes protobuf messages; a namespace maps to the generated protobuf code

**Status:** Accepted
**Date:** 2026-10-12

## Context

`spec/consensus-node-client/proto.md` declares four types — `TransactionBody`, `TransactionResponse`,
`TransactionReceipt`, `TransactionRecord` — in the namespace `consensusnode.proto`, described in the spec itself as
"minimal placeholders … concrete protobuf fields are intentionally omitted". They exist because
`consensusnode.transactions.spi.TransactionSupport`, the SPI for custom services and transaction types, has to name
them: it updates a transaction body and builds a response, a receipt and a record.

At the same time every SDK compiles the vendored definitions in `tooling/protobuf` and therefore already has types
with exactly these names. The generated code consequently contained each of them **twice**:

| | generated from the spec | generated from protobuf |
|---|---|---|
| Java | `org.hiero.consensusnode.proto.TransactionBody` (empty `interface`) | `com.hederahashgraph.api.proto.java.TransactionBody` |
| Rust | `consensusnode/proto/transaction_body.rs` | `OUT_DIR`, included by `proto.rs` |
| TypeScript | `consensusnode/proto/TransactionBody.ts` | `internal/proto/…/transaction_pb.ts` |

The two are unrelated types. An implementor of `TransactionSupport` receives the empty placeholder, which carries no
fields and cannot be converted into the real message — **the SPI is not implementable**. Removing the placeholders
without a replacement is not possible either: the generator defers every type whose references are missing, so
`TransactionSupport` and everything referring to it would silently disappear from the generated API.

This collides with the rule "protobuf is never public API", which [ADR-0007](0007-separate-generated-and-hand-written-modules.md)
and `CLAUDE.md` state without exception. The rule has a good reason: the wire format changes with every network
release, so every protocol change would become a breaking change of the SDK.

## Decision

**The protobuf messages that the SPI names are public API; everything else generated from protobuf stays internal.
A namespace whose types the protobuf build already provides is not generated — the generator maps the namespace as a
whole to the location of that code, configured per language.**

Two parts:

1. **The rule is narrowed, not dropped.** "Protobuf is never public API" becomes "protobuf is public API only where
   the SPI exposes it". The SPI exists because the consensus node is service oriented and supports custom services
   and transaction types; whoever adds one works at the protocol level by definition and needs the wire format. The
   typed API that normal applications use is unaffected — none of its types mentions a protobuf message.

2. **The mapping is a namespace-level configuration key, not a per-type one.** The message names of HAPI match the
   type names of `consensusnode.proto` one to one, so one key per language suffices:

   ```properties
   java.externalNamespace.consensusnode.proto = com.hederahashgraph.api.proto.java
   ```

   For such a namespace the generator produces no type, no package documentation and no export; every reference to
   one of its types resolves to the configured location instead. The types stay declared in the spec, because that
   is what makes the specs language neutral: the spec names the message, each language says where its protobuf
   build puts it.

   Where the key is not set the generator keeps generating the placeholders, so a language can adopt this
   independently.

## Consequences

- `TransactionSupport` becomes implementable: it hands out the real protobuf messages.
- The duplicate types disappear. In Java five files (four types plus `package-info.java`) are gone and
  `org.hiero.consensusnode.proto` no longer exists as a package.
- **Java:** `sdk-java/protobuf` exports `com.hederahashgraph.api.proto.java` **unqualified** instead of
  `exports … to` the three SDK modules, and `sdk-java/generated/org.hiero.consensus.node.client` requires the
  protobuf module **transitively** — a user of the SPI must be able to read it. The other two packages of the
  protobuf module (`com.hederahashgraph.service.proto.java`, `com.hedera.mirror.api.proto`) keep their qualified
  exports. The generator derives both from the presence of an external namespace in the module, so a regeneration
  preserves it.
- A protobuf change in those four messages is now a potentially breaking change of the SDK. This is the price of an
  implementable SPI, and it is limited to the four messages: the SDK does not re-export the rest.
- **TypeScript, Rust and Go still generate the placeholders.** Their key is not implemented yet. TypeScript needs
  one more step: its four messages live in four different modules and the module is not derivable from the message
  name (`TransactionBody` is in `transaction_pb.ts`), so the namespace has to point at a small re-export module.

## Alternatives considered

- **Keep the placeholders and convert between them.** Converting requires fields, which the placeholders do not
  have. It would mean specifying the complete wire format in the meta-language — duplicating `tooling/protobuf` in a
  second notation and keeping both in sync by hand.
- **Drop `consensusnode.proto` and type the SPI with `bytes`.** Honest about the wire format being opaque, but it
  moves parsing and serialization into every SPI implementation and loses the type that documents what the bytes
  are. It also hides which protobuf message is meant.
- **Map each type separately** (`java.externalType.consensusnode.proto.TransactionBody = …`). Four keys per language
  instead of one, with no gain: the names already match. A future namespace whose names do not match can still get
  a per-type key — this decision does not rule it out.
- **Remove the SPI.** The SPI is the reason the consensus node API is extensible at all; `TransactionStatus` is an
  abstraction with an `int32` code for the same reason. Removing it would make custom services unsupportable.

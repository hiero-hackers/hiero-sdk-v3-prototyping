# Hiero Proto API

This section defines the Hiero Proto API.

## Description

SDKs communicate with the network nodes via gRPC using the Hiero Protobuf messages. This namespace names the
protocol-level messages the SDK API refers to: the raw transaction body, response, receipt and record. Most
applications never use them; they work with the typed transaction and query API instead. They are part of the API
only for the SPI that adds support for custom services and transaction types, which has to build and read the wire
format itself.

Each type here is the protobuf message of the same name, as the protobuf compiler of the language produces it. An
SDK does not define these types; it reuses the generated ones.

## Design Notes

The protobuf messages are generated in every SDK from the vendored definitions in `tooling/protobuf` with
language-specific tools, so their concrete form is language dependent. The declarations below exist so the specs can
*refer* to them by a language-neutral name; the fields are deliberately omitted, because the generated message is
the single source of truth for them.

A generator must therefore not generate a type for this namespace. Instead it maps the namespace as a whole to the
location of the generated protobuf code — one configuration key per language, because only the project knows where
its protobuf build puts the result:

| Language | Configuration key | Value in this repository |
|---|---|---|
| Java | `java.externalNamespace.consensusnode.proto` | `com.hederahashgraph.api.proto.java` |

The message names of HAPI match the type names of this namespace one to one, so the mapping needs no per-type
entry. Where a language does not set the key, the generator falls back to generating empty placeholder types.

Because the SPI exposes these messages, they are the one part of the protobuf code that is public API; everything
else stays internal. [ADR-0009](../../docs/adr/0009-protobuf-messages-in-the-spi.md) records that decision.

## API Schema

```
namespace consensusnode.proto

// The protobuf body of a transaction.
abstraction TransactionBody {}
// The protobuf response of a consensus node to a submitted transaction.
abstraction TransactionResponse {}
// The protobuf receipt of a transaction.
abstraction TransactionReceipt {}
// The protobuf record of a transaction.
abstraction TransactionRecord {}
```

## Questions & Comments

- The TypeScript, Rust and Go generators do not support `externalNamespace` yet, so they still generate the empty
  placeholders. Their mapping is open: in TypeScript the four messages live in four different modules and the module
  cannot be derived from the message name (`TransactionBody` is in `transaction_pb.ts`), so a namespace-level mapping
  needs a small re-export module to point at.

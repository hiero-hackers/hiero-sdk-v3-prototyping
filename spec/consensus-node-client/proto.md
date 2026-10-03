# Hiero Proto API

This section defines the Hiero Proto API.

## Description

SDKs communicate with the network nodes via gRPC using the Hiero Protobuf messages. This package contains the
protocol-level types the SDK API refers to, such as the raw transaction body, response, receipt and record. Most
applications never use these types directly; they work with the typed transaction and query API instead.

## Design Notes

The gRPC stubs are generated in each SDK from the Hiero Protobuf messages with language-specific tools, so the stubs
are language dependent. The types below are minimal placeholders that make the external dependencies explicit;
concrete protobuf fields are intentionally omitted and will be defined from hedera-protobufs.

## API Schema

```
namespace consensusnode.proto

// Minimal placeholders to make external dependencies explicit in this draft.
// Concrete protobuf fields are intentionally omitted and will be defined from hedera-protobufs.

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
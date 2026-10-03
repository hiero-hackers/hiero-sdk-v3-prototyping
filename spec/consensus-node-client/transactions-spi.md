# Transactions SPI API

This section defines the SPI API for transactions.

## Description

Service provider interface for adding support for new transaction types to the SDK. The consensus
node can run custom services next to its built-in ones, and these services can define their own
transaction types. To make such a transaction type usable with the SDK, implement
`TransactionSupport` for it.

A `TransactionSupport` describes one concrete transaction type (e.g. `AccountCreateTransaction`):
it names the transaction type and the gRPC method used to submit it, and converts between the
transaction, response, receipt and record types and their protobuf representations. The SDK looks
up the matching `TransactionSupport` via `getTransactionSupport(...)`, and `getAllTransactionSupports()`
returns all registered implementations.

## Design Notes

- Since the consensus node is designed in a service-oriented manner, transactions are handled by a
  separate service. The consensus node supports adding custom services next to the services that are
  part of the consensus node repository. Since new and custom services can provide new transaction
  types, the SDKs must be able to handle these new transaction types. The transactions SPI defines the
  interface that must be implemented by the custom service that provides new transaction types.
- The domain-specific type is the concrete `Transaction` subtype (e.g. `AccountCreateTransaction`);
  the SPI converts between protobuf types and that subtype. (An earlier version of this text referred
  to a `TransactionBuilder`/`Transaction` model in which the builder, e.g.
  `AccountCreateTransactionBuilder`, was the domain-specific type and `Transaction` was universal.)

## API Schema

```
namespace consensusnode.transactions.spi
requires {Receipt, Response, Transaction, Record} from consensusnode.transactions
requires {MethodDescriptor} from grpc
requires {TransactionBody, TransactionResponse, TransactionReceipt, TransactionRecord} from consensusnode.proto

// Adds support for one concrete transaction type: converts between the transaction and its result
// types and their protobuf representations.
abstraction TransactionSupport<$$Receipt extends Receipt, $$Transaction extends Transaction<$$Receipt, $$Transaction>> {

    type getTransactionType() // the concrete transaction type this support handles

    MethodDescriptor getMethodDescriptor() // the gRPC method used to submit the transaction

    TransactionBody updateBody(transaction:$$Transaction, protoBody:TransactionBody) // updates the proto TransactionBody with the fields from the Transaction

    $$Transaction create(protoBody:TransactionBody) // creates a Transaction from a proto TransactionBody

    Response<$$Receipt> create(protoResponse:TransactionResponse) // creates a Response from a proto TransactionResponse

    $$Receipt create(protoReceipt:TransactionReceipt) // creates a Receipt from a proto TransactionReceipt

    Record<$$Receipt> create(protoRecord:TransactionRecord) // creates a Record from a proto TransactionRecord
}

// factory methods that need to be implemented

@@throws(not-found-error) @@static TransactionSupport<ANY, ANY> getTransactionSupport(transactionType:type) // returns the TransactionSupport for the given transaction type; throws if none is registered

@@static set<TransactionSupport<ANY, ANY>> getAllTransactionSupports() // returns all TransactionSupport instances
```

## Questions & Comments

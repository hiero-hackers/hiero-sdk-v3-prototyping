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

## Testing

Tests of the lookup of `TransactionSupport` implementations, written as given / when / then. The current SDKs have no
such SPI, so the tests check only what this spec states. The conversions of a `TransactionSupport` work on protobuf
messages and are tested by each implementation.

```tests
// Source: this spec (getAllTransactionSupports returns every registered support, getTransactionSupport the one of a
// type).
test "every registered transaction support is found by its transaction type" {
    for support in getAllTransactionSupports() {
        // when
        let found = getTransactionSupport(transactionType: support.getTransactionType())

        // then
        assertEquals(expected: support.getTransactionType(), actual: found.getTransactionType())
    }
}

// Source: this spec (getTransactionSupport throws if no support is registered for the type). A receipt is no
// transaction, so no support is registered for it.
test "a type without a registered transaction support is rejected" {
    // when / then
    @@expectThrows(not-found-error)
    getTransactionSupport(transactionType: Receipt)
}
```

## Questions & Comments

- **Are the built-in transaction types registered as `TransactionSupport`?** `PackedTransaction.fromBytes(...)` and
  `getResponse(...)` resolve the concrete transaction type at runtime, which suggests that every built-in type
  (`AccountCreateTransaction`, `PrngTransaction`, ...) has a `TransactionSupport` that `getTransactionSupport(...)`
  finds. The spec does not say so; if it did, a test could check that the support of each built-in type names that
  type.

- **Three of the four `TransactionSupport.create(...)` methods cannot be implemented.** `create(TransactionResponse)`
  has to build a `Response` and `create(TransactionReceipt)` a receipt, and both need a `TransactionId` — but neither
  HAPI message carries one (`TransactionResponse` holds only the precheck code and the cost, `TransactionReceipt` has
  no transaction id). `create(TransactionBody)` would read a transaction back from its body, which needs a mapping from
  body to transaction per type that the spec does not define. The Java implementation throws for these three; only
  `create(TransactionRecord)` works, because the record carries the `transactionID`. Should the methods take the
  `TransactionId` as an additional parameter, be removed, or should the spec define the body-to-transaction mapping
  (which `ScheduleInfo.scheduledTransaction` needs as well)?

- **There is no public concrete receipt for transactions of unknown type.** A record whose transaction type the SDK
  cannot map to a typed receipt — e.g. an entry of `AccountRecords.records`, which mixes all transaction types — still
  needs a `Receipt`, but `Receipt` is an abstraction and every concrete receipt belongs to one transaction type. The
  Java implementation uses an internal, untyped receipt class, so a caller cannot narrow such an element with
  `instanceof` to a public type. Should the spec define a public general receipt type (status plus the generic HAPI
  receipt fields), or should records of unknown types be represented differently?

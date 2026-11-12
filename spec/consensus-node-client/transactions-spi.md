# Transactions SPI API

This section defines the SPI API for transactions.

## Description

Service provider interface for adding support for new transaction types to the SDK. The consensus
node can run custom services next to its built-in ones, and these services can define their own
transaction types. To make such a transaction type usable with the SDK, implement
`TransactionSupport` for it.

A `TransactionSupport` describes one concrete transaction type (e.g. `AccountCreateTransaction`):
it names the transaction type and the gRPC method used to submit it, writes the fields of the
transaction into the protobuf `TransactionBody`, and reads its typed receipt from the protobuf
`TransactionReceipt` or `TransactionRecord`. Everything that is the same for all transaction types
is done by the SDK and not by a `TransactionSupport`: it builds the `Response` of a submitted
transaction, reads the values every receipt has (`ReceiptHeader`) and builds the `Record` around
the typed receipt. The SDK looks up the matching `TransactionSupport` via `getTransactionSupport(...)`,
and `getAllTransactionSupports()` returns all registered implementations.

A custom service can also define its own status codes. Name them with a `TransactionStatusSupport`, which returns the
`TransactionStatus` values of the service. Every status code the SDK knows comes from such a support: the SDK registers
the one of `BasicTransactionStatus` itself, a module such as the CLPR module registers its own. The SDK reads the
status of every answer — the precheck of a request and the status of a receipt — and turns its code into a status with
`getTransactionStatus(code)`, the status of the registered support that names the code. A code that no support names
is an error (`unknown-status-error`): the request fails without another attempt. The supports are loaded once, when
the first client is created; if two of them name the same code, creating the client fails with
`status-conflict-error`.

## Design Notes

- **Status codes are named by a support of their own.** The codes belong to a service, not to one transaction type
  (the CLPR service has eight transaction types and one set of codes), and queries report codes as well, for which
  there is no `TransactionSupport`. A `TransactionStatusSupport` therefore names the codes of a service once.
- **The codes of the protocol are a support as well.** `BasicTransactionStatus` is not a special case beside the
  supports: the SDK registers a `TransactionStatusSupport` for it, so a code of another support that collides with a
  code of the protocol is found by the same check, and `getAllTransactionStatusSupports()` lists every code the SDK
  knows. The support of `BasicTransactionStatus` cannot be missing; it is part of the SDK.
- **Unknown and conflicting codes are errors.** A status that the SDK cannot name would reach the caller as a bare
  number, and a code with two names would make the status of an answer depend on the order in which the supports are
  loaded. Both are rejected: a conflict when the client is created, before any request; an unknown code when it
  arrives.

- Since the consensus node is designed in a service-oriented manner, transactions are handled by a
  separate service. The consensus node supports adding custom services next to the services that are
  part of the consensus node repository. Since new and custom services can provide new transaction
  types, the SDKs must be able to handle these new transaction types. The transactions SPI defines the
  interface that must be implemented by the custom service that provides new transaction types.
- The domain-specific type is the concrete `Transaction` subtype (e.g. `AccountCreateTransaction`);
  the SPI converts between protobuf types and that subtype. (An earlier version of this text referred
  to a `TransactionBuilder`/`Transaction` model in which the builder, e.g.
  `AccountCreateTransactionBuilder`, was the domain-specific type and `Transaction` was universal.)
- **Only the type-specific part is converted by a `TransactionSupport`.** The HAPI messages that are
  the same for every transaction type are handled by the SDK:
  - `TransactionResponse` holds only the precheck code and an approximate fee (only set on
    `INSUFFICIENT_TX_FEE`), and every gRPC method that takes a `Transaction` answers with it. The
    current SDKs (Java, JavaScript, Go, Rust) build their response for every transaction type the
    same way, from the transaction id they sent, the target node and the hash of the sent bytes,
    without reading the message. So there is no `create(TransactionResponse)`: `Response` holds
    only the `TransactionId`, which the SDK knows.
  - A `TransactionReceipt` carries no transaction id - the client fetches it by that id - but every
    receipt holds one. The SDK reads the transaction id, the status and the exchange rates into a
    `ReceiptHeader` and passes it with the protobuf message, so a `TransactionSupport` only reads the
    fields of its own type (as the current SDKs pass the transaction id into
    `TransactionReceipt.fromProtobuf`).
  - A `TransactionRecord` carries its transaction id and consensus timestamp; the SDK builds the
    `Record` from them and asks the `TransactionSupport` only for the typed receipt. That receipt is
    read from the record and not from its embedded receipt, because a record holds values the receipt
    does not (the random number or bytes of a `PrngTransaction`, for example).

## API Schema

```
namespace consensusnode.transactions.spi
requires {Receipt, Transaction} from consensusnode.transactions
requires {TransactionStatus} from consensusnode.status
requires {TransactionId} from ledger
requires {ExchangeRate} from nativeToken
requires {MethodDescriptor} from grpc
requires {TransactionBody, TransactionReceipt, TransactionRecord} from consensusnode.proto

// The values every receipt holds, read by the SDK: the receipt message itself carries no transaction id.
ReceiptHeader {
    @@immutable transactionId: TransactionId     // the id of the transaction the receipt belongs to
    @@immutable status: TransactionStatus        // the status of the transaction
    @@immutable exchangeRate: ExchangeRate       // the exchange rate at the time of the transaction
    @@immutable nextExchangeRate: ExchangeRate   // the next exchange rate
}

// Adds support for one concrete transaction type: converts between the transaction and its result
// types and their protobuf representations.
abstraction TransactionSupport<$$Receipt extends Receipt, $$Transaction extends Transaction<$$Receipt, $$Transaction>> {

    type getTransactionType() // the concrete transaction type this support handles

    MethodDescriptor getMethodDescriptor() // the gRPC method used to submit the transaction

    TransactionBody updateBody(transaction:$$Transaction, protoBody:TransactionBody) // updates the proto TransactionBody with the fields from the Transaction

    $$Transaction create(protoBody:TransactionBody) // creates a Transaction from a proto TransactionBody

    // Creates the typed receipt of a receipt query: the values of the header plus the fields of this transaction
    // type in the receipt message.
    $$Receipt createReceipt(header:ReceiptHeader, protoReceipt:TransactionReceipt)

    // Creates the typed receipt of a record: the values of the header plus the fields of this transaction type in
    // the record, which can hold more than its embedded receipt.
    $$Receipt createReceipt(header:ReceiptHeader, protoRecord:TransactionRecord)
}

// factory methods that need to be implemented

@@throws(not-found-error) @@static TransactionSupport<ANY, ANY> getTransactionSupport(transactionType:type) // returns the TransactionSupport for the given transaction type; throws if none is registered

@@static set<TransactionSupport<ANY, ANY>> getAllTransactionSupports() // returns all TransactionSupport instances

// The status codes of a service: those of the protocol (`BasicTransactionStatus`, registered by the SDK itself), of an
// optional service such as CLPR, or of a custom service.
abstraction TransactionStatusSupport {

    set<TransactionStatus> getStatuses() // the statuses of the service; no two with the same code
}

// Returns every registered TransactionStatusSupport, among them the one of BasicTransactionStatus. They are loaded once,
// when the first client is created or this function or getTransactionStatus is called first. Throws if two supports
// name the same code.
@@throws(status-conflict-error) @@static set<TransactionStatusSupport> getAllTransactionStatusSupports()

// Returns the status of a code: the status of the registered TransactionStatusSupport that names it. Throws if no
// support names the code, or if the registered supports conflict.
@@throws(unknown-status-error, status-conflict-error) @@static TransactionStatus getTransactionStatus(code: int32)
```

## Default Instances for Testing

```
// The header of a receipt of the default transaction id.
instance ReceiptHeader = ReceiptHeader{transactionId: DEFAULT, status: DEFAULT, exchangeRate: DEFAULT,
        nextExchangeRate: DEFAULT}
```

## Testing

Tests of the lookup of `TransactionSupport` implementations, written as given / when / then. The current SDKs have no
such SPI, so the tests check only what this spec states. The conversions of a `TransactionSupport` work on protobuf
messages and are tested by each implementation.

```tests
// Source: this spec (getAllTransactionSupports returns every registered support, getTransactionSupport the one of a
// type).
@@testId("every-registered-transaction-support-is-found-by-its-transaction-type")
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
@@testId("a-type-without-a-registered-transaction-support-is-rejected")
test "a type without a registered transaction support is rejected" {
    // when / then
    @@expectThrows(not-found-error)
    getTransactionSupport(transactionType: Receipt)
}

// Source: this spec (the SDK registers the support of BasicTransactionStatus, so getTransactionStatus returns the
// BasicTransactionStatus of a code of the protocol); HAPI response_code.proto
// (SUCCESS is 22, INVALID_SIGNATURE 7).
@@testId("a-code-of-the-protocol-is-its-basic-transaction-status")
test "a code of the protocol is its basic transaction status" {
    // given
    let success: TransactionStatus = consensusnode.status.BasicTransactionStatus.SUCCESS
    let invalidSignature: TransactionStatus = consensusnode.status.BasicTransactionStatus.INVALID_SIGNATURE

    // when / then
    assertEquals(expected: success, actual: getTransactionStatus(code: 22))
    assertEquals(expected: invalidSignature, actual: getTransactionStatus(code: 7))
}

// Source: this spec (a code that no registered TransactionStatusSupport names is an
// error); HAPI response_code.proto (no code is negative, none is above 1,000).
@@testId("a-code-no-one-names-is-rejected")
test "a code no one names is rejected" {
    for code in [-1, 1000000] {
        // when / then
        @@expectThrows(unknown-status-error)
        getTransactionStatus(code: code)
    }
}

// Source: this spec (every status of a registered support is found by its code).
@@testId("every-status-of-a-registered-support-is-found-by-its-code")
test "every status of a registered support is found by its code" {
    for support in getAllTransactionStatusSupports() {
        for status in support.getStatuses() {
            // when / then
            assertEquals(expected: status, actual: getTransactionStatus(code: status.code))
        }
    }
}
```

## Questions & Comments

- **A conflict cannot be tested by the spec.** `status-conflict-error` needs two registered supports that name the
  same code; the tests of a spec cannot register a support. Each implementation tests it.

- **Are the built-in transaction types registered as `TransactionSupport`?** `PackedTransaction.fromBytes(...)`,
  `Response.queryReceipt(client)` and `UntypedReceipt.typed(...)` resolve the concrete transaction type at runtime, which suggests that every built-in type
  (`AccountCreateTransaction`, `PrngTransaction`, ...) has a `TransactionSupport` that `getTransactionSupport(...)`
  finds. The spec does not say so; if it did, a test could check that the support of each built-in type names that
  type.

- **`create(TransactionBody)` cannot be implemented yet.** It would read a transaction back from its body, which needs
  a mapping from body to transaction per type that the spec does not define (`ScheduleInfo.scheduledTransaction` needs
  it as well). The Java implementation throws. Should the mapping be defined per transaction type, or the method be
  removed? — open

- **There is no public concrete receipt for transactions of unknown type.** A record whose transaction type the SDK
  cannot map to a typed receipt — e.g. an entry of `AccountRecords.records`, which mixes all transaction types — still
  needs a `Receipt`, but `Receipt` is an abstraction and every concrete receipt belongs to one transaction type. The
  Java implementation uses an internal, untyped receipt class, so a caller cannot narrow such an element with
  `instanceof` to a public type. Should the spec define a public general receipt type (status plus the generic HAPI
  receipt fields), or should records of unknown types be represented differently?

# Status API

This namespace defines the status codes the consensus node reports.

## Description

Every answer of a consensus node carries a status: the precheck of a submitted transaction or query, and the
receipt of an executed transaction. A `TransactionStatus` is identified by its numeric code. `BasicTransactionStatus`
names the codes of the built-in services of the consensus node; custom services can define further codes, so a
status the SDK does not name is still a `TransactionStatus` with its code.

## Design Notes

- **Own namespace at the bottom of the consensus node client.** The protocol uses the same status codes
  (`ResponseCodeEnum`) for the precheck of transactions and queries and for receipts. The client needs them to classify
  the attempts of a request (`consensusnode.client`), and the transactions build on the client, so the status cannot
  live in `consensusnode.transactions` without a dependency cycle between the namespaces.

## API Schema

```
namespace consensusnode.status

// The status of a transaction, identified by a numeric code. The set of statuses is open: custom
// services on the consensus node can define their own transaction types and status codes.
abstraction TransactionStatus {
  @@immutable code:int32 // the numeric status code reported by the consensus node
}

// The status codes used by the built-in services of the consensus node. The codes are the HAPI
// ResponseCodeEnum values.
enum BasicTransactionStatus(code: int32) extends TransactionStatus {
    OK(0)
    INVALID_TRANSACTION(1)
    PAYER_ACCOUNT_NOT_FOUND(2)
    // not complete yet: further status codes are still to be added here

    GRPC_WEB_PROXY_NOT_SUPPORTED(399)
}
```

## Testing

```tests
// Source: ResponseCodeEnum of the HAPI (response_code.proto).
@@testId("the-basic-transaction-statuses-have-the-codes-of-the-protocol")
test "the basic transaction statuses have the codes of the protocol" {
    assertEquals(expected: 0, actual: BasicTransactionStatus.OK.code)
    assertEquals(expected: 1, actual: BasicTransactionStatus.INVALID_TRANSACTION.code)
    assertEquals(expected: 2, actual: BasicTransactionStatus.PAYER_ACCOUNT_NOT_FOUND.code)
    assertEquals(expected: 399, actual: BasicTransactionStatus.GRPC_WEB_PROXY_NOT_SUPPORTED.code)
}
```

## Questions & Comments

- **`BasicTransactionStatus` has no `SUCCESS`.** The enum defines `OK(0)`, `INVALID_TRANSACTION(1)`,
  `PAYER_ACCOUNT_NOT_FOUND(2)` and `GRPC_WEB_PROXY_NOT_SUPPORTED(399)`, but the status a successful
  transaction reports in HAPI is `SUCCESS` with code 22. Every caller that wants to know whether a
  transaction succeeded has to compare against a hard-coded 22 — the TCK runtime does exactly that.
  `TransactionStatus` is deliberately open (an `int32` code, for custom services), so the question is
  which statuses the spec itself should name. The classification of attempts in `client.md` names further statuses
  (`BUSY`, `RECEIPT_NOT_FOUND`, `INVALID_NODE_ACCOUNT`, ...) that are missing here as well.
- **The name.** The same codes are the status of a query, so `TransactionStatus` names only half of what it is used
  for. The protocol calls them response codes. — open

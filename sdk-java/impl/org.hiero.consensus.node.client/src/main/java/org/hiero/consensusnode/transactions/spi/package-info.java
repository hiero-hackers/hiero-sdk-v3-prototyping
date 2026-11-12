/// Service provider interface for adding support for new transaction types to the SDK. The consensus
/// node can run custom services next to its built-in ones, and these services can define their own
/// transaction types. To make such a transaction type usable with the SDK, implement
/// `TransactionSupport` for it.
///
/// A `TransactionSupport` describes one concrete transaction type (e.g. `AccountCreateTransaction`):
/// it names the transaction type and the gRPC method used to submit it, writes the fields of the
/// transaction into the protobuf `TransactionBody`, and reads its typed receipt from the protobuf
/// `TransactionReceipt` or `TransactionRecord`. Everything that is the same for all transaction types
/// is done by the SDK and not by a `TransactionSupport`: it builds the `Response` of a submitted
/// transaction, reads the values every receipt has (`ReceiptHeader`) and builds the `Record` around
/// the typed receipt. The SDK looks up the matching `TransactionSupport` via `getTransactionSupport(...)`,
/// and `getAllTransactionSupports()` returns all registered implementations.
///
/// A custom service can also define its own status codes. Name them with a `TransactionStatusSupport`, which returns the
/// `TransactionStatus` values of the service. Every status code the SDK knows comes from such a support: the SDK registers
/// the one of `BasicTransactionStatus` itself, a module such as the CLPR module registers its own. The SDK reads the
/// status of every answer — the precheck of a request and the status of a receipt — and turns its code into a status with
/// `getTransactionStatus(code)`, the status of the registered support that names the code. A code that no support names
/// is an error (`unknown-status-error`): the request fails without another attempt. The supports are loaded once, when
/// the first client is created; if two of them name the same code, creating the client fails with
/// `status-conflict-error`.
package org.hiero.consensusnode.transactions.spi;

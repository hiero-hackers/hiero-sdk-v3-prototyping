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
package org.hiero.consensusnode.transactions.spi;

/// Query transactions from the Mirror Node. Use the `TransactionRepository` (available as
/// `MirrorNodeClient.transaction`) to look up a transaction by its `TransactionId`, to page through all transactions or
/// through the transactions of an account, optionally filtered by transaction type, by result or by whether the
/// account's balance was credited or debited. A `TransactionInfo` contains the fees, timestamps, result and all HBAR,
/// token, NFT and staking-reward transfers of the transaction, and the custom fees that were charged for it.
///
/// A transaction id can belong to several transactions: a transaction submitted by a user, the child transactions the
/// network created for it (with a nonce greater than 0) and a scheduled transaction. `findAllById` returns all of them,
/// `findById` the one with the nonce of the id that is not scheduled.
///
/// The type of a transaction is a `TransactionType` that carries the value the Mirror Node REST API uses for it
/// (`protocolName`). The types known to the SDK are available as constants in `BasicTransactionType`. A type the SDK does
/// not know yet (for example from a service added to the network later) is still returned as a `TransactionType` with the
/// received `protocolName`, so no information is lost and the value can be used as a filter again.
package org.hiero.mirrornode.transaction;

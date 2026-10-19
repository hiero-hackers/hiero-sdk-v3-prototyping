/// The receipt of a transaction tells whether it succeeded and which entity it created. You usually read it through the
/// `Response` you got when you submitted the transaction: `response.queryReceipt(client)` returns the receipt typed for
/// the transaction, e.g. an `AccountCreateReceipt` with the id of the new account.
///
/// **`TransactionReceiptQuery`** reads the receipt by the transaction id alone. Use it when you do not know the type of
/// the transaction, and to read the receipts of related transactions:
///
/// - **Duplicates** (`includeDuplicates`) — further transactions that were submitted with the same transaction id and
///   reached consensus after the first one. Their status is usually `DUPLICATE_TRANSACTION`.
/// - **Children** (`includeChildren`) — transactions the network created while it executed this one, e.g. the
///   transactions a smart contract call triggered. Their type is not known to the caller.
///
/// The query is free. The network keeps a receipt for about three minutes after consensus; before consensus and after
/// that period it answers that the receipt is unknown. The query waits while the receipt is not yet available and while
/// the node is busy, within the retry settings of the query, and fails when the receipt does not become available.
/// For a transaction older than about three minutes, read its record or ask the mirror node.
///
/// The result contains **`UntypedReceipt`s**: receipts with every attribute the network reports, each of them optional.
/// When you know the type of the transaction, turn an untyped receipt into the typed one with
/// `typed(transactionType)`:
///
/// ```
/// TransactionReceipts receipts = TransactionReceiptQuery{transactionId: id, includeChildren: true}
///     .submit(client).value;
/// AccountCreateReceipt receipt = receipts.receipt.typed(transactionType: AccountCreateTransaction);
/// for (UntypedReceipt child : receipts.children) { ... }
/// ```
///
/// The query returns the receipt also when the transaction failed: read its `status`.
package org.hiero.consensusnode.queries.receipts;

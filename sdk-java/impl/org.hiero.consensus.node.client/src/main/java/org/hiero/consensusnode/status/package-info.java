/// Every answer of a consensus node carries a status: the precheck of a submitted transaction or query, and the
/// receipt of an executed transaction. A `TransactionStatus` is identified by its numeric code. `BasicTransactionStatus`
/// names the codes of the built-in services of the consensus node. Every status the SDK knows is registered as a
/// `TransactionStatusSupport` (`consensusnode.transactions.spi`): the SDK registers the one of `BasicTransactionStatus`,
/// a module or a custom service its own codes. An answer with a code that no support names fails with
/// `unknown-status-error`.
package org.hiero.consensusnode.status;

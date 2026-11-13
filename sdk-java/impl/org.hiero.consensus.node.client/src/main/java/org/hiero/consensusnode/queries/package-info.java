/// A query is a read-only request to a consensus node that returns a typed result without changing ledger state. There
/// are two kinds of queries:
///
/// - **`FreeQuery<$$Result>`** — a free query that the network answers without charging you, for example an account
///   balance or a transaction receipt.
/// - **`PaidQuery<$$Result>`** — a query that must be paid for in the network's native token. It adds `getCost` to
///   ask for the current price and `maxQueryPayment` to cap what you are willing to pay. Paid queries are always paid
///   by the client's operator account.
///
/// Both are `Submittable`, so they share the execution settings a request can override (`retryPolicy`,
/// `attemptTimeout`) and are sent with `submit(client)`. Concrete query types (such as `AccountBalanceQuery` or
/// `TokenInfoQuery`) only add their own inputs and result type.
///
/// ### Payment
///
/// You never build the payment of a paid query yourself. On every `submit(client)` the SDK first asks the network for
/// the current price of the query, then pays exactly that amount from the operator account to the answering node and
/// sends the actual query together with the payment. If `maxQueryPayment` is set and the quoted price is higher, the
/// call fails before anything is paid.
///
/// The payment is a transfer transaction of the operator account, signed by the client's `TransactionSigner`:
///
/// - `rule:query-payment` The payment of a query is a transfer of its cost from the operator account to the node that
///   answers the query, with a maximum transaction fee of 100,000,000 base units of the native token of the network
///   (1 ℏ on Hedera) and a valid duration of 120 seconds. Every payment has a transaction id of its own.
/// - `rule:cost-request-payment` The request for the cost of a query (`getCost`, and the first request of every
///   `submit(client)` of a paid query) carries a payment of the same form with an amount of 0.
///
/// Use `getCost(client)` if you need to know the price before submitting, for example to ask a user for confirmation.
/// The returned price is a snapshot, not a reservation — the price may change before you submit. Set
/// `maxQueryPayment` if you need a hard upper bound.
///
/// ### Responses
///
/// `submit(client)` returns the typed payload wrapped in an envelope:
///
/// - `QueryResponse<$$Result>` contains the `value` and the consensus node that answered (`answeredBy`).
/// - `PaidQueryResponse<$$Result>` additionally contains the `cost` that was actually paid.
///
/// If you only need the payload, read `.value`: `query.submit(client).value`. Use the envelope when you need the
/// metadata, for example for billing or auditing.
package org.hiero.consensusnode.queries;

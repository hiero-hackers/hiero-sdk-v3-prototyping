/// A `HieroClient` is your entry point to a Hiero network. It represents a connection to one concrete network and an
/// operator `Account` — the account that pays for and signs the requests you send. Create a client once with
/// `createClient(...)` from a `NetworkSetting` and an operator account, and reuse it for all queries and transactions.
///
/// Everything you send to the network — queries as well as packed transactions — is a `Submittable`. Call
/// `submit(client)` to hand it to the network and receive its typed result asynchronously.
///
/// ### How a request is executed
///
/// A request is sent in one or more **attempts**. Each attempt goes to one consensus node and ends in an
/// `AttemptOutcome`: the node accepted the request, it was not reachable or did not answer in time, it was busy or
/// throttled the request, it was the wrong node, the answer is not ready yet (a receipt before consensus), or it rejected
/// the request. Which outcome an answer of the network has is fixed and the same in every SDK.
///
/// What happens after an attempt that did not succeed is decided by three exchangeable parts, which the client holds in
/// its `ExecutionSettings`:
///
/// - the **`RetryPolicy`** decides whether to try again — on the same node or on another one — and how long to wait
///   before, or to give up;
/// - the **`NodeSelector`** picks the node for an attempt and keeps track of nodes that failed, so that they are avoided
///   for a while by all requests of the client;
/// - the **`attemptTimeout`** bounds the time a single attempt may take.
///
/// `ExecutionSettings.createDefault()` gives the behavior of the SDK; `ExecutionSettings.createDefault(maxAttempts,
/// attemptTimeout)` the same with your own number of attempts and attempt timeout. Implement `RetryPolicy` or `NodeSelector` to use your own strategy, and
/// add an `AttemptListener` to log or measure every attempt. A single request can override the retry policy and the
/// attempt timeout of the client. When the retry policy gives up, `submit(client)` fails with
/// `attempts-exhausted-error`. An answer with a status code that the SDK cannot name fails the request at once with
/// `unknown-status-error` (see `TransactionStatusSupport`), and creating a client fails with `status-conflict-error` if
/// two registered status supports name the same code.
///
/// A transaction is packed for a fixed set of consensus nodes, so its attempts only go to these nodes.
///
/// ### The outcome of an attempt
///
/// Every SDK classifies the answer of a node in the same way:
///
/// | `AttemptOutcome` | Answer of the node |
/// |---|---|
/// | `SUCCESS` | precheck `OK`; for a receipt or record a status other than `UNKNOWN` and `OK` |
/// | `NODE_UNAVAILABLE` | transport failure `CONNECTION`, `OVERLOADED` or `INTERRUPTED` |
/// | `NODE_TIMEOUT` | transport failure `TIMEOUT` |
/// | `NODE_BUSY` | precheck `BUSY`, `PLATFORM_TRANSACTION_NOT_CREATED`, `PLATFORM_NOT_ACTIVE` |
/// | `WRONG_NODE` | precheck `INVALID_NODE_ACCOUNT` |
/// | `PENDING` | a receipt or record that is not available yet: precheck `UNKNOWN`, `RECEIPT_NOT_FOUND` or `RECORD_NOT_FOUND`, receipt status `UNKNOWN` or `OK` |
/// | `REJECTED` | every other status, and the transport failure `PROTOCOL` |
///
/// `DUPLICATE_TRANSACTION`, for example, is `REJECTED`. The result of a transport failure is classified by its kind, which
/// the SDK derives from the error of its transport.
///
/// ### The default execution
///
/// `ExecutionSettings.createDefault(maxAttempts, attemptTimeout)` executes every request by the following rules;
/// `ExecutionSettings.createDefault()` is the same with 10 attempts and an attempt timeout of 10 seconds. Every SDK
/// implements them exactly. Except for the choice among equally suitable nodes, which is random, the execution is
/// deterministic: the same answers of the network lead to the same decisions and the same waits.
///
/// **Candidates.** The nodes the attempts of a request may go to:
///
/// 1. a packed transaction: the nodes it was packed for;
/// 2. a `TransactionReceiptQuery` whose `nodeAccountId` is set, and the receipt and record queries of a `Response` whose
///    `nodeAccountId` is set: that node alone;
/// 3. every other request: all consensus nodes of the network of the client.
///
/// **Attempts.**
///
/// 4. A request is sent in attempts, numbered from 1. Every attempt counts towards `maxAttempts`, whatever its
///    outcome; this includes every attempt that asks again for a receipt or record that is not available yet.
/// 5. An attempt starts when the SDK begins to send the request to its node, including establishing the connection.
///    It ends when the complete answer has arrived, when the transport fails, or when `attemptTimeout` has elapsed
///    since its start, whichever comes first. An attempt that ends by the timeout has the result `TransportFailure` with
///    the kind `TIMEOUT`.
/// 6. The answer of an attempt is classified into an `AttemptOutcome` as described in "The outcome of an attempt".
///    After that, in this order:
///    1. the node selector is told about the attempt (`NodeSelector.attemptCompleted`);
///    2. every `AttemptListener` is told about it, in the order of `listeners`. An error raised by a listener is
///       ignored: it neither changes nor ends the request;
///    3. if the outcome is `SUCCESS`, the request is complete. Otherwise the retry policy decides (rules 7 to 10).
///
/// **Retry policy.**
///
/// 7. The decision depends on the outcome of the last attempt:
///
///    | Outcome of the last attempt | Decision |
///    |---|---|
///    | `NODE_UNAVAILABLE`, `NODE_TIMEOUT`, `NODE_BUSY`, `WRONG_NODE` | try again on another node (`RetryOnOtherNode`) |
///    | `PENDING` | try again on the same node (`RetryOnSameNode`) |
///    | `REJECTED` | give up (`GiveUp`) |
///
/// 8. When the last attempt is attempt number `maxAttempts`, the policy gives up, whatever the outcome.
/// 9. Before attempt n + 1 the SDK waits min(250 ms × 2^(n − 1), 8 s): 250 ms before the second attempt, then 500 ms,
///    1 s, 2 s, 4 s, and 8 s before every further attempt. The wait starts when attempt n ends. There is no random
///    share.
/// 10. A request therefore takes at most `maxAttempts` × `attemptTimeout` plus the waits between its attempts; with
///     10 attempts the waits add up to 39.75 s.
///
/// **Node selection.**
///
/// 11. The node selector keeps, for every node, the number k of its attempts in a row with the outcome
///     `NODE_UNAVAILABLE`, `NODE_TIMEOUT` or `WRONG_NODE`, starting at 0. This state is shared by all requests of the
///     client.
/// 12. After an attempt with one of these three outcomes, k of its node grows by 1, and the node is avoided until
///     min(8 s × 2^(k − 1), 1 h) after the end of the attempt: 8 s after the first such attempt, then 16 s, 32 s and so
///     on, at most 1 hour. After an attempt with any other outcome, k of its node is 0 again and the node is no longer
///     avoided.
/// 13. For the first attempt, and after a decision to try again on another node, the node is chosen among the
///     candidates; after such a decision without the node of the last attempt, unless it is the only candidate. The
///     choice is uniformly random among the candidates that are not avoided at that moment. When every candidate is
///     avoided, it is the candidate whose avoidance ends first; when several end at the same time, uniformly random
///     among them.
/// 14. After a decision to try again on the same node, the attempt goes to the node of the last attempt, also when that
///     node is avoided; no node is chosen.
///
/// **Paid queries.**
///
/// 15. Before a paid query is sent, the SDK asks for its cost in a request of its own, which follows the same rules.
/// 16. Every attempt of a paid query carries a payment to its node. An attempt on the same node as the attempt before
///     it sends the same payment again; an attempt on another node gets a new payment to that node.
///
/// **Giving up.**
///
/// 17. When the retry policy gives up, `submit(client)` fails with `attempts-exhausted-error`. The error describes the
///     outcome and the result of the last attempt.
/// 18. When an answer carries a status code that no registered `TransactionStatusSupport` names (the SDK registers the
///     one of `BasicTransactionStatus` itself), `submit(client)` fails with `unknown-status-error`; there is no further
///     attempt.
///
/// By default, transactions are signed with the operator's private key. If the key lives elsewhere (for example in an
/// HSM or a remote signing service), pass your own `TransactionSigner` when creating the client.
package org.hiero.consensusnode.client;

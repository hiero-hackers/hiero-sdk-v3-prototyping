# Client API

This section defines the client API.

## Description

A `HieroClient` is your entry point to a Hiero network. It represents a connection to one concrete network and an
operator `Account` — the account that pays for and signs the requests you send. Create a client once with
`createClient(...)` from a `NetworkSetting` and an operator account, and reuse it for all queries and transactions.

Everything you send to the network — queries as well as packed transactions — is a `Submittable`. Call
`submit(client)` to hand it to the network and receive its typed result asynchronously.

### How a request is executed

A request is sent in one or more **attempts**. Each attempt goes to one consensus node and ends in an
`AttemptOutcome`: the node accepted the request, it was not reachable or did not answer in time, it was busy, it
throttled the request, it was the wrong node, the answer is not ready yet (a receipt before consensus), or it rejected
the request. Which outcome an answer of the network has is fixed and the same in every SDK.

What happens after an attempt that did not succeed is decided by three exchangeable parts, which the client holds in
its `ExecutionSettings`:

- the **`RetryPolicy`** decides whether to try again — on the same node or on another one — and how long to wait
  before, or to give up;
- the **`NodeSelector`** picks the node for an attempt and keeps track of nodes that failed, so that they are avoided
  for a while by all requests of the client;
- the **`attemptTimeout`** bounds the time a single attempt may take.

`ExecutionSettings.createDefault()` gives the behavior of the SDK; `ExecutionSettings.createDefault(maxAttempts,
attemptTimeout)` the same with your own number of attempts and attempt timeout. Implement `RetryPolicy` or `NodeSelector` to use your own strategy, and
add an `AttemptListener` to log or measure every attempt. A single request can override the retry policy and the
attempt timeout of the client. When the retry policy gives up, `submit(client)` fails with
`attempts-exhausted-error`.

A transaction is packed for a fixed set of consensus nodes, so its attempts only go to these nodes.

### The outcome of an attempt

Every SDK classifies the answer of a node in the same way:

| `AttemptOutcome` | Answer of the node |
|---|---|
| `SUCCESS` | precheck `OK`; for a receipt or record a status other than `UNKNOWN` and `OK` |
| `NODE_UNAVAILABLE` | transport failure `CONNECTION`, `OVERLOADED` or `INTERRUPTED` |
| `NODE_TIMEOUT` | transport failure `TIMEOUT` |
| `NODE_BUSY` | precheck `BUSY`, `PLATFORM_TRANSACTION_NOT_CREATED`, `PLATFORM_NOT_ACTIVE` |
| `THROTTLED` | precheck `THROTTLED` |
| `WRONG_NODE` | precheck `INVALID_NODE_ACCOUNT` |
| `PENDING` | a receipt or record that is not available yet: precheck `UNKNOWN` or `RECEIPT_NOT_FOUND`, receipt status `UNKNOWN` or `OK` |
| `REJECTED` | every other status, and the transport failure `PROTOCOL` |

`DUPLICATE_TRANSACTION`, for example, is `REJECTED`. The result of a transport failure is classified by its kind, which
the SDK derives from the error of its transport.

### The default execution

`ExecutionSettings.createDefault(maxAttempts, attemptTimeout)` executes every request by the following rules;
`ExecutionSettings.createDefault()` is the same with 10 attempts and an attempt timeout of 10 seconds. Every SDK
implements them exactly. Except for the choice among equally suitable nodes, which is random, the execution is
deterministic: the same answers of the network lead to the same decisions and the same waits.

**Candidates.** The nodes the attempts of a request may go to:

1. a packed transaction: the nodes it was packed for;
2. a `TransactionReceiptQuery` whose `nodeAccountId` is set, and the receipt and record queries of a `Response` whose
   `nodeAccountId` is set: that node alone;
3. every other request: all consensus nodes of the network of the client.

**Attempts.**

4. A request is sent in attempts, numbered from 1. Every attempt counts towards `maxAttempts`, whatever its
   outcome; this includes every attempt that asks again for a receipt or record that is not available yet.
5. An attempt starts when the SDK begins to send the request to its node, including establishing the connection.
   It ends when the complete answer has arrived, when the transport fails, or when `attemptTimeout` has elapsed
   since its start, whichever comes first. An attempt that ends by the timeout has the result `TransportFailure` with
   the kind `TIMEOUT`.
6. The answer of an attempt is classified into an `AttemptOutcome` as described in "The outcome of an attempt".
   After that, in this order:
   1. the node selector is told about the attempt (`NodeSelector.attemptCompleted`);
   2. every `AttemptListener` is told about it, in the order of `listeners`. An error raised by a listener is
      ignored: it neither changes nor ends the request;
   3. if the outcome is `SUCCESS`, the request is complete. Otherwise the retry policy decides (rules 7 to 10).

**Retry policy.**

7. The decision depends on the outcome of the last attempt:

   | Outcome of the last attempt | Decision |
   |---|---|
   | `NODE_UNAVAILABLE`, `NODE_TIMEOUT`, `NODE_BUSY`, `WRONG_NODE` | try again on another node (`RetryOnOtherNode`) |
   | `PENDING` | try again on the same node (`RetryOnSameNode`) |
   | `THROTTLED`, `REJECTED` | give up (`GiveUp`) |

8. When the last attempt is attempt number `maxAttempts`, the policy gives up, whatever the outcome.
9. Before attempt n + 1 the SDK waits min(250 ms × 2^(n − 1), 8 s): 250 ms before the second attempt, then 500 ms,
   1 s, 2 s, 4 s, and 8 s before every further attempt. The wait starts when attempt n ends. There is no random
   share.
10. A request therefore takes at most `maxAttempts` × `attemptTimeout` plus the waits between its attempts; with
    10 attempts the waits add up to 39.75 s.

**Node selection.**

11. The node selector keeps, for every node, the number k of its attempts in a row with the outcome
    `NODE_UNAVAILABLE`, `NODE_TIMEOUT` or `WRONG_NODE`, starting at 0. This state is shared by all requests of the
    client.
12. After an attempt with one of these three outcomes, k of its node grows by 1, and the node is avoided until
    min(8 s × 2^(k − 1), 1 h) after the end of the attempt: 8 s after the first such attempt, then 16 s, 32 s and so
    on, at most 1 hour. After an attempt with any other outcome, k of its node is 0 again and the node is no longer
    avoided.
13. For the first attempt, and after a decision to try again on another node, the node is chosen among the
    candidates; after such a decision without the node of the last attempt, unless it is the only candidate. The
    choice is uniformly random among the candidates that are not avoided at that moment. When every candidate is
    avoided, it is the candidate whose avoidance ends first; when several end at the same time, uniformly random
    among them.
14. After a decision to try again on the same node, the attempt goes to the node of the last attempt, also when that
    node is avoided; no node is chosen.

**Paid queries.**

15. Before a paid query is sent, the SDK asks for its cost in a request of its own, which follows the same rules.
16. Every attempt of a paid query carries a payment to its node. An attempt on the same node as the attempt before
    it sends the same payment again; an attempt on another node gets a new payment to that node.

**Giving up.**

17. When the retry policy gives up, `submit(client)` fails with `attempts-exhausted-error`. The error describes the
    outcome and the result of the last attempt.

By default, transactions are signed with the operator's private key. If the key lives elsewhere (for example in an
HSM or a remote signing service), pass your own `TransactionSigner` when creating the client.

## Design Notes

- **Classification and decision are separate.** The SDK classifies every attempt into an `AttemptOutcome`, and that
  classification is normative; only the reaction to it is exchangeable. A custom `RetryPolicy` can still look at the
  raw `AttemptResult` (the status of the node, or the kind of the transport failure). The current Hiero SDKs differ in
  exactly this point: the same answer (`BUSY`, a gRPC deadline, `PLATFORM_TRANSACTION_NOT_CREATED`) is retried on the
  same node, on another node or not at all depending on the SDK, and none of them lets a caller replace the strategy.
- **Transport failures are transport neutral.** `TransportFailureKind` does not name gRPC codes, so that another
  transport only needs its own mapping. For gRPC:

  | gRPC | `TransportFailureKind` |
  |---|---|
  | `UNAVAILABLE`, no connection | `CONNECTION` |
  | `DEADLINE_EXCEEDED`, the attempt timeout elapsed | `TIMEOUT` |
  | `RESOURCE_EXHAUSTED` | `OVERLOADED` |
  | `INTERNAL` with an `RST_STREAM` reset, `CANCELLED` by the server | `INTERRUPTED` |
  | an answer that cannot be read, every other code | `PROTOCOL` |

- **`RetryPolicy` is stateless, `NodeSelector` is not.** A policy decides from the attempts of one request
  (`RetryContext`), so a request can bring its own. The health of the nodes is shared by all requests of a client and
  lives in the `NodeSelector`, which therefore exists once per client. `RetryContext` is a type of its own so that it
  can grow (e.g. by a retry hint of the network, as proposed by HIP-1383) without changing the signature of `decide`.
- **No overall timeout setting.** The default policy is bounded by the number of attempts and the attempt timeout, so
  its longest duration follows from the two values a caller sets. A policy that needs a total time can give up based
  on `RetryContext.elapsed`.
- **Language-specific execution means.** The sentence in the documentation of `ExecutionSettings` invites a language
  to add its own means of running asynchronous work (in Java an `Executor`); the language guides describe it.

## API Schema

```
namespace consensusnode.client
requires {AccountId, ConsensusNode, Network} from ledger
requires {NetworkSetting} from ledger.config
requires {PrivateKey, PublicKey} from keys
requires {NativeTokenUnit} from nativeToken
requires {TransactionStatus} from consensusnode.status

// An account that signs and pays for requests, identified by its id and private key.
Account {
    @@immutable accountId: AccountId // the account id of the operator
    @@immutable privateKey: PrivateKey // the private key of the operator
}

// A signature of a transaction for one specific consensus node.
type NodeSignature {
      @@immutable node: AccountId       // the consensus node's fee account
      @@immutable publicKey: PublicKey  // the public key that verifies the signature
      @@immutable signature: bytes      // the signature over the transaction bytes
}

// Signs transactions on behalf of the operator. Implement it to sign with a key that is not held in memory,
// for example in an HSM or a remote signing service.
abstraction TransactionSigner {

  // Signs the given transaction bytes for the given consensus node.
  NodeSignature signTransaction(transactionBytes: bytes, node: AccountId)
}

// Common base for anything that is sent to the consensus node network and produces a typed result — both
// queries (`Query`) and packed transactions (`PackedTransaction`). It is sent in attempts as the `ExecutionSettings`
// of the client define; the retry policy and the attempt timeout can be overridden for this request.
abstraction Submittable<$$Result> {

    @@nullable retryPolicy: RetryPolicy    // the retry policy for this request; if absent, the one of the client is used
    @@nullable attemptTimeout: duration    // the time one attempt may take; if absent, the one of the client is used

    // Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    // and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    // handled transparently. Fails with `attempts-exhausted-error` when the retry policy gives up.
    @@async @@throws(attempts-exhausted-error) $$Result submit(client: HieroClient<ANY>)
}

// How the SDK classifies the outcome of one attempt. The classification is the same in every SDK.
enum AttemptOutcome {
    SUCCESS,           // the node accepted the request, or the requested receipt or record is final
    NODE_UNAVAILABLE,  // the node could not be reached, refused further requests or broke off the answer
    NODE_TIMEOUT,      // the node did not answer within the attempt timeout
    NODE_BUSY,         // the node answered that it is busy or not ready to handle the request
    THROTTLED,         // the node answered that the request exceeds the throttle limits of the network
    WRONG_NODE,        // the node answered that the request is meant for another node
    PENDING,           // the requested receipt or record is not available yet
    REJECTED           // the node rejected the request, or its answer could not be read
}

// What one attempt got from the node: an answer or a transport failure.
@@sealed(NodeAnswer, TransportFailure)
abstraction AttemptResult {}

// The node answered: the status of its precheck, or the status of the requested receipt.
NodeAnswer extends AttemptResult {
    @@immutable status: TransactionStatus  // the status the node reported
}

// Why no answer of the node arrived, independent of the transport.
enum TransportFailureKind {
    CONNECTION,   // there is no connection to the node, or it broke down
    TIMEOUT,      // no answer arrived within the attempt timeout
    OVERLOADED,   // the connection refuses further requests because it is exhausted
    INTERRUPTED,  // the answer was broken off on the way
    PROTOCOL      // the answer could not be read
}

// The node did not answer.
TransportFailure extends AttemptResult {
    @@immutable kind: TransportFailureKind  // why no answer arrived
    @@immutable message: string             // a description for diagnosis and logs; not meant to be evaluated
}

// One attempt to send a request to a consensus node.
Attempt {
    @@immutable @@min(1) number: int32      // the number of the attempt within its request, 1 for the first one
    @@immutable node: ConsensusNode         // the node the attempt was sent to
    @@immutable result: AttemptResult       // what the attempt got from the node
    @@immutable outcome: AttemptOutcome     // how the SDK classifies the result
    @@immutable duration: duration          // the time the attempt took
}

// What happens after an attempt that did not succeed.
@@sealed(RetryOnSameNode, RetryOnOtherNode, GiveUp)
abstraction RetryDecision {}

// Try again on the node of the last attempt after the delay.
RetryOnSameNode extends RetryDecision {
    @@immutable delay: duration  // the time to wait before the next attempt
}

// Try again on another node after the delay. The `NodeSelector` picks the node among the candidates of the request
// other than the node of the last attempt; when there is no other candidate, the same node is used.
RetryOnOtherNode extends RetryDecision {
    @@immutable delay: duration  // the time to wait before the next attempt
}

// Do not try again: the request fails with `attempts-exhausted-error`.
GiveUp extends RetryDecision {}

// What a `RetryPolicy` knows when it decides.
RetryContext {
    @@immutable request: Submittable<ANY>  // the request, e.g. to tell a transaction from a query
    @@immutable attempts: list<Attempt>    // the attempts of the request so far, the last one at the end; never empty
    @@immutable elapsed: duration          // the time since the first attempt started
}

// Decides after every attempt that did not succeed whether and where the request is tried again. Implement it to
// use your own strategy. A policy is shared by all requests that use it and must not keep state of a request.
@@threadSafe
abstraction RetryPolicy {

    // The decision after the last attempt of the context, which did not succeed.
    RetryDecision decide(context: RetryContext)
}

// Picks the consensus node for an attempt and learns from the attempts of all requests of a client which nodes to
// avoid. Implement it to use your own strategy. One selector serves all requests of a client concurrently.
@@threadSafe
abstraction NodeSelector {

    // The node for the next attempt: one of the candidates, which are never empty. For a transaction the candidates
    // are the nodes it was packed for; `attempts` are the attempts of the request so far, empty for the first one.
    ConsensusNode select(candidates: list<ConsensusNode>, attempts: list<Attempt>)

    // Called after every attempt of every request of the client.
    void attemptCompleted(attempt: Attempt)
}

// Is told about every attempt, e.g. for logging and metrics; it cannot change the course of a request.
@@threadSafe
abstraction AttemptListener {

    // Called after every attempt of every request of the client.
    void attemptCompleted(request: Submittable<ANY>, attempt: Attempt)
}

// How a client executes requests: which node an attempt goes to, whether and when a failed attempt is repeated, how
// long a single attempt may take, and who is told about each attempt.
// A language that has its own means to run asynchronous work (an executor, a scheduler, a runtime handle) adds an
// attribute for it here, with a default, so that callers can choose where the work of the SDK runs.
@@finalType
ExecutionSettings {
    @@immutable retryPolicy: RetryPolicy                         // decides whether and where a request is tried again
    @@immutable nodeSelector: NodeSelector                       // picks the node of an attempt
    @@immutable attemptTimeout: duration                         // the time one attempt may take
    @@immutable @@default([]) listeners: list<AttemptListener>   // told about every attempt

    // The execution settings of the SDK: `createDefault(maxAttempts, attemptTimeout)` with 10 attempts and an attempt
    // timeout of 10 seconds. A request takes at most 10 attempts of 10 seconds each plus the waits between them
    // (39.75 seconds), about 2 minutes and 20 seconds.
    @@static ExecutionSettings createDefault()

    // The execution settings of the SDK with the given number of attempts and attempt timeout, and no listeners. They
    // execute a request as the section "The default execution" of this API describes: on another node after a node
    // failure, on the same node while a receipt is pending, giving up after `THROTTLED`, a rejection or the last
    // attempt; waiting 250 ms before the second attempt, doubled up to 8 s; avoiding a failed node for 8 s, doubled
    // up to 1 hour. A request takes at most `maxAttempts` times `attemptTimeout` plus the waits between the attempts.
    @@static ExecutionSettings createDefault(@@min(1) maxAttempts: int32, @@min(1) attemptTimeout: duration)
}

// A connection to one network with one operator account; the entry point for all queries and transactions.
HieroClient<$$Unit extends NativeTokenUnit> {
    @@immutable operatorAccount: Account // the account that pays for and signs requests
    @@immutable network: Network<$$Unit> // the network to connect to
    @@immutable transactionSigner: TransactionSigner // signs transactions; by default the operator account's private key is used
    @@immutable executionSettings: ExecutionSettings // how requests are executed; by default `ExecutionSettings.createDefault()`

    // TO_BE_DEFINED_IN_FUTURE_VERSIONS
}

// factory methods of `HieroClient` that should be added to the namespace in the best language dependent way

// Creates a client for the given network that uses the operator account to pay for and sign requests.
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account)
// Creates a client for the given network that uses the operator account to pay for requests and the given
// signer to sign transactions.
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account, transactionSigner: TransactionSigner)
// Creates a client for the given network that uses the operator account to pay for requests, the given signer to
// sign transactions and the given settings to execute requests.
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account,
        transactionSigner: TransactionSigner, executionSettings: ExecutionSettings)
```

## Default Instances for Testing

```
// The operator account: the default account id with the default private key.
instance Account = Account{accountId: DEFAULT, privateKey: DEFAULT}

// A client for the default network with the default operator account.
instance HieroClient<ANY> = createClient(networkSettings: DEFAULT, operatorAccount: DEFAULT)

// The signer of a client: by default it signs with the private key of the operator account.
instance TransactionSigner = DEFAULT(HieroClient<ANY>).transactionSigner

// The execution settings of the SDK.
instance ExecutionSettings = ExecutionSettings.createDefault()
instance RetryPolicy = DEFAULT(ExecutionSettings).retryPolicy
instance NodeSelector = DEFAULT(ExecutionSettings).nodeSelector
```

## Examples

The following example shows how to create a `HieroClient` instance:

```
AccountId accountId = ...;
PrivateKey privateKey = ...;
Account operatorAccount = new Account(accountId, privateKey);

NetworkSetting networkSettings = ...;

HieroClient client = HieroClient.createClient(networkSettings, operatorAccount);
```

## Testing

Offline tests of client creation and of the default signer, written as given / when / then. Nothing is sent to a
network; connecting, node selection and retries need a network and are tested by the Hiero TCK. The comment above
each test names where the current SDKs test the same.

```tests
// Source: the Go SDK (TestUnitClientFromConfigWithOperator), the Python SDK (client_test.py,
// test_for_testnet_then_set_operator) and the C++ SDK (ClientUnitTests): a client keeps its operator.
@@testId("a-client-keeps-the-operator-account-it-was-created-with")
test "a client keeps the operator account it was created with" {
    // given
    let operator = DEFAULT(Account)

    // when
    let client = createClient(networkSettings: DEFAULT, operatorAccount: operator)

    // then
    assertEquals(expected: operator, actual: client.operatorAccount)
    assertEquals(expected: operator.accountId, actual: client.operatorAccount.accountId)
    assertEquals(expected: operator.privateKey, actual: client.operatorAccount.privateKey)
}

// Source: the Java SDK (ClientTest.forMainnet, forNameReturnsCorrectNetwork), the Go SDK (TestUnitClientFromConfig)
// and the Python SDK (client_test.py, test_factory_basic_setup): a client is bound to the network it was created for.
@@testId("a-client-is-bound-to-the-network-of-its-network-setting")
test "a client is bound to the network of its network setting" {
    // given
    let setting = DEFAULT(NetworkSetting)

    // when
    let client = createClient(networkSettings: setting, operatorAccount: DEFAULT)

    // then
    assertEquals(expected: setting.network, actual: client.network)
}

// Source: this spec (by default the operator account's private key signs); the Java and JavaScript SDKs
// (Client.setOperator signs with the operator's private key) and the Go SDK (the operator's key signs).
@@testId("the-default-signer-signs-with-the-private-key-of-the-operator-account")
test "the default signer signs with the private key of the operator account" {
    // given
    let operator = DEFAULT(Account)
    let client = createClient(networkSettings: DEFAULT, operatorAccount: operator)
    let node = AccountId.fromString(value: "0.0.3")
    let transactionBytes = utf8(text: "transaction body")

    // when
    let signature = client.transactionSigner.signTransaction(transactionBytes: transactionBytes, node: node)

    // then
    assertEquals(expected: node, actual: signature.node)
    assertEquals(expected: operator.privateKey.createPublicKey(), actual: signature.publicKey)
    assertTrue(condition: signature.publicKey.verify(message: transactionBytes, signature: signature.signature))
    assertFalse(condition: signature.publicKey.verify(message: utf8(text: "another body"),
            signature: signature.signature))
}

// Source: the Java and JavaScript SDKs (Client.setOperatorWith: an operator whose transactions are signed by a
// given signer instead of its private key). The second key is the secp256k1 key of the tests of keys.md.
@@testId("a-client-created-with-a-signer-signs-with-that-signer")
test "a client created with a signer signs with that signer" {
    // given
    let signerKey = keys.createPrivateKey(algorithm: keys.KeyAlgorithm.ECDSA, encoding: keys.ByteImportEncoding.HEX,
            value: "8776c6b831a1b61ac10dac0304a2843de4716f54b1919bb91a2685d0fe3f3048")
    let signerAccount = Account{accountId: AccountId.fromString(value: "0.0.1002"), privateKey: signerKey}
    let signer = createClient(networkSettings: DEFAULT, operatorAccount: signerAccount).transactionSigner
    let transactionBytes = utf8(text: "transaction body")

    // when
    let client = createClient(networkSettings: DEFAULT, operatorAccount: DEFAULT, transactionSigner: signer)
    let signature = client.transactionSigner.signTransaction(transactionBytes: transactionBytes,
            node: AccountId.fromString(value: "0.0.3"))

    // then
    assertEquals(expected: DEFAULT(Account), actual: client.operatorAccount)
    assertEquals(expected: signerKey.createPublicKey(), actual: signature.publicKey)
    assertTrue(condition: signature.publicKey.verify(message: transactionBytes, signature: signature.signature))
}
```

## Questions & Comments

- **A `HieroClient` cannot reach its network.** `HieroClient` is immutable and holds operator, `Network`
  and `TransactionSigner`; `Network` carries the ledger id, a name and the native token unit, but no
  `ConsensusNode`. The nodes are only in the `NetworkSetting` that `createClient(...)` receives, and that
  object is not reachable from the client afterwards. An implementation therefore has nowhere to take the
  target node from when a transaction is signed or submitted, and nowhere to keep a connection to it.

  Found while implementing `createAccount` against the TCK; the spike works around it with a
  process-wide registry keyed by the client instance, which is a workaround, not a design. Options:
  carry the nodes (or the whole `NetworkSetting`) in `HieroClient`, or make `HieroClient` an abstraction
  whose implementation holds the connection state.

- **`ClientFactory.createClient` returns `HieroClient<?>`**, so the caller loses the native-token unit
  and every later use needs a wildcard. Should the factory be generic in the unit of the `NetworkSetting`?

- **Which error does `submit` raise for a rejected request?** `attempts-exhausted-error` is raised whenever the retry
  policy gives up, also after a `REJECTED` attempt (e.g. `INSUFFICIENT_PAYER_BALANCE`) that no policy would repeat.
  Callers need the status and the attempts in the error, but an error id cannot declare attributes in the
  meta-language. See the question on the status of a rejected transaction in `transactions.md`. — open
- **Retrying a transaction after `NODE_TIMEOUT`.** The node may have received the transaction although its answer did
  not arrive. The default policy sends it to another node, whose precheck may then answer `DUPLICATE_TRANSACTION`,
  which is classified `REJECTED` although the transaction may succeed. Should `DUPLICATE_TRANSACTION` after an earlier
  attempt of the same request count as `SUCCESS`? — open
- **The values of the defaults.** The default execution follows the majority of the current Hiero SDKs: 10 attempts,
  a wait from 250 ms to 8 s, 10 s per attempt, nodes avoided from 8 s to 1 h. None of these values is derived from
  measured answer times of the nodes; the current SDKs took them over from the Java SDK without a documented reason.
  Changing a value is not a change of the API, but of the specified behavior. — open, to be measured
- **Retrying `THROTTLED`.** The default gives up, as every current SDK does. A precheck `THROTTLED` costs no fee and
  is temporary, so waiting and trying again on another node would be possible; HIP-1383 proposes a retry hint of the
  network for exactly this. — open
- **A payment sent again.** Rule 16 sends the payment of a paid query again when the query is retried on the same
  node. If the earlier attempt timed out after the node had accepted the payment, the node answers
  `DUPLICATE_TRANSACTION` (the same question as for transactions above). — open
- **The address book after `WRONG_NODE`.** The current SDKs update their list of nodes when a node answers
  `INVALID_NODE_ACCOUNT`. Where V3 keeps its nodes is the first question above. — open
- **A replaceable clock.** Waiting between attempts makes tests of the execution slow. A clock in `ExecutionSettings`
  would let tests run without real waiting, in every language. — open
- **Errors of a `NodeSelector` or `RetryPolicy`.** What happens when a custom implementation fails or returns a node
  that is not a candidate is not specified. — open

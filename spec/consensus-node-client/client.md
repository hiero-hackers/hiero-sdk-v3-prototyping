# Client API

This section defines the client API.

## Description

A `HieroClient` is your entry point to a Hiero network. It represents a connection to one concrete network and an
operator `Account` — the account that pays for and signs the requests you send. Create a client once with
`createClient(...)` from a `NetworkSetting` and an operator account, and reuse it for all queries and transactions.

Everything you send to the network — queries as well as packed transactions — is a `Submittable`. Call
`submit(client)` to hand it to the network and receive its typed result asynchronously. The SDK selects a consensus
node, retries transient failures and bounds the total wait; you can tune that behavior per request with
`maxAttempts`, `minBackoff`, `maxBackoff` and `attemptTimeout`.

By default, transactions are signed with the operator's private key. If the key lives elsewhere (for example in an
HSM or a remote signing service), pass your own `TransactionSigner` when creating the client.

## API Schema

```
namespace consensusnode.client
requires {AccountId, Network} from ledger
requires {NetworkSetting} from ledger.config
requires {PrivateKey, PublicKey} from keys
requires {NativeTokenUnit} from nativeToken

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
// queries (`Query`) and packed transactions (`PackedTransaction`). It carries the retry settings (maximum
// attempts, backoff window, per-attempt timeout) and the `submit()` entry point. The SDK applies these settings
// when selecting a consensus node, retrying transient gRPC failures and bounding the total wait.
abstraction Submittable<$$Result> {

    @@nullable maxAttempts: int32      // maximum number of attempts; if absent, the SDK default is used
    @@nullable maxBackoff: int64       // upper bound of the backoff between attempts; if absent, the SDK default is used
    @@nullable minBackoff: int64       // lower bound of the backoff between attempts; if absent, the SDK default is used
    @@nullable attemptTimeout: int64   // timeout for a single attempt; if absent, the SDK default is used

    // Sends this request to the network and returns the typed result asynchronously. Node selection, retries
    // and operation-specific protocol details (for example cost discovery and payment for a `PaidQuery`) are
    // handled transparently.
    @@async $$Result submit(client: HieroClient<ANY>)
}

// A connection to one network with one operator account; the entry point for all queries and transactions.
HieroClient<$$Unit extends NativeTokenUnit> {
    @@immutable operatorAccount: Account // the account that pays for and signs requests
    @@immutable network: Network<$$Unit> // the network to connect to
    @@immutable transactionSigner: TransactionSigner // signs transactions; by default the operator account's private key is used

    // TO_BE_DEFINED_IN_FUTURE_VERSIONS
}

// factory methods of `HieroClient` that should be added to the namespace in the best language dependent way

// Creates a client for the given network that uses the operator account to pay for and sign requests.
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account)
// Creates a client for the given network that uses the operator account to pay for requests and the given
// signer to sign transactions.
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account, transactionSigner: TransactionSigner)
```

## Default Instances for Testing

```
// The operator account: the default account id with the default private key.
instance Account = Account{accountId: DEFAULT, privateKey: DEFAULT}

// A client for the default network with the default operator account.
instance HieroClient<ANY> = createClient(networkSettings: DEFAULT, operatorAccount: DEFAULT)

// The signer of a client: by default it signs with the private key of the operator account.
instance TransactionSigner = DEFAULT(HieroClient<ANY>).transactionSigner
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

- **The backoff and timeout settings of `Submittable` have no unit.** `minBackoff`, `maxBackoff` and
  `attemptTimeout` are `int64`, so neither callers nor tests know whether a value is in milliseconds or seconds (the
  V2 SDKs use durations: `Duration` in Java, milliseconds in JavaScript). The meta-language has `duration` (millisecond
  precision) for exactly this; should the three fields become `duration`? Until then the tests cannot check them as
  times.

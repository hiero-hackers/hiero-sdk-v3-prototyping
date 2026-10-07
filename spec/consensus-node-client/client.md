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

## Default Instances

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

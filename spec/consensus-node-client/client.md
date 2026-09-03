# Client API

This section defines the client API.

## Description

The client API is the API that will be used by the SDK to interact with the network.
A client defines a concrete network connection to a specific network with a specific operator account.

## API Schema

```
namespace consensusnode.client
requires {AccountId, ConsensusNode, MirrorNode, Network} from ledger
requires {NetworkSetting} from ledger.config
requires {PrivateKey} from keys
requires {NativeTokenUnit} from nativeToken

// Definition of an account that signs and pays for requests
Account {
    @@immutable accountId: AccountId // the account id of the operator
    @@immutable privateKey: PrivateKey // the private key of the operator
}

type NodeSignature {
      @@immutable node: AccountId       // the consensus node's fee account
      @@immutable publicKey: PublicKey
      @@immutable signature: bytes
}

// Helper to allow external signing of transactions
abstraction TransactionSigner {

  NodeSignature signTransaction(transactionBytes: bytes, node: AccountId)
}

// Common base for anything that is handed off to the consensus node network and
// produces a typed result — both queries (consensusnode.queries.Query) and
// packed transactions (consensusnode.transactions.PackedTransaction) extend this.
// Carries the shared retry-tuning knobs (max attempts, backoff window, per-attempt
// timeout) and the single submit() entry point. The SDK applies these settings
// when selecting a consensus node, retrying transient gRPC failures, and bounding
// the total wait.
abstraction Submittable<$$Result> {

    @@nullable maxAttempts: int32
    @@nullable maxBackoff: int64
    @@nullable minBackoff: int64
    @@nullable attemptTimeout: int64

    // Hand off to the network and return the typed result. Node selection,
    // retry, and any operation-specific protocol details (e.g. cost discovery
    // and payment for PaidQuery) are handled by the SDK transparently.
    @@async $$Result submit(client: HieroClient)
}

// The client API that will be used by the SDK to interact with the network
HieroClient<$$Unit extends NativeTokenUnit> {
    @@immutable operatorAccount: Account // the operator account
    @@immutable network: Network<$$Unit> // the network to connect to
    @@immutable transactionSigner: TransactionSigner // by default the operator account is used, but this allows to use an external signer for transactions

    // Immutable snapshots of the addresses currently used by this client
    set<ConsensusNode> getConsensusNodes()
    set<MirrorNode> getMirrorNodes()

    // TO_BE_DEFINED_IN_FUTURE_VERSIONS
}

// factory methods of `HieroClient` that should be added to the namespace in the best language dependent way

@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account)
@@static HieroClient<ANY> createClient(networkSettings: NetworkSetting, operatorAccount: Account, transactionSigner: TransactionSigner)
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

## Node Address Management

When a client is created, it initializes its consensus-node and mirror-node address snapshots from the supplied
`NetworkSetting`. `getConsensusNodes` and `getMirrorNodes` return immutable snapshots of the addresses currently used by
that client; later updates must not mutate a previously returned collection.

The client maintains its consensus-node addresses through an underlying periodic refresh process. Every `x` hours, the
process retrieves the latest consensus address book through one of the client's configured mirror nodes, validates the
result, and atomically installs a new consensus-node snapshot. The value of `x`, retry policy, and process lifecycle are
implementation configuration and must be documented by each SDK. The process starts with the bootstrap snapshot, runs
in the background while the client is active, and stops when the client is closed.

### Consensus-node refresh mechanism

The underlying process performs the following steps:

1. Select a usable mirror node from the client's fixed mirror-node snapshot.
2. Request the latest consensus address book from that mirror node. For the built-in Hedera settings, the source is the
   network's `/network/nodes` endpoint specified in [`hedera`](../base/hedera.md#built-in-network-settings).
3. Convert every usable service endpoint in the response into a `ConsensusNode` containing its IP address, port, and
   node account. A node that publishes multiple usable endpoints produces one entry for each endpoint.
4. Validate the complete candidate snapshot. It must be non-empty, every address and port must be valid, and every
   entry must identify a node account. Duplicate endpoints must be handled consistently and must not produce an
   ambiguous routing entry.
5. Build a new immutable snapshot without modifying the active snapshot.
6. Atomically replace the active consensus-node snapshot only after the entire candidate snapshot passes validation.

Requests executing concurrently with a successful refresh use either the complete previous snapshot or the complete
new snapshot. They must never observe a partially updated address book. If mirror-node selection, retrieval, parsing,
or validation fails, the process reports the failure through the SDK's normal diagnostics and leaves the previous
snapshot active. The next scheduled refresh may retry the update.

Consensus refresh does not discover or replace mirror nodes. The mirror-node snapshot comes from the `NetworkSetting`
supplied when the client is constructed and remains fixed for the lifetime of that client. To use different mirror-node
addresses, an application constructs a new client from a setting containing those addresses.

The refresh interval, retry backoff, endpoint health checking, and protocol selection are implementation details. An
SDK must retain the last valid snapshot after a failed attempt and must not allow overlapping scheduled refreshes to
install results out of order.

## Testing

### `consensusnode.client/bootstrap-node-snapshots-are-exposed`

 - **Given** a client created from a network setting.
 - **When** its consensus and mirror nodes are retrieved.
 - **Then** they equal the setting's bootstrap snapshots and are immutable.

### `consensusnode.client/periodic-refresh-replaces-consensus-snapshot`

 - **Given** an active client whose periodic refresh interval has elapsed.
 - **When** the underlying process receives a valid newer address book.
 - **Then** the consensus-node snapshot is atomically replaced and previously returned snapshots remain unchanged.

### `consensusnode.client/failed-refresh-preserves-consensus-snapshot`

 - **Given** a client with a valid consensus-node snapshot.
 - **When** a periodic refresh fails or returns an invalid address book.
 - **Then** the failure is reported and the previous snapshot remains active.

### `consensusnode.client/refresh-does-not-change-mirror-nodes`

 - **Given** a client with fixed mirror nodes.
 - **When** its consensus nodes are refreshed.
 - **Then** its mirror-node snapshot is unchanged.

### `consensusnode.client/closed-client-does-not-refresh`

 - **Given** a client that is closed.
 - **When** another refresh interval elapses.
 - **Then** no consensus-node refresh is performed.

## Questions & Comments

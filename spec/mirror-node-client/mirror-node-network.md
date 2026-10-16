# Mirror Node Network Query API

## Description

Query network-wide data from the Mirror Node. Use the `NetworkRepository` (available as `MirrorNodeClient.network`)
to read the current and next HBAR exchange rate, the current fee schedule (`fees`), the staking parameters (`stake`),
the HBAR supply (`supplies`) and the address book of consensus nodes (`nodes`). `estimateFees` estimates the fee of a
candidate transaction before it is submitted (HIP-1313).

## API Schema

```
namespace mirrornode.network
requires {AccountId, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common

@@finalType
ExchangeRate {
    @@immutable centEquivalent: int32
    @@immutable hbarEquivalent: int32
    @@immutable expirationTime: zonedDateTime
}

@@finalType
ExchangeRates {
    @@immutable currentRate: ExchangeRate
    @@immutable nextRate: ExchangeRate
}

@@finalType
NetworkFee {
    @@immutable gas: int64
    @@immutable transactionType: string
}

@@finalType
NetworkStake {
    @@immutable maxStakeReward: int64
    @@immutable maxStakeRewardPerHbar: int64
    @@immutable maxTotalReward: int64
    @@immutable nodeRewardFeeFraction: double
    @@immutable reservedStakingRewards: int64
    @@immutable rewardBalanceThreshold: int64
    @@immutable stakeTotal: int64
    @@immutable stakingPeriod: seconds
    @@immutable stakingPeriodsStored: int64
    @@immutable stakingRewardFeeFraction: double
    @@immutable stakingRewardRate: int64
    @@immutable stakingStartThreshold: int64
    @@immutable unreservedStakingRewardBalance: int64
}

@@finalType
NetworkSupplies {
    @@immutable releasedSupply: int256
    @@immutable totalSupply: int256
}

// A reachable gRPC endpoint of a consensus node (GET /network/nodes). One of ipAddressV4 /
// domainName is populated (HIP-1046 added domain names).
@@finalType
ServiceEndpoint {
    @@immutable @@nullable ipAddressV4: string        // dotted-quad IPv4, when published
    @@immutable port: int32
    @@immutable @@nullable domainName: string         // DNS name, when published (HIP-1046)
}

// One consensus-node entry in the address book. Returned by NetworkRepository.nodes()
// (GET /network/nodes).
@@finalType
NetworkNode {
    @@immutable nodeId: int64                          // stable HIP-869 node id
    @@immutable accountId: AccountId                   // fee account that receives this node's share
    @@immutable @@nullable description: string
    @@immutable @@nullable publicKey: string           // node gossip public key (hex)
    @@immutable @@nullable nodeCertHash: string        // SHA-384 of the node's TLS certificate (hex)
    @@immutable @@default([]) serviceEndpoints: list<ServiceEndpoint>
    @@immutable @@nullable adminKey: Authority         // DAB admin key (HIP-869); null on pre-DAB entries
    @@immutable stake: int64                           // total stake delegated to this node (tinybars)
    @@immutable @@nullable timestamp: zonedDateTime    // consensus time of this address-book entry
}

// Estimated fee for a candidate transaction (HIP-1313, congestion-adjusted). Returned by
// NetworkRepository.estimateFees().
@@finalType
FeeEstimate {
    @@immutable estimatedFee: int64                    // estimated total fee in tinybars
}

NetworkRepository {
    @@async @@throws(mirror-node-error)
    @@nullable ExchangeRates exchangeRates()

    @@async @@throws(mirror-node-error)
    list<NetworkFee> fees()

    @@async @@throws(mirror-node-error)
    @@nullable NetworkStake stake()

    @@async @@throws(mirror-node-error)
    @@nullable NetworkSupplies supplies()

    // The network's consensus-node list (address book).
    // Maps to GET /api/v1/network/nodes.
    @@async @@throws(mirror-node-error)
    Page<NetworkNode> nodes()

    // Estimate the fee for a candidate transaction (HIP-1313). The transaction is passed as its
    // protobuf-encoded bytes. Maps to POST /api/v1/network/fees.
    @@async @@throws(mirror-node-error)
    FeeEstimate estimateFees(transaction: bytes)
}

@@static NetworkRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `NetworkRepository` need a Mirror Node, and `ExchangeRate` and `NetworkStake` cannot be created in a
test because the test language has no literal for `zonedDateTime` and `seconds`.

```tests
// Source: the Java SDK (MirrorNodeServiceEndpointTest: IP address 1.2.3.4, domain name "test.mirror.com", port 443);
// HIP-1046 (an endpoint is published either with an IP address or with a domain name).
test "a service endpoint keeps either its IPv4 address or its domain name and its port" {
    // when
    let byAddress = ServiceEndpoint{ipAddressV4: "1.2.3.4", port: 443}
    let byName = ServiceEndpoint{port: 443, domainName: "test.mirror.com"}

    // then
    assertEquals(expected: "1.2.3.4", actual: byAddress.ipAddressV4)
    assertNull(actual: byAddress.domainName)
    assertEquals(expected: 443, actual: byAddress.port)
    assertEquals(expected: "test.mirror.com", actual: byName.domainName)
    assertNull(actual: byName.ipAddressV4)
    assertEquals(expected: 443, actual: byName.port)
}

// Source: this spec (`serviceEndpoints` defaults to an empty list; `adminKey` is null on entries from before HIP-869).
test "a network node without service endpoints and admin key has an empty endpoint list and no admin key" {
    // given
    let account = AccountId.fromString(value: "0.0.3")

    // when
    let node = NetworkNode{nodeId: 0, accountId: account, stake: 0}

    // then
    assertEquals(expected: 0, actual: node.nodeId)
    assertEquals(expected: account, actual: node.accountId)
    assertEquals(expected: 0, actual: length(value: node.serviceEndpoints))
    assertNull(actual: node.adminKey)
    assertNull(actual: node.timestamp)
}

// Source: the Java SDK (MirrorNodeServiceEndpointTest); this spec (the address book entry lists its endpoints).
test "a network node keeps its service endpoints in order" {
    // given
    let endpoints = [ServiceEndpoint{ipAddressV4: "1.2.3.4", port: 443},
            ServiceEndpoint{port: 443, domainName: "test.mirror.com"}]

    // when
    let node = NetworkNode{nodeId: 0, accountId: AccountId.fromString(value: "0.0.3"), serviceEndpoints: endpoints,
            stake: 0}

    // then
    assertEquals(expected: endpoints, actual: node.serviceEndpoints)
}
```

## Questions & Comments

- **`fees()` vs `estimateFees()`.** `fees()` (existing) returns the network's *current* per-type
  gas fee schedule (`GET /network/fees`). `estimateFees()` is the HIP-1313 *estimation* of a
  specific candidate transaction (`POST /network/fees`) — a different operation despite sharing the
  path.
- **`FeeEstimate` is provisional.** The OpenAPI references `FeeEstimateResponse` but does not expose
  its field set, and the full HIP-1313 fee-estimation model (node / network / service breakdown
  plus the high-volume congestion multiplier) is tracked in
  [`missing-features.md`](../../docs/missing-features.md) §3.4. Only `estimatedFee` is modelled here;
  expand once §3.4 lands the authoritative shape.
- **`estimateFees(transaction: bytes)` takes encoded bytes, not a typed transaction.** The
  mirror-node layer is independent of `consensus-node-client`, so it cannot reference
  `PackedTransaction`; the protobuf-encoded transaction bytes are the layering-clean input (and
  match what the REST endpoint accepts).
- **`NetworkNode` is the mirror-node read shape**, distinct from the consensus-node
  `NodeAddress` returned by `NodeAddressBookQuery`
  ([`queries-network.md`](../consensus-node-admin-client/queries-network.md)) — same entity, two
  sources, slightly different fields. `ServiceEndpoint` is redefined locally here (the mirror node
  cannot import the consensus-admin `ServiceEndpoint`).
- **`NetworkNode.adminKey` is an `Authority`.** The mirror returns the DAB admin key as a `Key`;
  V3 types it as the authorization `Authority` (per [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md)).
- **`ServiceEndpoint` does not state its invariants as annotations.** Its comment says that one of `ipAddressV4` /
  `domainName` is populated, but there is no `@@oneOf(ipAddressV4, domainName)`, and `port` has no
  `@@min(0) @@max(65535)` — the Java SDK rejects `-1` and `65536` (`MirrorNodeServiceEndpointTest`). With the
  annotations, these SDK tests would apply here too (once the test language can expect a construction error).

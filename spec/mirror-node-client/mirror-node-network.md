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

The queries of `NetworkRepository` need a Mirror Node; only the value types are tested here.

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

// Source: this spec (`timestamp` is the consensus time of the address-book entry).
test "a network node keeps the consensus time of its address-book entry" {
    // given
    let time = DEFAULT(zonedDateTime)

    // when
    let node = NetworkNode{nodeId: 0, accountId: AccountId.fromString(value: "0.0.3"), stake: 0, timestamp: time}

    // then
    assertEquals(expected: time, actual: node.timestamp)
}

// Source: the Hiero Enterprise Java test fixture exchange-rate.json (current rate 212848 cents for 30000 HBAR expiring
// at 1785852000, next rate 211949 cents for 30000 HBAR expiring at 1785855600; the epoch seconds written as ISO 8601).
test "exchange rates keep the current and the next rate with their expiration times" {
    // given
    let current = ExchangeRate{centEquivalent: 212848, hbarEquivalent: 30000,
            expirationTime: toZonedDateTime(text: "2026-08-04T14:00:00Z")}
    let next = ExchangeRate{centEquivalent: 211949, hbarEquivalent: 30000,
            expirationTime: toZonedDateTime(text: "2026-08-04T15:00:00Z")}

    // when
    let rates = ExchangeRates{currentRate: current, nextRate: next}

    // then
    assertEquals(expected: current, actual: rates.currentRate)
    assertEquals(expected: next, actual: rates.nextRate)
    assertEquals(expected: 212848, actual: rates.currentRate.centEquivalent)
    assertEquals(expected: 30000, actual: rates.currentRate.hbarEquivalent)
    assertEquals(expected: toZonedDateTime(text: "2026-08-04T14:00:00Z"), actual: rates.currentRate.expirationTime)
    assertEquals(expected: toZonedDateTime(text: "2026-08-04T15:00:00Z"), actual: rates.nextRate.expirationTime)
    assertNotEquals(unexpected: rates.currentRate, actual: rates.nextRate)
}

// Source: this spec (exchange rates are values: rates that differ only in their expiration time are different).
test "exchange rates that differ only in their expiration time are different" {
    // when
    let first = ExchangeRate{centEquivalent: 212848, hbarEquivalent: 30000,
            expirationTime: toZonedDateTime(text: "2026-08-04T14:00:00Z")}
    let second = ExchangeRate{centEquivalent: 212848, hbarEquivalent: 30000,
            expirationTime: toZonedDateTime(text: "2026-08-04T15:00:00Z")}

    // then
    assertNotEquals(unexpected: first, actual: second)
    assertEquals(expected: ExchangeRate{centEquivalent: 212848, hbarEquivalent: 30000,
            expirationTime: toZonedDateTime(text: "2026-08-04T14:00:00Z")}, actual: first)
}

// Source: the Hiero Enterprise Java test fixture network-stake.json (its values; `staking_period_duration` 1440
// minutes, i.e. one day, is the staking period of 86400 seconds).
test "network stake keeps its staking parameters and its staking period" {
    // when
    let stake = NetworkStake{maxStakeReward: 650000000000000000, maxStakeRewardPerHbar: 5287,
            maxTotalReward: 44512288670430, nodeRewardFeeFraction: 0.1, reservedStakingRewards: 2781375493493491,
            rewardBalanceThreshold: 8500000000000000, stakeTotal: 1465970599400000000,
            stakingPeriod: toSeconds(value: 86400), stakingPeriodsStored: 365, stakingRewardFeeFraction: 0.1,
            stakingRewardRate: 44512288670430, stakingStartThreshold: 25000000000000000,
            unreservedStakingRewardBalance: 14652305964403937}

    // then
    assertEquals(expected: toSeconds(value: 86400), actual: stake.stakingPeriod)
    assertEquals(expected: 365, actual: stake.stakingPeriodsStored)
    assertEquals(expected: 1465970599400000000, actual: stake.stakeTotal)
    assertEquals(expected: 650000000000000000, actual: stake.maxStakeReward)
    assertEquals(expected: 5287, actual: stake.maxStakeRewardPerHbar)
    assertEquals(expected: 0.1, actual: stake.nodeRewardFeeFraction)
    assertEquals(expected: 0.1, actual: stake.stakingRewardFeeFraction)
    assertEquals(expected: 14652305964403937, actual: stake.unreservedStakingRewardBalance)
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
  expand once §3.4 lands the authoritative shape. The Java implementation of `estimateFees` is provisional
  for the same reason and has not been checked against a mirror node that supports HIP-1313.
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
  annotations, these SDK tests would apply here too.
- **`NetworkStake.stakingPeriod` loses the current period.** The Mirror Node returns both
  `staking_period` — the *current* period as a timestamp range (`from` / `to`) — and
  `staking_period_duration`, the length of a period in **minutes** (1440 on mainnet). V3 has a
  single `stakingPeriod: seconds`, which can only hold the length; the start and end of the current
  period are lost, and the comment does not say that the minutes are converted. Should `NetworkStake`
  have `stakingPeriodStart` / `stakingPeriodEnd: zonedDateTime` (or a range type, see the allowance
  note in [`mirror-node-account.md`](mirror-node-account.md)) next to the duration?

- **`ContractRepository` and `NetworkRepository` are concrete types.** The other five repositories are abstractions,
  but these two are declared without `abstraction`, so they can be instantiated directly — and a
  `new ContractRepository()` / `new NetworkRepository()` has no `MirrorNode` and cannot reach a mirror node. The Java
  implementation creates working instances only through `createRepository(...)` / `createMirrorNodeClient(...)`.
  Should both be abstractions like the other repositories?

# Mirror Node Network Query API

## Description

Query network-wide data from the Mirror Node. Use the `NetworkRepository` (available as `MirrorNodeClient.network`)
to read the current and next HBAR exchange rate, the current fee schedule (`fees`), the staking parameters (`stake`),
the HBAR supply (`supplies`), the address book of consensus nodes (`nodes`) and the other nodes registered in the
network, such as block nodes (`registeredNodes`). `estimateFees` estimates the fee of a candidate transaction before
it is submitted (HIP-1313).

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Keys are `Authority`.** `admin_key` (a `Key` with `_type` and `key`) of `NetworkNode` and `RegisteredNode` is an
  `Authority` (ADR-0004).
- **Times are `zonedDateTime`, durations `seconds`.** The `seconds.nanoseconds` timestamps and the epoch seconds of
  `ExchangeRate.expiration_time` are `zonedDateTime`; `staking_period_duration` (minutes) is
  `NetworkStake.stakingPeriodDuration` as `seconds`.
- **Empty endpoint fields are `null`.** The REST API returns `ip_address_v4` and `domain_name` of a `ServiceEndpoint`
  as empty strings when they are not set; here they are `null`.
- **The type-specific objects of a registered service endpoint are flattened.** `block_node.endpoint_apis` is
  `RegisteredServiceEndpoint.blockNodeApis`, `general_service.description` is `generalServiceDescription`;
  `mirror_node` and `rpc_relay` are objects without properties in the OpenAPI definition and are left out -
  `RegisteredServiceEndpoint.type` already says what the endpoint is.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.
- **`fees()` returns `NetworkFees`.** The response holds the list of fees and the time they were valid; a plain list
  would lose the time.


## API Schema

```
namespace mirrornode.network
requires {Address, AccountId, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common
requires {Order, TimestampRange} from mirrornode.common

@@finalType
ExchangeRate {
    @@immutable centEquivalent: int32
    @@immutable hbarEquivalent: int32
    @@immutable expirationTime: zonedDateTime
}

// The current and the next exchange rate between HBAR and US cents.
@@finalType
ExchangeRates {
    @@immutable currentRate: ExchangeRate
    @@immutable nextRate: ExchangeRate
    @@immutable @@nullable timestamp: zonedDateTime     // consensus time at which the rates were valid
}

@@finalType
NetworkFee {
    @@immutable gas: int64
    @@immutable transactionType: string
}

// The gas fee schedule of the network.
@@finalType
NetworkFees {
    @@immutable @@default([]) fees: list<NetworkFee>
    @@immutable @@nullable timestamp: zonedDateTime     // consensus time at which the fee schedule was valid
}

@@finalType
NetworkStake {
    @@immutable maxStakeRewarded: int64                 // maximum tinybars that can be staked for reward at the maximum per-HBAR reward rate
    @@immutable maxStakingRewardRatePerHbar: int64
    @@immutable maxTotalReward: int64                   // tinybars paid as staking rewards in the ending period
    @@immutable nodeRewardFeeFraction: double
    @@immutable reservedStakingRewards: int64           // tinybars of account 0.0.800 reserved for earned, uncollected rewards
    @@immutable rewardBalanceThreshold: int64           // unreserved tinybars of 0.0.800 needed for the maximum reward rate
    @@immutable stakeTotal: int64
    @@immutable stakingPeriod: TimestampRange           // the current staking period
    @@immutable stakingPeriodDuration: seconds          // the length of a staking period
    @@immutable stakingPeriodsStored: int64
    @@immutable stakingRewardFeeFraction: double
    @@immutable stakingRewardRate: int64
    @@immutable stakingStartThreshold: int64
    @@immutable unreservedStakingRewardBalance: int64   // unreserved balance of 0.0.800 at the close of the ending period
}

@@finalType
NetworkSupplies {
    @@immutable releasedSupply: int256
    @@immutable totalSupply: int256
    @@immutable @@nullable timestamp: zonedDateTime     // consensus time at which the released supply was valid
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
    @@immutable nodeId: int64                                   // stable HIP-869 node id
    @@immutable accountId: AccountId                            // fee account that receives this node's share
    @@immutable @@nullable adminKey: Authority                  // DAB admin key (HIP-869); null on pre-DAB entries
    @@immutable @@default([]) associatedRegisteredNodes: list<int64>   // ids of the registered nodes associated with this node
    @@immutable @@nullable declineReward: bool                  // whether the node declines staking rewards
    @@immutable @@nullable description: string
    @@immutable @@nullable fileId: Address                      // the address-book file the entry comes from
    @@immutable @@nullable grpcProxyEndpoint: ServiceEndpoint   // gRPC-web proxy endpoint (HIP-1046)
    @@immutable @@nullable maxStake: int64                      // maximum stake (tinybars) counted as consensus weight
    @@immutable @@nullable memo: string
    @@immutable @@nullable minStake: int64                      // minimum stake (tinybars) for a non-zero consensus weight
    @@immutable @@nullable nodeCertHash: string                 // SHA-384 of the node's TLS certificate (hex)
    @@immutable @@nullable publicKey: string                    // node gossip public key (hex)
    @@immutable @@nullable rewardRateStart: int64               // tinybars earned per whole HBAR in the last staking period
    @@immutable @@default([]) serviceEndpoints: list<ServiceEndpoint>
    @@immutable @@nullable stake: int64                         // consensus weight at the beginning of the staking period (tinybars)
    @@immutable @@nullable stakeNotRewarded: int64              // stake of the accounts staked to the node that decline rewards
    @@immutable @@nullable stakeRewarded: int64                 // stake of the accounts staked to the node that receive rewards
    @@immutable @@nullable stakingPeriod: TimestampRange        // the staking period
    @@immutable timestamp: TimestampRange                       // the period this address-book entry is valid for
}

// The kind of service a registered node offers.
enum RegisteredNodeType {
    BLOCK_NODE
    GENERAL_SERVICE
    MIRROR_NODE
    RPC_RELAY
}

// An API that a block node offers on an endpoint.
enum RegisteredBlockNodeApi {
    OTHER
    STATUS
    PUBLISH
    SUBSCRIBE_STREAM
    STATE_PROOF
    UNRECOGNIZED
}

// An endpoint of a registered node. At least one of domainName / ipAddress is set.
@@finalType
RegisteredServiceEndpoint {
    @@immutable @@default([]) blockNodeApis: list<RegisteredBlockNodeApi>    // the APIs of a block node; empty for other types
    @@immutable @@nullable domainName: string
    @@immutable @@nullable generalServiceDescription: string    // the description of a general service
    @@immutable @@nullable ipAddress: string
    @@immutable @@min(0) @@max(65535) port: int32
    @@immutable requiresTls: bool
    @@immutable type: RegisteredNodeType
}

// A node registered in the network that is not a consensus node, e.g. a block node or a mirror node.
// Returned by NetworkRepository.registeredNodes() (GET /network/registered-nodes).
@@finalType
RegisteredNode {
    @@immutable registeredNodeId: int64
    @@immutable @@nullable adminKey: Authority
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable @@nullable description: string
    @@immutable @@default([]) serviceEndpoints: list<RegisteredServiceEndpoint>
    @@immutable timestamp: TimestampRange                       // the period this entry is valid for
}

// How a fee estimate treats the state of the network.
enum FeeEstimateMode {
    INTRINSIC   // from the transaction alone
    STATE       // with the current state of the network
}

// An extra fee of a fee component, e.g. for additional signatures.
@@finalType
FeeExtra {
    @@immutable charged: int64          // number of charged items: max(0, count - included)
    @@immutable count: int64            // number of items in the transaction
    @@immutable feePerUnit: int64       // price of one item in tinycents
    @@immutable included: int64         // number of items included for free
    @@immutable name: string            // name of the extra fee in the fee schedule
    @@immutable subtotal: int64         // charged × feePerUnit, in tinycents
}

// The node or the service component of a fee estimate.
@@finalType
FeeEstimateComponent {
    @@immutable base: int64                             // base fee in tinycents
    @@immutable @@default([]) extras: list<FeeExtra>
}

// The network component of a fee estimate: the node subtotal multiplied by the network multiplier.
@@finalType
FeeEstimateNetwork {
    @@immutable multiplier: int32
    @@immutable subtotal: int64         // in tinycents
}

// Estimated fee for a candidate transaction (HIP-1313, congestion-adjusted). Returned by
// NetworkRepository.estimateFees().
@@finalType
FeeEstimate {
    @@immutable highVolumeMultiplier: int64
    @@immutable network: FeeEstimateNetwork
    @@immutable node: FeeEstimateComponent       // paid to the node that submitted the transaction
    @@immutable service: FeeEstimateComponent    // execution and storage
    @@immutable total: int64                     // estimated total fee in tinycents
}

NetworkRepository {
    // The current and the next exchange rate. Maps to GET /api/v1/network/exchangerate.
    @@async @@throws(mirror-node-error)
    @@nullable ExchangeRates exchangeRates()

    // The exchange rates at the given consensus time. Maps to GET /api/v1/network/exchangerate.
    @@async @@throws(mirror-node-error)
    @@nullable ExchangeRates exchangeRates(timestamp: list<string>)

    // The gas fee schedule. Maps to GET /api/v1/network/fees.
    @@async @@throws(mirror-node-error)
    NetworkFees fees()

    // The gas fee schedule at the given consensus time. Maps to GET /api/v1/network/fees.
    @@async @@throws(mirror-node-error)
    NetworkFees fees(@@nullable order: Order, timestamp: list<string>)

    // The staking parameters. Maps to GET /api/v1/network/stake.
    @@async @@throws(mirror-node-error)
    @@nullable NetworkStake stake()

    // The HBAR supply. Maps to GET /api/v1/network/supply.
    @@async @@throws(mirror-node-error)
    @@nullable NetworkSupplies supplies()

    // The HBAR supply at the given consensus time. Maps to GET /api/v1/network/supply.
    @@async @@throws(mirror-node-error)
    @@nullable NetworkSupplies supplies(timestamp: list<string>)

    // The network's consensus-node list (address book).
    // Maps to GET /api/v1/network/nodes.
    @@async @@throws(mirror-node-error)
    Page<NetworkNode> nodes()

    // The network's consensus-node list (address book), filtered by the address-book file and the node id.
    // Maps to GET /api/v1/network/nodes.
    @@async @@throws(mirror-node-error)
    Page<NetworkNode> nodes(fileId: list<string>, @@nullable @@min(1) @@max(100) limit: int32, nodeId: list<string>,
            @@nullable order: Order)

    // The registered nodes of the network. Maps to GET /api/v1/network/registered-nodes.
    @@async @@throws(mirror-node-error)
    Page<RegisteredNode> registeredNodes()

    // The registered nodes of the network, filtered by id and type. Maps to GET /api/v1/network/registered-nodes.
    @@async @@throws(mirror-node-error)
    Page<RegisteredNode> registeredNodes(@@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order,
            registeredNodeId: list<string>, @@nullable type: RegisteredNodeType)

    // Estimate the fee for a candidate transaction (HIP-1313). The transaction is passed as its
    // protobuf-encoded bytes. Maps to POST /api/v1/network/fees.
    @@async @@throws(mirror-node-error)
    FeeEstimate estimateFees(transaction: bytes)

    // Estimate the fee for a candidate transaction (HIP-1313) in the given mode, for the given high-volume throttle
    // utilization (in basis points). Maps to POST /api/v1/network/fees.
    @@async @@throws(mirror-node-error)
    FeeEstimate estimateFees(transaction: bytes, @@nullable mode: FeeEstimateMode,
            @@nullable @@min(0) @@max(10000) highVolumeThrottle: int32)
}

@@static NetworkRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `NetworkRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: the Java SDK (MirrorNodeServiceEndpointTest: IP address 1.2.3.4, domain name "test.mirror.com", port 443);
// HIP-1046 (an endpoint is published either with an IP address or with a domain name).
@@testId("a-service-endpoint-keeps-either-its-ipv4-address-or-its-domain-name-and-its-port")
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
@@testId("a-network-node-without-service-endpoints-and-admin-key-has-an-empty-endpoint-list-and-no-admin-key")
test "a network node without service endpoints and admin key has an empty endpoint list and no admin key" {
    // given
    let account = AccountId.fromString(value: "0.0.3")

    // when
    let node = NetworkNode{nodeId: 0, accountId: account, timestamp: TimestampRange{from: DEFAULT(zonedDateTime)}}

    // then
    assertEquals(expected: 0, actual: node.nodeId)
    assertEquals(expected: account, actual: node.accountId)
    assertEquals(expected: 0, actual: length(value: node.serviceEndpoints))
    assertNull(actual: node.adminKey)
    assertNull(actual: node.stake)
}

// Source: the Java SDK (MirrorNodeServiceEndpointTest); this spec (the address book entry lists its endpoints).
@@testId("a-network-node-keeps-its-service-endpoints-in-order")
test "a network node keeps its service endpoints in order" {
    // given
    let endpoints = [ServiceEndpoint{ipAddressV4: "1.2.3.4", port: 443},
            ServiceEndpoint{port: 443, domainName: "test.mirror.com"}]

    // when
    let node = NetworkNode{nodeId: 0, accountId: AccountId.fromString(value: "0.0.3"), serviceEndpoints: endpoints,
            timestamp: TimestampRange{from: DEFAULT(zonedDateTime)}}

    // then
    assertEquals(expected: endpoints, actual: node.serviceEndpoints)
}

// Source: this spec (`timestamp` is the period the address-book entry is valid for).
@@testId("a-network-node-keeps-the-consensus-time-of-its-address-book-entry")
test "a network node keeps the consensus time of its address-book entry" {
    // given
    let period = TimestampRange{from: DEFAULT(zonedDateTime)}

    // when
    let node = NetworkNode{nodeId: 0, accountId: AccountId.fromString(value: "0.0.3"), stake: 0, timestamp: period}

    // then
    assertEquals(expected: period, actual: node.timestamp)
    assertEquals(expected: 0, actual: node.stake)
}

// Source: the Hiero Enterprise Java test fixture exchange-rate.json (current rate 212848 cents for 30000 HBAR expiring
// at 1785852000, next rate 211949 cents for 30000 HBAR expiring at 1785855600; the epoch seconds written as ISO 8601).
@@testId("exchange-rates-keep-the-current-and-the-next-rate-with-their-expiration-times")
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
@@testId("exchange-rates-that-differ-only-in-their-expiration-time-are-different")
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
// minutes, i.e. one day, is the staking period duration of 86400 seconds); the example of the mirror node OpenAPI
// definition (staking period from 1655164800 to 1655251200).
@@testId("network-stake-keeps-its-staking-parameters-and-its-staking-period")
test "network stake keeps its staking parameters and its staking period" {
    // when
    let period = TimestampRange{from: toZonedDateTime(text: "2022-06-14T00:00:00Z"),
            to: toZonedDateTime(text: "2022-06-15T00:00:00Z")}
    let stake = NetworkStake{maxStakeRewarded: 650000000000000000, maxStakingRewardRatePerHbar: 5287,
            maxTotalReward: 44512288670430, nodeRewardFeeFraction: 0.1, reservedStakingRewards: 2781375493493491,
            rewardBalanceThreshold: 8500000000000000, stakeTotal: 1465970599400000000, stakingPeriod: period,
            stakingPeriodDuration: toSeconds(value: 86400), stakingPeriodsStored: 365, stakingRewardFeeFraction: 0.1,
            stakingRewardRate: 44512288670430, stakingStartThreshold: 25000000000000000,
            unreservedStakingRewardBalance: 14652305964403937}

    // then
    assertEquals(expected: period, actual: stake.stakingPeriod)
    assertEquals(expected: toSeconds(value: 86400), actual: stake.stakingPeriodDuration)
    assertEquals(expected: 365, actual: stake.stakingPeriodsStored)
    assertEquals(expected: 1465970599400000000, actual: stake.stakeTotal)
    assertEquals(expected: 650000000000000000, actual: stake.maxStakeRewarded)
    assertEquals(expected: 5287, actual: stake.maxStakingRewardRatePerHbar)
    assertEquals(expected: 0.1, actual: stake.nodeRewardFeeFraction)
    assertEquals(expected: 0.1, actual: stake.stakingRewardFeeFraction)
    assertEquals(expected: 14652305964403937, actual: stake.unreservedStakingRewardBalance)
}
```

## Questions & Comments

- **`fees()` vs `estimateFees()`.** `fees()` returns the network's *current* per-type
  gas fee schedule (`GET /network/fees`). `estimateFees()` is the HIP-1313 *estimation* of a
  specific candidate transaction (`POST /network/fees`) — a different operation despite sharing the
  path.
- **`estimateFees` has not been checked against a mirror node.** Its shape is the one of the pinned OpenAPI
  definition; the Java implementation has not been run against a mirror node that supports HIP-1313.
- **`estimateFees(transaction: bytes)` takes encoded bytes, not a typed transaction.** The
  mirror-node layer is independent of `consensus-node-client`, so it cannot reference
  `PackedTransaction`; the protobuf-encoded transaction bytes are the layering-clean input (and
  match what the REST endpoint accepts).
- **`NetworkNode` is the mirror-node read shape**, distinct from the consensus-node
  `NodeAddress` returned by `NodeAddressBookQuery`
  ([`queries-network.md`](../consensus-node-admin-client/queries-network.md)) — same entity, two
  sources, slightly different fields. `ServiceEndpoint` is redefined locally here (the mirror node
  cannot import the consensus-admin `ServiceEndpoint`).
- **`ServiceEndpoint` does not state its invariants as annotations.** Its comment says that one of `ipAddressV4` /
  `domainName` is populated, but there is no `@@oneOf(ipAddressV4, domainName)`, and `port` has no
  `@@min(0) @@max(65535)` — the Java SDK rejects `-1` and `65536` (`MirrorNodeServiceEndpointTest`). With the
  annotations, these SDK tests would apply here too. The same holds for `domainName` / `ipAddress` of
  `RegisteredServiceEndpoint`, of which at least one is set.
- **`ContractRepository` and `NetworkRepository` are concrete types.** The other five repositories are abstractions,
  but these two are declared without `abstraction`, so they can be instantiated directly — and a
  `new ContractRepository()` / `new NetworkRepository()` has no `MirrorNode` and cannot reach a mirror node. The Java
  implementation creates working instances only through `createRepository(...)` / `createMirrorNodeClient(...)`.
  Should both be abstractions like the other repositories?

# Dynamic Address Book Node Transaction API

## Description

The **Dynamic Address Book** (HIP-869, with the HIP-1046 follow-up) lets the governance
council register, update, and remove consensus nodes via on-network transactions instead of
static JSON files baked into a release. Each consensus node is identified by a numeric
`nodeId` and is bound to:

- An **`accountId`** that receives the node's staking rewards.
- A **gossip endpoint set** (`gossipEndpoints`) — internal endpoints used for hashgraph
  gossip between consensus nodes. Not reachable by clients.
- A **service endpoint set** (`serviceEndpoints`) — public gRPC endpoints that clients use
  to submit transactions.
- A **gossip CA certificate** (`gossipCaCertificate`) — the X.509 certificate (DER bytes)
  used to terminate mutual TLS on the gossip channel.
- A **gRPC certificate hash** (`grpcCertificateHash`) — SHA-384 of the certificate served
  on the public gRPC endpoints; clients pin against this hash.
- An **`adminAuthority`** — required to update or delete the node entry. There is no system-key
  override here; rotating the admin key requires both the old and new keys.
- A **`declineReward`** flag — when `true`, the node opts out of receiving staking rewards.
- An optional **`grpcWebProxyEndpoint`** (HIP-1046) — a gRPC-web proxy endpoint that
  browser clients can use without their own TLS terminator.

A node is created once with `NodeCreateTransaction`, can be updated any number of times with `NodeUpdateTransaction`,
and is finally removed with `NodeDeleteTransaction`. Deleting a node does not free its `nodeId`; deleted nodes remain
in the address book in a tombstoned state so that historical references still resolve.

The `nodeId` is the node's **stable network identifier**: it is assigned on creation, returned in
`NodeCreateReceipt.nodeId`, and stays the same for the lifetime of the node — independent of account rotation,
endpoint migration or certificate rollover. The node's account, in contrast, may change.

### Signing requirements

| Transaction               | Signers required                                                                                                                                              |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `NodeCreateTransaction`   | the *payer*; **and** the address-book *council* signer; **and** the new `adminAuthority` (anti-spoofing); **and** the `accountId`'s key (so the council cannot bind another party's account to a node) |
| `NodeUpdateTransaction`   | the *payer*; **and** the node's current `adminAuthority`; **and**, if `adminAuthority` itself is being rotated, the *new* key; **and**, if `accountId` is changed, the new account's key |
| `NodeDeleteTransaction`   | the *payer*; **and** the node's current `adminAuthority`                                                                                                            |

The council signer is configured at the consensus-node level and is provided through a `TransactionSigner` like any
other externally held key.

### Service endpoints

Each `ServiceEndpoint` has a port and either an `IpAddress` **or** a domain name — exactly one of the two. IP
addresses are IPv4 only today; use a domain name if the node runs behind a load balancer, if you want to decouple the
address from a specific IP, or if you need IPv6 reachability (via an AAAA record). The network rejects an endpoint
with both or neither set with `INVALID_ENDPOINT`.

## Design Notes

### `nodeId` vs. the read-side `ConsensusNode.account`

The DAB transactions identify a node by its sequential **`nodeId: int64`** — the same
identifier the consensus layer uses internally for gossip and stake distribution.
[`base/ledger.md`](../base/ledger.md) separately defines a `ConsensusNode` type with an
**`account: AccountId`** field. Both refer to the same node, from two different angles:

- `account` is the node's **fee account** (where the per-transaction fee share is paid).
  Clients select a node for transaction routing by its `account`; the value may change
  when the operator rotates the reward account via `NodeUpdate.accountId`.
- `nodeId` is the node's **stable network identifier**. It is assigned at `NodeCreate`,
  returned in `NodeCreateReceipt.nodeId`, and remains constant for the lifetime of the
  node — independent of account rotation, endpoint migration, or certificate rollover.

The same dichotomy already appears in
[`transactions-accounts.md`](../consensus-node-client/transactions-accounts.md), where
`stakedAccountId: AccountId` and `stakedNodeId: int64` are modelled as mutually exclusive
alternatives. The DAB transactions take only `nodeId`, because HAPI's address-book service
addresses nodes purely by their stable identifier (account rotation must not break a
pending `NodeUpdate`). `ConsensusNode` does **not** currently carry `nodeId` — see
*Questions & Comments* for why and what an extension would look like.

### ServiceEndpoint addressing

- `ipAddress` — an `IpAddress` value (defined in [`base/ledger.md`](../base/ledger.md)).
  IPv4-only today (the wire shape is HAPI `ipAddressV4`, 4 bytes); the type is named
  `IpAddress` rather than `IpV4Address` so adding IPv6 later only requires loosening the
  type's byte-length constraint, not renaming the field.
- `domainName` — DNS name, used when the operator runs behind a load balancer, wants to
  decouple addressing from a specific IP, or needs IPv6 reachability today (via an AAAA
  record on the resolved name).

The consensus node validates the `@@oneOf` at submission time; setting both or neither
fails with `INVALID_ENDPOINT`.

### Why the endpoint lists of `NodeUpdateTransaction` are not nullable

Per [ADR-0010](../../docs/adr/0010-collections-are-never-nullable.md) a collection never carries
`@@nullable`; the empty list is the absent one. Nothing is lost here, because the protocol has no
third state either: `node_update.proto` documents `gossip_endpoint` and `service_endpoint` as plain
`repeated` fields with "If set, the new list SHALL replace the existing list." and "This list MUST
NOT be empty.", and protobuf tracks no presence for a repeated field. Empty, absent and unchanged
are the same value on the wire, and a node's endpoint list cannot be cleared at all. Should a
protocol change ever allow clearing, the distinction returns as a type, not as `null`.

## API Schema

```
namespace consensusnode.admin.nodes
requires {AccountId, IpAddress} from ledger
requires {Authority} from authority
requires {Receipt, Transaction} from consensusnode.transactions

// One reachable endpoint on a consensus node. Exactly one of ipAddress or domainName must
// be set; the network rejects an endpoint with both (or neither) as INVALID_ENDPOINT.
@@oneOf(ipAddress, domainName)
type ServiceEndpoint {
    @@immutable @@nullable ipAddress: IpAddress   // IPv4 address; absent when domainName is used
    @@immutable @@nullable domainName: string     // DNS name; absent when ipAddress is used
    @@immutable port: uint16                      // listening port (0 is invalid)
}

// Registers a new consensus node in the address book. All endpoint, certificate, and key
// fields are required; the network has no defaults for any of them.
@@finalType
NodeCreateTransaction extends Transaction<NodeCreateReceipt, NodeCreateTransaction> {
    @@immutable accountId: AccountId                                     // account that receives the node's staking rewards
    @@immutable @@nullable description: string                           // free-form description (at most 100 characters)
    @@immutable @@minSize(1) gossipEndpoints: list<ServiceEndpoint>    // inter-node hashgraph gossip endpoints
    @@immutable @@minSize(1) serviceEndpoints: list<ServiceEndpoint>   // public gRPC endpoints for client transactions
    @@immutable gossipCaCertificate: bytes                               // X.509 DER bytes of the certificate that terminates mutual TLS for gossip
    @@immutable grpcCertificateHash: bytes                               // SHA-384 of the certificate served on serviceEndpoints; clients pin against this
    @@immutable adminAuthority: Authority                                    // required to update / delete the node; must co-sign this create
    @@immutable @@default(false) declineReward: bool                     // true → node opts out of staking rewards
    @@immutable @@nullable grpcWebProxyEndpoint: ServiceEndpoint         // optional gRPC-web proxy endpoint (HIP-1046)
}

// Receipt of a `NodeCreateTransaction`; contains the id assigned to the new node.
@@finalType
NodeCreateReceipt extends Receipt {
    @@immutable nodeId: int64                                            // network-assigned numeric id of the new node
}

// Updates one or more of a node's mutable fields. Fields that are not set are left unchanged. Setting an endpoint
// list replaces the entire current list — there is no way to add or remove a single entry, and no way to clear a
// list: an empty list means "leave this list as it is".
@@finalType
NodeUpdateTransaction extends Transaction<NodeUpdateReceipt, NodeUpdateTransaction> {
    @@immutable nodeId: int64                                            // the node to update
    @@immutable @@nullable accountId: AccountId                          // when set, the new account's key must also sign
    @@immutable @@nullable description: string                           // new free-form description
    @@immutable @@default([]) gossipEndpoints: list<ServiceEndpoint>     // when not empty, replaces the entire list; empty leaves it unchanged
    @@immutable @@default([]) serviceEndpoints: list<ServiceEndpoint>    // when not empty, replaces the entire list; empty leaves it unchanged
    @@immutable @@nullable gossipCaCertificate: bytes                    // new gossip CA certificate (X.509 DER bytes)
    @@immutable @@nullable grpcCertificateHash: bytes                    // new SHA-384 hash of the gRPC certificate
    @@immutable @@nullable adminAuthority: Authority                         // when set, the new key must also sign
    @@immutable @@nullable declineReward: bool                           // whether the node opts out of staking rewards
    @@immutable @@nullable grpcWebProxyEndpoint: ServiceEndpoint         // new gRPC-web proxy endpoint (HIP-1046)
}

// Receipt of a `NodeUpdateTransaction`.
@@finalType
NodeUpdateReceipt extends Receipt {
}

// Deletes a node from the address book. The node stays in the address book as a deleted (tombstoned) entry and its
// nodeId is not freed, so historical references still resolve.
@@finalType
NodeDeleteTransaction extends Transaction<NodeDeleteReceipt, NodeDeleteTransaction> {
    @@immutable nodeId: int64                                            // the node to delete
}

// Receipt of a `NodeDeleteTransaction`.
@@finalType
NodeDeleteReceipt extends Receipt {
}
```

## Default Instances for Testing

```
// A gossip endpoint by IPv4 address, as in the example "Register a new consensus node".
instance ServiceEndpoint = ServiceEndpoint{ipAddress: IpAddress{bytes: [10, 0, 0, 7]}, domainName: null, port: 50211}

// A gRPC endpoint by domain name, as in the example "Register a new consensus node".
values NODE_DOMAIN_ENDPOINT: ServiceEndpoint = ServiceEndpoint{ipAddress: null, domainName: "node7.consensus.example",
        port: 50211}

// Stand-ins for the DER bytes of a gossip CA certificate and for the SHA-384 hash (48 bytes) of a gRPC certificate;
// their content does not matter to the transactions.
values GOSSIP_CA_CERTIFICATE: bytes = utf8(text: "gossip CA certificate")

values GRPC_CERTIFICATE_HASH: bytes = repeat(value: hex(text: "cd"), count: 48)
```

## Examples

### Register a new consensus node

```
HieroClient        client       = ...;   // council operator; signer wraps council HSM
Account            nodeOperator = ...;   // owns the new accountId, must co-sign
TransactionSigner  adminSigner  = ...;   // wraps the new node's adminAuthority

PackedTransaction<...> packed = new NodeCreateTransaction()
    .accountId(nodeOperator.accountId)
    .description("consensus-node-7")
    .gossipEndpoints([
        new ServiceEndpoint(IpAddress.fromString("10.0.0.7"), /* domainName */ null, 50211),
    ])
    .serviceEndpoints([
        new ServiceEndpoint(null, "node7.consensus.example", 50211),
    ])
    .gossipCaCertificate(gossipCertDer)
    .grpcCertificateHash(grpcCertSha384)
    .adminAuthority(Authority.of(newAdminPublicKey))
    .signWithOperator(client)        // council + payer
    .sign(nodeOperator)              // accountId binding
    .sign(adminSigner);              // adminAuthority binding (anti-spoofing)

int64 nodeId = packed.submit(client).queryReceipt().nodeId;
```

### Update a node's service endpoints

```
new NodeUpdateTransaction()
    .nodeId(nodeId)
    .serviceEndpoints([
        new ServiceEndpoint(null, "node7-new.consensus.example", 50211),
    ])
    .signWithOperator(client)        // payer
    .sign(adminSigner);              // current adminAuthority
```

### Rotate the admin key

```
PackedTransaction<...> packed = new NodeUpdateTransaction()
    .nodeId(nodeId)
    .adminAuthority(Authority.of(newAdminPublicKey))
    .signWithOperator(client)        // payer
    .sign(currentAdminSigner)        // current adminAuthority
    .sign(newAdminSigner);           // new adminAuthority

packed.submit(client);
```

### Delete a node

```
new NodeDeleteTransaction()
    .nodeId(nodeId)
    .signWithOperator(client)
    .sign(adminSigner);
```

## Testing

Offline tests of the endpoints and of the attributes the node transactions keep, written as given / when / then.
The `@@oneOf` of an endpoint and the `@@minSize(1)` of the endpoint lists reject a violating value with
`invalid-argument-error`; the checks of the consensus node (signatures, limits of the network) are not tested here. The
comment above each test names the SDK tests it comes from.

```tests
// Source: the Go (TestEndpointSetAndGetAddress, TestEndpointSetAndGetPort), Python (test_constructor_with_values),
// JavaScript ("should create EndPoint with both address and port") and C++ Endpoint unit tests.
test "an endpoint by IP address keeps the address and the port" {
    // given
    let ipAddress = IpAddress{bytes: [10, 0, 0, 7]}

    // when
    let endpoint = ServiceEndpoint{ipAddress: ipAddress, domainName: null, port: 50211}

    // then
    assertEquals(expected: ipAddress, actual: endpoint.ipAddress)
    assertNull(actual: endpoint.domainName)
    assertEquals(expected: 50211, actual: endpoint.port)
}

// Source: the Go (TestEndpointSetAndGetDomainName), JavaScript ("should create EndPoint from domain name JSON"),
// Python and Java (validateAllowsOnlyDomain) Endpoint unit tests.
test "an endpoint by domain name keeps the name and the port" {
    // when
    let endpoint = ServiceEndpoint{ipAddress: null, domainName: "node7.consensus.example", port: 50211}

    // then
    assertNull(actual: endpoint.ipAddress)
    assertEquals(expected: "node7.consensus.example", actual: endpoint.domainName)
    assertEquals(expected: 50211, actual: endpoint.port)
}

// Source: the Go (TestEndpointRoundTripProtobuf) and Python (test_serialization_roundtrip) Endpoint unit tests, which
// compare endpoints by value; the Equality of the test language.
test "endpoints are equal when address, name and port are equal" {
    // given
    let endpoint = DEFAULT(ServiceEndpoint)

    // when
    let same = ServiceEndpoint{ipAddress: IpAddress{bytes: [10, 0, 0, 7]}, domainName: null, port: 50211}
    let otherPort = ServiceEndpoint{ipAddress: IpAddress{bytes: [10, 0, 0, 7]}, domainName: null, port: 50212}

    // then
    assertEquals(expected: endpoint, actual: same)
    assertNotEquals(unexpected: endpoint, actual: otherPort)
    assertNotEquals(unexpected: endpoint, actual: NODE_DOMAIN_ENDPOINT)
}

// Source: the Java (validateThrowsOnIpAndDomain), Go (TestEndpointValidateFailure "BothAddressAndDomain") and Python
// (test_validate_no_ip_and_domain_containing_both_ip_and_domain) Endpoint unit tests; `@@oneOf(ipAddress, domainName)`
// of this spec.
test "an endpoint with both an IP address and a domain name is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ServiceEndpoint{ipAddress: IpAddress{bytes: [10, 0, 0, 7]}, domainName: "node7.consensus.example", port: 50211}
}

// Source: the Go Endpoint unit test (TestEndpointValidateFailure "NeitherAddressNorDomain");
// `@@oneOf(ipAddress, domainName)` of this spec.
test "an endpoint with neither an IP address nor a domain name is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ServiceEndpoint{ipAddress: null, domainName: null, port: 50211}
}

// Source: the Java, Go (TestUnitNodeCreateTransactionGet), Python (test_constructor_with_parameters), C++, JavaScript
// and Swift NodeCreateTransaction unit tests.
test "a node create transaction keeps all its attributes" {
    // given
    let account = DEFAULT(AccountId)
    let adminAuthority = DEFAULT(Authority)

    // when
    let transaction = NodeCreateTransaction{accountId: account, description: "consensus-node-7",
            gossipEndpoints: [DEFAULT(ServiceEndpoint)], serviceEndpoints: [NODE_DOMAIN_ENDPOINT],
            gossipCaCertificate: GOSSIP_CA_CERTIFICATE, grpcCertificateHash: GRPC_CERTIFICATE_HASH,
            adminAuthority: adminAuthority, declineReward: true, grpcWebProxyEndpoint: NODE_DOMAIN_ENDPOINT}

    // then
    assertEquals(expected: account, actual: transaction.accountId)
    assertEquals(expected: "consensus-node-7", actual: transaction.description)
    assertEquals(expected: [DEFAULT(ServiceEndpoint)], actual: transaction.gossipEndpoints)
    assertEquals(expected: [NODE_DOMAIN_ENDPOINT], actual: transaction.serviceEndpoints)
    assertEquals(expected: GOSSIP_CA_CERTIFICATE, actual: transaction.gossipCaCertificate)
    assertEquals(expected: GRPC_CERTIFICATE_HASH, actual: transaction.grpcCertificateHash)
    assertEquals(expected: adminAuthority, actual: transaction.adminAuthority)
    assertTrue(condition: transaction.declineReward)
    assertEquals(expected: NODE_DOMAIN_ENDPOINT, actual: transaction.grpcWebProxyEndpoint)
}

// Source: the Python (test_constructor_default_values) and Go (TestUnitNodeCreateTransactionDeclineReward)
// NodeCreateTransaction unit tests; `@@default(false)` of declineReward.
test "a node create transaction takes the reward and has no description and no proxy by default" {
    // when
    let transaction = NodeCreateTransaction{accountId: DEFAULT(AccountId), gossipEndpoints: [DEFAULT(ServiceEndpoint)],
            serviceEndpoints: [NODE_DOMAIN_ENDPOINT], gossipCaCertificate: GOSSIP_CA_CERTIFICATE,
            grpcCertificateHash: GRPC_CERTIFICATE_HASH, adminAuthority: DEFAULT(Authority)}

    // then
    assertFalse(condition: transaction.declineReward)
    assertNull(actual: transaction.description)
    assertNull(actual: transaction.grpcWebProxyEndpoint)
}

// Source: HAPI node_create.proto (gossip_endpoint: "This list MUST NOT be empty"); `@@minSize(1)` of this spec.
test "a node create transaction without gossip endpoints is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NodeCreateTransaction{accountId: DEFAULT(AccountId), gossipEndpoints: [], serviceEndpoints: [NODE_DOMAIN_ENDPOINT],
            gossipCaCertificate: GOSSIP_CA_CERTIFICATE, grpcCertificateHash: GRPC_CERTIFICATE_HASH,
            adminAuthority: DEFAULT(Authority)}
}

// Source: HAPI node_create.proto (service_endpoint: "This list MUST NOT be empty"); `@@minSize(1)` of this spec.
test "a node create transaction without service endpoints is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    NodeCreateTransaction{accountId: DEFAULT(AccountId), gossipEndpoints: [DEFAULT(ServiceEndpoint)],
            serviceEndpoints: [], gossipCaCertificate: GOSSIP_CA_CERTIFICATE,
            grpcCertificateHash: GRPC_CERTIFICATE_HASH, adminAuthority: DEFAULT(Authority)}
}

// Source: the Python (test_constructor_default_values), Go (TestUnitNodeUpdateTransactionSetNothing) and JavaScript
// NodeUpdateTransaction unit tests; the API schema of this spec (fields that are not set are left unchanged, an empty
// endpoint list leaves the list unchanged).
test "a node update transaction changes nothing but what is set" {
    // when
    let transaction = NodeUpdateTransaction{nodeId: 7}

    // then
    assertEquals(expected: 7, actual: transaction.nodeId)
    assertNull(actual: transaction.accountId)
    assertNull(actual: transaction.description)
    assertEquals(expected: 0, actual: length(value: transaction.gossipEndpoints))
    assertEquals(expected: 0, actual: length(value: transaction.serviceEndpoints))
    assertNull(actual: transaction.gossipCaCertificate)
    assertNull(actual: transaction.grpcCertificateHash)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.declineReward)
    assertNull(actual: transaction.grpcWebProxyEndpoint)
}

// Source: the Java, Go (TestUnitNodeUpdateTransactionGet), Python (test_constructor_with_parameters), C++, JavaScript
// and Swift NodeUpdateTransaction unit tests.
test "a node update transaction keeps the attributes it sets" {
    // given
    let account = DEFAULT(AccountId)
    let adminAuthority = DEFAULT(Authority)

    // when
    let transaction = NodeUpdateTransaction{nodeId: 7, accountId: account, description: "consensus-node-7",
            gossipEndpoints: [DEFAULT(ServiceEndpoint)], serviceEndpoints: [NODE_DOMAIN_ENDPOINT],
            gossipCaCertificate: GOSSIP_CA_CERTIFICATE, grpcCertificateHash: GRPC_CERTIFICATE_HASH,
            adminAuthority: adminAuthority, grpcWebProxyEndpoint: NODE_DOMAIN_ENDPOINT}

    // then
    assertEquals(expected: 7, actual: transaction.nodeId)
    assertEquals(expected: account, actual: transaction.accountId)
    assertEquals(expected: "consensus-node-7", actual: transaction.description)
    assertEquals(expected: [DEFAULT(ServiceEndpoint)], actual: transaction.gossipEndpoints)
    assertEquals(expected: [NODE_DOMAIN_ENDPOINT], actual: transaction.serviceEndpoints)
    assertEquals(expected: GOSSIP_CA_CERTIFICATE, actual: transaction.gossipCaCertificate)
    assertEquals(expected: GRPC_CERTIFICATE_HASH, actual: transaction.grpcCertificateHash)
    assertEquals(expected: adminAuthority, actual: transaction.adminAuthority)
    assertEquals(expected: NODE_DOMAIN_ENDPOINT, actual: transaction.grpcWebProxyEndpoint)
}

// Source: the Go NodeUpdateTransaction unit test (TestUnitNodeUpdateTransactionDeclineReward): true and false are both
// kept and differ from "not set".
test "a node update transaction keeps declineReward true and false" {
    for declineReward in [true, false] {
        // when
        let transaction = NodeUpdateTransaction{nodeId: 7, declineReward: declineReward}

        // then
        assertEquals(expected: declineReward, actual: transaction.declineReward)
    }
}

// Source: the Java, Go, Python (test_constructor_with_parameter), C++, JavaScript and Swift NodeDeleteTransaction unit
// tests.
test "a node delete transaction keeps its node id" {
    // when
    let transaction = NodeDeleteTransaction{nodeId: 7}

    // then
    assertEquals(expected: 7, actual: transaction.nodeId)
}
```

## Questions & Comments

- **`ConsensusNode` in [`base/ledger.md`](../base/ledger.md) does not carry `nodeId`
  today.** It models the routing / fee-account view (`ip`, `port`, `account: AccountId`),
  which predates HIP-869 and is sufficient for clients that only need to *talk to* a node.
  The DAB transactions on the other hand need the *stable* identifier that survives an
  account rotation, hence `int64 nodeId`. Resolving the asymmetry on the read side
  (`ConsensusNode { nodeId, ip, port, account }`) is an additive change deliberately
  deferred: existing `ConsensusNode` consumers are not broken by leaving the field out, and
  the right time to introduce it is together with the `NodeId` type from §3.1 of
  `docs/missing-features.md` — adding an untyped `int64` first and then retyping later would
  churn every call site twice. Until then, callers that need to bridge the two worlds
  remember the `nodeId` returned by `NodeCreateReceipt` alongside the `Address` they obtain
  from the address-book lookup.

- **`gossipCaCertificate` and `grpcCertificateHash` are raw `bytes`.** The encoding rules
  (X.509 DER, SHA-384) are documented in the field comments but not enforced by the type
  system. A future `X509Certificate` / `CertificateFingerprint` pair in the `keys`
  namespace could make the encoding explicit, in line with how PKCS#8 / SPKI / PEM are
  handled there today.

- **`ServiceEndpoint.ipAddress` is typed as `IpAddress` (IPv4-only today).** The
  byte-length invariant is enforced by [`IpAddress`](../base/ledger.md) itself
  (`@@minSize(4) @@maxSize(4)`), not by server-side validation only. The type is
  intentionally named `IpAddress` rather than `IpV4Address`: once a HIP adds IPv6 to the
  consensus-node wire shape, only the `@@maxSize` constraint relaxes to `16` and every
  existing `IpAddress`-typed call site automatically supports both. Until that HIP exists,
  IPv6 reachability is achieved through `domainName` + a DNS AAAA record on the resolved
  host.

- **Lists are replace-on-set on update.** Matches HAPI but is a common foot-gun (callers
  expecting "update" to mean "add one endpoint" will silently drop the rest of the list).
  Consider whether a language-binding-level convenience that reads the current list,
  mutates a copy, and writes it back belongs in the enterprise layer.

- **No HIP-1046 multi-endpoint gRPC-web proxy field.** `grpcWebProxyEndpoint` is a single
  endpoint, matching the current HAPI shape; if HIP-1046 evolves to a list, both
  `NodeCreate` and `NodeUpdate` need to grow accordingly. Tracked here for visibility.

- **The client-side limits of the current SDKs are not part of this API.** The Go and Python SDKs reject, before
  submission, more than 10 gossip endpoints, more than 8 service endpoints, a description of more than 100 UTF-8
  bytes, an empty `gossipCaCertificate` and an endpoint with both an IP address and a domain name. This spec states
  only `@@minSize(1)` for the endpoint lists of `NodeCreateTransaction` and the "at most 100 characters" in a
  comment. Whether the limits belong into the API (`@@maxSize(10)`, `@@maxSize(8)`, `@@maxLength(100)`,
  `@@minSize(1)`, `@@min(1)` for the port) — they are network settings that may change — is open. Once declared, a
  `tests` block can check them like the `@@minSize(1)` of the endpoint lists (`invalid-argument-error`).

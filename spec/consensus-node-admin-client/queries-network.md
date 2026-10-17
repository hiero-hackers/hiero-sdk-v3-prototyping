# Network Queries API

## Description

Queries for network operators and tooling that need a live view of the consensus network:

- **`NetworkVersionInfoQuery`** — a free query that returns the network's HAPI protocol version and the version of
  the consensus node software (services). Use it to adapt behavior to a specific protocol revision, for example to
  check whether a HIP is already live on the network.
- **`NodeAddressBookQuery`** — a paid query that returns the current address book: all consensus nodes with their
  node ids, accounts, service endpoints and certificates. Use it to refresh your view of the network topology
  without going through a mirror node. Because the network charges for reading the address book, the query behaves
  like every other paid query: the price is determined automatically, paid by the operator, and can be capped with
  `maxQueryPayment`.

Ordinary applications rarely need these queries: a `HieroClient` already knows the network's version and node list
from its network settings. A free node list is also available from a mirror node.

## Design Notes

Both queries are admin-flavoured in the sense that ordinary applications normally do not
issue them — `HieroClient` already exposes the version and node list via its
`ledger.networkSetting()` snapshot. They land in `consensus-node-admin-client` rather than
in the main `consensus-node-client` because operator / tooling code that *does* need a live
snapshot is the same audience that runs the DAB transactions, and pulling the dependency
on the admin module is acceptable there.

`NetworkVersionInfoQuery` maps to HAPI's free `NetworkGetVersionInfoQuery` on the `NetworkService`.

### Why `NodeAddressBookQuery` is `PaidQuery`, not `Query`

HAPI does not have a dedicated free "address book" query on the network service. The
canonical way to read the address book is via `FileGetContents` on file `0.0.102` (on default Hedera
shards/realms), which is a paid `FileService` query. `NodeAddressBookQuery` is a typed convenience that hides the
file mechanism but **not** the cost: the underlying file read is still billed by the
network, so the query honestly extends `PaidQuery` with full cost-discovery behaviour. A
caller that wants a free node list today uses the mirror node (`GET /network/nodes` —
tracked in [`missing-features.md`](../../docs/missing-features.md) §4.2) instead.

### Reusing `ServiceEndpoint` from `consensusnode.admin.nodes`

A `NodeAddress` returned by `NodeAddressBookQuery` exposes its reachable endpoints as
`list<ServiceEndpoint>`, importing the same `ServiceEndpoint` type that
[`transactions-nodes.md`](transactions-nodes.md) defines for the write side. The shape is
identical — IPv4 (via `IpAddress`) or domain name, plus a port — and keeping it as one
type avoids a duplicate read/write shape that would have to evolve in lockstep.

## API Schema

```
namespace consensusnode.admin.network
requires {AccountId} from ledger
requires {FreeQuery, PaidQuery} from consensusnode.queries
requires {ServiceEndpoint} from consensusnode.admin.nodes

// Defined inline here for now; once the missing-features §3.1 SemanticVersion lands in `base/common`,
// this local definition should be removed and the base type imported instead.

// A semantic version (major.minor.patch) with optional pre-release and build metadata labels, as defined by
// semver.org.
type SemanticVersion {
    @@immutable major: int32                         // major version
    @@immutable minor: int32                         // minor version
    @@immutable patch: int32                         // patch version
    @@immutable @@nullable preReleaseLabel: string   // pre-release label such as "rc.1"; absent for a release version
    @@immutable @@nullable buildMetadata: string     // build metadata such as "exp.sha.5114f85"; absent if none

    // Canonical semver string, e.g. "0.49.1-rc.1+exp.sha.5114f85"
    string toString()
}

// Combined version snapshot: the HAPI protocol revision the network is speaking and the
// consensus node software (services) build that implements it. Returned by `NetworkVersionInfoQuery`.
type NetworkVersionInfo {
    @@immutable hapiVersion: SemanticVersion        // HAPI / protobuf protocol revision
    @@immutable servicesVersion: SemanticVersion    // consensus node software (services) build that implements the protocol
}

// Free query that returns the network's current HAPI and services version. Has no input
// fields — the answer is per-network, not per-entity.
@@finalType
NetworkVersionInfoQuery extends FreeQuery<NetworkVersionInfo> {
}

// One address-book entry: the stable node id plus everything a client needs to reach the node and verify its TLS
// certificates. Read-only counterpart of the data set with `NodeCreateTransaction`.
type NodeAddress {
    @@immutable nodeId: int64                                // stable HIP-869 identifier; survives account / endpoint rotation
    @@immutable accountId: AccountId                         // fee account that receives this node's per-transaction share
    @@immutable @@default([]) serviceEndpoints: list<ServiceEndpoint>   // public gRPC endpoints (clients submit transactions here)
    @@immutable @@nullable description: string               // free-form description of the node (at most 100 UTF-8 bytes)
    @@immutable gossipCaCertificate: bytes                   // X.509 DER bytes of the certificate that terminates mutual TLS for gossip
    @@immutable grpcCertificateHash: bytes                   // SHA-384 of the certificate served on serviceEndpoints; clients pin against this
    @@immutable rsaPublicKey: bytes                          // legacy gossip RSA public key (DER); only for tooling that still reads the legacy address-book format — prefer gossipCaCertificate and grpcCertificateHash
}

// Snapshot of the entire address book at consensus time. Nodes are listed by ascending `nodeId`.
type NodeAddressBook {
    @@immutable nodes: list<NodeAddress>     // all consensus nodes of the network
}

// Paid query that returns the current address book of the network. The network charges for reading the
// address-book file (0.0.102 on default Hedera shards/realms), and the operator pays that fee. Has no input
// fields — the answer is per network.
@@finalType
NodeAddressBookQuery extends PaidQuery<NodeAddressBook> {
}
```

## Default Instances for Testing

```
// The version of the comment of `SemanticVersion.toString()`: with a pre-release label and build metadata.
instance SemanticVersion = SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: "rc.1",
        buildMetadata: "exp.sha.5114f85"}

// A network that speaks the protocol revision of the default version and runs a release build of it.
instance NetworkVersionInfo = NetworkVersionInfo{hapiVersion: DEFAULT,
        servicesVersion: SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: null, buildMetadata: null}}

// The consensus node 0.0.3 of a local Solo network, reachable by domain name; the certificate, hash and key bytes are
// stand-ins, their content does not matter to the type.
instance NodeAddress = NodeAddress{nodeId: 0, accountId: AccountId{shard: 0, realm: 0, num: 3, checksum: ""},
        serviceEndpoints: [ServiceEndpoint{ipAddress: null, domainName: "network-node1-svc", port: 50211}],
        description: "node1", gossipCaCertificate: [1, 2, 3], grpcCertificateHash: [4, 5, 6], rsaPublicKey: [7, 8, 9]}

// Versions and their canonical semver text: the known answers of the Rust (semantic_version display tests) and Swift
// (SemanticVersionUnitTests) SDKs, the example of `SemanticVersion.toString()` in this spec, and examples of
// semver.org (items 9 and 10 of the specification).
values SEMANTIC_VERSION_TEXTS: map<string, SemanticVersion> = {
    "1.2.3": SemanticVersion{major: 1, minor: 2, patch: 3, preReleaseLabel: null, buildMetadata: null},
    "3.1.4-15.92": SemanticVersion{major: 3, minor: 1, patch: 4, preReleaseLabel: "15.92", buildMetadata: null},
    "1.41.1+6535asd": SemanticVersion{major: 1, minor: 41, patch: 1, preReleaseLabel: null, buildMetadata: "6535asd"},
    "0.1.4-0.9a2+sha.25531c": SemanticVersion{major: 0, minor: 1, patch: 4, preReleaseLabel: "0.9a2",
            buildMetadata: "sha.25531c"},
    "0.49.1-rc.1+exp.sha.5114f85": SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: "rc.1",
            buildMetadata: "exp.sha.5114f85"},
    "1.0.0-x.7.z.92": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: "x.7.z.92", buildMetadata: null},
    "1.0.0+20130313144700": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: null,
            buildMetadata: "20130313144700"},
    "1.0.0-beta+exp.sha.5114f85": SemanticVersion{major: 1, minor: 0, patch: 0, preReleaseLabel: "beta",
            buildMetadata: "exp.sha.5114f85"}
}
```

## Examples

### Read the network version (free)

```
HieroClient client = ...;

NetworkVersionInfo info = new NetworkVersionInfoQuery()
    .submit(client)
    .value;

SemanticVersion hapi     = info.hapiVersion;
SemanticVersion services = info.servicesVersion;
```

No cost discovery, no `maxQueryPayment` — `NetworkVersionInfoQuery` extends `Query`, not
`PaidQuery`.

### Refresh the address book (paid)

```
PaidQueryResponse<NodeAddressBook> response = new NodeAddressBookQuery()
    .maxQueryPayment(NativeToken.of(...))   // bound the spend
    .submit(client);

NodeAddressBook book = response.value;
NativeToken<ANY, ANY> paid = response.cost; // what was actually charged

for (NodeAddress n : book.nodes) {
    log("node " + n.nodeId + " @ account " + n.accountId.toString());
}
```

The operator pays. A configurable payer (paymaster / custodial / sponsored reads) is
deferred — see
[ADR-0002](../../docs/adr/0002-defer-paid-query-payer-customization.md).

### Gate behaviour on a HIP-availability check

```
NetworkVersionInfo info = new NetworkVersionInfoQuery().submit(client).value;

if (info.servicesVersion.major >= 0 && info.servicesVersion.minor >= 50) {
    // HIP-1046 grpcWebProxyEndpoint is supported on this network
    new NodeUpdateTransaction()
        .nodeId(nodeId)
        .grpcWebProxyEndpoint(...)
        .signWithOperatorAndSubmit(client);
}
```

## Testing

Offline tests of the version and address-book types, written as given / when / then. Sending the queries needs a
network and is not tested here. The comment above each test names its source.

```tests
// Source: the Rust (semantic_version display tests) and Swift (test_Basic, test_WithPrerelease, test_WithBuild,
// test_WithPrereleaseAndBuild) SDKs; the example of `SemanticVersion.toString()` in this spec; semver.org.
test "a semantic version is written in its canonical semver form" {
    for text in keys(value: SEMANTIC_VERSION_TEXTS) {
        // when
        let written = SEMANTIC_VERSION_TEXTS[text].toString()

        // then
        assertEquals(expected: text, actual: written)
    }
}

// Source: the JavaScript ("constructor stores major, minor and patch"), C++ (ConstructWithValues) and Swift
// (test_ParseWithPrereleaseAndBuild) SemanticVersion unit tests.
test "a semantic version keeps its numbers and labels" {
    // when
    let version = SemanticVersion{major: 1, minor: 2, patch: 0, preReleaseLabel: "beta.10", buildMetadata: "d13fe780"}

    // then
    assertEquals(expected: 1, actual: version.major)
    assertEquals(expected: 2, actual: version.minor)
    assertEquals(expected: 0, actual: version.patch)
    assertEquals(expected: "beta.10", actual: version.preReleaseLabel)
    assertEquals(expected: "d13fe780", actual: version.buildMetadata)
}

// Source: the Swift SemanticVersion unit tests (test_ParseMajorMinorPatch, test_ParseZeroMinorPatch: a release has no
// labels); the API schema of this spec (both labels are absent for a release version).
test "a release version has no labels and is written as major.minor.patch" {
    // when
    let version = SemanticVersion{major: 0, minor: 2, patch: 0, preReleaseLabel: null, buildMetadata: null}

    // then
    assertNull(actual: version.preReleaseLabel)
    assertNull(actual: version.buildMetadata)
    assertEquals(expected: "0.2.0", actual: version.toString())
}

// Source: the C++ NetworkVersionInfo unit tests (EqualityIdenticallyConstructed, InequalityDifferentProtobufVersion,
// InequalityDifferentServicesVersion); the Equality of the test language.
test "versions and version infos are equal when all their parts are equal" {
    // given
    let release = SemanticVersion{major: 0, minor: 49, patch: 1, preReleaseLabel: null, buildMetadata: null}
    let info = DEFAULT(NetworkVersionInfo)

    // when
    let same = NetworkVersionInfo{hapiVersion: DEFAULT(SemanticVersion), servicesVersion: release}
    let otherHapi = NetworkVersionInfo{hapiVersion: release, servicesVersion: release}
    let otherServices = NetworkVersionInfo{hapiVersion: DEFAULT(SemanticVersion), servicesVersion: DEFAULT(SemanticVersion)}

    // then
    assertNotEquals(unexpected: release, actual: DEFAULT(SemanticVersion))
    assertEquals(expected: info, actual: same)
    assertNotEquals(unexpected: info, actual: otherHapi)
    assertNotEquals(unexpected: info, actual: otherServices)
}

// Source: the JavaScript ("constructor stores both versions"), C++ (ConstructWithValues) and Java (shouldSerialize)
// NetworkVersionInfo unit tests.
test "a network version info keeps the HAPI and the services version" {
    // given
    let hapi = SemanticVersion{major: 0, minor: 50, patch: 0, preReleaseLabel: null, buildMetadata: null}
    let services = SemanticVersion{major: 0, minor: 50, patch: 3, preReleaseLabel: null, buildMetadata: null}

    // when
    let info = NetworkVersionInfo{hapiVersion: hapi, servicesVersion: services}

    // then
    assertEquals(expected: hapi, actual: info.hapiVersion)
    assertEquals(expected: services, actual: info.servicesVersion)
}

// Source: the C++ (GetSetNodeId, GetSetAccountId, GetSetCertHash, GetSetEndpoints, GetSetDescription, GetSetPublicKey)
// and Python (test_init) NodeAddress unit tests.
test "a node address keeps all its attributes" {
    // given
    let account = AccountId{shard: 0, realm: 0, num: 3, checksum: ""}
    let endpoints = [ServiceEndpoint{ipAddress: null, domainName: "network-node1-svc", port: 50211}]
    let certificate = utf8(text: "gossip CA certificate")
    let hash = repeat(value: hex(text: "cd"), count: 48)
    let rsaKey = utf8(text: "RSA public key")

    // when
    let address = NodeAddress{nodeId: 0, accountId: account, serviceEndpoints: endpoints, description: "node1",
            gossipCaCertificate: certificate, grpcCertificateHash: hash, rsaPublicKey: rsaKey}

    // then
    assertEquals(expected: 0, actual: address.nodeId)
    assertEquals(expected: account, actual: address.accountId)
    assertEquals(expected: endpoints, actual: address.serviceEndpoints)
    assertEquals(expected: "node1", actual: address.description)
    assertEquals(expected: certificate, actual: address.gossipCaCertificate)
    assertEquals(expected: hash, actual: address.grpcCertificateHash)
    assertEquals(expected: rsaKey, actual: address.rsaPublicKey)
}

// Source: the Python NodeAddress unit test (test_empty_addresses); `@@default([])` of serviceEndpoints and
// `@@nullable` of description.
test "a node address has no endpoints and no description by default" {
    // when
    let address = NodeAddress{nodeId: 0, accountId: AccountId{shard: 0, realm: 0, num: 3, checksum: ""},
            gossipCaCertificate: utf8(text: "gossip CA certificate"),
            grpcCertificateHash: repeat(value: hex(text: "cd"), count: 48), rsaPublicKey: utf8(text: "RSA public key")}

    // then
    assertEquals(expected: 0, actual: length(value: address.serviceEndpoints))
    assertNull(actual: address.description)
}

// Source: the Java AddressBookResponse test of the TCK and the C++ AddressBookQuery unit tests (an address book is a
// list of node addresses).
test "an address book keeps its nodes in order" {
    // given
    let node0 = DEFAULT(NodeAddress)
    let node1 = NodeAddress{nodeId: 1, accountId: AccountId{shard: 0, realm: 0, num: 4, checksum: ""},
            gossipCaCertificate: utf8(text: "gossip CA certificate"),
            grpcCertificateHash: repeat(value: hex(text: "cd"), count: 48), rsaPublicKey: utf8(text: "RSA public key")}

    // when
    let book = NodeAddressBook{nodes: [node0, node1]}

    // then
    assertEquals(expected: 2, actual: length(value: book.nodes))
    assertEquals(expected: node0, actual: book.nodes[0])
    assertEquals(expected: node1, actual: book.nodes[1])
}
```

## Questions & Comments

- **`SemanticVersion` is defined inline.** The same type appears in v2 SDKs across several
  service boundaries (network version, services version, future HIP-feature checks) and
  belongs in `base/common`. It is defined locally here so this file is self-contained, but
  once [`missing-features.md`](../../docs/missing-features.md) §3.1 lands a base `SemanticVersion`
  this local definition should be removed and the base type imported.

- **`NodeAddressBookQuery` is paid because it goes through the file service.** The
  underlying HAPI mechanism is `FileGetContents` on file `0.0.102`, which the network bills.
  Modelling it as a free query would lie about the cost. A future protocol change that
  introduces a dedicated free network-service query for the address book (no current HIP)
  would let this be retyped to `Query`, but that is a breaking type-level change by the
  same free/paid promotion rule as for any other query.

- **`NodeAddress.rsaPublicKey` is pre-DAB legacy.** The gossip layer authenticates via the
  `gossipCaCertificate` X.509 chain in modern HAPI; the RSA key is retained for tooling
  that still reads the legacy address-book format. New code should rely on
  `gossipCaCertificate` + `grpcCertificateHash`. A future HIP that drops the field would
  let it be removed cleanly (breaking change for anyone still reading it; nothing the
  consensus node would notice).

- **Result type returns an empty `nodeId: 0` field?** When a node is deleted via
  `NodeDeleteTransaction`, HAPI keeps the entry in the file under a tombstone flag, but
  this query's `NodeAddressBook` does not surface a `deleted` boolean per entry. Whether
  tombstoned entries should be visible (and how) is open — today they are filtered out
  client-side by the SDK before returning the typed result.

- **`NodeAddressBookQuery` has no input fields.** Address books are per-network, not
  per-shard / per-realm — a single network has one address book file. Multi-network
  callers issue this query once per `HieroClient` they hold.

- **Absent label: `null` or empty string?** The current SDKs (Rust, Swift, C++, Python) model an absent pre-release
  label or build metadata as `""`, as the protobuf `SemanticVersion` does. Here they are `@@nullable`, so `""` would be
  a second way to say "absent" — and semver.org forbids empty identifiers. Whether `@@minLength(1)` should exclude it,
  and how an SDK maps a `""` read from the network, is open.

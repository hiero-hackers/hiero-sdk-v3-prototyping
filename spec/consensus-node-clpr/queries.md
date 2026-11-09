# CLPR Queries API

This namespace defines the queries of the Cross Ledger Protocol (CLPR) service of the consensus node: the
configuration and the endpoint manifest of a ledger, together with the state proofs a peer ledger needs to establish a
Channel.

## Description

Two ledgers establish a Channel by passing each other proofs of their configuration (see
`consensusnode.clpr.transactions`). Only the consensus node can produce these proofs, so they are read with two free
queries:

- **`ClprLedgerConfigurationQuery`** returns the `ClprLedgerConfiguration` of the ledger: the protocol version, the
  chain id, the address of the CLPR service, the `ClprThrottles`, the initial trust anchor and the
  `configurationStateProof`.
- **`ClprEndpointManifestQuery`** returns the `ClprEndpointManifest` of the ledger: its CLPR endpoints and the
  `manifestStateProof`.

Neither query needs a payment or the privilege of the network administrator; both fail with `CLPR_NOT_ENABLED` on a
ledger without CLPR.

Pass the proofs unchanged as `configProof` and `endpointManifestProof` of a `ClprCompleteChannelTransaction` on the
peer ledger, if the verifier there accepts their encoding. A proof is taken from a signed snapshot of the ledger
state, so it does not necessarily authenticate the live values returned beside it, and the endpoint manifest is not
necessarily the newest one. A proof can be **empty** — when there is no usable snapshot yet or the ledger rejected its
own proof. This is a successful answer without proof material, not an error: query again later if you need the proof.
An empty `manifestStateProof` does not mean that the endpoint list is empty.

## Design Notes

- **Names.** The proposal (hiero-ledger/sdk-collaboration-hub, `proposals/hips/hip-1535.md`) names the queries
  `ClprGetLedgerConfigurationQuery` and `ClprGetEndpointManifestQuery` and their results `…Info`. Like every query of
  this SDK, a query here is named after its result (`TopicInfoQuery` returns `TopicInfo`), without "Get".
- **One result per query.** The protocol answers with the configuration (resp. manifest) and the state proof as two
  fields of the response; as in the proposal, the result type joins both.
- **Free queries.** Both map to free HAPI queries of the `ClprService` and extend `FreeQuery`.
- **Wire types kept.** `ClprServiceEndpoint.ipAddress` is a string as on the wire (a numeric IPv4 or IPv6 address, no
  host name) — `IpAddress` of `ledger` holds IPv4 only. The `timestamp` of a configuration is a consensus time and is
  therefore a `zonedDateTime`, as the consensus timestamps of `consensusnode.transactions`.

## API Schema

```
namespace consensusnode.clpr.queries
requires {FreeQuery} from consensusnode.queries
requires {ClprThrottles} from consensusnode.clpr

// The CLPR configuration of a ledger, with the state proof that a peer ledger passes to its verifier contract.
@@hip("1535")
@@finalType
ClprLedgerConfiguration {
    @@immutable protocolVersion: uint32           // the CLPR protocol version of the ledger, set at genesis
    @@immutable chainId: string                   // the CAIP-2 id of the ledger, e.g. "hedera:mainnet"; set at genesis
    @@immutable serviceAddress: bytes             // the address of the CLPR service of the ledger
    @@immutable timestamp: zonedDateTime          // when the configuration was last changed
    @@immutable throttles: ClprThrottles          // the capacity limits of the CLPR service
    @@immutable initialTrustAnchor: bytes         // the trust anchor a peer starts to verify this ledger with
    @@immutable initialTrustAnchorId: bytes       // the id of the initial trust anchor
    // The opaque state proof of the configuration from a signed snapshot; pass it unchanged to the peer ledger. Empty
    // when no usable snapshot exists.
    @@immutable configurationStateProof: bytes
}

// Free query that returns the CLPR configuration of the ledger. Has no input fields.
@@hip("1535")
@@finalType
ClprLedgerConfigurationQuery extends FreeQuery<ClprLedgerConfiguration> {
}

// The network address of a CLPR endpoint: a numeric IPv4 or IPv6 address, no host name.
@@hip("1535")
@@finalType
ClprServiceEndpoint {
    @@immutable ipAddress: string   // the numeric IPv4 or IPv6 address
    @@immutable port: uint16        // the port
}

// A CLPR relay endpoint of a ledger, as it appears in its endpoint manifest.
@@hip("1535")
@@finalType
ClprEndpoint {
    @@immutable serviceEndpoint: ClprServiceEndpoint   // where the endpoint is reached
    @@immutable tlsCertificate: bytes                  // the DER-encoded certificate of the endpoint for mutual TLS
    @@immutable accountId: bytes                       // the account on the ledger that the endpoint belongs to
}

// The CLPR endpoints of a ledger, with the state proof that a peer ledger passes to its verifier contract.
@@hip("1535")
@@finalType
ClprEndpointManifest {
    @@immutable version: uint64                 // the version of the manifest
    @@immutable serviceAddress: bytes           // the address of the CLPR service of the ledger
    @@immutable endpoints: list<ClprEndpoint>   // the endpoints of the ledger; may be empty
    // The opaque state proof of the manifest from a signed snapshot; pass it unchanged to the peer ledger. Empty when
    // no usable snapshot exists, also if the endpoint list is not empty.
    @@immutable manifestStateProof: bytes
}

// Free query that returns the CLPR endpoint manifest of the ledger. Has no input fields.
@@hip("1535")
@@finalType
ClprEndpointManifestQuery extends FreeQuery<ClprEndpointManifest> {
}
```

## Default Instances for Testing

```
// A ledger configuration whose snapshot proof is not available yet, with the service address 0x…016e; the trust anchor
// bytes are stand-ins.
instance ClprLedgerConfiguration = ClprLedgerConfiguration{protocolVersion: 1, chainId: "hedera:testnet",
        serviceAddress: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 110],
        timestamp: DEFAULT(zonedDateTime), throttles: DEFAULT(consensusnode.clpr.ClprThrottles),
        initialTrustAnchor: [1, 2, 3], initialTrustAnchorId: [4, 5, 6], configurationStateProof: []}

// An endpoint of a local network; the certificate and account bytes are stand-ins.
instance ClprEndpoint = ClprEndpoint{serviceEndpoint: ClprServiceEndpoint{ipAddress: "127.0.0.1", port: 50211},
        tlsCertificate: [1, 2, 3], accountId: [4, 5, 6]}
```

## Testing

Offline tests of the result types, written as given / when / then. Sending the queries needs a network with CLPR
enabled and is not tested here. The comment above each test names its source.

```tests
// Source: the SDK proposal for HIP-1535 (Test Plan 20: an empty proof is preserved as a successful read); HAPI
// clpr_get_endpoint_manifest.proto.
@@testId("an-endpoint-manifest-keeps-its-endpoints-and-an-empty-proof")
@@hip("1535")
test "an endpoint manifest keeps its endpoints and an empty proof" {
    // given
    let endpoint = DEFAULT(ClprEndpoint)

    // when
    let manifest = ClprEndpointManifest{version: 3, serviceAddress: hex(text: "000000000000000000000000000000000000016e"),
            endpoints: [endpoint], manifestStateProof: []}

    // then
    assertEquals(expected: 3, actual: manifest.version)
    assertEquals(expected: [endpoint], actual: manifest.endpoints)
    assertEquals(expected: 0, actual: length(value: manifest.manifestStateProof))
}

// Source: the SDK proposal for HIP-1535 (Test Plan 19: a proof is preserved byte for byte); HAPI
// clpr_get_ledger_configuration.proto.
@@testId("a-ledger-configuration-keeps-its-proof-byte-for-byte")
@@hip("1535")
test "a ledger configuration keeps its proof byte for byte" {
    // given
    let proof = hex(text: "00ff10ef")
    let throttles = DEFAULT(consensusnode.clpr.ClprThrottles)

    // when
    let configuration = ClprLedgerConfiguration{protocolVersion: 1, chainId: "eip155:1",
            serviceAddress: hex(text: "000000000000000000000000000000000000016e"),
            timestamp: DEFAULT(zonedDateTime), throttles: throttles,
            initialTrustAnchor: utf8(text: "trust anchor"), initialTrustAnchorId: utf8(text: "trust anchor id"),
            configurationStateProof: proof}

    // then
    assertEquals(expected: "eip155:1", actual: configuration.chainId)
    assertEquals(expected: throttles, actual: configuration.throttles)
    assertEquals(expected: proof, actual: configuration.configurationStateProof)
}

// Source: this spec (both queries are free queries without input fields; a new query has no execution settings of
// its own).
@@testId("the-clpr-queries-start-without-settings")
@@hip("1535")
test "the CLPR queries start without settings" {
    // when
    let configuration = ClprLedgerConfigurationQuery{}
    let manifest = ClprEndpointManifestQuery{}

    // then
    assertNull(actual: configuration.retryPolicy)
    assertNull(actual: manifest.attemptTimeout)
}
```

## Questions & Comments

- **The mirror node queries of the proposal are not here.** The proposal also defines `MirrorNodeClprChannelQuery`
  and `MirrorNodeClprConnectorQuery` (with `MirrorNodeClprChannel`, `MirrorNodeClprConnector` and
  `ClprChannelStatus`), but as follow-up scope: their REST routes are not agreed with the mirror node team and are not
  part of `tooling/openapi`. Once they are, they belong into `spec/mirror-node-client` (namespace `mirrornode.clpr`),
  not into this module.
- **`ClprEndpoint.accountId` is `bytes`.** The protocol documents it only as "on-ledger account associated with this
  endpoint node" and gives no encoding. If it is an account id of this ledger, it should be an `AccountId`; if it can
  be an account of a peer ledger (e.g. an EVM address), `bytes` is right.
- **`serviceAddress`, `initialTrustAnchor` and `initialTrustAnchorId` are opaque `bytes`.** The proposal does not
  describe their encoding further. The service address of a Hiero ledger is the 20-byte address `0x…016e`; an
  `EvmAddress` would fit it, but a peer ledger of another kind may use a different address format.
- **Is `ClprServiceEndpoint.ipAddress` validated?** The protocol accepts only numeric addresses. A `@@pattern` for
  IPv4 and IPv6 would reject host names in the SDK; the endpoints are only read here, so no check is specified.

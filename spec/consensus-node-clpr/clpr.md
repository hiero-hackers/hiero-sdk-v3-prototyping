# CLPR API

This namespace defines the types that the transactions and queries of the Cross Ledger Protocol (CLPR) share: the
capacity limits of a ledger and the status codes of the CLPR service.

## Description

**CLPR** ("Clipper", HIP-1535) connects a Hiero ledger with other ledgers. Two ledgers exchange messages over a
**Channel**; a **Connector** authorizes and pays for the execution of the messages it sends over a Channel. The CLPR
service of the consensus node keeps the Channels and Connectors of its ledger, receives the messages of the peer ledgers
in bundles and publishes the configuration of its ledger, so that a peer ledger can verify it.

`ClprThrottles` are the capacity limits a ledger enforces on its CLPR service: the size of a message and of a bundle,
the depth of the outbound queue of a Channel and the number of endpoints. A ledger reports them in its
`ClprLedgerConfiguration`; the network administrator changes them with a `ClprUpdateLedgerConfigurationTransaction`,
which always replaces all seven values. Five of them must be greater than zero; a `maxLocalEndpoints` or
`maxPeerEndpoints` of `0` means "no limit". `maxSyncBytes` must be at least `maxMessagePayloadBytes + 65,536` bytes, so
that a bundle with one message of the maximum size still has room for its proof; the network rejects a configuration
that violates it with `INVALID_CLPR_CONFIGURATION`.

`ClprTransactionStatus` names the status codes of the CLPR service. A code that is defined by the protocol but that the
service does not return today is listed as well, so that every status of the network can be read.

## Design Notes

- **Own module for an optional service.** CLPR is disabled on a ledger by default (`clpr.enabled=false`) and only few
  applications use it, so its API is a module of its own on top of the consensus node client, like the admin client.
  It registers its transaction types with the client in the same way (`TransactionSupport` of
  `consensusnode.transactions.spi`).
- **Status codes as an own enum.** `TransactionStatus` is open for the codes of further services, so the CLPR codes
  are an enum of this module and not values of `BasicTransactionStatus`. `BasicTransactionStatus` lists the values of
  the protobuf definitions pinned in `tooling/protobuf` (consensus node v0.77.2), which have no CLPR yet. The codes and
  their comments are taken from `response_code.proto` of consensus node commit `76ba2903`, the baseline of the
  SDK proposal for HIP-1535 (hiero-ledger/sdk-collaboration-hub, `proposals/hips/hip-1535.md`).
- **`ClprThrottles` is a plain value.** The proposal adds a factory `ClprThrottles.of(...)` that requires every field;
  here every field is required by the type itself, and the validation annotations reject the values that must be
  greater than zero.

## API Schema

```
namespace consensusnode.clpr
requires {TransactionStatus} from consensusnode.status

// The capacity limits a ledger enforces on its CLPR service. maxSyncBytes must be at least
// maxMessagePayloadBytes + 65,536 bytes; the network rejects a configuration that violates this with
// INVALID_CLPR_CONFIGURATION.
@@hip("1535")
@@finalType
ClprThrottles {
    @@immutable @@min(1) maxMessagesPerBundle: uint32      // the maximum number of messages in one bundle
    @@immutable @@min(1) maxMessagePayloadBytes: uint32    // the maximum size of the payload of one message, in bytes
    @@immutable @@min(1) maxGasPerMessage: uint64          // the maximum gas the execution of one message may use
    @@immutable @@min(1) maxQueueDepth: uint32             // the maximum number of outbound messages a Channel may queue
    @@immutable @@min(1) maxSyncBytes: uint64              // the maximum size of one bundle, in bytes
    @@immutable maxLocalEndpoints: uint32                  // the maximum number of endpoints of this ledger; 0: no limit
    @@immutable maxPeerEndpoints: uint32                   // the maximum number of endpoints of a peer ledger; 0: no limit
}

// The status codes of the CLPR service of the consensus node, with their name and code.
@@hip("1535")
enum ClprTransactionStatus(code: int32) extends TransactionStatus {
    // The CLPR service is not enabled on this ledger.
    CLPR_NOT_ENABLED(537)
    // A CLPR configuration field has an invalid value.
    INVALID_CLPR_CONFIGURATION(538)
    // The chain_id in a CLPR ledger configuration is immutable (set at genesis from configuration properties).
    CLPR_CHAIN_ID_IMMUTABLE(539)
    // Too many endpoints were specified in the CLPR configuration.
    CLPR_TOO_MANY_SEED_ENDPOINTS(540)
    // An endpoint in the CLPR configuration is missing required fields (service_endpoint, tls_certificate, or
    // ecdsa_signing_key).
    CLPR_INVALID_SEED_ENDPOINT(541)
    // A CLPR Channel with the given channel_id already exists, or (on registerChannel) the ownership commitment is
    // already registered, either pending reveal or bound to an existing Channel in any status, including CLOSED.
    CLPR_CHANNEL_ALREADY_EXISTS(542)
    // The signature on the CLPR channel completion is invalid.
    CLPR_INVALID_SIGNATURE(543)
    // The verifier contract account does not exist or is not a smart contract.
    CLPR_INVALID_VERIFIER_CONTRACT(544)
    // The verifier contract's verifyConfig call failed or returned invalid data.
    CLPR_VERIFIER_CONFIG_FAILED(545)
    // The keccak256(channel_id || public_key) does not match any stored ownership commitment.
    CLPR_COMMITMENT_MISMATCH(546)
    // The specified channel_id does not correspond to any known CLPR channel.
    CLPR_CHANNEL_NOT_FOUND(547)
    // The channel's current status does not allow the requested transition (e.g., attempting to close an
    // already-closing or closed channel).
    CLPR_INVALID_CHANNEL_STATUS(548)
    // The specified message ID does not exist in the queue.
    CLPR_MESSAGE_NOT_FOUND(549)
    // The endpoint signature is invalid or the signer is not a registered endpoint.
    CLPR_INVALID_ENDPOINT_SIGNATURE(551)
    // The bundle proof verification failed.
    CLPR_BUNDLE_VERIFICATION_FAILED(552)
    // The running hash in the bundle does not match the expected value.
    CLPR_RUNNING_HASH_MISMATCH(553)
    // The message queue has reached the maximum depth for this Channel.
    CLPR_QUEUE_FULL(554)
    // The specified Connector does not exist.
    CLPR_CONNECTOR_NOT_FOUND(555)
    // A Connector with the same (channel_id, connector_id) already exists, or (on registerConnector) the commitment is
    // already registered, either pending reveal or already used to create a Connector, including one that has since
    // been deregistered.
    CLPR_CONNECTOR_ALREADY_EXISTS(556)
    // The caller is not the admin of the specified Connector.
    CLPR_CONNECTOR_UNAUTHORIZED(557)
    // The connector_id is missing or empty.
    CLPR_INVALID_CONNECTOR_ADDRESS(558)
    // The connector_contract does not refer to a deployed smart contract.
    CLPR_INVALID_CONNECTOR_CONTRACT(559)
    // The locked_stake amount is below the minimum required by ClprConfig.
    CLPR_INSUFFICIENT_STAKE(560)
    // The message payload exceeds the destination ledger's max_message_payload_bytes.
    CLPR_PAYLOAD_TOO_LARGE(561)
    // The Connector's authorization contract rejected the message. Not the same as AUTHORIZATION_FAILED, which a
    // payer without the privilege of the network administrator receives.
    CLPR_AUTHORIZATION_FAILED(562)
    // The Connector has exceeded its per-Channel queue quota.
    CLPR_QUEUE_QUOTA_EXCEEDED(564)
    // The Connector's balance is insufficient to cover the queue fee (including any congestion escalation).
    CLPR_INSUFFICIENT_CONNECTOR_BALANCE(565)
    // Internal CLPR state is corrupt (e.g., an outbound message ID was assigned but the message does not exist in the
    // queue).
    CLPR_INTERNAL_STATE_CORRUPTION(566)
    // Cannot deregister a Connector that still has unresolved outbound in-flight messages.
    CLPR_CONNECTOR_HAS_IN_FLIGHT_MESSAGES(567)
    // A per-message protobuf decode failed inside a verifier-accepted bundle.
    CLPR_BUNDLE_DECODE_FAILED(568)
    // The submitted bundle carries no application messages, no trust anchor advance, and no ack progress.
    //
    // Deprecated by the protocol; use CLPR_NO_PROGRESS instead.
    @@deprecated CLPR_EMPTY_BUNDLE(569)
    // The submitting endpoint has exceeded its fair share of the per-ledger max_bundles_per_sec throttle.
    CLPR_RATE_LIMITED(570)
    // A submitted bundle satisfies none of the Bundle Progress Criteria.
    CLPR_NO_PROGRESS(571)
}
```

## Default Instances for Testing

```
// The limits of a small ledger: maxSyncBytes is exactly maxMessagePayloadBytes + 65,536, no limit on the endpoints.
instance ClprThrottles = ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 6144, maxGasPerMessage: 1000000,
        maxQueueDepth: 100, maxSyncBytes: 71680, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
```

## Testing

Offline tests of the throttles and the status codes, written as given / when / then. The comment above each test names
its source.

```tests
// Source: the SDK proposal for HIP-1535 (Test Plan 30: a throttle value keeps all seven fields); HAPI
// clpr_ledger_configuration.proto (ClprThrottles).
@@testId("throttles-keep-all-seven-limits")
@@hip("1535")
test "throttles keep all seven limits" {
    // when
    let throttles = ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 6144, maxGasPerMessage: 1000000,
            maxQueueDepth: 100, maxSyncBytes: 71680, maxLocalEndpoints: 4, maxPeerEndpoints: 8}

    // then
    assertEquals(expected: 10, actual: throttles.maxMessagesPerBundle)
    assertEquals(expected: 6144, actual: throttles.maxMessagePayloadBytes)
    assertEquals(expected: 1000000, actual: throttles.maxGasPerMessage)
    assertEquals(expected: 100, actual: throttles.maxQueueDepth)
    assertEquals(expected: 71680, actual: throttles.maxSyncBytes)
    assertEquals(expected: 4, actual: throttles.maxLocalEndpoints)
    assertEquals(expected: 8, actual: throttles.maxPeerEndpoints)
}

// Source: the SDK proposal for HIP-1535 (ClprUpdateLedgerConfigurationTransaction: five limits must be strictly
// positive, 0 fails with INVALID_CLPR_CONFIGURATION; Test Plan 30: the factory rejects an invalid limit locally);
// `@@min(1)` of this spec.
@@testId("throttles-with-a-limit-of-zero-are-rejected")
@@hip("1535")
test "throttles with a limit of zero are rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ClprThrottles{maxMessagesPerBundle: 0, maxMessagePayloadBytes: 6144, maxGasPerMessage: 1000000, maxQueueDepth: 100,
            maxSyncBytes: 71680, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
    @@expectThrows(invalid-argument-error)
    ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 0, maxGasPerMessage: 1000000, maxQueueDepth: 100,
            maxSyncBytes: 71680, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
    @@expectThrows(invalid-argument-error)
    ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 6144, maxGasPerMessage: 0, maxQueueDepth: 100,
            maxSyncBytes: 71680, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
    @@expectThrows(invalid-argument-error)
    ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 6144, maxGasPerMessage: 1000000, maxQueueDepth: 0,
            maxSyncBytes: 71680, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
    @@expectThrows(invalid-argument-error)
    ClprThrottles{maxMessagesPerBundle: 10, maxMessagePayloadBytes: 6144, maxGasPerMessage: 1000000, maxQueueDepth: 100,
            maxSyncBytes: 0, maxLocalEndpoints: 0, maxPeerEndpoints: 0}
}

// Source: HAPI response_code.proto of consensus node commit 76ba2903 (the CLPR values and their numbers); the SDK
// proposal for HIP-1535 (Response-code coverage: name and number are preserved).
@@testId("clpr-statuses-carry-the-codes-of-the-protocol")
@@hip("1535")
test "CLPR statuses carry the codes of the protocol" {
    // then
    assertEquals(expected: 537, actual: ClprTransactionStatus.CLPR_NOT_ENABLED.code)
    assertEquals(expected: 549, actual: ClprTransactionStatus.CLPR_MESSAGE_NOT_FOUND.code)
    assertEquals(expected: 551, actual: ClprTransactionStatus.CLPR_INVALID_ENDPOINT_SIGNATURE.code)
    assertEquals(expected: 562, actual: ClprTransactionStatus.CLPR_AUTHORIZATION_FAILED.code)
    assertEquals(expected: 564, actual: ClprTransactionStatus.CLPR_QUEUE_QUOTA_EXCEEDED.code)
    assertEquals(expected: 571, actual: ClprTransactionStatus.CLPR_NO_PROGRESS.code)
}
```

## Questions & Comments

- **The cross-field rule of `maxSyncBytes` is not enforced by the SDK.** The proposal asks the SDK to reject
  `maxSyncBytes < maxMessagePayloadBytes + 65,536` locally. The meta-language has no annotation that relates two
  fields, so today only the network rejects it (`INVALID_CLPR_CONFIGURATION`). Is a type-level annotation for such
  a rule worth adding, or is the network check enough?
- **Where do the CLPR status codes belong once the protobuf definitions are updated?** `status.md` takes over every
  value of `response_code.proto` mechanically, so an update of `tooling/protobuf` to a consensus node release with CLPR
  puts the same 33 values (and `CONFIG_FILE_PART_UPLOADED`, 536, which is not CLPR) into `BasicTransactionStatus`.
  Two enums would then name the same codes. Should the mechanical take-over skip the codes of optional services, or
  should `ClprTransactionStatus` be dropped then?
- **Several codes are not returned by the service today.** According to the proposal, `CLPR_CHAIN_ID_IMMUTABLE`,
  `CLPR_TOO_MANY_SEED_ENDPOINTS`, `CLPR_INVALID_SEED_ENDPOINT`, `CLPR_MESSAGE_NOT_FOUND`,
  `CLPR_INVALID_ENDPOINT_SIGNATURE`, `CLPR_INVALID_CONNECTOR_ADDRESS`, `CLPR_INSUFFICIENT_CONNECTOR_BALANCE` and
  `CLPR_RATE_LIMITED` have no throw path in the pinned handlers; only `CLPR_EMPTY_BUNDLE` is deprecated by the protocol.
  They are listed so that a status can always be read; should the unreachable ones be marked in the documentation?
- **Is `CLPR_RATE_LIMITED` retryable?** Once the service returns it, a bundle could be submitted again later. Whether
  it belongs to the statuses the default `RetryPolicy` retries is open (the proposal asks the CLPR working group).

# Block Node Status API

This namespace defines the status of a block node: the blocks it serves, the software it runs and the data needed to
verify its blocks.

## Description

`BlockNodeClient.status()` returns a `BlockNodeStatus`, a short summary: the first and the last block the block node
serves and the next block it expects from the network. A request for a block between `firstAvailableBlock` and
`lastAvailableBlock` should succeed; a request outside this range fails.

`BlockNodeClient.statusDetail()` returns a `BlockNodeStatusDetail`. It lists the exact ranges of blocks the block node
serves — a block node can have gaps — and the versions of its software, of the block stream it supports and of its
plugins. It also holds the data that a client needs to verify the proofs of the blocks: the TSS data of the ledger
(`TssData`) and the address books of the consensus nodes that signed older blocks. The list of ranges can be very long.

## Design Notes

- **Sentinels are `null`.** `last_available_block` and `next_expected_block` can be `uint64_max`: the block node then
  accepts any block next (it is new or backfilling its history), and the last available block is unknown — use
  `statusDetail()` for the exact ranges. Here both are `null` in that case. The open end of an address book range
  (`end_block` of `uint64_max`) is a `null` `endBlock` as well.
- **The address books are opaque.** `node_address_book` is the protobuf message `NodeAddressBook` of the consensus
  node, and only the node ids and RSA public keys of its entries are used to verify blocks. Like the blocks, they are
  returned as their protobuf encoding and not as SDK types.
- **`TssData` is all or nothing.** The protocol sets all of its fields or none; here a missing `TssData` is `null`.
- **`SemanticVersion` is declared here.** A semantic version is not a type of `base/common` yet; until it is, this
  namespace declares its own, like `consensusnode.admin.network`.

## API Schema

```
namespace blocknode.status

// The status of a block node.
@@finalType
BlockNodeStatus {
    @@immutable firstAvailableBlock: uint64   // the number of the first block the block node serves
    // The number of the last block the block node serves, or null if the block node accepts any block next and the
    // last available block is not known.
    @@immutable @@nullable lastAvailableBlock: uint64
    // True if the block node serves only the latest state snapshot.
    @@immutable onlyLatestState: bool
    // The number of the next block the block node expects from the network, or null if it accepts any block next.
    @@immutable @@nullable nextExpectedBlock: uint64
}

// The detailed status of a block node.
@@finalType
BlockNodeStatusDetail {
    @@immutable versions: BlockNodeVersions                   // the versions of the block node
    @@immutable availableRanges: list<BlockRange>            // the ranges of blocks the block node serves
    @@immutable storedRanges: list<BlockRange>               // the ranges of blocks the block node has stored
    @@immutable @@nullable tssData: TssData                   // the TSS data of the ledger, or null if the block node has none
    // The protobuf encoding of the current address book of the consensus nodes (`NodeAddressBook`); empty if the block
    // node has none.
    @@immutable nodeAddressBook: bytes
    // The address books of the consensus nodes by block range, oldest first; used to verify older blocks.
    @@immutable addressBookHistory: list<AddressBookRange>
}

// A contiguous range of blocks.
@@finalType
BlockRange {
    @@immutable start: uint64   // the number of the first block of the range
    @@immutable end: uint64     // the number of the last block of the range (inclusive)
}

// The versions of a block node.
@@finalType
BlockNodeVersions {
    @@immutable streamProtoVersion: SemanticVersion          // the version of the block stream the block node supports
    @@immutable blockNodeVersion: SemanticVersion            // the version of the block node software
    @@immutable installedPluginVersions: list<PluginVersion> // the plugins of the block node; may be empty
}

// The version of a plugin of a block node.
@@finalType
PluginVersion {
    @@immutable pluginId: string                  // the id of the plugin, by default the class name of the plugin
    @@immutable softwareVersion: SemanticVersion  // the version of the plugin
    @@immutable featureNames: list<string>        // the features the plugin provides; may be empty
}

// A semantic version (major.minor.patch) with optional pre-release and build metadata labels, as defined by
// semver.org.
@@finalType
SemanticVersion {
    @@immutable major: int32                          // major version
    @@immutable minor: int32                          // minor version
    @@immutable patch: int32                          // patch version
    @@immutable @@default("") preReleaseLabel: string // pre-release label such as "rc.1"; empty for a release version
    @@immutable @@default("") buildMetadata: string   // build metadata such as "exp.sha.5114f85"; empty if none
}

// The TSS data of a ledger, used to verify the proofs of its blocks.
@@finalType
TssData {
    @@immutable ledgerId: bytes                       // the id of the ledger
    @@immutable wrapsVerificationKey: bytes           // the WRAPS verification key of the ledger
    @@immutable currentRoster: list<TssRosterEntry>   // the current TSS roster
    @@immutable validFromBlock: uint64                // the number of the first block this data is valid for
}

// A consensus node in the TSS roster of a ledger.
@@finalType
TssRosterEntry {
    @@immutable nodeId: uint64            // the id of the node
    @@immutable weight: uint64            // the weight of the node
    // The Schnorr public key of the node; a placeholder if the node did not take part in the roster election in time.
    @@immutable schnorrPublicKey: bytes
}

// The address book of the consensus nodes that was in effect for a range of blocks.
@@finalType
AddressBookRange {
    // The protobuf encoding of the address book (`NodeAddressBook`) of the range.
    @@immutable addressBook: bytes
    @@immutable startBlock: uint64              // the number of the first block of the range
    // The number of the last block of the range (inclusive), or null if the range is open-ended.
    @@immutable @@nullable endBlock: uint64
}
```

## Default Instances for Testing

```
// A version 0.44.2 without labels.
instance SemanticVersion = SemanticVersion{major: 0, minor: 44, patch: 2}

instance BlockRange = BlockRange{start: 0, end: 100}

instance BlockNodeVersions = BlockNodeVersions{streamProtoVersion: DEFAULT(SemanticVersion),
        blockNodeVersion: DEFAULT(SemanticVersion), installedPluginVersions: []}

// A block node that serves blocks 0 to 100 and has no TSS data and no address books.
instance BlockNodeStatusDetail = BlockNodeStatusDetail{versions: DEFAULT(BlockNodeVersions),
        availableRanges: [DEFAULT(BlockRange)], storedRanges: [DEFAULT(BlockRange)], nodeAddressBook: [],
        addressBookHistory: []}
```

## Testing

Reading the status needs a block node; only the value types are tested here.

```tests
// Source: node_service.proto of the block node (ServerStatusResponse: `last_available_block` and
// `next_expected_block` may be uint64_max, then the block node accepts any block next); this spec (both are null then).
@@testId("a-status-without-a-known-last-block-has-no-last-and-no-expected-block")
test "a status without a known last block has no last and no expected block" {
    // when
    let status = BlockNodeStatus{firstAvailableBlock: 0, onlyLatestState: false}

    // then
    assertEquals(expected: 0, actual: status.firstAvailableBlock)
    assertNull(actual: status.lastAvailableBlock)
    assertNull(actual: status.nextExpectedBlock)
}

// Source: node_service.proto of the block node (RangedNodeAddressBook: an `end_block` of uint64_max is open-ended);
// this spec (an open end is null).
@@testId("an-open-ended-address-book-range-has-no-end-block")
test "an open-ended address book range has no end block" {
    // when
    let range = AddressBookRange{addressBook: hex(text: "0a00"), startBlock: 1000}

    // then
    assertEquals(expected: 1000, actual: range.startBlock)
    assertEquals(expected: hex(text: "0a00"), actual: range.addressBook)
    assertNull(actual: range.endBlock)
}

// Source: node_service.proto of the block node (TssData is set completely or not at all).
@@testId("a-detailed-status-without-tss-data-has-none")
test "a detailed status without TSS data has none" {
    // when
    let detail = DEFAULT(BlockNodeStatusDetail)

    // then
    assertNull(actual: detail.tssData)
    assertEquals(expected: [DEFAULT(BlockRange)], actual: detail.availableRanges)
}
```

## Questions & Comments

- **`availableRanges` and `storedRanges` are not explained by the protocol.** Both are "block ranges" of the block node;
  the protocol does not say how a stored block differs from an available one (e.g. a stored block that is not
  verified yet).
- **The end of a `BlockRange` is not checked.** `end` must not be smaller than `start`; no validation annotation can
  compare two fields.
- **`SemanticVersion` is declared twice.** It has the same shape as the one of `consensusnode.admin.network`; both should
  be replaced by one type in `base/common`.
- **The address books stay opaque.** `consensusnode.admin.network` has a typed `NodeAddressBook`, but importing it would
  make this namespace depend on the admin client, and the block node uses only the node ids and RSA keys. Should
  `NodeAddressBook` move to a lower layer so that both can share it?

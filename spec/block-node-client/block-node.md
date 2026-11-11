# Block Node Client API

This namespace defines how to read blocks from a Hiero block node: the `BlockNodeClient` and the blocks it returns.

## Description

A block node stores the block stream of a network: every block as the consensus nodes produced it, with its block
proof. Create a `BlockNodeClient` for a `BlockNode` with `createBlockNodeClient` and use it to

- read the status of the block node (`status`, `statusDetail`) — which blocks it has, which versions it runs and the
  data needed to verify its blocks,
- read a single block (`findByNumber`, `findLatest`),
- stream a range of blocks, or all blocks from a start block on as they are produced (`subscribe`).

Reading from a block node needs no account, no payment and no signature.

A `Block` is the sequence of its `BlockItem`s, from the block header to the block proofs: the transactions of the
block with their results and outputs, the state changes, and the hashes and signatures that prove the block. Every item
has a kind (`BlockItemKind`) and its content as bytes, in the protobuf encoding of the block stream of the network
release that produced the block. The SDK decodes only the `BlockHeader` (number, consensus time, versions, hash
algorithm) and the `BlockFooter` (the hashes that chain the block to its predecessor); decode the other items with the
block stream definitions when you need them. The SDK does not verify a block. A block can be many megabytes in size.

Ask the block node for its `status` before reading blocks: a block outside its available range is not served.
`findByNumber` returns `null` for a block that the block node does not have. Every method fails with a
`block-node-error` if the block node cannot be reached, rejects the request or fails.

## Design Notes

- **A client with methods, not query objects.** The queries of the consensus node are objects (`FreeQuery`,
  `PaidQuery`) because they carry execution settings and are paid. A block node request has neither, so the
  operations are methods of `BlockNodeClient`, like the repositories of the mirror node client.
- **Items are kind and bytes.** The block stream is the wire format of the consensus node; it changes with every
  network release. Typed items would make each of these changes a breaking change of the SDK. A `BlockItem` is
  therefore the model that the block node and the mirror node use internally (`BlockItemUnparsed` of the block node,
  read by the importer of the mirror node): the field number of the item in the `item` oneof of `BlockItem` and the
  bytes of that field — for a message the encoded message, for `signed_transaction` the bytes themselves. The items
  stay byte for byte as received, which keeps the root hash of the block computable: it is taken over the encoded
  items, so decoding and re-encoding them could change it.
- **The kind of an item is open.** New kinds of items are added to the block stream (13 to 18 and 20 are reserved
  today). `BlockItemKind` is an abstraction with the field number as `code`, and `BasicBlockItemKind` lists the kinds
  of the block stream, like `TransactionStatus` and `BasicTransactionStatus`; an item of an unknown kind is returned
  with a kind that is not a `BasicBlockItemKind` instead of failing the block.
- **Only header and footer are typed.** Every reader of a block needs its number, its consensus time and the hash of
  its predecessor first; `BlockHeader` and `BlockFooter` are small and stable. They are decoded from the first
  `BLOCK_HEADER` and the `BLOCK_FOOTER` item and stay in `items` as well. The block proofs stay bytes: a proof is one
  of three variants (TSS-signed block, state proof, signed record file), and the state proof is still changing. The
  transaction level — a signed transaction with its result, outputs, traces and state changes — needs the protobuf
  messages of the consensus node and is left to a later module on top of `consensus-node-client`.
- **The stream is assembled into blocks.** `subscribeBlockStream` sends a block as several `BlockItemSet` messages
  followed by a `BlockEnd`. `subscribe` joins the items of one block and yields it as one `Block` once its `BlockEnd`
  has arrived, so a stream and `findByNumber` return the same items for a block.
- **Sentinels are `null`.** The protocol uses `uint64_max` for "no end" (`end_block_number` of a subscription) and as
  the block number of the latest block; here they are a `null` end block and `findLatest()`.
- **Status codes are errors or `null`.** `SUCCESS` is the result; `NOT_AVAILABLE` and `NOT_FOUND` of `getBlock` are a
  `null` result; every other code (`INVALID_REQUEST`, `ERROR`, `UNKNOWN`, and for a subscription
  `INVALID_START_BLOCK_NUMBER`, `INVALID_END_BLOCK_NUMBER`, `NOT_AVAILABLE`) is a `block-node-error`. A failed
  connection is a `block-node-error` as well, as a repository of the mirror node client reports every failure as a
  `mirror-node-error`.
- **Not every service is covered.** `BlockStreamPublishService` and `ReconnectService` are used by consensus nodes and
  other block nodes, not by applications. `ProofService` and `StateService` are not covered yet (see Questions &
  Comments).

## API Schema

```
namespace blocknode
requires {BlockNode} from ledger
requires {BlockNodeStatus, BlockNodeStatusDetail, SemanticVersion} from blocknode.status

// The kind of a block item. Open for kinds unknown to the SDK, so that the blocks of newer network releases stay
// readable.
abstraction BlockItemKind {
    @@immutable code: int32   // the field number of the item in the block stream message `BlockItem`
}

// The kinds of block items of the block stream.
enum BasicBlockItemKind(code: int32) extends BlockItemKind {
    BLOCK_HEADER(1)            // starts a block: number, consensus time, versions, hash algorithm
    EVENT_HEADER(2)            // starts an event of the consensus algorithm
    ROUND_HEADER(3)            // starts a round of the consensus algorithm
    SIGNED_TRANSACTION(4)      // a signed transaction as submitted to the network
    TRANSACTION_RESULT(5)      // the result of a transaction
    TRANSACTION_OUTPUT(6)      // an output of a transaction that is specific to its type, e.g. of a contract call
    STATE_CHANGES(7)           // changes of the network state
    FILTERED_SINGLE_ITEM(8)    // stands in for an item that was removed from a filtered block
    BLOCK_PROOF(9)             // a proof of the block
    RECORD_FILE(10)            // a record file of the former record stream, wrapped into a block
    TRACE_DATA(11)             // informational trace data, e.g. of the EVM
    BLOCK_FOOTER(12)           // ends the hashed content of a block
    REDACTED_ITEM(19)          // stands in for an item whose content was redacted
}

// One item of a block.
@@finalType
BlockItem {
    @@immutable kind: BlockItemKind   // the kind of the item
    // The content of the item in the protobuf encoding of the block stream: the encoded message of its kind, or for a
    // SIGNED_TRANSACTION the bytes of the signed transaction.
    @@immutable content: bytes
}

// The hash algorithm of a block.
enum BlockHashAlgorithm {
    SHA2_384   // SHA-384
}

// The header of a block.
@@finalType
BlockHeader {
    @@immutable number: uint64                         // the number of the block; one more than its predecessor's
    @@immutable timestamp: zonedDateTime               // the consensus time of the block
    @@immutable hapiProtoVersion: SemanticVersion      // the version of the HAPI protobuf definitions of the block
    @@immutable softwareVersion: SemanticVersion       // the version of the consensus node software that produced it
    @@immutable hashAlgorithm: BlockHashAlgorithm      // the hash algorithm of the block
}

// The footer of a block: the hashes that chain a block to its predecessor and to the network state.
@@finalType
BlockFooter {
    @@immutable previousBlockRootHash: bytes           // the root hash of the previous block
    @@immutable rootHashOfAllBlockHashesTree: bytes    // the root hash of the tree of the hashes of all previous blocks
    @@immutable startOfBlockStateRootHash: bytes       // the root hash of the network state at the start of the block
}

// A block of the block stream as stored by a block node. The SDK does not verify a block.
@@finalType
Block {
    @@immutable header: BlockHeader          // decoded from the first BLOCK_HEADER item
    @@immutable footer: BlockFooter          // decoded from the BLOCK_FOOTER item
    // Every item of the block in order, byte for byte as received: from the BLOCK_HEADER to the BLOCK_PROOF items,
    // header and footer included.
    @@immutable @@minSize(1) items: list<BlockItem>
}

// Reads the status and the blocks of one block node.
abstraction BlockNodeClient {
    @@immutable blockNode: BlockNode   // the block node this client talks to

    // The status of the block node: the range of blocks it serves and the next block it expects.
    @@async @@throws(block-node-error)
    BlockNodeStatus status()

    // The detailed status of the block node: its exact block ranges, its versions and the data needed to verify its
    // blocks.
    @@async @@throws(block-node-error)
    BlockNodeStatusDetail statusDetail()

    // The block with the given number, or null if the block node does not have it.
    @@async @@throws(block-node-error)
    @@nullable Block findByNumber(number: uint64)

    // The latest block of the block node, or null if it has none.
    @@async @@throws(block-node-error)
    @@nullable Block findLatest()

    // The blocks from startBlockNumber to endBlockNumber (both inclusive), in ascending order. Without an end block
    // the stream does not end: it continues with every new block as soon as the block node has it. The stream fails
    // with a block-node-error if the block node does not serve the range.
    @@streaming @@throws(block-node-error)
    Block subscribe(startBlockNumber: uint64, @@nullable endBlockNumber: uint64)
}

// Creates a client for the given block node. Creating the client sends no request.
@@static
BlockNodeClient createBlockNodeClient(blockNode: BlockNode)
```

## Default Instances for Testing

```
instance BlockNodeClient = createBlockNodeClient(blockNode: DEFAULT(BlockNode))

// A header item whose content bytes are stand-ins, not a decodable header.
instance BlockItem = BlockItem{kind: BasicBlockItemKind.BLOCK_HEADER, content: [1, 2, 3]}

// The header of block 1 of a network on version 0.70.0, at the default consensus time.
instance BlockHeader = BlockHeader{number: 1, timestamp: DEFAULT(zonedDateTime),
        hapiProtoVersion: blocknode.status.SemanticVersion{major: 0, minor: 70, patch: 0},
        softwareVersion: blocknode.status.SemanticVersion{major: 0, minor: 70, patch: 0},
        hashAlgorithm: BlockHashAlgorithm.SHA2_384}

// A footer whose hashes are stand-ins of 48 bytes.
instance BlockFooter = BlockFooter{
        previousBlockRootHash: [1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
        rootHashOfAllBlockHashesTree: [2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2],
        startOfBlockStateRootHash: [3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3]}

// Block 1 with only its header item.
instance Block = Block{header: DEFAULT(BlockHeader), footer: DEFAULT(BlockFooter), items: [DEFAULT(BlockItem)]}
```

## Examples

```
blockNode = BlockNode(host: "block-node.example.com", port: 40840, requiresTls: true)
client = createBlockNodeClient(blockNode)

// Read the newest block the block node has
status = await client.status()
block = await client.findByNumber(status.lastAvailableBlock)

// Follow the chain from that block on and hand the transactions to a decoder of the block stream
for await (block in client.subscribe(startBlockNumber: status.lastAvailableBlock, endBlockNumber: null)) {
    for (item in block.items) {
        if (item.kind == BasicBlockItemKind.SIGNED_TRANSACTION) {
            process(block.header.number, block.header.timestamp, item.content)
        }
    }
}
```

## Testing

Reading blocks needs a block node; only the client and the value types are tested here.

```tests
// Source: this spec (a client talks to the block node it was created for; creating the client sends no request).
@@testId("a-block-node-client-talks-to-the-block-node-it-was-created-for")
test "a block node client talks to the block node it was created for" {
    // given
    let blockNode = BlockNode{host: "block-node.example.com", port: 40840, requiresTls: true}

    // when
    let client = createBlockNodeClient(blockNode: blockNode)

    // then
    assertEquals(expected: blockNode, actual: client.blockNode)
}

// Source: this spec (the host of a block node must not be empty).
@@testId("a-block-node-without-a-host-is-rejected")
test "a block node without a host is rejected" {
    @@expectThrows(invalid-argument-error)
    BlockNode{host: "", port: 40840, requiresTls: false}
}

// Source: block_item.proto of the consensus node (the field numbers of the `item` oneof of `BlockItem`).
@@testId("block-item-kinds-carry-the-field-numbers-of-the-block-stream")
test "block item kinds carry the field numbers of the block stream" {
    assertEquals(expected: 1, actual: BasicBlockItemKind.BLOCK_HEADER.code)
    assertEquals(expected: 4, actual: BasicBlockItemKind.SIGNED_TRANSACTION.code)
    assertEquals(expected: 7, actual: BasicBlockItemKind.STATE_CHANGES.code)
    assertEquals(expected: 9, actual: BasicBlockItemKind.BLOCK_PROOF.code)
    assertEquals(expected: 12, actual: BasicBlockItemKind.BLOCK_FOOTER.code)
    assertEquals(expected: 19, actual: BasicBlockItemKind.REDACTED_ITEM.code)
}

// Source: this spec (the content of an item is kept byte for byte and not decoded).
@@testId("a-block-item-keeps-its-content-byte-for-byte")
test "a block item keeps its content byte for byte" {
    // given
    let content = hex(text: "0a00ff10ef")

    // when
    let item = BlockItem{kind: BasicBlockItemKind.SIGNED_TRANSACTION, content: content}

    // then
    assertEquals(expected: 4, actual: item.kind.code)
    assertEquals(expected: content, actual: item.content)
}

// Source: block_header.proto and block_footer.proto of the consensus node (the fields of `BlockHeader` and
// `BlockFooter`); this spec (header and footer stay in the items).
@@testId("a-block-keeps-its-header-footer-and-items")
test "a block keeps its header, footer and items" {
    // given
    let version = SemanticVersion{major: 0, minor: 70, patch: 0}
    let header = BlockHeader{number: 42, timestamp: toZonedDateTime(text: "2026-01-02T03:04:05Z"),
            hapiProtoVersion: version, softwareVersion: version, hashAlgorithm: BlockHashAlgorithm.SHA2_384}
    let footer = BlockFooter{previousBlockRootHash: repeat(value: hex(text: "01"), count: 48),
            rootHashOfAllBlockHashesTree: repeat(value: hex(text: "02"), count: 48),
            startOfBlockStateRootHash: repeat(value: hex(text: "03"), count: 48)}
    let items = [BlockItem{kind: BasicBlockItemKind.BLOCK_HEADER, content: hex(text: "1803")},
            BlockItem{kind: BasicBlockItemKind.BLOCK_FOOTER, content: hex(text: "0a00")}]

    // when
    let block = Block{header: header, footer: footer, items: items}

    // then
    assertEquals(expected: 42, actual: block.header.number)
    assertEquals(expected: BlockHashAlgorithm.SHA2_384, actual: block.header.hashAlgorithm)
    assertEquals(expected: footer.previousBlockRootHash, actual: block.footer.previousBlockRootHash)
    assertEquals(expected: items, actual: block.items)
}

// Source: this spec (a block has at least its header item).
@@testId("a-block-without-items-is-rejected")
test "a block without items is rejected" {
    @@expectThrows(invalid-argument-error)
    Block{header: DEFAULT(BlockHeader), footer: DEFAULT(BlockFooter), items: []}
}
```

## Questions & Comments

- **`BlockNode.host` is a string.** `BlockNode` is declared in `ledger` beside `ConsensusNode` and `MirrorNode`, but
  `ConsensusNode.ip` is an `IpAddress`: a registered block node (`mirrornode.network.RegisteredServiceEndpoint`) has a
  domain name or an IP address. A `Network` knows no block nodes, as it knows no mirror nodes.
- **A registered block node cannot be turned into a `BlockNode`.** The mirror node lists the block nodes of a network
  (`NetworkRepository.registeredNodes()`); a `BlockNode` has to be built from such an endpoint by hand. A factory would
  make this namespace depend on the mirror node client.
- **The client is never closed.** A `BlockNodeClient` holds a gRPC channel but has no `close()` — the same open
  question as for the mirror node client (see `mirror-node.md`).
- **`NOT_FOUND` and `NOT_AVAILABLE` are both `null`.** The protocol distinguishes a block that is outside the range of
  the block node (`NOT_AVAILABLE`) from one that should be there but is missing (`NOT_FOUND`, retry or ask another
  block node). `findByNumber` returns `null` for both; should `NOT_FOUND` be an error instead?
- **`block-node-error` carries no status code.** Telling an invalid start block from a failing block node needs the
  code of the protocol; an error type with a code is not expressible in the meta-language yet.
- **Header, footer and items are not checked against each other.** `header` and `footer` are decoded from the items,
  but a `Block` built by hand can hold a header that does not match its first item; no annotation can express it.
  Should `Block` be built only by the SDK (no public constructor), or should it hold only the items and decode header
  and footer on access?
- **Is `BlockHashAlgorithm` open?** The protocol knows only `SHA2_384` today. A closed enum fails on a block with a new
  algorithm; an open type like `BlockItemKind` would not.
- **A block of an older layout.** The block footer was added to the block stream later than the header; the importer of
  the mirror node rejects a block without a footer, and so does this spec (`footer` is not nullable). Are there blocks
  in the history of a network without a footer that a block node still serves?
- **No helpers for the items.** The items of one kind (all `SIGNED_TRANSACTION` items, the `BLOCK_PROOF` items) are
  found by filtering `items`. The transaction level — a transaction with its result, outputs, traces and state changes,
  as the mirror node groups them — needs the protobuf messages of the consensus node and is not part of this module.
- **Blocks from other sources.** Block nodes also write blocks as files (the mirror node reads them from cloud
  storage as well). A `Block` can only be obtained from a `BlockNodeClient`; a `Block.fromBytes` that reads the
  protobuf encoding of `Block` would open the types for such files.
- **No retry, no failover.** The consensus node client retries and switches nodes (`RetryPolicy`, `NodeSelector`); a
  `BlockNodeClient` talks to one block node and does neither. Should a subscription resume after a lost connection
  from the block after the last one received?
- **Proofs and state snapshots are not covered.** `ProofService` (`getBlockContentsProof`, `getStateProof`) returns a
  state proof that the protocol itself still declares as a placeholder (`StateProofPlaceholder`); `StateService`
  (`stateSnapshot`) serves state snapshots, which are of interest to operators rather than applications. Both can be
  added once their messages are final.
- **Payment of a block node.** The protocol mentions that a block node may charge for "future" streams and fulfils a
  subscription "as far as payment supports", but defines no payment. Nothing here pays.

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

A block is returned as a `RawBlock`: its number and its content, the protobuf encoding of the `Block` message of the
block stream (`com.hedera.hapi.block.stream.Block`), from the `BlockHeader` to the `BlockProof`. The SDK does not
decode, verify or change the content; decode it with the block stream definitions of the network release that produced
the block. A block can be many megabytes in size.

Ask the block node for its `status` before reading blocks: a block outside its available range is not served.
`findByNumber` returns `null` for a block that the block node does not have. Every method fails with a
`block-node-error` if the block node cannot be reached, rejects the request or fails.

## Design Notes

- **A client with methods, not query objects.** The queries of the consensus node are objects (`FreeQuery`,
  `PaidQuery`) because they carry execution settings and are paid. A block node request has neither, so the
  operations are methods of `BlockNodeClient`, like the repositories of the mirror node client.
- **Blocks are opaque.** The block stream is the wire format of the consensus node; it changes with every network
  release. Typed block items would make each of these changes a breaking change of the SDK, so the content of a block
  stays the protobuf encoding of `Block`. A typed view of the block stream can be added on top of `RawBlock` later.
- **The stream is assembled into blocks.** `subscribeBlockStream` sends a block as several `BlockItemSet` messages
  followed by a `BlockEnd`. `subscribe` joins the items of one block and yields it as one `RawBlock` once its
  `BlockEnd` has arrived, so a stream and `findByNumber` return the same content for a block. The encoding of the joined
  items is the encoding of `Block` (both messages hold the block items as their repeated field 1).
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
requires {BlockNodeStatus, BlockNodeStatusDetail} from blocknode.status

// A block as stored by a block node. The content is not decoded or verified by the SDK.
@@finalType
RawBlock {
    @@immutable number: uint64   // the number of the block
    // The protobuf encoding of the block stream message `Block`: every block item of the block in order, from the
    // `BlockHeader` to the `BlockProof`.
    @@immutable content: bytes
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
    @@nullable RawBlock findByNumber(number: uint64)

    // The latest block of the block node, or null if it has none.
    @@async @@throws(block-node-error)
    @@nullable RawBlock findLatest()

    // The blocks from startBlockNumber to endBlockNumber (both inclusive), in ascending order. Without an end block
    // the stream does not end: it continues with every new block as soon as the block node has it. The stream fails
    // with a block-node-error if the block node does not serve the range.
    @@streaming @@throws(block-node-error)
    RawBlock subscribe(startBlockNumber: uint64, @@nullable endBlockNumber: uint64)
}

// Creates a client for the given block node. Creating the client sends no request.
@@static
BlockNodeClient createBlockNodeClient(blockNode: BlockNode)
```

## Default Instances for Testing

```
instance BlockNodeClient = createBlockNodeClient(blockNode: DEFAULT(BlockNode))

// A block whose content bytes are stand-ins, not a decodable block.
instance RawBlock = RawBlock{number: 1, content: [1, 2, 3]}
```

## Examples

```
blockNode = BlockNode(host: "block-node.example.com", port: 40840, requiresTls: true)
client = createBlockNodeClient(blockNode)

// Read the newest block the block node has
status = await client.status()
block = await client.findByNumber(status.lastAvailableBlock)

// Follow the chain from that block on
for await (block in client.subscribe(startBlockNumber: status.lastAvailableBlock, endBlockNumber: null)) {
    process(block.number, block.content)
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

// Source: this spec (the content of a block is kept byte for byte and not decoded).
@@testId("a-raw-block-keeps-its-content-byte-for-byte")
test "a raw block keeps its content byte for byte" {
    // given
    let content = hex(text: "0a00ff10ef")

    // when
    let block = RawBlock{number: 42, content: content}

    // then
    assertEquals(expected: 42, actual: block.number)
    assertEquals(expected: content, actual: block.content)
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
- **The number of a block from `findLatest`.** `BlockResponse` carries no block number; the SDK has to read it from the
  `BlockHeader` at the start of the content — the only part of the block it decodes.
- **No retry, no failover.** The consensus node client retries and switches nodes (`RetryPolicy`, `NodeSelector`); a
  `BlockNodeClient` talks to one block node and does neither. Should a subscription resume after a lost connection
  from the block after the last one received?
- **Proofs and state snapshots are not covered.** `ProofService` (`getBlockContentsProof`, `getStateProof`) returns a
  state proof that the protocol itself still declares as a placeholder (`StateProofPlaceholder`); `StateService`
  (`stateSnapshot`) serves state snapshots, which are of interest to operators rather than applications. Both can be
  added once their messages are final.
- **Payment of a block node.** The protocol mentions that a block node may charge for "future" streams and fulfils a
  subscription "as far as payment supports", but defines no payment. Nothing here pays.

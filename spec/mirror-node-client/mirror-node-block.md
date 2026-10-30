# Mirror Node Block Query API

## Description

Query the blocks of the network from the Mirror Node. Use the `BlockRepository` (available as
`MirrorNodeClient.blocks`) to look up a single block by its number (`findByNumber`) or its hash (`findByHash`), and to
list the blocks of the network (`findAll`), optionally filtered by block number and consensus time.

A `Block` describes one block of the network: its number, its hash and the hash of the block before it, the number of
transactions it contains, the consensus time range it covers, and its EVM-related data (the gas used and the logs
bloom filter).

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **One endpoint, two lookups.** `GET /blocks/{hashOrNumber}` accepts either a block number or a block hash in one
  path parameter. Here it is two methods with their own types: `findByNumber(number: int64)` and
  `findByHash(hash: bytes)`; both call the same endpoint.
- **Hashes and the logs bloom are `bytes`.** `hash`, `previous_hash` and `logs_bloom` are hex texts with a `0x`
  prefix in the REST API; here they are the decoded bytes, like the other hashes of the mirror node client.
  `findByHash` sends the hash as hex text with a `0x` prefix.
- **`count` and `size` are `int64`.** The OpenAPI definition declares them as `integer` without a format.
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.block
requires {MirrorNode} from ledger
requires {Page} from common
requires {Order, TimestampRange} from mirrornode.common

// A block of the network.
@@finalType
Block {
    @@immutable @@min(0) count: int64                       // number of transactions in the block
    @@immutable @@nullable @@min(0) gasUsed: int64          // gas used by the EVM transactions of the block
    @@immutable @@nullable hapiVersion: string              // version of the services API the block was created with
    @@immutable hash: bytes                                 // hash of the block
    @@immutable @@nullable logsBloom: bytes                 // bloom filter of the EVM logs of the block (256 bytes)
    @@immutable name: string                                // name of the record file of the block
    @@immutable @@min(0) number: int64                      // number of the block
    @@immutable previousHash: bytes                         // hash of the previous block
    @@immutable @@nullable size: int64                      // size of the block in bytes
    @@immutable timestamp: TimestampRange                   // consensus time range of the transactions of the block
}

abstraction BlockRepository {
    // The block with the given number, or null if there is none. Maps to GET /api/v1/blocks/{hashOrNumber}.
    @@async @@throws(mirror-node-error)
    @@nullable Block findByNumber(@@min(0) number: int64)

    // The block with the given hash (32 bytes in the Ethereum format or 48 bytes in the Hedera format), or null if
    // there is none. Maps to GET /api/v1/blocks/{hashOrNumber}.
    @@async @@throws(mirror-node-error)
    @@nullable Block findByHash(hash: bytes)

    // The blocks of the network. Maps to GET /api/v1/blocks.
    @@async @@throws(mirror-node-error)
    Page<Block> findAll()

    // The blocks of the network, filtered by block number and consensus time. Maps to GET /api/v1/blocks.
    @@async @@throws(mirror-node-error)
    Page<Block> findAll(blockNumber: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>)
}

@@static BlockRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `BlockRepository` need a Mirror Node; only the value type is tested here.

```tests
// Source: the example of the Block schema of the mirror node OpenAPI definition (block 77 with 3 transactions, gas used
// 300000, HAPI version "0.11.0", record file "2022-05-03T06_46_26.060890949Z.rcd", size 8192, timestamp range from
// "1651560386.060890949" to "1651560386.661997287"); millisecond precision.
@@testId("a-block-keeps-its-number-hashes-and-consensus-time-range")
test "a block keeps its number, hashes and consensus time range" {
    // given
    let hash = hex(text: "3c08bbbee74d287b1dcd3f0ca6d1d2cb92c90883c4acf9747de9f3f3162ad25b999fc7e86699f60f2a3fb3ed9a646c6b")
    let previousHash = hex(text: "f7d6481f659c866c35391ee230c374f163642ebf13a5e604e04a95a9ca48a298dc2dfa10f51bcbaab8ae23bc6d662a0b")
    let range = TimestampRange{from: toZonedDateTime(text: "2022-05-03T06:46:26.060Z"),
            to: toZonedDateTime(text: "2022-05-03T06:46:26.661Z")}

    // when
    let block = Block{count: 3, gasUsed: 300000, hapiVersion: "0.11.0", hash: hash,
            name: "2022-05-03T06_46_26.060890949Z.rcd", number: 77, previousHash: previousHash, size: 8192,
            timestamp: range}

    // then
    assertEquals(expected: 77, actual: block.number)
    assertEquals(expected: 3, actual: block.count)
    assertEquals(expected: 300000, actual: block.gasUsed)
    assertEquals(expected: "0.11.0", actual: block.hapiVersion)
    assertEquals(expected: hash, actual: block.hash)
    assertEquals(expected: 48, actual: length(value: block.hash))
    assertEquals(expected: previousHash, actual: block.previousHash)
    assertEquals(expected: "2022-05-03T06_46_26.060890949Z.rcd", actual: block.name)
    assertEquals(expected: 8192, actual: block.size)
    assertEquals(expected: range, actual: block.timestamp)
    assertNull(actual: block.logsBloom)
}

// Source: the Block schema of the mirror node OpenAPI definition (`gas_used`, `hapi_version`, `logs_bloom` and `size`
// may be null).
@@testId("a-block-without-evm-data-has-no-gas-used-and-no-logs-bloom")
test "a block without EVM data has no gas used and no logs bloom" {
    // when
    let block = Block{count: 0, hash: hex(text: "00"), name: "block", number: 0, previousHash: hex(text: "00"),
            timestamp: TimestampRange{from: DEFAULT(zonedDateTime)}}

    // then
    assertNull(actual: block.gasUsed)
    assertNull(actual: block.hapiVersion)
    assertNull(actual: block.logsBloom)
    assertNull(actual: block.size)
}
```

## Questions & Comments

- **Block hashes are not a type of their own.** `findByHash` takes `bytes` and documents the two accepted lengths (32
  and 48 bytes) only in its comment; the SDK does not check them, the Mirror Node rejects a hash of another length.
  Should there be a `BlockHash` type or a length annotation that allows exactly two lengths?
- **`findByNumber` and `findByHash` share one endpoint.** A single `findByHashOrNumber(text: string)` would mirror the
  REST API 1:1 but lose the types. Is the split the intended shape for path parameters that accept two formats?

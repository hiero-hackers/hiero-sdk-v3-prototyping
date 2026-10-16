# Mirror Node Topic Query API

## Description

Query consensus topics and their messages from the Mirror Node. Use the `TopicRepository` (available as
`MirrorNodeClient.topic`) to look up a `Topic`, to page through the messages submitted to a topic, or to fetch a
single message by its sequence number. A `TopicMessage` carries the consensus timestamp, payer, running hash and
sequence number of the message; messages that were submitted in several chunks carry `ChunkInfo` describing the
chunk.

## API Schema

```
namespace mirrornode.topic
requires {Address, AccountId, MirrorNode, TransactionId} from ledger
requires {Authority} from authority
requires {FixedFee} from mirrornode.common
requires {Page} from common

@@finalType
Topic {
    @@immutable topicId: Address
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable submitAuthority: Authority
    @@immutable @@nullable feeScheduleAuthority: Authority
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable autoRenewPeriod: seconds
    @@immutable createdTimestamp: zonedDateTime
    @@immutable deleted: bool
    @@immutable memo: string
    @@immutable fixedFees: list<FixedFee>
    @@immutable feeExemptAuthorities: list<Authority>   // empty if no authority is exempt from the custom fees
    @@immutable fromTimestamp: zonedDateTime
    @@immutable toTimestamp: zonedDateTime
}

@@finalType
ChunkInfo {
    @@immutable initialTransactionId: TransactionId
    @@immutable nonce: int32
    @@immutable number: int32
    @@immutable total: int32
    @@immutable scheduled: bool
}

@@finalType
TopicMessage {
    @@immutable @@nullable chunkInfo: ChunkInfo
    @@immutable consensusTimestamp: zonedDateTime
    @@immutable message: string
    @@immutable payerAccountId: AccountId
    @@immutable runningHash: bytes
    @@immutable runningHashVersion: int32
    @@immutable sequenceNumber: int64
    @@immutable topicId: Address
}

abstraction TopicRepository {
    @@async @@throws(mirror-node-error)
    @@nullable Topic findById(topicId: Address)

    @@async @@throws(mirror-node-error)
    Page<TopicMessage> getMessages(topicId: Address)

    @@async @@throws(mirror-node-error)
    @@nullable TopicMessage getMessageBySequenceNumber(topicId: Address, sequenceNumber: int64)
}

@@static TopicRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `TopicRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (a message submitted in several chunks carries the chunk's number and the total number of
// chunks, and the id of the transaction of its first chunk).
test "chunk info keeps the initial transaction id, the chunk number and the total" {
    // given
    let initial = DEFAULT(TransactionId)

    // when
    let chunk = ChunkInfo{initialTransactionId: initial, nonce: 0, number: 2, total: 3, scheduled: false}

    // then
    assertEquals(expected: initial, actual: chunk.initialTransactionId)
    assertEquals(expected: 2, actual: chunk.number)
    assertEquals(expected: 3, actual: chunk.total)
    assertFalse(condition: chunk.scheduled)
}

// Source: the Hiero Enterprise Java test fixture topic.json (topic 0.0.1926, auto-renew period 7776000 seconds, created
// "1706816673.106110015", timestamp range from the creation, memo "1706816668096 Mirror node created test topic", no
// custom fees, no fee-exempt keys); millisecond precision. The fixture has no end of the timestamp range, which this
// spec requires, so the test uses some valid time for it.
test "a topic keeps its auto-renew period and its creation time" {
    // given
    let created = toZonedDateTime(text: "2024-02-01T19:44:33.106Z")

    // when
    let topic = Topic{topicId: Address.fromString(address: "0.0.1926"), autoRenewPeriod: toSeconds(value: 7776000),
            createdTimestamp: created, deleted: false, memo: "1706816668096 Mirror node created test topic",
            fixedFees: [], feeExemptAuthorities: [], fromTimestamp: created, toTimestamp: DEFAULT(zonedDateTime)}

    // then
    assertEquals(expected: Address.fromString(address: "0.0.1926"), actual: topic.topicId)
    assertEquals(expected: toSeconds(value: 7776000), actual: topic.autoRenewPeriod)
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:44:33.106Z"), actual: topic.createdTimestamp)
    assertEquals(expected: created, actual: topic.fromTimestamp)
    assertEquals(expected: "1706816668096 Mirror node created test topic", actual: topic.memo)
    assertEquals(expected: 0, actual: length(value: topic.fixedFees))
    assertEquals(expected: 0, actual: length(value: topic.feeExemptAuthorities))
    assertNull(actual: topic.adminAuthority)
    assertNull(actual: topic.autoRenewAccount)
}

// Source: the Hiero Enterprise Java test fixture topic-message.json (topic 0.0.9411905, payer 0.0.6105114, consensus
// timestamp "1783075674.960707349", running hash version 3, sequence number 3, chunk 3 of 14); millisecond precision.
// A running hash of version 3 is a SHA-384 hash of 48 bytes (HAPI, TransactionReceipt.topicRunningHashVersion).
test "a topic message keeps its consensus timestamp, payer, running hash and sequence number" {
    // given
    let consensus = toZonedDateTime(text: "2026-07-03T10:47:54.960Z")
    let runningHash = base64(text: "qAIp0612V63kRt90Jwnmg3TmOn9WYZ50hzqGeiAACy569p0XYSEzqp5u6b1e4Nmd")
    let chunk = ChunkInfo{initialTransactionId: DEFAULT(TransactionId), nonce: 0, number: 3, total: 14,
            scheduled: false}

    // when
    let message = TopicMessage{chunkInfo: chunk, consensusTimestamp: consensus, message: "message",
            payerAccountId: AccountId.fromString(value: "0.0.6105114"), runningHash: runningHash,
            runningHashVersion: 3, sequenceNumber: 3, topicId: Address.fromString(address: "0.0.9411905")}

    // then
    assertEquals(expected: toZonedDateTime(text: "2026-07-03T10:47:54.960Z"), actual: message.consensusTimestamp)
    assertEquals(expected: AccountId.fromString(value: "0.0.6105114"), actual: message.payerAccountId)
    assertEquals(expected: runningHash, actual: message.runningHash)
    assertEquals(expected: 48, actual: length(value: message.runningHash))
    assertEquals(expected: 3, actual: message.runningHashVersion)
    assertEquals(expected: 3, actual: message.sequenceNumber)
    assertEquals(expected: Address.fromString(address: "0.0.9411905"), actual: message.topicId)
    assertEquals(expected: chunk, actual: message.chunkInfo)
}

// Source: this spec (`chunkInfo` is nullable: only a message submitted in several chunks carries it; a topic message is
// a value, two messages that differ only in their consensus timestamp are different).
test "a topic message submitted in one piece has no chunk info" {
    // given
    let topic = Address.fromString(address: "0.0.9411905")
    let payer = AccountId.fromString(value: "0.0.6105114")
    let runningHash = base64(text: "qAIp0612V63kRt90Jwnmg3TmOn9WYZ50hzqGeiAACy569p0XYSEzqp5u6b1e4Nmd")

    // when
    let message = TopicMessage{consensusTimestamp: toZonedDateTime(text: "2026-07-03T10:47:54.960Z"),
            message: "message", payerAccountId: payer, runningHash: runningHash, runningHashVersion: 3,
            sequenceNumber: 1, topicId: topic}
    let later = TopicMessage{consensusTimestamp: toZonedDateTime(text: "2026-07-03T10:47:54.961Z"),
            message: "message", payerAccountId: payer, runningHash: runningHash, runningHashVersion: 3,
            sequenceNumber: 1, topicId: topic}

    // then
    assertNull(actual: message.chunkInfo)
    assertNotEquals(unexpected: message, actual: later)
}
```

## Questions & Comments
- **`Topic.toTimestamp` is not nullable, but the Mirror Node leaves it open.** The REST API returns the validity of the
  topic state as a range `timestamp: {from, to}`, and for the current state `to` is `null` (e.g. the Hiero Enterprise
  Java test fixture `topic.json`). A `Topic` of the current state cannot be built without inventing an end time. Should
  `toTimestamp` be `@@nullable`?
- **`TopicMessage.message` is a `string`**, but the REST API returns the message Base64-encoded and a message can be
  arbitrary bytes. Is it the Base64 text, the decoded UTF-8 text, or should it be `bytes`?

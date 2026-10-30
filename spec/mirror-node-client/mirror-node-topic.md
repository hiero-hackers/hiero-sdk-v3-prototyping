# Mirror Node Topic Query API

## Description

Query consensus topics and their messages from the Mirror Node. Use the `TopicRepository` (available as
`MirrorNodeClient.topic`) to look up a `Topic`, to page through the messages submitted to a topic, or to fetch a
single message by its sequence number. A `TopicMessage` carries the consensus timestamp, payer, running hash and
sequence number of the message; messages that were submitted in several chunks carry `ChunkInfo` describing the
chunk.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **Keys are `Authority` and named after it.** `admin_key`, `submit_key` and `fee_schedule_key` of a topic are
  `adminAuthority`, `submitAuthority` and `feeScheduleAuthority`; `fee_exempt_key_list` is `feeExemptAuthorities`
  (ADR-0004). A key without key material (the empty key list of an immutable topic) is `null` resp. left out.
- **The custom fees of a topic are flattened.** `custom_fees.fixed_fees` is `Topic.fixedFees`,
  `custom_fees.created_timestamp` is `Topic.customFeesCreatedTimestamp`.
- **`TopicMessage.message` is text.** The REST API returns the message Base64-encoded (or as UTF-8 text with
  `encoding=utf-8`); `message` holds the UTF-8 text of the message in both cases.
- **The nonce and the scheduled flag of a chunk are on `ChunkInfo`.** `chunk_info.initial_transaction_id` is an object
  with `account_id`, `transaction_valid_start`, `nonce` and `scheduled`; the first two (and the nonce) make up
  `initialTransactionId`, `nonce` and `scheduled` are also `ChunkInfo.nonce` and `ChunkInfo.scheduled`. An absent
  nonce is `0`, an absent scheduled flag `false`.
- **The path parameter `timestamp` of `GET /topics/messages/{timestamp}` is a `zonedDateTime`.** The SDK sends it as
  `seconds.nanoseconds`.

## API Schema

```
namespace mirrornode.topic
requires {Address, AccountId, MirrorNode, TransactionId} from ledger
requires {Authority} from authority
requires {FixedFee, Order, TimestampRange} from mirrornode.common
requires {Page} from common

@@finalType
Topic {
    @@immutable topicId: Address
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable submitAuthority: Authority
    @@immutable @@nullable feeScheduleAuthority: Authority
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable @@nullable autoRenewPeriod: seconds          // how long the lifetime is extended after expiration
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable @@nullable customFeesCreatedTimestamp: zonedDateTime   // since when the custom fees apply
    @@immutable @@nullable deleted: bool
    @@immutable memo: string
    @@immutable @@default([]) fixedFees: list<FixedFee>    // charged for each message submitted to the topic
    @@immutable @@default([]) feeExemptAuthorities: list<Authority>   // empty if no authority is exempt from the custom fees
    @@immutable timestamp: TimestampRange                   // the period this state of the topic is valid for
}

// The chunk of a message that was submitted in several chunks.
@@finalType
ChunkInfo {
    @@immutable initialTransactionId: TransactionId     // the transaction of the first chunk
    @@immutable nonce: int32                            // the nonce of the initial transaction
    @@immutable number: int32                           // the number of this chunk, starting at 1
    @@immutable total: int32                            // the number of chunks of the message
    @@immutable scheduled: bool                         // whether the initial transaction was scheduled
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
    // The topic with the given id. Maps to GET /api/v1/topics/{topicId}.
    @@async @@throws(mirror-node-error)
    @@nullable Topic findById(topicId: Address)

    // The messages of a topic. Maps to GET /api/v1/topics/{topicId}/messages.
    @@async @@throws(mirror-node-error)
    Page<TopicMessage> getMessages(topicId: Address)

    // The messages of a topic, filtered by sequence number and consensus time. `encoding` is the encoding in which the
    // mirror node returns the messages ("base64" or "utf-8"). Maps to GET /api/v1/topics/{topicId}/messages.
    @@async @@throws(mirror-node-error)
    Page<TopicMessage> getMessages(topicId: Address, @@nullable encoding: string,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order,
            @@nullable @@min(0) sequenceNumber: int64, timestamp: list<string>)

    // The message of a topic with the given sequence number.
    // Maps to GET /api/v1/topics/{topicId}/messages/{sequenceNumber}.
    @@async @@throws(mirror-node-error)
    @@nullable TopicMessage getMessageBySequenceNumber(topicId: Address, @@min(0) sequenceNumber: int64)

    // The topic message that reached consensus at the given time. Maps to GET /api/v1/topics/messages/{timestamp}.
    @@async @@throws(mirror-node-error)
    @@nullable TopicMessage getMessageByTimestamp(timestamp: zonedDateTime)
}

@@static TopicRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `TopicRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: this spec (a message submitted in several chunks carries the chunk's number and the total number of
// chunks, and the id of the transaction of its first chunk).
@@testId("chunk-info-keeps-the-initial-transaction-id-the-chunk-number-and-the-total")
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
// custom fees, no fee-exempt keys); millisecond precision. The fixture has no end of the timestamp range: the state is
// the current one.
@@testId("a-topic-keeps-its-auto-renew-period-and-its-creation-time")
test "a topic keeps its auto-renew period and its creation time" {
    // given
    let created = toZonedDateTime(text: "2024-02-01T19:44:33.106Z")

    // when
    let topic = Topic{topicId: Address.fromString(address: "0.0.1926"), autoRenewPeriod: toSeconds(value: 7776000),
            createdTimestamp: created, deleted: false, memo: "1706816668096 Mirror node created test topic",
            fixedFees: [], feeExemptAuthorities: [], timestamp: TimestampRange{from: created}}

    // then
    assertEquals(expected: Address.fromString(address: "0.0.1926"), actual: topic.topicId)
    assertEquals(expected: toSeconds(value: 7776000), actual: topic.autoRenewPeriod)
    assertEquals(expected: toZonedDateTime(text: "2024-02-01T19:44:33.106Z"), actual: topic.createdTimestamp)
    assertEquals(expected: created, actual: topic.timestamp.from)
    assertNull(actual: topic.timestamp.to)
    assertEquals(expected: "1706816668096 Mirror node created test topic", actual: topic.memo)
    assertEquals(expected: 0, actual: length(value: topic.fixedFees))
    assertEquals(expected: 0, actual: length(value: topic.feeExemptAuthorities))
    assertNull(actual: topic.adminAuthority)
    assertNull(actual: topic.autoRenewAccount)
}

// Source: the Hiero Enterprise Java test fixture topic-message.json (topic 0.0.9411905, payer 0.0.6105114, consensus
// timestamp "1783075674.960707349", running hash version 3, sequence number 3, chunk 3 of 14); millisecond precision.
// A running hash of version 3 is a SHA-384 hash of 48 bytes (HAPI, TransactionReceipt.topicRunningHashVersion).
@@testId("a-topic-message-keeps-its-consensus-timestamp-payer-running-hash-and-sequence-number")
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
@@testId("a-topic-message-submitted-in-one-piece-has-no-chunk-info")
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

- **`TopicMessage.message` is a `string`**, but a message can be arbitrary bytes. The Java implementation decodes the
  Base64 message of the REST API as UTF-8 text, which garbles a binary message. Should it be `bytes`?

- **`TopicRepository` has no find-all.** The other repositories with entities have a `findAll()` (or a comparable
  list method), but `TopicRepository` can only look up a topic by id. The Mirror Node REST API has no `GET /topics`
  list endpoint, so a find-all would have to go through another endpoint. Because of this gap,
  `TopicService.getAll()` in [`service-topic.md`](../enterprise/service-topic.md) cannot be implemented. Should
  `TopicRepository` get a find-all, or should `TopicService.getAll()` be removed?

- **`sequencenumber` takes no operators in the OpenAPI definition.** It is declared as a plain integer, so
  `getMessages` takes one sequence number; the mirror node itself also accepts conditions such as `gt:5` (like
  `timestamp`). Should `sequenceNumber` become a `list<string>` of conditions?

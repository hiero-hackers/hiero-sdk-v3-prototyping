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

The queries of `TopicRepository` need a Mirror Node, and `Topic` and `TopicMessage` cannot be created in a test
because the test language has no literal for `zonedDateTime` and `seconds`.

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
```

## Questions & Comments

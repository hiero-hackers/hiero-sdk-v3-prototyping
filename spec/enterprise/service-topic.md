# Topic Service API

## Description

`TopicService` works with consensus topics (the Consensus Service): creating and deleting topics, submitting messages
and reading them. Messages can be read page by page with `getMessages`, fetched by sequence number, or received live
with `subscribe`, which keeps delivering new messages as they reach consensus. A topic without a submit authority is
public, so anyone may submit messages to it; a topic with an admin authority can be updated or deleted later.

## API Schema

```
namespace enterprise.service.topic
requires {Page} from common
requires {Address, AccountId} from ledger
requires {Authority} from authority
requires {Session} from enterprise.service

@@finalType
Topic {
    @@immutable topicId: Address
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable submitAuthority: Authority
    @@immutable createdTimestamp: zonedDateTime
    @@immutable memo: string
}

// The parameters of a new topic: everything that can be set when it is created. Nothing is required: without values
// the topic is public and cannot be changed.
@@finalType
TopicCreateParams {
    // Allows to update and delete the topic; without it the topic cannot be changed. It must sign the creation.
    @@immutable @@nullable adminAuthority: Authority
    // Allows to submit messages; without it anybody can submit messages.
    @@immutable @@nullable submitAuthority: Authority
    @@immutable @@nullable @@maxBytes(100) topicMemo: string
    // The renewal period applied when the expiration approaches; the network default if absent.
    @@immutable @@nullable autoRenewPeriod: seconds
    // The account that pays for the renewal; it must sign the creation.
    @@immutable @@nullable autoRenewAccount: AccountId
}

@@finalType
TopicMessage {
    @@immutable consensusTimestamp: zonedDateTime
    @@immutable message: string
    @@immutable payerAccountId: AccountId
    @@immutable sequenceNumber: int64
    @@immutable topicId: Address
}

TopicService {

    // Create a public topic (anyone may submit messages). An admin authority allows later updates and deletion.
    // The same as createTopic with TopicCreateParams that have only these values.
    @@throws(service-error) Topic createTopic()

    @@throws(service-error) Topic createTopic(@@maxBytes(100) memo: string)

    @@throws(service-error) Topic createTopic(adminAuthority: Authority, @@maxBytes(100) memo: string)

    // Create a topic with the given parameters: the general form of the methods above, for every option of a new
    // topic.
    @@throws(service-error) Topic createTopic(params: TopicCreateParams)
    
    @@throws(service-error) @@nullable Topic findById(topicId: Address)
    
    @@throws(service-error) Page<Topic> getAll()

    // Delete a topic (requires the topic's admin authority)
    @@throws(service-error) void deleteTopic(topicId: Address)

    // Submit a message to a topic
    @@throws(service-error) TopicMessage submitMessage(topicId: Address, message: bytes)
    
    @@throws(service-error) Page<TopicMessage> getMessages(topicId: Address)
    
    @@throws(service-error) TopicMessage getMessageBySequenceNumber(topicId: Address, sequenceNumber: int64)
    
    @@streaming TopicMessage subscribe(topicId: Address)
}

// Creates the service for the given session. With a framework integration the service is usually obtained via
// dependency injection instead.
@@static
TopicService createService(session: Session)
```

## Default Instances for Testing

```
// The parameters of a public topic that cannot be changed: no values.
instance TopicCreateParams = TopicCreateParams{}
```

## Testing

Tests of `TopicCreateParams`, `Topic` and `TopicMessage`. The methods of `TopicService` talk to the network and are
not unit-tested here. A memo of more UTF-8 bytes than its `@@maxBytes` is rejected with `invalid-argument-error`. Points in time
have millisecond precision, the precision every target language can hold.

```tests
// Source: this spec (TopicCreateParams: "Nothing is required: without values the topic is public and cannot be
// changed"); the Python SDK (topic_create_transaction_test.py, test_constructor_default_values: no admin and no submit
// key).
test "topic parameters without values describe a public topic that cannot be changed" {
    // when
    let params = DEFAULT(TopicCreateParams)

    // then
    assertNull(actual: params.adminAuthority)
    assertNull(actual: params.submitAuthority)
    assertNull(actual: params.topicMemo)
    assertNull(actual: params.autoRenewPeriod)
    assertNull(actual: params.autoRenewAccount)
}

// Source: the TopicCreateTransaction unit tests of the Java (TopicCreateTransactionTest), Go
// (topic_create_transaction_unit_test.go), C++ (TopicCreateTransactionUnitTests), Swift
// (TopicCreateTransactionUnitTests) and Python (topic_create_transaction_test.py, test_constructor) SDKs: every value
// that is set is read back unchanged. The auto-renew period is the one of the Java test (Duration.ofHours(24), which
// is exactly 86400 seconds).
test "topic parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let autoRenewAccount = DEFAULT(AccountId)
    let autoRenewPeriod = toSeconds(value: 86400)

    // when
    let params = TopicCreateParams{adminAuthority: authority, submitAuthority: authority, topicMemo: "memo",
            autoRenewPeriod: autoRenewPeriod, autoRenewAccount: autoRenewAccount}

    // then
    assertEquals(expected: authority, actual: params.adminAuthority)
    assertEquals(expected: authority, actual: params.submitAuthority)
    assertEquals(expected: "memo", actual: params.topicMemo)
    assertEquals(expected: autoRenewPeriod, actual: params.autoRenewPeriod)
    assertEquals(expected: autoRenewAccount, actual: params.autoRenewAccount)
}

// Source: this spec (@@maxBytes(100) of topicMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
// characters is.
test "a topic memo of 100 characters is accepted" {
    // given
    let memo = repeat(value: "a", count: 100)

    // when
    let params = TopicCreateParams{topicMemo: memo}

    // then
    assertEquals(expected: memo, actual: params.topicMemo)
}

// Source: this spec (@@maxBytes(100) of topicMemo); HAPI limits a memo to 100 bytes.
test "a topic memo of 101 characters is rejected" {
    // when / then
    @@expectThrows(invalid-argument-error)
    TopicCreateParams{topicMemo: repeat(value: "a", count: 101)}
}

// Source: hiero-enterprise-java (Topic, and the Mirror Node fixture topic.json of MirrorNodeJsonConverterTest: topic
// 0.0.1926 with an admin key, a submit key and the memo "1706816668096 Mirror node created test topic"). The created
// timestamp of the fixture has nanosecond precision, so the test uses some valid point in time instead.
test "a topic keeps the values it is created with" {
    // given
    let topicId = Address{shard: 0, realm: 0, num: 1926, checksum: ""}
    let authority = DEFAULT(Authority)
    let createdTimestamp = DEFAULT(zonedDateTime)

    // when
    let topic = Topic{topicId: topicId, adminAuthority: authority, submitAuthority: authority,
            createdTimestamp: createdTimestamp, memo: "1706816668096 Mirror node created test topic"}

    // then
    assertEquals(expected: topicId, actual: topic.topicId)
    assertEquals(expected: authority, actual: topic.adminAuthority)
    assertEquals(expected: authority, actual: topic.submitAuthority)
    assertEquals(expected: createdTimestamp, actual: topic.createdTimestamp)
    assertEquals(expected: "1706816668096 Mirror node created test topic", actual: topic.memo)
}

// Source: this spec (Description: "A topic without a submit authority is public"; TopicCreateParams.adminAuthority:
// "without it the topic cannot be changed").
test "a topic without authorities has neither an admin nor a submit authority" {
    // when
    let topic = Topic{topicId: DEFAULT(Address), adminAuthority: null, submitAuthority: null,
            createdTimestamp: DEFAULT(zonedDateTime), memo: ""}

    // then
    assertNull(actual: topic.adminAuthority)
    assertNull(actual: topic.submitAuthority)
}

// Source: this spec (Topic is a value of its attributes); the points in time are the fixed timestamps of the Java
// SDK tests (TopicMessageTest: Instant.ofEpochSecond(1554158542), FileCreateTransactionTest:
// Instant.ofEpochSecond(1554158728); converted exactly to 2019-04-01T22:42:22Z and 2019-04-01T22:45:28Z).
test "topics created at different times are not equal" {
    // given
    let created = toZonedDateTime(text: "2019-04-01T22:42:22Z")
    let createdLater = toZonedDateTime(text: "2019-04-01T22:45:28Z")

    // when
    let topic = Topic{topicId: DEFAULT(Address), adminAuthority: null, submitAuthority: null,
            createdTimestamp: created, memo: "memo"}
    let sameTopic = Topic{topicId: DEFAULT(Address), adminAuthority: null, submitAuthority: null,
            createdTimestamp: created, memo: "memo"}
    let laterTopic = Topic{topicId: DEFAULT(Address), adminAuthority: null, submitAuthority: null,
            createdTimestamp: createdLater, memo: "memo"}

    // then
    assertEquals(expected: topic, actual: sameTopic)
    assertNotEquals(unexpected: topic, actual: laterTopic)
    assertTrue(condition: topic.createdTimestamp < laterTopic.createdTimestamp)
}

// Source: the Java SDK (TopicMessageTest, constructWithArgs: consensus timestamp Instant.ofEpochSecond(1554158542),
// converted exactly to 2019-04-01T22:42:22Z, sequence number 7, payer 0.0.1 of the transaction id); every value that
// is set is read back unchanged.
test "a topic message keeps the values it is created with" {
    // given
    let consensusTimestamp = toZonedDateTime(text: "2019-04-01T22:42:22Z")
    let payer = AccountId{shard: 0, realm: 0, num: 1, checksum: ""}
    let topicId = DEFAULT(Address)

    // when
    let message = TopicMessage{consensusTimestamp: consensusTimestamp, message: "Hello, Hiero!",
            payerAccountId: payer, sequenceNumber: 7, topicId: topicId}

    // then
    assertEquals(expected: consensusTimestamp, actual: message.consensusTimestamp)
    assertEquals(expected: "Hello, Hiero!", actual: message.message)
    assertEquals(expected: payer, actual: message.payerAccountId)
    assertEquals(expected: 7, actual: message.sequenceNumber)
    assertEquals(expected: topicId, actual: message.topicId)
}
```

## Questions & Comments

- The authorization-key fields/parameters here (`Topic.adminAuthority`, `Topic.submitAuthority`, and `createTopic(adminAuthority)`)
  accept the full `Authority` type, so multisig (m-of-n) and contract-controlled keys work at the enterprise layer
  too — not just single public keys. See [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and
  [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

- **`submitMessage` and `getAll` cannot be implemented yet.** `consensusnode.transactions` has no `TopicMessageSubmit`
  transaction (see [`transactions-topics.md`](../consensus-node-client/transactions-topics.md)), and the mirror-node
  `TopicRepository` has no find-all (see [`mirror-node-topic.md`](../mirror-node-client/mirror-node-topic.md)), so the
  Java implementation fails for both methods. Should `getAll` stay in the API once there is no Mirror Node endpoint
  that lists topics?

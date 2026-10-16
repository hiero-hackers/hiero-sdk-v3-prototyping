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
    @@immutable @@nullable @@maxLength(100) topicMemo: string
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

    @@throws(service-error) Topic createTopic(memo: string)

    @@throws(service-error) Topic createTopic(adminAuthority: Authority, memo: string)

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

Tests of `TopicCreateParams`. The methods of `TopicService` talk to the network and are not unit-tested here. A
violated `@@maxLength` cannot be expected yet: the test language expects errors of calls only, and creating a
`TopicCreateParams` declares none. `Topic` and `TopicMessage` hold a `zonedDateTime`, which the test language cannot
write yet (it has no date and time literals), so they are not tested.

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
// that is set is read back unchanged.
test "topic parameters keep the values they are created with" {
    // given
    let authority = DEFAULT(Authority)
    let autoRenewAccount = DEFAULT(AccountId)

    // when
    let params = TopicCreateParams{adminAuthority: authority, submitAuthority: authority, topicMemo: "memo",
            autoRenewAccount: autoRenewAccount}

    // then
    assertEquals(expected: authority, actual: params.adminAuthority)
    assertEquals(expected: authority, actual: params.submitAuthority)
    assertEquals(expected: "memo", actual: params.topicMemo)
    assertEquals(expected: autoRenewAccount, actual: params.autoRenewAccount)
}

// Source: this spec (@@maxLength(100) of topicMemo); HAPI limits a memo to 100 bytes, which ASCII text of 100
// characters is.
test "a topic memo of 100 characters is accepted" {
    // given
    let memo = repeat(value: "a", count: 100)

    // when
    let params = TopicCreateParams{topicMemo: memo}

    // then
    assertEquals(expected: memo, actual: params.topicMemo)
}
```

## Questions & Comments

- The authorization-key fields/parameters here (`Topic.adminAuthority`, `Topic.submitAuthority`, and `createTopic(adminAuthority)`)
  accept the full `Authority` type, so multisig (m-of-n) and contract-controlled keys work at the enterprise layer
  too — not just single public keys. See [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and
  [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

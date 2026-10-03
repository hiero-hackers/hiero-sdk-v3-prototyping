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
    @@throws(service-error) Topic createTopic()

    @@throws(service-error) Topic createTopic(memo: string)

    @@throws(service-error) Topic createTopic(adminAuthority: Authority, memo: string)
    
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

## Questions & Comments

- The authorization-key fields/parameters here (`Topic.adminAuthority`, `Topic.submitAuthority`, and `createTopic(adminAuthority)`)
  accept the full `Authority` type, so multisig (m-of-n) and contract-controlled keys work at the enterprise layer
  too — not just single public keys. See [ADR-0004](../../docs/adr/0004-authority-authorization-sum-type.md) and
  [`authority.md`](../base/authority.md).
- Open option: simple single-key convenience overloads taking a `PublicKey` directly could be added later if the
  enterprise layer wants extra ergonomics for the common single-signer case.

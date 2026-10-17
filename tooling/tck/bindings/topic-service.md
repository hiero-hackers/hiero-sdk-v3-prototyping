# Topic service

The TCK methods of the consensus (topic) service
([topic-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/topic-service)):
creating, updating and deleting topics, and the topic query.

## Gaps

- **Topic fees** (HIP-991: `feeScheduleKey`, `feeExemptKeys`, `customFees`): the V3 topic transactions and
  `TopicInfo` do not model them.
- **`isDeleted`**, **`ledgerId`** of `getTopicInfo`: `TopicInfo` has neither; **`queryPayment`**: a paid query has
  only a maximum payment.
- **`submitTopicMessage`** is not bound: the V3 specs have no message submit transaction.

```bindings
requires {TopicCreateTransaction, TopicUpdateTransaction, TopicDeleteTransaction} from consensusnode.transactions.topics
requires {TopicInfoQuery} from consensusnode.queries.topics

// Creates a topic (TopicCreateTransaction.md).
binding createTopic -> TopicCreateTransaction {
    topicMemo        = memo : string
    adminAuthority   = adminKey : key
    submitAuthority  = submitKey : key
    autoRenewPeriod  = autoRenewPeriod : seconds
    autoRenewAccount = autoRenewAccount : accountId
    unsupported feeScheduleKey "TopicCreateTransaction has no fee schedule key (HIP-991)"
    unsupported feeExemptKeys "TopicCreateTransaction has no fee exempt keys (HIP-991)"
    unsupported customFees "TopicCreateTransaction has no custom fees (HIP-991)"
    result topicId = receipt.topicId : address
    result status  = receipt.status : status
}

// Updates a topic (TopicUpdateTransaction.md).
binding updateTopic -> TopicUpdateTransaction {
    topicId          = topicId : address
    topicMemo        = memo : string
    adminAuthority   = adminKey : key
    submitAuthority  = submitKey : key
    autoRenewPeriod  = autoRenewPeriod : seconds
    autoRenewAccount = autoRenewAccount : accountId
    expirationTime   = expirationTime : timestamp
    unsupported feeScheduleKey "TopicUpdateTransaction has no fee schedule key (HIP-991)"
    unsupported feeExemptKeys "TopicUpdateTransaction has no fee exempt keys (HIP-991)"
    unsupported customFees "TopicUpdateTransaction has no custom fees (HIP-991)"
    result status = receipt.status : status
}

// Deletes a topic (TopicDeleteTransaction.md).
binding deleteTopic -> TopicDeleteTransaction {
    topicId = topicId : address
    result status = receipt.status : status
}

// Queries the information of a topic (TopicInfoQuery.md).
binding getTopicInfo -> TopicInfoQuery {
    topicId         = topicId : address
    maxQueryPayment = maxQueryPayment : tinybar
    unsupported queryPayment "a paid query has only a maximum payment (PaidQuery.maxQueryPayment)"
    result topicId            = response.topicId : address
    result topicMemo          = response.topicMemo : string
    result sequenceNumber     = response.sequenceNumber : int64
    result adminKey           = response.adminAuthority : key
    result submitKey          = response.submitAuthority : key
    result autoRenewAccountId = response.autoRenewAccount : accountId
    result autoRenewPeriod    = response.autoRenewPeriod : seconds
    result expirationTime     = response.expirationTime : timestamp
    unsupported result feeScheduleKey "TopicInfo has no fee schedule key (HIP-991)"
    unsupported result feeExemptKeys "TopicInfo has no fee exempt keys (HIP-991)"
    unsupported result customFees "TopicInfo has no custom fees (HIP-991)"
    unsupported result isDeleted "TopicInfo has no deleted flag"
    unsupported result ledgerId "TopicInfo has no ledger ID"
}
```

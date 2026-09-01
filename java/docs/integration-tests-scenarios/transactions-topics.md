# Transactions / Topics

## Scenarios

### `transactions.topics/can-create-topic`

> **Implementation:** `TopicCreateIntegrationTest.canCreateTopic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:17`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create topic.
 - **Then** the operation completes without error.

### `transactions.topics/can-create-topic-with-no-fields-set`

> **Implementation:** `TopicCreateIntegrationTest.canCreateTopicWithNoFieldsSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:40`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create topic with no field set.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.topics/creates-and-updates-revenue-generating-topic`

> **Implementation:** `TopicCreateIntegrationTest.createsAndUpdatesRevenueGeneratingTopic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:50`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “creates and updates revenue-generating topic” behavior.
 - **Then** the operation completes without error, the returned information fee schedule key equals the expected operator key, the retrieved value equals the expected value, and the expected observable result is returned.

### `transactions.topics/fails-to-create-revenue-generating-topic-with-invalid-fee-exempt-key`

> **Implementation:** `TopicCreateIntegrationTest.failsToCreateRevenueGeneratingTopicWithInvalidFeeExemptKey`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:151`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “fails to create revenue-generating topic with invalid fee exempt key” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.topics/fails-to-update-fee-schedule-key-without-permissions`

> **Implementation:** `TopicCreateIntegrationTest.failsToUpdateFeeScheduleKeyWithoutPermissions`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:212`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “fails to update fee schedule key without permissions” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.topics/fails-to-update-custom-fees-without-fee-schedule-key`

> **Implementation:** `TopicCreateIntegrationTest.failsToUpdateCustomFeesWithoutFeeScheduleKey`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:237`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “fails to update custom fees without a fee schedule key” behavior.
 - **Then** the request is rejected with the expected error.

### `transactions.topics/charges-hbar-fees-with-limits-applied`

> **Implementation:** `TopicCreateIntegrationTest.chargesHbarFeesWithLimitsApplied`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:271`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “charges HBAR fees with limits applied” behavior.
 - **Then** the operation completes without error and the returned balance value in tinybars is less than the expected value.

### `transactions.topics/exempts-fee-exempt-keys-from-hbar-fees`

> **Implementation:** `TopicCreateIntegrationTest.exemptsFeeExemptKeysFromHbarFees`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:308`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “exempts fee-exempt keys from HBAR fees” behavior.
 - **Then** the operation completes without error and the returned balance value in tinybars is greater than the expected value.

### `transactions.topics/create-topic-transaction-should-assign-automatically-auto-renew-account-id`

> **Implementation:** `TopicCreateIntegrationTest.createTopicTransactionShouldAssignAutomaticallyAutoRenewAccountId`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:348`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to assign auto Renew Account identifier to the topic creator.
 - **Then** the operation completes without error and the auto renew account identifier is present.

### `transactions.topics/create-topic-transaction-with-transaction-id-should-assign-auto-renew-account-id-to-transaction-id-account-id`

> **Implementation:** `TopicCreateIntegrationTest.createTopicTransactionWithTransactionIdShouldAssignAutoRenewAccountIdToTransactionIdAccountId`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:361`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to assign auto Renew Account identifier to the transaction identifier account identifier.
 - **Then** the operation completes without error and the auto renew account identifier equals the expected account identifier.

### `transactions.topics/can-clear-custom-fees-list-and-fee-exempt-keys-list`

> **Implementation:** `TopicCreateIntegrationTest.canClearCustomFeesListAndFeeExemptKeysList`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:422`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to clear custom Fees List, fee Exempt Keys List and fee Schedule Key.
 - **Then** the operation completes without error, the returned information fee schedule key equals the expected operator key, the retrieved value equals the expected value, and the expected observable result is returned.

### `transactions.topics/can-update-topic-without-specifying-anything-topic-should-have-the-same-values`

> **Implementation:** `TopicCreateIntegrationTest.canUpdateTopicWithoutSpecifyingAnythingTopicShouldHaveTheSameValues`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicCreateIntegrationTest.java:494`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to update without specifying anything.
 - **Then** the operation completes without error, the returned information fee schedule key equals the expected operator key, the retrieved value equals the expected value, and the expected observable result is returned.

### `transactions.topics/can-delete-topic`

> **Implementation:** `TopicDeleteIntegrationTest.canDeleteTopic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicDeleteIntegrationTest.java:15`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete topic.
 - **Then** the operation completes without error.

### `transactions.topics/cannot-delete-immutable-topic`

> **Implementation:** `TopicDeleteIntegrationTest.cannotDeleteImmutableTopic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicDeleteIntegrationTest.java:33`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete immutable topic.
 - **Then** the request is rejected with status UNAUTHORIZED.

### `transactions.topics/can-submit-a-topic-message`

> **Implementation:** `TopicMessageSubmitIntegrationTest.canSubmitATopicMessage`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageSubmitIntegrationTest.java:22`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to submit a topic message.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `transactions.topics/can-submit-a-large-topic-message`

> **Implementation:** `TopicMessageSubmitIntegrationTest.canSubmitALargeTopicMessage`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageSubmitIntegrationTest.java:60`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to submit a large topic message.
 - **Then** the request is rejected with the expected error.

### `transactions.topics/cannot-submit-message-when-topic-id-is-not-set`

> **Implementation:** `TopicMessageSubmitIntegrationTest.cannotSubmitMessageWhenTopicIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageSubmitIntegrationTest.java:110`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to submit message when topic identifier is not set.
 - **Then** the request is rejected with status INVALID_TOPIC_ID.

### `transactions.topics/decode-hex-regression-test`

> **Implementation:** `TopicMessageSubmitIntegrationTest.decodeHexRegressionTest`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageSubmitIntegrationTest.java:144`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “hex Decode Regression Test” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.topics/can-update-topic`

> **Implementation:** `TopicUpdateIntegrationTest.canUpdateTopic`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicUpdateIntegrationTest.java:15`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update topic.
 - **Then** the operation completes without error, the topic info topic memo equals the expected text, and the topic info auto renew account identifier is absent.

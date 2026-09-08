# Queries / Topics

## Scenarios

### `queries.topics/can-query-topic-info`

> **Implementation:** `TopicInfoIntegrationTest.canQueryTopicInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicInfoIntegrationTest.java:19`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query topic info.
 - **Then** the operation completes without error and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-query-topic-info`

> **Implementation:** `TopicInfoIntegrationTest.getCostQueryTopicInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicInfoIntegrationTest.java:42`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the operation completes without error, the cost is present, and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-big-max-query-topic-info`

> **Implementation:** `TopicInfoIntegrationTest.getCostBigMaxQueryTopicInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicInfoIntegrationTest.java:72`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the operation completes without error, the cost is present, and the returned information topic memo equals the expected text.

### `queries.topics/get-cost-small-max-query-topic-info`

> **Implementation:** `TopicInfoIntegrationTest.getCostSmallMaxQueryTopicInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicInfoIntegrationTest.java:102`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the request is rejected with the expected error.

### `queries.topics/get-cost-insufficient-tx-fee-query-topic-info`

> **Implementation:** `TopicInfoIntegrationTest.getCostInsufficientTxFeeQueryTopicInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicInfoIntegrationTest.java:128`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost for topic info query.
 - **Then** the request is rejected with the expected error.

### `queries.topics/can-receive-a-topic-message`

> **Implementation:** `TopicMessageIntegrationTest.canReceiveATopicMessage`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageIntegrationTest.java:16`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to receive a topic message.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `queries.topics/can-receive-a-large-topic-message`

> **Implementation:** `TopicMessageIntegrationTest.canReceiveALargeTopicMessage`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageIntegrationTest.java:70`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to receive a large topic message.
 - **Then** the operation completes without error, the returned information topic identifier equals the expected topic identifier, the returned information topic memo equals the expected text, and the returned information sequence number equals 0.

### `queries.topics/unsubscribing-does-not-log-retry-warnings`

> **Implementation:** `TopicMessageIntegrationTest.unsubscribingDoesNotLogRetryWarnings`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/TopicMessageIntegrationTest.java:127`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “unsubscribing does not log retry warnings” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

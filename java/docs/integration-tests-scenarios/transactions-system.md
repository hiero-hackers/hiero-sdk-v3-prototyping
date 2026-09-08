# Transactions / System

## Scenarios

### `transactions.system/cannot-create-live-hash-because-its-not-supported`

> **Implementation:** `LiveHashAddIntegrationTest.cannotCreateLiveHashBecauseItsNotSupported`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/LiveHashAddIntegrationTest.java:21`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create live hash because it's not supported.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.system/cannot-delete-live-hash-because-its-not-supported`

> **Implementation:** `LiveHashDeleteIntegrationTest.cannotDeleteLiveHashBecauseItsNotSupported`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/LiveHashDeleteIntegrationTest.java:20`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete live hash because it's not supported.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.system/load-test`

> **Implementation:** `LoadIntegrationTest.loadTest`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/LoadIntegrationTest.java:17`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “load test with multiple clients and single executor” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.system/all-system-transactions-are-not-supported`

> **Implementation:** `SystemIntegrationTest.allSystemTransactionsAreNotSupported`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/SystemIntegrationTest.java:22`
> **Status:** Disabled — Cannot run with solo action

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “all system transactions are not supported” behavior.
 - **Then** the request is rejected with status NOT_SUPPORTED.

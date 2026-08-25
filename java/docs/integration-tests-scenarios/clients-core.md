# Clients / Core

## Scenarios

### `clients.core/fails-when-no-nodes-are-matching`

> **Implementation:** `ClientIntegrationTest.failsWhenNoNodesAreMatching`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:17`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “fails when all the manually set nodes are not matching the address book” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/can-skip-nodes`

> **Implementation:** `ClientIntegrationTest.canSkipNodes`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:34`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to skip invalid nodes.
 - **Then** the operation completes without error.

### `clients.core/test-replace-nodes`

> **Implementation:** `ClientIntegrationTest.testReplaceNodes`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:49`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “set Network functions correctly” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `clients.core/transaction-id-network-is-verified`

> **Implementation:** `ClientIntegrationTest.transactionIdNetworkIsVerified`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:84`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “transaction identifier Network Is Verified” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/test-max-nodes-per-transaction`

> **Implementation:** `ClientIntegrationTest.testMaxNodesPerTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:97`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “set Max Nodes Per Transaction” behavior.
 - **Then** the operation completes without error, the transaction node account identifiers is present, and the transaction node account identifiers size equals 1.

### `clients.core/ping`

> **Implementation:** `ClientIntegrationTest.ping`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:113`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all`

> **Implementation:** `ClientIntegrationTest.pingAll`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:128`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all-bad-network`

> **Implementation:** `ClientIntegrationTest.pingAllBadNetwork`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:146`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All Bad Network” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/ping-async`

> **Implementation:** `ClientIntegrationTest.pingAsync`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:186`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping Async” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all-async`

> **Implementation:** `ClientIntegrationTest.pingAllAsync`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:201`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All Async” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/test-client-init-with-mirror-network`

> **Implementation:** `ClientIntegrationTest.testClientInitWithMirrorNetwork`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:219`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “for Mirror Network” behavior.
 - **Then** the operation completes without error, the mirror network has the expected size, the retrieved value equals the expected value, and the client network is present.

### `clients.core/test-client-init-with-mirror-network-an-custom-realm-and-shard`

> **Implementation:** `ClientIntegrationTest.testClientInitWithMirrorNetworkAnCustomRealmAndShard`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ClientIntegrationTest.java:233`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “for Mirror Network with custom realm and shard” behavior.
 - **Then** the operation completes without error, the mirror network has the expected size, the retrieved value equals the expected value, and the client network is present.

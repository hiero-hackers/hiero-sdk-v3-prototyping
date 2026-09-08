# Queries / Nodes

## Scenarios

### `queries.nodes/can-create-and-verify-registered-node-with-polling`

> **Implementation:** `RegisteredNodeAddressBookQueryIntegrationTest.canCreateAndVerifyRegisteredNodeWithPolling`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/RegisteredNodeAddressBookQueryIntegrationTest.java:19`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to query registered node using node identifier.
 - **Then** the operation completes without error, the registered node book registered nodes has the expected size, the node description equals the expected description, and the node service endpoints has the expected size.

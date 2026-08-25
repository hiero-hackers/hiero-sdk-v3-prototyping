# Queries / Network

## Scenarios

### `queries.network/cannot-query-network-version-info`

> **Implementation:** `NetworkVersionInfoIntegrationTest.cannotQueryNetworkVersionInfo`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/NetworkVersionInfoIntegrationTest.java:9`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query network version info.
 - **Then** the request is rejected with the expected error.

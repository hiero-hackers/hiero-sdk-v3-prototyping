# Transactions / Nodes

## Scenarios

### `transactions.nodes/can-create-new-network-node`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create new network node.
 - **Then** the operation completes without error.

### `transactions.nodes/can-create-new-network-node-with-registered-node`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create new network node with the registered node's identifier in associated Registered Nodes.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.nodes/can-execute-node-update-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to execute Node Update Transaction.
 - **Then** the operation completes without error.

### `transactions.nodes/can-delete-grpc-web-proxy-endpoint`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete g RPC web proxy endpoint.
 - **Then** the operation completes without error.

### `transactions.nodes/should-succeed-when-updating-node-account-id-with-proper-signatures`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to a new valid account with signatures from both the node admin key and the account identifier key, then the transaction succeeds” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned response is present, and the returned receipt status equals the expected status success.

### `transactions.nodes/test-node-update-transaction-can-change-to-same-account`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to the same existing account identifier with proper signatures, then the transaction succeeds” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.nodes/test-node-update-transaction-can-change-node-account-update-addressbook-and-retry`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a node whose account identifier has been updated, when a transaction is submitted to that node with the old account identifier after the Node Update Transaction reaches consensus, then the signed transaction for this node fails with INVALID_NODE_ACCOUNT_ID and the SDK retries successfully with another node” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the new node account identifier is present, and the returned receipt status equals the expected status success.

### `transactions.nodes/test-node-update-transaction-fails-with-invalid-signature-when-missing-node-admin-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to a new valid account with only the account identifier key signature (missing node admin signature), then the transaction fails with INVALID_SIGNATURE” behavior.
 - **Then** the operation completes without error and the receipt exception returned receipt status equals the expected status invalid signature.

### `transactions.nodes/test-node-update-transaction-fails-with-invalid-signature-when-missing-account-id-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to a new valid account with only the node admin key signature (missing account identifier signature), then the transaction fails with INVALID_SIGNATURE” behavior.
 - **Then** the operation completes without error and the receipt exception returned receipt status equals the expected status invalid signature.

### `transactions.nodes/test-node-update-transaction-fails-with-invalid-account-id-for-non-existent-account`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to a non-existent account with proper signatures, then the transaction fails with INVALID_ACCOUNT_ID” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.nodes/test-node-update-transaction-fails-with-account-deleted-for-deleted-account`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given a node with an existing account identifier, when a Node Update Transaction is submitted to change the account identifier to a deleted account with proper signatures, then the transaction fails with ACCOUNT_DELETED” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the delete receipt status equals the expected status success, and the receipt exception returned receipt status equals the expected status account deleted.

### `transactions.nodes/test-subsequent-transaction-with-new-node-account-id-succeeds`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given an successfully handled transaction with outdated node account identifier , when subsequent transaction that target the new node account identifier of that node is executed, then the transaction succeeds” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the new node account identifier is present, and the returned receipt status equals the expected status success.

### `transactions.nodes/test-sdk-updates-network-configuration-on-invalid-node-account`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given an SDK receives INVALID_NODE_ACCOUNT for a node, when updating its network configuration, then the SDK updates its network with the latest node account I Ds for subsequent transactions” behavior.
 - **Then** the operation completes without error, the new node account identifier is present, and the expected observable result is returned.

### `transactions.nodes/test-node-update-transaction-can-associate-with-registered-node-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an existing consensus node and an existing registered node, when a Node Update Transaction sets associated Registered Nodes to include the registered node identifier then the consensus node is updated with the association.” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.nodes/can-create-registered-node-with-block-node`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create a registered node with block Node Service Endpoint.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt registered node identifier is greater than the expected value.

### `transactions.nodes/can-create-registered-node-with-mirror-node`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create a registered node with mirror Node Service Endpoint.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt registered node identifier is greater than the expected value.

### `transactions.nodes/can-create-registered-node-with-rpc-relay`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create a registered node with rpc Relay Service Endpoint.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt registered node identifier is greater than the expected value.

### `transactions.nodes/can-create-registered-node-with-general-service`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create a registered node with general Service Endpoint.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt registered node identifier is greater than the expected value.

### `transactions.nodes/can-create-registered-node-with-multiple-service-endpoints`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create a registered node with multiple service endpoints.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, the returned receipt registered node identifier is greater than the expected value, and the registered nodes has the expected size.

### `transactions.nodes/registered-node-create-transaction-fails-invalid-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Registered Node Create Transaction with no admin key set fails with a precheck status of KEY_REQUIRED” behavior.
 - **Then** the request is rejected with status KEY_REQUIRED.

### `transactions.nodes/registered-node-create-transaction-empty-service-endpoints`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a Registered Node Create Transaction with an empty service endpoints list fails with INVALID_REGISTERED_ENDPOINT” behavior.
 - **Then** the request is rejected with status INVALID_REGISTERED_ENDPOINT.

### `transactions.nodes/can-delete-registered-node`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete a registered node.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the delete receipt status equals the expected status success.

### `transactions.nodes/should-cause-receipt-status-when-node-still-associated`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to return REGISTERED_NODE_STILL_ASSOCIATED when deleting an associated node.
 - **Then** the request is rejected with status REGISTERED_NODE_STILL_ASSOCIATED.

### `transactions.nodes/registered-node-delete-fails-deleted-node-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “when a Registered Node Delete Transaction is executed on already deleted registered node fails with INVALID_REGISTERED_NODE_ID” behavior.
 - **Then** the request is rejected with status INVALID_REGISTERED_NODE_ID.

### `transactions.nodes/registered-node-delete-fails-invalid-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Registered Node Delete Transaction targeting a non-existent registered Node identifier fails with INVALID_REGISTERED_NODE_ID” behavior.
 - **Then** the request is rejected with status INVALID_REGISTERED_NODE_ID.

### `transactions.nodes/can-update-registered-node-description`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update registered node description.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the node identifier is greater than the expected value, the update receipt status equals the expected status success, and the registered nodes has the expected size.

### `transactions.nodes/can-update-registered-node-service-endpoint`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update registered node endpoints.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the node identifier is greater than the expected value, the update receipt status equals the expected status success, and the registered nodes has the expected size.

### `transactions.nodes/can-rotate-admin-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to rotate registered node admin key.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the node identifier is greater than the expected value, the returned receipt status equals the expected status success, and the registered nodes has the expected size.

### `transactions.nodes/can-replace-ip-addr-to-domain`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an existing registered node created with an IP address endpoint when a Registered Node Update Transaction replaces it with a domain name endpoint” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the node identifier is greater than the expected value, the update receipt status equals the expected status success, and the registered nodes has the expected size.

### `transactions.nodes/registered-update-node-fails-invalid-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “when Registered Node Update Transaction sets a new admin key but only the old admin key signs fails with INVALID_SIGNATURE” behavior.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.nodes/registered-update-node-fails-invalid-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given a Registered Node Update Transaction targeting a non-existent registered Node identifier fails with INVALID_REGISTERED_NODE_ID” behavior.
 - **Then** the request is rejected with status INVALID_REGISTERED_NODE_ID.

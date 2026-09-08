# Clients / Core

## Scenarios

### `clients.core/fails-when-no-nodes-are-matching`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “fails when all the manually set nodes are not matching the address book” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/can-skip-nodes`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to skip invalid nodes.
 - **Then** the operation completes without error.

### `clients.core/test-replace-nodes`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “set Network functions correctly” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `clients.core/transaction-id-network-is-verified`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “transaction identifier Network Is Verified” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/test-max-nodes-per-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “set Max Nodes Per Transaction” behavior.
 - **Then** the operation completes without error, the transaction node account identifiers is present, and the transaction node account identifiers size equals 1.

### `clients.core/ping`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all-bad-network`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All Bad Network” behavior.
 - **Then** the request is rejected with the expected error.

### `clients.core/ping-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping Async” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/ping-all-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “ping All Async” behavior.
 - **Then** the operation completes without error and the node collection is not empty.

### `clients.core/test-client-init-with-mirror-network`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “for Mirror Network” behavior.
 - **Then** the operation completes without error, the mirror network has the expected size, the retrieved value equals the expected value, and the client network is present.

### `clients.core/test-client-init-with-mirror-network-an-custom-realm-and-shard`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “for Mirror Network with custom realm and shard” behavior.
 - **Then** the operation completes without error, the mirror network has the expected size, the retrieved value equals the expected value, and the client network is present.

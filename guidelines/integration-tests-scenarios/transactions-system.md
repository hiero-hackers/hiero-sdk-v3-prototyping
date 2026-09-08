# Transactions / System

## Scenarios

### `transactions.system/cannot-create-live-hash-because-its-not-supported`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create live hash because it's not supported.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.system/cannot-delete-live-hash-because-its-not-supported`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete live hash because it's not supported.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.system/load-test`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “load test with multiple clients and single executor” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.system/all-system-transactions-are-not-supported`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “all system transactions are not supported” behavior.
 - **Then** the request is rejected with status NOT_SUPPORTED.

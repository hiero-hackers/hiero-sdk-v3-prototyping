# Transactions / Hooks

## Scenarios

### `transactions.hooks/lambda-s-store-updates-storage-with-valid-signature`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists with storage, when a Hook Store Transaction updates storage slots with valid signatures, then the storage update transaction succeeds” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.hooks/lambda-s-store-fails-without-proper-signature`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists, when a Hook Store Transaction attempts to update storage without proper signatures, then the transaction fails with INVALID_SIGNATURE error” behavior.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.hooks/lambda-s-store-fails-with-non-existent-hook-id`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “when a Hook Store Transaction attempts to update storage with a non-existent Hook identifier, then the transaction fails with HOOK_NOT_FOUND” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.hooks/lambda-s-store-too-many-storage-updates-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists, when updating 256 storage slots, then TOO_MANY_EVM_HOOK_STORAGE_UPDATES is thrown” behavior.
 - **Then** the request is rejected with status TOO_MANY_EVM_HOOK_STORAGE_UPDATES.

# Transactions / Hooks

## Scenarios

### `transactions.hooks/lambda-s-store-updates-storage-with-valid-signature`

> **Implementation:** `HookStoreTransactionIntegrationTest.lambdaSStoreUpdatesStorageWithValidSignature`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/HookStoreTransactionIntegrationTest.java:20`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists with storage, when a Hook Store Transaction updates storage slots with valid signatures, then the storage update transaction succeeds” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.hooks/lambda-s-store-fails-without-proper-signature`

> **Implementation:** `HookStoreTransactionIntegrationTest.lambdaSStoreFailsWithoutProperSignature`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/HookStoreTransactionIntegrationTest.java:72`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists, when a Hook Store Transaction attempts to update storage without proper signatures, then the transaction fails with INVALID_SIGNATURE error” behavior.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.hooks/lambda-s-store-fails-with-non-existent-hook-id`

> **Implementation:** `HookStoreTransactionIntegrationTest.lambdaSStoreFailsWithNonExistentHookId`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/HookStoreTransactionIntegrationTest.java:127`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “when a Hook Store Transaction attempts to update storage with a non-existent Hook identifier, then the transaction fails with HOOK_NOT_FOUND” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.hooks/lambda-s-store-too-many-storage-updates-fails`

> **Implementation:** `HookStoreTransactionIntegrationTest.lambdaSStoreTooManyStorageUpdatesFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/HookStoreTransactionIntegrationTest.java:160`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a lambda hook exists, when updating 256 storage slots, then TOO_MANY_EVM_HOOK_STORAGE_UPDATES is thrown” behavior.
 - **Then** the request is rejected with status TOO_MANY_EVM_HOOK_STORAGE_UPDATES.

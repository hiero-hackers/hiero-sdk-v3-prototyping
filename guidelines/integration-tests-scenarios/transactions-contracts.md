# Transactions / Contracts

## Scenarios

### `transactions.contracts/create-contract-with-flow`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “create contract with flow” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-without-signing`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow without signing” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/create-contract-with-flow-private-key-sign`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow and sign with private key” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-public-key-sign`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow and sign with public key and transaction signer” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-with-unlimited-auto-token-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “create contract with flow with unlimited max auto associations” behavior.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-create-contract-with-flow-with-invalid-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create contract with flow with invalid max auto associations.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/can-create-contract`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.contracts/can-create-contract-with-no-admin-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with no admin key.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.contracts/cannot-create-contract-when-gas-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `transactions.contracts/cannot-create-contract-when-constructor-parameters-are-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract when constructor parameters are not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `transactions.contracts/cannot-create-contract-when-bytecode-file-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create contract when bytecode file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `transactions.contracts/can-create-contract-with-unlimited-auto-token-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with unlimited max auto associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-create-contract-with-invalid-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with invalid max auto associations.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/contract-create-with-basic-lambda-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with basic EVM hook, when executed, then receipt status is SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/contract-create-with-lambda-hook-and-storage-updates-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with EVM hook and storage updates, when executed, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/contract-create-with-duplicate-hook-ids-fails-precheck`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with duplicate hook I Ds, when executed, then HOOK_ID_REPEATED_IN_CREATION_DETAILS (precheck)” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.contracts/contract-create-with-lambda-hook-and-admin-key-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Contract Create Transaction with lambda hook and admin key, when executed with admin signature, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/can-delete-contract-with-admin-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete contract with admin key.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `transactions.contracts/cannot-delete-contract-which-has-no-admin-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete contract which has no admin key.
 - **Then** the request is rejected with status MODIFYING_IMMUTABLE_CONTRACT.

### `transactions.contracts/cannot-delete-contract-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/can-execute-contract-methods`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract methods.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/cannot-execute-contract-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to execute contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/cannot-execute-contract-when-contract-function-parameters-are-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract when contract function parameters are not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `transactions.contracts/cannot-execute-contract-when-gas-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `transactions.contracts/can-update-contract`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `transactions.contracts/cannot-update-contract-when-contract-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/cannot-update-contract-that-is-immutable`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract that is immutable.
 - **Then** the request is rejected with status MODIFYING_IMMUTABLE_CONTRACT.

### `transactions.contracts/setting-auto-renew-account-id-to-default-clears-field`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “setting auto Renew Account identifier to 0.0.0 clears the field” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/can-update-contract-max-auto-associations-to-unlimited`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract max auto associations to unlimited.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-update-contract-max-auto-associations-to-invalid`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract max auto associations to invalid value.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/contract-update-with-basic-lambda-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists without hooks, when a Contract Update Transaction adds a basic EVM hook with valid signatures, then the hook is successfully attached to the contract” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-duplicate-hook-ids-in-same-transaction-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Contract Update Transaction is configured with duplicate hook I Ds in the same creation details, when the transaction is executed, then the transaction fails with a HOOK_ID_REPEATED_IN_CREATION_DETAILS error during precheck” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.contracts/contract-update-with-existing-hook-id-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook, when a Contract Update Transaction attempts to add a hook with the same identifier that already exists on the contract, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.contracts/contract-update-with-lambda-hook-and-storage-updates-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists without hooks, when a Contract Update Transaction adds an EVM hook with initial storage updates, then the hook is attached and storage is initialized correctly” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-hook-id-already-in-use-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with an existing hook, when a Contract Update Transaction attempts to add another hook with the same identifier that is already in use, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.contracts/contract-update-with-hook-deletion-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook, when a Contract Update Transaction deletes the hook by identifier with valid signatures, then the hook is successfully removed from the contract” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-non-existent-hook-id-deletion-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with hooks, when a Contract Update Transaction attempts to delete a hook identifier that doesn't exist on the contract, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/contract-update-with-add-and-delete-same-hook-id-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Contract Update Transaction attempts to add and delete hooks with the same identifier in the same transaction, when the transaction is executed, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/contract-update-with-already-deleted-hook-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook that has been previously deleted, when a Contract Update Transaction attempts to delete the same hook again, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/can-create-large-contract`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create large contract.
 - **Then** the operation completes without error, the returned record contract function result signer nonce equals 1, and the returned record contract function result signer nonce equals 2.

### `transactions.contracts/can-execute-jumbo-transaction-below-the-limit`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute jumbo transaction below the limit.
 - **Then** the operation completes without error and the returned record contract function result signer nonce equals 1.

### `transactions.contracts/jumbo-transaction-above-the-limit-fails`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “jumbo transaction above the limit fails” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/signer-nonce-changed-on-ethereum-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “signer nonce changed on Ethereum transaction” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/jumbo-ethereum-transaction-with-large-calldata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “jumbo Ethereum transaction with large calldata” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

# Transactions / Contracts

## Scenarios

### `transactions.contracts/create-contract-with-flow`

> **Implementation:** `ContractCreateFlowIntegrationTest.createContractWithFlow`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:15`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “create contract with flow” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-without-signing`

> **Implementation:** `ContractCreateFlowIntegrationTest.createContractWithFlowWithoutSigning`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:48`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow without signing” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/create-contract-with-flow-private-key-sign`

> **Implementation:** `ContractCreateFlowIntegrationTest.createContractWithFlowPrivateKeySign`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:66`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow and sign with private key” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-public-key-sign`

> **Implementation:** `ContractCreateFlowIntegrationTest.createContractWithFlowPublicKeySign`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:104`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “create contract with flow and sign with public key and transaction signer” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/create-contract-with-flow-with-unlimited-auto-token-associations`

> **Implementation:** `ContractCreateFlowIntegrationTest.createContractWithFlowWithUnlimitedAutoTokenAssociations`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:142`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “create contract with flow with unlimited max auto associations” behavior.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-create-contract-with-flow-with-invalid-max-auto-associations`

> **Implementation:** `ContractCreateFlowIntegrationTest.cannotCreateContractWithFlowWithInvalidMaxAutoAssociations`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateFlowIntegrationTest.java:191`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create contract with flow with invalid max auto associations.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/can-create-contract`

> **Implementation:** `ContractCreateIntegrationTest.canCreateContract`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:27`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.contracts/can-create-contract-with-no-admin-key`

> **Implementation:** `ContractCreateIntegrationTest.canCreateContractWithNoAdminKey`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:74`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with no admin key.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.contracts/cannot-create-contract-when-gas-is-not-set`

> **Implementation:** `ContractCreateIntegrationTest.cannotCreateContractWhenGasIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:108`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `transactions.contracts/cannot-create-contract-when-constructor-parameters-are-not-set`

> **Implementation:** `ContractCreateIntegrationTest.cannotCreateContractWhenConstructorParametersAreNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:140`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract when constructor parameters are not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `transactions.contracts/cannot-create-contract-when-bytecode-file-id-is-not-set`

> **Implementation:** `ContractCreateIntegrationTest.cannotCreateContractWhenBytecodeFileIdIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:171`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create contract when bytecode file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `transactions.contracts/can-create-contract-with-unlimited-auto-token-associations`

> **Implementation:** `ContractCreateIntegrationTest.canCreateContractWithUnlimitedAutoTokenAssociations`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:191`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with unlimited max auto associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-create-contract-with-invalid-max-auto-associations`

> **Implementation:** `ContractCreateIntegrationTest.cannotCreateContractWithInvalidMaxAutoAssociations`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateIntegrationTest.java:252`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to create contract with invalid max auto associations.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/contract-create-with-basic-lambda-hook-succeeds`

> **Implementation:** `ContractCreateTransactionHooksIntegrationTest.contractCreateWithBasicLambdaHookSucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateTransactionHooksIntegrationTest.java:31`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with basic EVM hook, when executed, then receipt status is SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/contract-create-with-lambda-hook-and-storage-updates-succeeds`

> **Implementation:** `ContractCreateTransactionHooksIntegrationTest.contractCreateWithLambdaHookAndStorageUpdatesSucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateTransactionHooksIntegrationTest.java:57`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with EVM hook and storage updates, when executed, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/contract-create-with-duplicate-hook-ids-fails-precheck`

> **Implementation:** `ContractCreateTransactionHooksIntegrationTest.contractCreateWithDuplicateHookIdsFailsPrecheck`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateTransactionHooksIntegrationTest.java:84`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “given Contract Create Transaction with duplicate hook I Ds, when executed, then HOOK_ID_REPEATED_IN_CREATION_DETAILS (precheck)” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.contracts/contract-create-with-lambda-hook-and-admin-key-succeeds`

> **Implementation:** `ContractCreateTransactionHooksIntegrationTest.contractCreateWithLambdaHookAndAdminKeySucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractCreateTransactionHooksIntegrationTest.java:110`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Contract Create Transaction with lambda hook and admin key, when executed with admin signature, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, and the returned receipt contract identifier is present.

### `transactions.contracts/can-delete-contract-with-admin-key`

> **Implementation:** `ContractDeleteIntegrationTest.canDeleteContractWithAdminKey`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractDeleteIntegrationTest.java:23`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete contract with admin key.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `transactions.contracts/cannot-delete-contract-which-has-no-admin-key`

> **Implementation:** `ContractDeleteIntegrationTest.cannotDeleteContractWhichHasNoAdminKey`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractDeleteIntegrationTest.java:69`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete contract which has no admin key.
 - **Then** the request is rejected with status MODIFYING_IMMUTABLE_CONTRACT.

### `transactions.contracts/cannot-delete-contract-when-contract-id-is-not-set`

> **Implementation:** `ContractDeleteIntegrationTest.cannotDeleteContractWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractDeleteIntegrationTest.java:112`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/can-execute-contract-methods`

> **Implementation:** `ContractExecuteIntegrationTest.canExecuteContractMethods`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractExecuteIntegrationTest.java:24`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract methods.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/cannot-execute-contract-when-contract-id-is-not-set`

> **Implementation:** `ContractExecuteIntegrationTest.cannotExecuteContractWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractExecuteIntegrationTest.java:75`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to execute contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/cannot-execute-contract-when-contract-function-parameters-are-not-set`

> **Implementation:** `ContractExecuteIntegrationTest.cannotExecuteContractWhenContractFunctionParametersAreNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractExecuteIntegrationTest.java:92`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract when contract function parameters are not set.
 - **Then** the request is rejected with status CONTRACT_REVERT_EXECUTED.

### `transactions.contracts/cannot-execute-contract-when-gas-is-not-set`

> **Implementation:** `ContractExecuteIntegrationTest.cannotExecuteContractWhenGasIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractExecuteIntegrationTest.java:137`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to execute contract when gas is not set.
 - **Then** the request is rejected with status INSUFFICIENT_GAS.

### `transactions.contracts/can-update-contract`

> **Implementation:** `ContractUpdateIntegrationTest.canUpdateContract`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:28`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract.
 - **Then** the operation completes without error, the returned information contract identifier equals the expected contract identifier, the returned information account identifier is present, and the objects require non null(returned information account identifier) text equals the expected contract identifier text.

### `transactions.contracts/cannot-update-contract-when-contract-id-is-not-set`

> **Implementation:** `ContractUpdateIntegrationTest.cannotUpdateContractWhenContractIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:91`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update contract when contract identifier is not set.
 - **Then** the request is rejected with status INVALID_CONTRACT_ID.

### `transactions.contracts/cannot-update-contract-that-is-immutable`

> **Implementation:** `ContractUpdateIntegrationTest.cannotUpdateContractThatIsImmutable`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:107`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract that is immutable.
 - **Then** the request is rejected with status MODIFYING_IMMUTABLE_CONTRACT.

### `transactions.contracts/setting-auto-renew-account-id-to-default-clears-field`

> **Implementation:** `ContractUpdateIntegrationTest.settingAutoRenewAccountIdToDefaultClearsField`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:140`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “setting auto Renew Account identifier to 0.0.0 clears the field” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/can-update-contract-max-auto-associations-to-unlimited`

> **Implementation:** `ContractUpdateIntegrationTest.canUpdateContractMaxAutoAssociationsToUnlimited`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:196`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract max auto associations to unlimited.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.contracts/cannot-update-contract-max-auto-associations-to-invalid`

> **Implementation:** `ContractUpdateIntegrationTest.cannotUpdateContractMaxAutoAssociationsToInvalid`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateIntegrationTest.java:263`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update contract max auto associations to invalid value.
 - **Then** the request is rejected with the expected error.

### `transactions.contracts/contract-update-with-basic-lambda-hook-succeeds`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithBasicLambdaHookSucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:30`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists without hooks, when a Contract Update Transaction adds a basic EVM hook with valid signatures, then the hook is successfully attached to the contract” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-duplicate-hook-ids-in-same-transaction-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithDuplicateHookIdsInSameTransactionFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:61`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Contract Update Transaction is configured with duplicate hook I Ds in the same creation details, when the transaction is executed, then the transaction fails with a HOOK_ID_REPEATED_IN_CREATION_DETAILS error during precheck” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.contracts/contract-update-with-existing-hook-id-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithExistingHookIdFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:91`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook, when a Contract Update Transaction attempts to add a hook with the same identifier that already exists on the contract, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.contracts/contract-update-with-lambda-hook-and-storage-updates-succeeds`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithLambdaHookAndStorageUpdatesSucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:130`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists without hooks, when a Contract Update Transaction adds an EVM hook with initial storage updates, then the hook is attached and storage is initialized correctly” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-hook-id-already-in-use-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithHookIdAlreadyInUseFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:161`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with an existing hook, when a Contract Update Transaction attempts to add another hook with the same identifier that is already in use, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.contracts/contract-update-with-hook-deletion-succeeds`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithHookDeletionSucceeds`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:202`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook, when a Contract Update Transaction deletes the hook by identifier with valid signatures, then the hook is successfully removed from the contract” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.contracts/contract-update-with-non-existent-hook-id-deletion-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithNonExistentHookIdDeletionFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:239`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with hooks, when a Contract Update Transaction attempts to delete a hook identifier that doesn't exist on the contract, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/contract-update-with-add-and-delete-same-hook-id-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithAddAndDeleteSameHookIdFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:278`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a Contract Update Transaction attempts to add and delete hooks with the same identifier in the same transaction, when the transaction is executed, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/contract-update-with-already-deleted-hook-fails`

> **Implementation:** `ContractUpdateTransactionHooksIntegrationTest.contractUpdateWithAlreadyDeletedHookFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/ContractUpdateTransactionHooksIntegrationTest.java:311`
> **Status:** Disabled — Temporarily disabled, EVM Hooks are not enabled in v0.73.0

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “given a contract exists with a hook that has been previously deleted, when a Contract Update Transaction attempts to delete the same hook again, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.contracts/can-create-large-contract`

> **Implementation:** `EthereumFlowIntegrationTest.canCreateLargeContract`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EthereumFlowIntegrationTest.java:23`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create large contract.
 - **Then** the operation completes without error, the returned record contract function result signer nonce equals 1, and the returned record contract function result signer nonce equals 2.

### `transactions.contracts/can-execute-jumbo-transaction-below-the-limit`

> **Implementation:** `EthereumFlowIntegrationTest.canExecuteJumboTransactionBelowTheLimit`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EthereumFlowIntegrationTest.java:67`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute jumbo transaction below the limit.
 - **Then** the operation completes without error and the returned record contract function result signer nonce equals 1.

### `transactions.contracts/jumbo-transaction-above-the-limit-fails`

> **Implementation:** `EthereumFlowIntegrationTest.jumboTransactionAboveTheLimitFails`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EthereumFlowIntegrationTest.java:120`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “jumbo transaction above the limit fails” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/signer-nonce-changed-on-ethereum-transaction`

> **Implementation:** `EthereumTransactionIntegrationTest.signerNonceChangedOnEthereumTransaction`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EthereumTransactionIntegrationTest.java:38`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “signer nonce changed on Ethereum transaction” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.contracts/jumbo-ethereum-transaction-with-large-calldata`

> **Implementation:** `EthereumTransactionIntegrationTest.jumboEthereumTransactionWithLargeCalldata`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/EthereumTransactionIntegrationTest.java:137`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “jumbo Ethereum transaction with large calldata” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

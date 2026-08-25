# Transactions / Accounts

## Scenarios

### `transactions.accounts/can-spend-hbar-allowance`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to spend HBAR allowance.
 - **Then** the operation completes without error and the transfer found is true.

### `transactions.accounts/can-create-account-with-only-initial-balance-and-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with only initial balance and key.
 - **Then** the operation completes without error, the returned information account identifier equals the expected account identifier, the returned information is deleted is false, and the returned information key text) is equal to(key get public key( has the expected value.

### `transactions.accounts/can-create-account-with-no-initial-balance`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with no initial balance.
 - **Then** the operation completes without error, the returned information account identifier equals the expected account identifier, the returned information is deleted is false, and the returned information key text) is equal to(key get public key( has the expected value.

### `transactions.accounts/can-not-create-account-with-no-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create account with no key.
 - **Then** the request is rejected with status KEY_REQUIRED.

### `transactions.accounts/can-create-with-alias-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to create account using alias Key.
 - **Then** the operation completes without error and the key public key equals the expected returned information alias key.

### `transactions.accounts/manages-expiration`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “regenerates Transaction Ids in response to expiration” behavior.
 - **Then** the operation completes without error, the returned information account identifier equals the expected account identifier, the returned information is deleted is false, and the returned information key text) is equal to(key get public key( has the expected value.

### `transactions.accounts/create-account-with-alias-from-admin-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias from admin key.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information contract account identifier has the expected value.

### `transactions.accounts/create-account-with-alias-from-admin-key-with-receiver-sig-required`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias from admin key with receiver sig required.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information contract account identifier has the expected value.

### `transactions.accounts/cannot-create-account-with-alias-from-admin-key-with-receiver-sig-required-and-no-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias from admin key with receiver sig required without signature.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.accounts/create-account-with-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias different from admin key.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information contract account identifier has the expected value.

### `transactions.accounts/cannot-create-account-with-alias-without-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias different from admin key without signature.
 - **Then** the request is rejected with the expected error.

### `transactions.accounts/create-account-with-alias-with-receiver-sig-required`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias different from admin key with receiver sig required.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information contract account identifier has the expected value.

### `transactions.accounts/cannot-create-account-with-alias-with-receiver-sig-required-without-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias different from admin key and receiver sig required without signature.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.accounts/cannot-create-account-with-alias-without-both-key-signatures`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with alias different from admin key without both key signature.
 - **Then** the request is rejected with the expected error.

### `transactions.accounts/create-account-using-set-key-with-alias-account-should-have-same-key-and-same-keys-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with ECDSA key using set Key With Alias, account should have same ECDSA as key and same key's alias.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information key equals the expected ecdsa key public key.

### `transactions.accounts/create-account-using-set-key-with-alias-account-should-have-key-as-key-and-ecdsak-ey-as-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with using set Key With Alias, account should have key as key and ECDSA key as alias.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information key equals the expected key public key.

### `transactions.accounts/create-account-using-set-key-without-alias-account-should-have-key-as-key-and-no-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account using set Key Without Alias, account should have key as key and no alias.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information key equals the expected key public key.

### `transactions.accounts/create-account-using-set-key-with-alias-with-ed25519-key-should-throw-an-exception`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account using set Key With Alias with only ed25519 key, exception should be thrown.
 - **Then** the request is rejected with the expected error.

### `transactions.accounts/create-account-using-set-key-with-alias-with-public-key-should-have-public-key-and-derived-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with ECDSA public key using set Key With Alias, account should have public key as key and derived alias.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information key equals the expected public key.

### `transactions.accounts/create-account-using-set-key-with-alias-with-ed25519-key-and-public-ecdsa-key-should-have-ed25519-key-and-derived-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with ED25519 key and ECDSA public key for alias, account should have ED25519 key and derived alias.
 - **Then** the operation completes without error, the account identifier is present, the returned information account identifier is present, and the returned information key equals the expected account key public key.

### `transactions.accounts/can-create-account-with-high-volume`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with high-volume throttles enabled.
 - **Then** the operation completes without error, the returned record high volume pricing multiplier is at least the expected value, the returned information account identifier equals the expected account identifier, and the returned information is deleted is false.

### `transactions.accounts/can-create-account-with-high-volume-and-valid-max-fee`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create account with high-volume throttles and valid max transaction fee.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, the account identifier is present, and the returned information account identifier equals the expected account identifier.

### `transactions.accounts/account-create-with-basic-lambda-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Account Create Transaction with basic EVM hook, when executed, then receipt status is SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/account-create-with-lambda-hook-and-storage-updates-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Account Create Transaction with an EVM hook and storage updates, when executed, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/account-create-with-duplicate-hook-ids-fails-precheck`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Account Create Transaction with duplicate hook I Ds, when executed, then HOOK_ID_REPEATED_IN_CREATION_DETAILS (precheck)” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.accounts/account-create-with-lambda-hook-and-admin-key-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “given Account Create Transaction with an EVM hook and admin key, when executed with admin signature, then SUCCESS” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/can-delete-account`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to delete account.
 - **Then** the operation completes without error, the returned information account identifier equals the expected account identifier, the returned information is deleted is false, and the returned information key text) is equal to(key get public key( has the expected value.

### `transactions.accounts/cannot-create-account-with-no-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete invalid account identifier.
 - **Then** the request is rejected with status ACCOUNT_ID_DOES_NOT_EXIST.

### `transactions.accounts/cannot-delete-account-that-has-not-signed-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete account that has not signed transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.accounts/can-update-account-with-new-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update account with a new key.
 - **Then** the operation completes without error, the returned information account identifier equals the expected account identifier, the returned information is deleted is false, and the returned information key text) is equal to(key1 get public key( has the expected value.

### `transactions.accounts/cannot-update-account-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update account when account identifier is not set.
 - **Then** the request is rejected with status ACCOUNT_ID_DOES_NOT_EXIST.

### `transactions.accounts/account-update-with-basic-lambda-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists without hooks, when an Account Update Transaction adds a basic EVM hook, then the hook is successfully attached to the account” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/account-update-with-duplicate-hook-ids-in-same-transaction-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an Account Update Transaction is configured with duplicate hook I Ds in the same creation details, when the transaction is executed, then the transaction fails with a HOOK_ID_REPEATED_IN_CREATION_DETAILS error” behavior.
 - **Then** the request is rejected with status HOOK_ID_REPEATED_IN_CREATION_DETAILS.

### `transactions.accounts/account-update-with-existing-hook-id-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists with a hook, when an Account Update Transaction attempts to add a hook with the same identifier that already exists on the account, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.accounts/account-update-with-lambda-hook-and-storage-updates-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists without hooks, when an Account Update Transaction adds an EVM hook with initial storage updates, then the hook is attached and storage is initialized correctly” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/account-update-with-hook-id-already-in-use-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists with an existing hook, when an Account Update Transaction attempts to add another hook with the same identifier that is already in use, then the transaction fails with a HOOK_ID_IN_USE error” behavior.
 - **Then** the request is rejected with status HOOK_ID_IN_USE.

### `transactions.accounts/account-update-with-hook-deletion-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists with a hook, when an Account Update Transaction deletes the hook by identifier with valid signatures, then the hook is successfully removed from the account” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.accounts/account-update-with-non-existent-hook-id-deletion-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists with hooks, when an Account Update Transaction attempts to delete a hook identifier that doesn't exist on the account, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.accounts/account-update-with-add-and-delete-same-hook-id-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an Account Update Transaction attempts to add and delete hooks with the same identifier in the same transaction, when the transaction is executed, then the transaction fails with a HOOK_NOT_FOUND error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.accounts/account-update-with-already-deleted-hook-fails`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account exists with a hook that has been previously deleted, when an Account Update Transaction attempts to delete the same hook again, then the transaction fails with a HOOK_DELETED error” behavior.
 - **Then** the request is rejected with status HOOK_NOT_FOUND.

### `transactions.accounts/privileged-system-account-can-create-large-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “privileged system payer can create files larger than 6kb” behavior.
 - **Then** the operation completes without error, the serialized length is greater than the expected value, the serialized length is less than the expected value, and the returned receipt file identifier is present.

### `transactions.accounts/privileged-system-account-can-update-large-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “privileged system payer can update files with contents larger than 6kb” behavior.
 - **Then** the operation completes without error, the serialized length is greater than the expected value, and the serialized length is less than the expected value.

### `transactions.accounts/privileged-system-account-can-append-large-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “privileged system payer can append to file making it larger than 6kb” behavior.
 - **Then** the operation completes without error, the transaction to bytes length is less than the expected value, and the transaction to bytes length is greater than the expected value.

### `transactions.accounts/non-privileged-account-cannot-create-large-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “non-privileged account cannot create files larger than 6kb” behavior.
 - **Then** the operation completes without error and the exception status equals the expected status transaction oversize.

### `transactions.accounts/non-privileged-account-cannot-create-account-with-large-key-list`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “non-privileged account cannot create account with large Key List larger than 6kb” behavior.
 - **Then** the operation completes without error and the exception status equals the expected status transaction oversize.

### `transactions.accounts/privileged-account-at-near130-kb-limit`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “privileged account can create file just under 130kb limit” behavior.
 - **Then** the operation completes without error, the serialized length is less than the expected value, the serialized length is greater than the expected value, and the returned receipt file identifier is present.

### `transactions.accounts/non-privileged-account-can-create-small-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “non-privileged account can create file under 6kb limit” behavior.
 - **Then** the operation completes without error, the serialized length is less than the expected value, and the returned receipt file identifier is present.

### `transactions.accounts/treasury-account-can-create-large-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “treasury account (0.0.2) can create large files” behavior.
 - **Then** the operation completes without error, the serialized length is greater than the expected value, the serialized length is less than the expected value, and the returned receipt file identifier is present.

### `transactions.accounts/privileged-system-account-can-create-account-with-large-key-list`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “privileged system payer can create account with large Key List (180 keys)” behavior.
 - **Then** the operation completes without error, the serialized length is greater than the expected value, the serialized length is less than the expected value, and the account identifier is present.

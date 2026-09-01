# Transactions / Tokens

## Scenarios

### `transactions.tokens/cannot-transfer-without-allowance-approval`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer on behalf of spender account without allowance approval.
 - **Then** the request is rejected with status SPENDER_DOES_NOT_HAVE_ALLOWANCE.

### `transactions.tokens/cannot-transfer-after-allowance-remove`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer on behalf of spender account after removing the allowance approval.
 - **Then** the request is rejected with status SPENDER_DOES_NOT_HAVE_ALLOWANCE.

### `transactions.tokens/cannot-remove-single-serial-when-allowance-is-given-for-all`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to remove single serial number allowance when the allowance is given for all serials at once.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/account-given-allowance-for-all-should-be-able-to-give-allowance-for-single`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “account, which given the allowance for all serials at once, should be able to give allowances for single serial numbers to other accounts” behavior.
 - **Then** the request is rejected with status SPENDER_DOES_NOT_HAVE_ALLOWANCE.

### `transactions.tokens/can-cancel-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when they are in pending state” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-cancel-tokens-when-token-is-frozen`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when token is frozen” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-cancel-tokens-when-token-is-paused`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when token is paused” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-cancel-tokens-when-token-is-deleted`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when token is deleted” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-cancel-tokens-to-multiple-receivers`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when they are in pending state to multiple receivers” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-cancel-tokens-from-multiple-airdrop-txns`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “cancels the tokens when they are in pending state from multiple airdrop transactions” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/cannot-cancel-tokens-for-non-existing-airdrop`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to cancel the tokens when they are not airdropped.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/canon-cancel-tokens-for-already-canceled-airdrop`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to cancel the tokens when they are already canceled.
 - **Then** the request is rejected with status INVALID_PENDING_AIRDROP_ID.

### `transactions.tokens/canon-cancel-with-empty-pending-airdrops-list`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to cancel the tokens with empty list.
 - **Then** the request is rejected with status EMPTY_PENDING_AIRDROP_ID_LIST.

### `transactions.tokens/cannot-cancel-tokens-with-duplicate-entries`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to cancel the tokens with duplicate entries.
 - **Then** the request is rejected with status PENDING_AIRDROP_ID_REPEATED.

### `transactions.tokens/can-claim-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “claims the tokens when they are in pending state” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-claim-tokens-to-multiple-receivers`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “claims the tokens when they are in pending state to multiple receivers” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-claim-tokens-from-multiple-airdrop-txns`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “claims the tokens when they are in pending state from multiple airdrop transactions” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/cannot-claim-tokens-for-non-existing-airdrop`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens when they are not airdropped.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-claim-tokens-for-already-claimed-airdrop`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens when they are already claimed.
 - **Then** the request is rejected with status INVALID_PENDING_AIRDROP_ID.

### `transactions.tokens/cannot-claim-with-empty-pending-airdrops-list`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to claim the tokens with empty list.
 - **Then** the request is rejected with status EMPTY_PENDING_AIRDROP_ID_LIST.

### `transactions.tokens/cannot-claim-tokens-with-duplicate-entries`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens with duplicate entries.
 - **Then** the request is rejected with status PENDING_AIRDROP_ID_REPEATED.

### `transactions.tokens/cannot-claim-tokens-when-token-is-paused`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens when token is paused.
 - **Then** the request is rejected with status TOKEN_IS_PAUSED.

### `transactions.tokens/cannot-claim-tokens-when-token-is-deleted`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens when token is deleted.
 - **Then** the request is rejected with status TOKEN_WAS_DELETED.

### `transactions.tokens/cannot-claim-tokens-when-token-is-frozen`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to claim the tokens when token is frozen.
 - **Then** the request is rejected with status ACCOUNT_FROZEN_FOR_TOKEN.

### `transactions.tokens/can-airdrop-associated-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “transfers tokens when the account is associated” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-airdrop-non-associated-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “tokens are in pending state when the account is not associated” behavior.
 - **Then** the operation completes without error and the returned record pending airdrop records is present.

### `transactions.tokens/can-airdrop-to-alias`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “airdrop creates a hollow account and transfers the tokens” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-airdrop-with-custom-fee`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to airdrop with custom fees.
 - **Then** the operation completes without error.

### `transactions.tokens/can-airdrop-tokens-with-receiver-sig-required-fungible`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to airdrop ft with receiver Sig=true.
 - **Then** the operation completes without error.

### `transactions.tokens/can-airdrop-tokens-with-receiver-sig-required-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to airdrop NFT with receiver Sig=true.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-airdrop-tokens-with-allowance-and-without-balance-fungible`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to airdrop ft with no balance.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.tokens/cannot-airdrop-tokens-with-allowance-and-without-balance-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to airdrop NFT with no balance.
 - **Then** the request is rejected with status NOT_SUPPORTED.

### `transactions.tokens/cannot-airdrop-tokens-with-invalid-body`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to airdrop with invalid body.
 - **Then** the request is rejected with status EMPTY_TOKEN_TRANSFER_BODY or INVALID_TRANSACTION_BODY.

### `transactions.tokens/can-transfer-fungible-tokens-to-accounts-with-limited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to transfer Fungible Tokens to accounts with Limited Max Auto Associations.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-transfer-nfts-to-accounts-with-limited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer Nfts to accounts with Limited Max Auto Associations.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-set-unlimited-max-auto-associations-for-account`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to set unlimited max auto associations for Account.
 - **Then** the operation completes without error and the expected observable result is returned.

### `transactions.tokens/can-transfer-fungible-tokens-to-accounts-with-unlimited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to transfer Fungible Tokens to accounts with Unlimited Max Auto Associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-transfer-fungible-tokens-with-decimals-to-accounts-with-unlimited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to transfer Fungible Tokens (With Decimals) to accounts with Unlimited Max Auto Associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-transfer-fungible-tokens-on-behalf-of-owner-to-account-with-unlimited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to transfer Fungible Tokens on Behalf Of Owner to account with Unlimited Max Auto Associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-transfer-nfts-to-accounts-with-unlimited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer Nfts to accounts with Unlimited Max Auto Associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-transfer-nfts-on-behalf-of-owner-to-account-with-unlimited-max-auto-associations`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer Nfts on Behalf Of Owner to account with Unlimited Max Auto Associations.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/cannot-set-invalid-max-auto-associations-values`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to set Invalid Max Auto Associations Values.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-burn-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to burn tokens.
 - **Then** the operation completes without error and the returned receipt total supply equals the expected 1000000 - 10.

### `transactions.tokens/cannot-burn-tokens-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to burn tokens when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/can-burn-tokens-when-amount-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to burn tokens when amount is not set.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.tokens/cannot-burn-tokens-when-supply-key-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to burn tokens when supply key does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-burn-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to burn NF Ts.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-burn-nfts-when-nft-is-not-owned`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to burn NF Ts when NFT is not owned by treasury.
 - **Then** the request is rejected with status TREASURY_MUST_OWN_BURNED_NFT.

### `transactions.tokens/can-create-token-with-operator-as-all-keys`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with operator as all keys.
 - **Then** the operation completes without error.

### `transactions.tokens/can-create-token-with-minimal-properties-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with minimal properties set.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-create-token-when-token-name-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token when token name is not set.
 - **Then** the request is rejected with status MISSING_TOKEN_NAME.

### `transactions.tokens/cannot-create-token-when-token-symbol-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token when token symbol is not set.
 - **Then** the request is rejected with status MISSING_TOKEN_SYMBOL.

### `transactions.tokens/cannot-create-token-when-token-treasury-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token when token treasury account identifier is not set.
 - **Then** the request is rejected with status INVALID_TREASURY_ACCOUNT_FOR_TOKEN.

### `transactions.tokens/cannot-create-token-when-token-treasury-account-id-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token when token treasury account identifier does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-create-token-when-admin-key-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create token when admin key does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-create-token-with-custom-fees`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with custom fees.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-create-more-than-ten-custom-fees`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create custom fee list with > 10 entries.
 - **Then** the request is rejected with status CUSTOM_FEES_LIST_TOO_LONG.

### `transactions.tokens/can-create-ten-fixed-fees`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create custom fee list with 10 fixed fees.
 - **Then** the operation completes without error.

### `transactions.tokens/can-create-ten-fractional-fees`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create custom fee list with 10 fractional fees.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-create-min-greater-than-max`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create a token with a custom fee where min > max.
 - **Then** the request is rejected with status FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT.

### `transactions.tokens/cannot-create-invalid-fee-collector`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create a token with invalid fee collector account identifier.
 - **Then** the request is rejected with status INVALID_CUSTOM_FEE_COLLECTOR.

### `transactions.tokens/cannot-create-negative-fee`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create a token with a negative custom fee.
 - **Then** the request is rejected with status CUSTOM_FEE_MUST_BE_POSITIVE.

### `transactions.tokens/cannot-create-zero-denominator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create custom fee with 0 denominator.
 - **Then** the request is rejected with status FRACTION_DIVIDES_BY_ZERO.

### `transactions.tokens/can-create-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create NFT.
 - **Then** the operation completes without error.

### `transactions.tokens/can-create-royalty-fee`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create NFT with royalty fee.
 - **Then** the operation completes without error.

### `transactions.tokens/can-create-token-with-minimal-properties-set-auto-renew-account-should-be-automatically-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with minimal properties set and auto Renew Account should be automatically set.
 - **Then** the operation completes without error, the auto renew account is present, and the auto renew account has the expected value.

### `transactions.tokens/can-set-auto-renew-period`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to set auto-renew period when creating token.
 - **Then** the operation completes without error, the token info auto renew account equals the expected operator account identifier, the token info auto renew period equals the expected auto renew period, and the token info expiration time epoch second equals the expected expiration time epoch second.

### `transactions.tokens/can-set-expiration-time`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to set expiration time when creating token.
 - **Then** the operation completes without error and the token info expiration time epoch second equals the expected expiration time epoch second.

### `transactions.tokens/when-transaction-id-is-set-auto-renew-account-id-should-be-equal-to-account-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “auto Renew Account identifier should be equal to Account identifier” behavior.
 - **Then** the operation completes without error and the token info auto renew account equals the expected account identifier.

### `transactions.tokens/can-create-token-with-decimal-adjustment-for-supply-values`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with decimal adjustment for supply values.
 - **Then** the operation completes without error, the token info decimals equals the expected decimals, the token info total supply equals the expected expected initial supply, and the token info max supply equals the expected expected max supply.

### `transactions.tokens/can-create-nft-with-zero-decimals-and-zero-initial-supply`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create NFT with zero decimals and zero initial supply.
 - **Then** the operation completes without error, the token info token type equals the expected token type non fungible unique, the token info decimals equals 0, and the token info total supply equals 0.

### `transactions.tokens/can-create-token-with-different-decimal-precision-values`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token with different decimal precision values.
 - **Then** the operation completes without error, the token info decimals equals the expected decimals, and the token info total supply equals the expected expected supply.

### `transactions.tokens/can-create-token-when-auto-renew-period-is-null`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create token when auto Renew Period is null.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the returned receipt status equals the expected status success, the token identifier is present, and the token info name equals the expected text.

### `transactions.tokens/can-delete-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete token.
 - **Then** the operation completes without error.

### `transactions.tokens/can-delete-token-with-only-admin-key-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete token with only admin key set.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-delete-token-when-admin-key-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete token when admin key does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-delete-token-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to delete token when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/can-associate-account-with-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to dissociate account with token.
 - **Then** the operation completes without error.

### `transactions.tokens/can-execute-token-dissociate-transaction-even-when-token-i-ds-are-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute token dissociate transaction even when token I Ds are not set.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-dissociate-account-with-tokens-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to dissociate account with tokens when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-dissociate-account-when-account-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to dissociate account with tokens when account does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-dissociate-account-from-token-when-account-was-not-associated-with`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to dissociate account from token when account was not associated with.
 - **Then** the request is rejected with status TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.

### `transactions.tokens/token-fee-schedule-update-can-update-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update token fees.
 - **Then** the operation completes without error, the returned information token identifier equals the expected token identifier, the returned information name equals the expected text, and the returned information symbol equals the expected text.

### `transactions.tokens/cannot-update-with-any-other-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update fee schedule with any key other than fee schedule key.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-freeze-account-with-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to freeze account with token.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-freeze-account-on-token-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to freeze account on token when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/cannot-freeze-account-on-token-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to freeze account on token when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-freeze-account-on-token-when-account-was-not-associated-with`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to freeze account on token when account was not associated with.
 - **Then** the request is rejected with status TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.

### `transactions.tokens/can-grant-kyc-account-with-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to grant kyc to account with token.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-grant-kyc-to-account-on-token-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to grant kyc to account on token when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/cannot-grant-kyc-to-account-on-token-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to grant kyc to account on token when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-grant-kyc-to-account-on-token-when-account-was-not-associated-with`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to grant kyc to account on token when account was not associated with.
 - **Then** the request is rejected with status TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.

### `transactions.tokens/can-manually-associate-account-with-fungible-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to manually associate Account with a Fungible Token.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-manually-associate-account-with-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to manually associate Account with NFT.
 - **Then** the operation completes without error.

### `transactions.tokens/can-manually-associate-contract-with-fungible-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to manually associate Contract with a Fungible Token.
 - **Then** the operation completes without error, the contract info contract identifier equals the expected contract identifier, the contract info account identifier is present, and the objects require non null(contract info account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.tokens/can-manually-associate-contract-with-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to manually associate contract with NFT.
 - **Then** the operation completes without error, the contract info contract identifier equals the expected contract identifier, the contract info account identifier is present, and the objects require non null(contract info account identifier) text) is equal to(objects require non null(contract identifier has the expected value.

### `transactions.tokens/can-execute-token-associate-transaction-even-when-token-i-ds-are-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to execute token associate transaction even when token I Ds are not set.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-associate-account-with-tokens-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to manually associate Account with a Token when Account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-associate-account-when-account-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to manually Associate Account with a Token when Account Does Not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-mint-tokens`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to mint tokens.
 - **Then** the operation completes without error and the returned receipt total supply equals the expected 1000000 + 10.

### `transactions.tokens/cannot-mint-more-than-max-supply`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to mint more tokens than max supply.
 - **Then** the request is rejected with status TOKEN_MAX_SUPPLY_REACHED.

### `transactions.tokens/cannot-mint-tokens-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to mint tokens when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/can-mint-tokens-when-amount-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to mint tokens when amount is not set.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.tokens/cannot-mint-tokens-when-supply-key-does-not-sign-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to mint tokens when supply key does not sign transaction.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-mint-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to mint NF Ts.
 - **Then** the operation completes without error and the returned receipt serials size equals 10.

### `transactions.tokens/cannot-mint-nfts-if-metadata-too-big`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to mint NF Ts if metadata too big.
 - **Then** the request is rejected with status METADATA_TOO_LONG.

### `transactions.tokens/can-transfer-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer NF Ts.
 - **Then** the operation completes without error, the account identifier is present, and the token identifier is present.

### `transactions.tokens/cannot-transfer-unowned-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer NF Ts you don't own.
 - **Then** the request is rejected with status SENDER_DOES_NOT_OWN_NFT_SERIAL_NO.

### `transactions.tokens/can-execute-token-pause-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute token pause transaction.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-pause-with-no-token-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to pause with no token identifier.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-flow-for-fungible-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject flow for Fungible Token.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-flow-for-fungible-token-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject flow for Fungible Token (Async).
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-flow-for-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject flow for NFT.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-flow-for-nft-when-rejecting-only-part-of-owned-nf-ts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject flow for NFT when rejecting Only Part Of Owned NF Ts.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-transaction-for-fungible-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for Fungible Token.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-execute-token-reject-transaction-for-nft`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for NFT.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-execute-token-reject-transaction-for-ft-and-nft-in-one-tx`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for FT and NFT in One Tx.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-execute-token-reject-transaction-for-ft-and-nft-when-treasury-receiver-sig-required-is-enabled`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for FT and NFT when Treasury receiver Sig Required is Enabled.
 - **Then** the operation completes without error and the retrieved value equals the expected value.

### `transactions.tokens/can-execute-token-reject-transaction-for-ft-and-nft-when-token-is-frozen`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for FT and NFT when Token is Frozen.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-execute-token-reject-transaction-for-ft-and-nft-when-token-is-paused`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute Token Reject transaction for FT and NFT when Token is Paused.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-remove-allowance-when-executing-token-reject-for-ft-and-nft`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to remove allowance when executing Token Reject transaction for FT and NFT.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-nft-when-using-add-or-set-token-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to reject NFT when executing Token Reject with Add or Set Token identifier.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-when-executing-token-reject-and-duplicating-token-reference`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to reject a Token when executing Token Reject and Duplicating Token Reference.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-when-owner-has-empty-balance`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to reject a Token when Owner Has Empty Balance.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-when-treasury-rejects`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to reject a Token when Treasury Rejects itself.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-with-invalid-signature`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to reject a Token with Invalid Signature.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-when-token-or-nft-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to reject a Token when Token Or NFT identifier is not set.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-reject-token-when-token-reference-list-size-exceeded`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to reject a Token when executing Token Reject and Token Reference List Size Exceeded.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-revoke-kyc-account-with-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to revoke kyc to account with token.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-revoke-kyc-to-account-on-token-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to revoke kyc to account on token when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/cannot-revoke-kyc-to-account-on-token-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to revoke kyc to account on token when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-revoke-kyc-to-account-on-token-when-account-was-not-associated-with`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to revoke kyc to account on token when account was not associated with.
 - **Then** the request is rejected with status TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.

### `transactions.tokens/token-transfer-test`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer tokens.
 - **Then** the operation completes without error, the account identifier is present, and the token identifier is present.

### `transactions.tokens/insufficient-balance-for-fee`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to transfer tokens if balance is insufficient to pay fee.
 - **Then** the request is rejected with status INSUFFICIENT_SENDER_ACCOUNT_BALANCE_FOR_CUSTOM_FEE or INSUFFICIENT_PAYER_BALANCE_FOR_CUSTOM_FEE.

### `transactions.tokens/can-unfreeze-account-with-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to unfreeze account with token.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-unfreeze-account-on-token-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to unfreeze account on token when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/cannot-unfreeze-account-on-token-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to unfreeze account on token when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-unfreeze-account-on-token-when-account-was-not-associated-with`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to unfreeze account on token when account was not associated with.
 - **Then** the request is rejected with status TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.

### `transactions.tokens/can-execute-token-unpause-transaction`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to execute token unpause transaction.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-unpause-with-no-token-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to unpause with no token identifier.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/token-update-can-update-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update token.
 - **Then** the operation completes without error, the returned information token identifier equals the expected token identifier, the returned information name equals the expected text, and the returned information symbol equals the expected text.

### `transactions.tokens/cannot-update-immutable-token`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update immutable token.
 - **Then** the request is rejected with status TOKEN_IS_IMMUTABLE.

### `transactions.tokens/can-update-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a fungible token's metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, and the token info after metadata update metadata equals the expected updated token metadata.

### `transactions.tokens/can-update-non-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a non fungible token's metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, and the token info after metadata update metadata equals the expected updated token metadata.

### `transactions.tokens/can-update-immutable-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update an immutable fungible token's metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, the token info after creation metadata key text) is equal to(metadata key get public key( has the expected value, and the token info after metadata update metadata equals the expected updated token metadata.

### `transactions.tokens/can-update-immutable-non-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update an immutable non fungible token's metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, the token info after creation metadata key text) is equal to(metadata key get public key( has the expected value, and the token info after metadata update metadata equals the expected updated token metadata.

### `transactions.tokens/cannot-update-fungible-token-metadata-when-its-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a fungible token with metadata when it is not set.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/cannot-update-non-fungible-token-metadata-when-its-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a non fungible token with metadata when it is not set.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-erase-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to erase fungible token metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, and the token info after setting empty metadata metadata equals the expected empty token metadata.

### `transactions.tokens/can-erase-non-fungible-token-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to erase non fungible token metadata.
 - **Then** the operation completes without error, the token info after creation metadata equals the expected initial token metadata, and the token info after setting empty metadata metadata equals the expected empty token metadata.

### `transactions.tokens/cannot-update-fungible-token-metadata-when-transaction-is-not-signed-with-metadata-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update a fungible token with metadata when transaction is not signed with an admin or a metadata key.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-non-fungible-token-metadata-when-transaction-is-not-signed-with-metadata-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update a non fungible token with metadata when transaction is not signed with an admin or a metadata key.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-fungible-token-metadata-when-metadata-key-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a fungible token with metadata when admin and metadata keys are not set.
 - **Then** the request is rejected with status TOKEN_IS_IMMUTABLE.

### `transactions.tokens/cannot-update-non-fungible-token-metadata-when-metadata-key-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update a non fungible token with metadata when admin and metadata keys are not set.
 - **Then** the request is rejected with status TOKEN_IS_IMMUTABLE.

### `transactions.tokens/can-make-token-immutable-when-updating-keys-to-empty-key-list-signing-with-admin-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to make a token immutable when updating keys to an empty Key List, signing with an Admin Key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the operation completes without error, the token info before update admin key text) is equal to(admin key get public key( has the expected value, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, and the token info before update kyc key text) is equal to(kyc key get public key( has the expected value.

### `transactions.tokens/can-remove-all-lower-privilege-keys-when-updating-keys-to-empty-key-list-signing-with-admin-key-with-key-verification-set-to-full-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to remove all of token’s lower-privilege keys when updating keys to an empty Key List, signing with an Admin Key, and setting the key verification mode to FULL_VALIDATION.
 - **Then** the operation completes without error, the token info before update admin key text) is equal to(admin key get public key( has the expected value, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, and the token info before update kyc key text) is equal to(kyc key get public key( has the expected value.

### `transactions.tokens/can-update-all-lower-privilege-keys-to-unusable-key-when-signing-with-admin-key-with-key-verification-set-to-full-validation-and-then-revert-previous-keys`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys to an unusable key (i.e. all-zeros key), when signing with an Admin Key, and setting the key verification mode to FULL_VALIDATION, and then revert previous keys.
 - **Then** the operation completes without error, the token info before update admin key text) is equal to(admin key get public key( has the expected value, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, and the token info before update kyc key text) is equal to(kyc key get public key( has the expected value.

### `transactions.tokens/can-update-all-lower-privilege-keys-when-signing-with-admin-key-and-new-lower-privilege-key-with-key-verification-set-to-full-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys when signing with an Admin Key and new respective lower-privilege key, and setting key verification mode to FULL_VALIDATION.
 - **Then** the operation completes without error, the token info before update admin key text) is equal to(admin key get public key( has the expected value, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, and the token info before update kyc key text) is equal to(kyc key get public key( has the expected value.

### `transactions.tokens/cannot-make-token-immutable-when-updating-keys-to-empty-key-list-signing-with-different-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to make a token immutable when updating keys to an empty Key List, signing with a key that is different from an Admin Key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-make-token-immutable-when-updating-keys-to-unusable-key-signing-with-different-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to make a token immutable when updating keys to an unusable key (i.e. all-zeros key), signing with a key that is different from an Admin Key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-admin-key-to-unusable-key-signing-with-admin-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update the Admin Key to an unusable key (i.e. all-zeros key), signing with an Admin Key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-update-all-lower-privilege-keys-to-unusable-key-when-signing-with-respective-lower-privilege-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys to an unusable key (i.e. all-zeros key), when signing with a respective lower-privilege key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the operation completes without error, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, the token info before update kyc key text) is equal to(kyc key get public key( has the expected value, and the token info before update freeze key text) is equal to(freeze key get public key( has the expected value.

### `transactions.tokens/can-update-all-lower-privilege-keys-when-signing-with-old-lower-privilege-key-and-new-lower-privilege-key-with-key-verification-set-to-ful-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys when signing with an old lower-privilege key and with a new lower-privilege key, and setting key verification mode to FULL_VALIDATION.
 - **Then** the operation completes without error, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, the token info before update kyc key text) is equal to(kyc key get public key( has the expected value, and the token info before update freeze key text) is equal to(freeze key get public key( has the expected value.

### `transactions.tokens/can-update-all-lower-privilege-keys-when-signing-only-with-old-lower-privilege-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys when signing ONLY with an old lower-privilege key, and setting key verification mode to NO_VALIDATION.
 - **Then** the operation completes without error, the token info before update wipe key text) is equal to(wipe key get public key( has the expected value, the token info before update kyc key text) is equal to(kyc key get public key( has the expected value, and the token info before update freeze key text) is equal to(freeze key get public key( has the expected value.

### `transactions.tokens/cannot-remove-all-lower-privilege-keys-when-updating-keys-to-empty-key-list-signing-with-respective-lower-privilege-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to remove all of token’s lower-privilege keys when updating them to an empty Key List, signing with a respective lower-privilege key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status TOKEN_IS_IMMUTABLE.

### `transactions.tokens/cannot-update-all-lower-privilege-keys-to-unusable-key-when-signing-with-different-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys to an unusable key (i.e. all-zeros key), when signing with a key that is different from a respective lower-privilege key, and setting the key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-all-lower-privilege-keys-to-unusable-key-when-signing-only-with-old-respective-lower-privilege-key-with-key-verification-set-to-full-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys to an unusable key (i.e. all-zeros key), when signing ONLY with an old respective lower-privilege key, and setting the key verification mode to FULL_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-all-lower-privilege-keys-to-unusable-key-when-signing-with-old-respective-lower-privilege-key-and-new-respective-lower-privilege-key-with-key-verification-set-to-full-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys to an unusable key (i.e. all-zeros key), when signing with an old respective lower-privilege key and new respective lower-privilege key, and setting the key verification mode to FULL_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-all-lower-privilege-keys-when-signing-only-with-old-respective-lower-privilege-key-with-key-verification-set-to-full-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys, when signing ONLY with an old respective lower-privilege key, and setting the key verification mode to FULL_VALIDATION.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-all-lower-privilege-keys-when-updating-keys-to-structurally-invalid-keys-signing-only-with-old-respective-lower-privilege-key-with-key-verification-set-to-no-validation`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update all of token’s lower-privilege keys when updating them to a keys with an invalid structure and signing with an old respective lower-privilege and setting key verification mode to NO_VALIDATION.
 - **Then** the request is rejected with status INVALID_WIPE_KEY or INVALID_KYC_KEY or INVALID_FREEZE_KEY or INVALID_PAUSE_KEY or INVALID_SUPPLY_KEY or INVALID_CUSTOM_FEE_SCHEDULE_KEY or INVALID_METADATA_KEY.

### `transactions.tokens/can-update-nft-metadata-of-entire-collection`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update the metadata of the entire NFT collection.
 - **Then** the operation completes without error, the metadata list after mint to array equals the expected initial metadata list to array, and the metadata list after update to array equals the expected updated metadata list to array.

### `transactions.tokens/can-update-nft-metadata-of-part-of-collection`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update the metadata of a part of the NFT collection.
 - **Then** the operation completes without error, the metadata list after mint to array equals the expected initial metadata list to array, the metadata list after update to array equals the expected updated metadata list to array, and the metadata list to array) is equal to( initial metadata list sub list(nft count / 2, nft count has the expected value.

### `transactions.tokens/cannot-update-nft-metadata-when-its-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update NF Ts metadata when it is not set.
 - **Then** the request is rejected with the expected error.

### `transactions.tokens/can-erase-nf-ts-metadata`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to erase NF Ts metadata.
 - **Then** the operation completes without error, the metadata list after mint to array equals the expected initial metadata list to array, and the metadata list after update to array equals the expected empty metadata list to array.

### `transactions.tokens/cannot-update-nft-metadata-when-transaction-is-not-signed-with-metadata-key`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update NFT metadata when transaction is not signed with metadata key.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/cannot-update-nft-metadata-when-metadata-key-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update NFT metadata when metadata key is not set.
 - **Then** the request is rejected with status INVALID_SIGNATURE.

### `transactions.tokens/can-wipe-accounts-balance`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts balance.
 - **Then** the operation completes without error.

### `transactions.tokens/can-wipe-accounts-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts NF Ts.
 - **Then** the operation completes without error.

### `transactions.tokens/cannot-wipe-accounts-nfts-if-not-owned`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts NF Ts if the account doesn't own them.
 - **Then** the request is rejected with status ACCOUNT_DOES_NOT_OWN_WIPED_NFT.

### `transactions.tokens/cannot-wipe-accounts-balance-when-account-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts balance when account identifier is not set.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `transactions.tokens/cannot-wipe-accounts-balance-when-token-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts balance when token identifier is not set.
 - **Then** the request is rejected with status INVALID_TOKEN_ID.

### `transactions.tokens/can-wipe-accounts-balance-when-amount-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to wipe accounts balance when amount is not set.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

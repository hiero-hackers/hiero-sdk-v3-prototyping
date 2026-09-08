# Queries / Accounts

## Scenarios

### `queries.accounts/can-connect-to-previewnet-with-tls`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to connect to a consensus node with TLS.
 - **Then** the operation completes without error, the entry key ends with :50212, and at least one connection succeeds.

### `queries.accounts/can-connect-to-testnet-with-tls`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to connect to a consensus node using TLS.
 - **Then** the operation completes without error, the entry key ends with :50212, and at least one connection succeeds.

### `queries.accounts/can-connect-to-mainnet-with-tls`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to connect to a consensus node using TLS.
 - **Then** the operation completes without error, the entry key ends with :50212, and at least one connection succeeds.

### `queries.accounts/cannot-connect-to-previewnet-when-network-name-is-null-and-certificate-verification-is-enabled`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to connect without a network name while certificate verification is enabled.
 - **Then** the request is rejected with the expected error.

### `queries.accounts/can-fetch-balance-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to fetch balance for operator account.
 - **Then** the operation completes without error and the returned balance HBAR balance value in tinybars is greater than zero.

### `queries.accounts/get-cost-balance-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to fetch cost for the query.
 - **Then** the operation completes without error, the acc balance HBAR balance value in tinybars is greater than zero, and the cost value in tinybars equals 0.

### `queries.accounts/get-cost-big-max-balance-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to fetch cost for the query, big max set.
 - **Then** the operation completes without error and the acc balance HBAR balance value in tinybars is greater than zero.

### `queries.accounts/get-cost-small-max-balance-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to fetch cost for the query, very small max set.
 - **Then** the operation completes without error and the acc balance HBAR balance value in tinybars is greater than zero.

### `queries.accounts/can-not-fetch-balance-for-invalid-account-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to fetch balance for invalid account identifier.
 - **Then** the request is rejected with status INVALID_ACCOUNT_ID.

### `queries.accounts/can-fetch-token-balances-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to fetch token balances for operator account.
 - **Then** the operation completes without error, the retrieved value equals the expected value, and the query text is not empty.

### `queries.accounts/can-populate-account-id-num-sync`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to populate Account identifier num from mirror node (using synchronous operation).
 - **Then** the operation completes without error and the new account identifier num equals the expected account identifier num.

### `queries.accounts/can-populate-account-id-num-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to populate Account identifier num from mirror node (using asynchronous operation).
 - **Then** the operation completes without error and the new account identifier num equals the expected account identifier num.

### `queries.accounts/can-populate-account-id-evm-address-sync`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to populate Account identifier EVM address from mirror node (using synchronous operation).
 - **Then** the operation completes without error and the evm address account evm address equals the expected account identifier evm address.

### `queries.accounts/can-populate-account-id-evm-address-async`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to populate Account identifier EVM address from mirror node (using asynchronous operation).
 - **Then** the operation completes without error and the evm address account evm address equals the expected account identifier evm address.

### `queries.accounts/can-query-account-info-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query account info for operator account.
 - **Then** the operation completes without error, the returned information account identifier equals the expected operator account identifier, the returned information is deleted is false, and the returned information key equals the expected operator key.

### `queries.accounts/get-cost-account-info-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to get cost for account info query.
 - **Then** the operation completes without error and the returned account information account identifier equals the expected operator account identifier.

### `queries.accounts/get-cost-big-max-account-info-for-client-operator`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to get cost for account info query, with a bix max.
 - **Then** the operation completes without error and the returned account information account identifier equals the expected operator account identifier.

### `queries.accounts/get-cost-small-max-account-info-for-client-operator`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to get cost for account info query, with a small max.
 - **Then** the request is rejected with the expected error.

### `queries.accounts/get-cost-insufficient-tx-fee-account-info-for-client-operator`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.accounts/account-info-flow-verify-functions`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client exercises the “account Info Flow.verify functions” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.accounts/can-query-account-records`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to query account records.
 - **Then** the operation completes without error and the records is not empty.

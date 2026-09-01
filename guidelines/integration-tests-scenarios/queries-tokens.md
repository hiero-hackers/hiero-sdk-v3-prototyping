# Queries / Tokens

## Scenarios

### `queries.tokens/can-query-token-info-when-all-keys-are-different`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to query token info when all keys are different.
 - **Then** the operation completes without error, the returned information token identifier equals the expected token identifier, the returned information name equals the expected text, and the returned information symbol equals the expected text.

### `queries.tokens/can-query-token-info-when-token-is-created-with-minimal-properties`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query token with minimal properties.
 - **Then** the operation completes without error, the returned information token identifier equals the expected token identifier, the returned information name equals the expected text, and the returned information symbol equals the expected text.

### `queries.tokens/can-query-nfts`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT.
 - **Then** the operation completes without error, the mint receipt serials size equals 10, the returned information token identifier equals the expected token identifier, and the returned information name equals the expected text.

### `queries.tokens/get-cost-query-token-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “get cost of token info query” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.tokens/get-cost-big-max-query-token-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “get cost of token info query, with big max” behavior.
 - **Then** the operation completes without error and the expected observable result is returned.

### `queries.tokens/get-cost-small-max-token-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query token info when all keys are different.
 - **Then** the request is rejected with the expected error.

### `queries.tokens/get-cost-insufficient-tx-fee-query-token-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “throws insufficient transaction fee error” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.tokens/can-query-nft-info-by-nft-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT info by NFT identifier.
 - **Then** the operation completes without error, the nft infos size equals 1, and the retrieved value equals the expected value.

### `queries.tokens/cannot-query-nft-info-by-invalid-nft-id`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT info by invalid NFT identifier.
 - **Then** the request is rejected with status INVALID_NFT_ID.

### `queries.tokens/cannot-query-nft-info-by-invalid-serial-number`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT info by invalid NFT identifier Serial Number.
 - **Then** the request is rejected with status INVALID_TOKEN_NFT_SERIAL_NUMBER.

### `queries.tokens/can-query-nft-info-by-account-id`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT info by Account identifier.
 - **Then** the operation completes without error, the nft infos size equals 10, the returned information nft identifier token identifier equals the expected token identifier, and the serials remove(returned information nft identifier serial) is true.

### `queries.tokens/can-query-nft-info-by-token-id`

> **Conformance:** Deferred — Deferral rationale not recorded

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query NFT info by Token identifier.
 - **Then** the operation completes without error, the nft infos size equals 10, the returned information nft identifier token identifier equals the expected token identifier, and the serials remove(returned information nft identifier serial) is true.

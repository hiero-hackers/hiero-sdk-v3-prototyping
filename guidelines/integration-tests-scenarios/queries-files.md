# Queries / Files

## Scenarios

### `queries.files/can-fetch-fee-schedules`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client exercises the “fee Schedules (Current And Next Fee Schedule) is fetched and parsed from file 0.0.111” behavior.
 - **Then** the operation completes without error and the fee schedules current is present.

### `queries.files/can-query-file-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query file contents.
 - **Then** the operation completes without error and the contents to string utf8 equals the expected text.

### `queries.files/can-query-empty-file-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query empty file contents.
 - **Then** the operation completes without error and the contents size equals 0.

### `queries.files/cannot-query-file-contents-when-file-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to query file contents when file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `queries.files/get-cost-big-max-query-file-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error and the contents to string utf8 equals the expected text.

### `queries.files/get-cost-small-max-query-file-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/get-cost-insufficient-tx-fee-query-file-contents`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/can-query-file-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to query file info.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `queries.files/can-query-file-info-with-no-admin-key-or-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to query file info with no admin key or contents.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `queries.files/get-cost-big-max-query-file-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to get cost, even with a big max.
 - **Then** the operation completes without error.

### `queries.files/get-cost-small-max-query-file-info`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “error, max is smaller than set payment.” behavior.
 - **Then** the request is rejected with the expected error.

### `queries.files/get-cost-insufficient-tx-fee-query-file-info`

> **Conformance:** Deferred — Unavailable in the Solo environment

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client exercises the “insufficient tx fee error.” behavior.
 - **Then** the request is rejected with the expected error.

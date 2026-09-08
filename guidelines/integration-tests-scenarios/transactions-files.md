# Transactions / Files

## Scenarios

### `transactions.files/can-append-to-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append to file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-append-large-contents-to-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append large contents to file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-append-large-contents-to-file-despite-expiration`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append large contents to file despite TRANSACTION_EXPIRATION response codes.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-file-append-sign-for-multiple-nodes`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “file Append with add Signature - can sign for multiple nodes with large content” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the file identifier is present, the signable body list is not empty, and the append receipt status equals the expected status success.

### `transactions.files/can-create-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-create-file-with-no-contents`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create file with no contents.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `transactions.files/can-create-file-with-no-keys`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create file with no keys.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `transactions.files/can-delete-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/cannot-delete-immutable-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete immutable file.
 - **Then** the request is rejected with status UNAUTHORIZED.

### `transactions.files/can-update-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/cannot-update-immutable-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update immutable file.
 - **Then** the request is rejected with status UNAUTHORIZED.

### `transactions.files/cannot-update-file-when-file-id-is-not-set`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update file when file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `transactions.files/can-update-fee-schedule-file`

> **Conformance:** Required

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update fee schedule file.
 - **Then** the operation completes without error and the returned receipt status equals the expected status fee schedule file part uploaded.

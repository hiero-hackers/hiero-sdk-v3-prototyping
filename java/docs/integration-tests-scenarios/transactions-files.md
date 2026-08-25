# Transactions / Files

## Scenarios

### `transactions.files/can-append-to-file`

> **Implementation:** `FileAppendIntegrationTest.canAppendToFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileAppendIntegrationTest.java:14`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append to file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-append-large-contents-to-file`

> **Implementation:** `FileAppendIntegrationTest.canAppendLargeContentsToFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileAppendIntegrationTest.java:57`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append large contents to file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-append-large-contents-to-file-despite-expiration`

> **Implementation:** `FileAppendIntegrationTest.canAppendLargeContentsToFileDespiteExpiration`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileAppendIntegrationTest.java:111`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to append large contents to file despite TRANSACTION_EXPIRATION response codes.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-file-append-sign-for-multiple-nodes`

> **Implementation:** `FileAppendIntegrationTest.canFileAppendSignForMultipleNodes`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileAppendIntegrationTest.java:172`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “file Append with add Signature - can sign for multiple nodes with large content” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, the file identifier is present, the signable body list is not empty, and the append receipt status equals the expected status success.

### `transactions.files/can-create-file`

> **Implementation:** `FileCreateIntegrationTest.canCreateFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileCreateIntegrationTest.java:15`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/can-create-file-with-no-contents`

> **Implementation:** `FileCreateIntegrationTest.canCreateFileWithNoContents`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileCreateIntegrationTest.java:42`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with fresh keys generated for this scenario.
 - **When** the client attempts to create file with no contents.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `transactions.files/can-create-file-with-no-keys`

> **Implementation:** `FileCreateIntegrationTest.canCreateFileWithNoKeys`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileCreateIntegrationTest.java:68`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to create file with no keys.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 0, and the returned information is deleted is false.

### `transactions.files/can-delete-file`

> **Implementation:** `FileDeleteIntegrationTest.canDeleteFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileDeleteIntegrationTest.java:18`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to delete file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/cannot-delete-immutable-file`

> **Implementation:** `FileDeleteIntegrationTest.cannotDeleteImmutableFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileDeleteIntegrationTest.java:45`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to delete immutable file.
 - **Then** the request is rejected with status UNAUTHORIZED.

### `transactions.files/can-update-file`

> **Implementation:** `FileUpdateIntegrationTest.canUpdateFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileUpdateIntegrationTest.java:23`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client attempts to update file.
 - **Then** the operation completes without error, the returned information file identifier equals the expected file identifier, the returned information size equals 28, and the returned information is deleted is false.

### `transactions.files/cannot-update-immutable-file`

> **Implementation:** `FileUpdateIntegrationTest.cannotUpdateImmutableFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileUpdateIntegrationTest.java:65`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently.
 - **When** the client attempts to update immutable file.
 - **Then** the request is rejected with status UNAUTHORIZED.

### `transactions.files/cannot-update-file-when-file-id-is-not-set`

> **Implementation:** `FileUpdateIntegrationTest.cannotUpdateFileWhenFileIDIsNotSet`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileUpdateIntegrationTest.java:95`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update file when file identifier is not set.
 - **Then** the request is rejected with status INVALID_FILE_ID.

### `transactions.files/can-update-fee-schedule-file`

> **Implementation:** `FileUpdateIntegrationTest.canUpdateFeeScheduleFile`  
> **Source:** `sdk/src/testIntegration/java/com/hedera/hashgraph/sdk/test/integration/FileUpdateIntegrationTest.java:111`
> **Status:** Runnable

 - **Given** a client connected to a Solo network with a funded operator account.
 - **When** the client attempts to update fee schedule file.
 - **Then** the operation completes without error and the returned receipt status equals the expected status fee schedule file part uploaded.

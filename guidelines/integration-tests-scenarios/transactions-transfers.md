# Transactions / Transfers

## Scenarios

### `transactions.transfers/transfer-with-pre-transaction-allowance-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account has a pre-transaction allowance hook configured, when a Transfer Transaction attempts to transfer HBAR from that account, then the hook is called before the transfer and approves the transaction” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the transfer receipt status equals the expected status success.

### `transactions.transfers/multiple-accounts-hooks-must-all-approve`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given multiple accounts in a transfer have different allowance hooks, when a Transfer Transaction involves all accounts, then each account's respective hooks are called and must all approve for the transaction to succeed” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.transfers/transfer-with-pre-post-transaction-allowance-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account has a pre/post-transaction allowance hook configured, when a successful HBAR transfer occurs, then the hook is called both before and after the transfer execution” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.transfers/fungible-token-transfer-with-allowance-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account has an allowance hook for token transfers, when a Transfer Transaction includes token transfers from that account, then the hook validates the token allowance and approves valid transfers” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the resp status equals the expected status success.

### `transactions.transfers/nft-transfer-with-allowance-hook-succeeds`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given an account has an NFT allowance hook configured, when a Transfer Transaction attempts to transfer an NFT from that account, then the hook validates the NFT allowance and processes the transfer accordingly” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

### `transactions.transfers/sender-and-receiver-hooks-execute-for-hbar-transfer`

> **Conformance:** Deferred — Requires EVM Hooks support

 - **Given** a client connected to a Solo network with a funded operator account, with the required prerequisite entities created independently and fresh keys generated for this scenario.
 - **When** the client exercises the “given a sender account has an allowance hook and receiver account has a different allowance hook, when a Transfer Transaction occurs between them, then both sender and receiver hooks are executed in the correct order” behavior.
 - **Then** the operation completes without error, the receipt reports SUCCESS, and the returned receipt status equals the expected status success.

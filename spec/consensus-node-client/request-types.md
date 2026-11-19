# Request Type API

This namespace defines the request types of the consensus node: the transaction and query types of the protocol.

## Description

Every transaction and query the consensus node handles has a request type, identified by its numeric code. The
network names request types where it reports something per type of request, for example the prices of its fee
schedule (`FeeScheduleQuery`). `BasicRequestType` names the request types of the built-in services of the consensus
node.

The set of request types is open. Every request type the SDK knows is registered as a `RequestTypeSupport`
(`consensusnode.transactions.spi`): the SDK registers the one of `BasicRequestType`, a module or a custom service its
own request types (the CLPR module, for example, `ClprRequestType`). `getRequestType(code)` returns the request type
of a code.

## Design Notes

- **Own namespace at the bottom of the consensus node client.** Like `consensusnode.status`: request types concern
  transactions and queries alike, and the SPI that registers them must not depend on the namespaces that use them.
- **`RequestType` instead of `HederaFunctionality`.** The protocol's name carries the name of one network; the V2
  SDKs call it `RequestType` as well.
- **Every code of the protocol.** `BasicRequestType` lists every value of `HederaFunctionality` in
  `tooling/protobuf/consensus-node/services/basic_types.proto` except the request types of the CLPR service (117 to
  128), which the module `consensus-node-clpr` names (`ClprRequestType`), in its order, converted to
  `UPPER_SNAKE_CASE`, with the first sentence of the protocol's comment; values the protocol deprecates are
  `@@deprecated`. Like `BasicTransactionStatus`, the list is taken over mechanically and has to be taken over again
  when the protobuf definitions are updated.

## API Schema

```
namespace consensusnode.requestTypes

// A type of request of the protocol: a transaction or query type, identified by a numeric code. The set of request
// types is open: optional and custom services on the consensus node can define their own.
abstraction RequestType {
    @@immutable code: int32 // the numeric code of the request type in the protocol
}

// The request types of the built-in services of the consensus node: every value of the protocol's
// HederaFunctionality except those of the CLPR service, with their name and code.
enum BasicRequestType(code: int32) extends RequestType {
    // Unused - The first value is unused because this default value is ambiguous with an "unset" value and therefore
    // should not be used.
    NONE(0)
    // Transfer tokens among accounts.
    CRYPTO_TRANSFER(1)
    // Update an account.
    CRYPTO_UPDATE(2)
    // Delete an account.
    CRYPTO_DELETE(3)
    // Add a livehash to an account
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated CRYPTO_ADD_LIVE_HASH(4)
    // Delete a livehash from an account
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated CRYPTO_DELETE_LIVE_HASH(5)
    // Execute a smart contract call.
    CONTRACT_CALL(6)
    // Create a smart contract.
    CONTRACT_CREATE(7)
    // Update a smart contract.
    CONTRACT_UPDATE(8)
    // Create a "file" stored in the ledger.
    FILE_CREATE(9)
    // Append data to a "file" stored in the ledger.
    FILE_APPEND(10)
    // Update a "file" stored in the ledger.
    FILE_UPDATE(11)
    // Delete a "file" stored in the ledger.
    FILE_DELETE(12)
    // Get the balance for an account.
    CRYPTO_GET_ACCOUNT_BALANCE(13)
    // Get a full account record.
    CRYPTO_GET_ACCOUNT_RECORDS(14)
    // Get information about a token.
    CRYPTO_GET_INFO(15)
    // Execute a local smart contract call.
    CONTRACT_CALL_LOCAL(16)
    // Get information about a smart contract.
    CONTRACT_GET_INFO(17)
    // Get the compiled bytecode that implements a smart contract.
    CONTRACT_GET_BYTECODE(18)
    // Get a smart contract record by reference to the solidity ID.
    GET_BY_SOLIDITY_ID(19)
    // Get a smart contract by reference to the contract key.
    GET_BY_KEY(20)
    // Get the live hash for an account
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated CRYPTO_GET_LIVE_HASH(21)
    // Get the accounts proxy staking to a given account.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated CRYPTO_GET_STAKERS(22)
    // Get the contents of a "file" stored in the ledger.
    FILE_GET_CONTENTS(23)
    // Get the metadata for a "file" stored in the ledger.
    FILE_GET_INFO(24)
    // Get transaction record(s) for a specified transaction ID.
    TRANSACTION_GET_RECORD(25)
    // Get all transaction records for a specified contract ID in the past 24 hours.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated CONTRACT_GET_RECORDS(26)
    // Create a new account
    CRYPTO_CREATE(27)
    // Delete a "system" "file" stored in the ledger.
    SYSTEM_DELETE(28)
    // Undo the delete of a "system" "file" stored in the ledger.
    SYSTEM_UNDELETE(29)
    // Delete a smart contract
    CONTRACT_DELETE(30)
    // Stop all processing and "freeze" the entire network.
    FREEZE(31)
    // Create a Transaction Record.
    CREATE_TRANSACTION_RECORD(32)
    // Auto-renew an account.
    CRYPTO_ACCOUNT_AUTO_RENEW(33)
    // Auto-renew a smart contract.
    CONTRACT_AUTO_RENEW(34)
    // Get version information for the ledger.
    GET_VERSION_INFO(35)
    // Get a receipt for a specified transaction ID.
    TRANSACTION_GET_RECEIPT(36)
    // Create a topic for the Hedera Consensus Service (HCS).
    CONSENSUS_CREATE_TOPIC(50)
    // Update an HCS topic.
    CONSENSUS_UPDATE_TOPIC(51)
    // Delete an HCS topic.
    CONSENSUS_DELETE_TOPIC(52)
    // Get metadata (information) for an HCS topic.
    CONSENSUS_GET_TOPIC_INFO(53)
    // Publish a message to an HCS topic.
    CONSENSUS_SUBMIT_MESSAGE(54)
    // Submit a transaction, bypassing intake checking.
    UNCHECKED_SUBMIT(55)
    // Create a token for the Hedera Token Service (HTS).
    TOKEN_CREATE(56)
    // Get metadata (information) for an HTS token.
    TOKEN_GET_INFO(58)
    // Freeze a specific account with respect to a specific HTS token.
    TOKEN_FREEZE_ACCOUNT(59)
    // Remove a "freeze" from an account with respect to a specific HTS token.
    TOKEN_UNFREEZE_ACCOUNT(60)
    // Grant KYC status to an account for a specific HTS token.
    TOKEN_GRANT_KYC_TO_ACCOUNT(61)
    // Revoke KYC status from an account for a specific HTS token.
    TOKEN_REVOKE_KYC_FROM_ACCOUNT(62)
    // Delete a specific HTS token.
    TOKEN_DELETE(63)
    // Update a specific HTS token.
    TOKEN_UPDATE(64)
    // Mint HTS token amounts to the treasury account for that token.
    TOKEN_MINT(65)
    // Burn HTS token amounts from the treasury account for that token.
    TOKEN_BURN(66)
    // Wipe all amounts for a specific HTS token from a specified account.
    TOKEN_ACCOUNT_WIPE(67)
    // Associate a specific HTS token to an account.
    TOKEN_ASSOCIATE_TO_ACCOUNT(68)
    // Dissociate a specific HTS token from an account.
    TOKEN_DISSOCIATE_FROM_ACCOUNT(69)
    // Create a scheduled transaction
    SCHEDULE_CREATE(70)
    // Delete a scheduled transaction
    SCHEDULE_DELETE(71)
    // Sign a scheduled transaction
    SCHEDULE_SIGN(72)
    // Get metadata (information) for a scheduled transaction
    SCHEDULE_GET_INFO(73)
    // Get NFT metadata (information) for a range of NFTs associated to a specific non-fungible/unique HTS token and
    // owned by a specific account.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated TOKEN_GET_ACCOUNT_NFT_INFOS(74)
    // Get metadata (information) for a specific NFT identified by token and serial number.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated TOKEN_GET_NFT_INFO(75)
    // Get NFT metadata (information) for a range of NFTs associated to a specific non-fungible/unique HTS token.
    TOKEN_GET_NFT_INFOS(76)
    // Update a token's custom fee schedule.
    TOKEN_FEE_SCHEDULE_UPDATE(77)
    // Get execution time(s) for one or more "recent" TransactionIDs.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated NETWORK_GET_EXECUTION_TIME(78)
    // Pause a specific HTS token
    TOKEN_PAUSE(79)
    // Unpause a paused HTS token.
    TOKEN_UNPAUSE(80)
    // Approve an allowance for a spender relative to the owner account, which MUST sign the transaction.
    CRYPTO_APPROVE_ALLOWANCE(81)
    // Delete (unapprove) an allowance previously approved for the owner account.
    CRYPTO_DELETE_ALLOWANCE(82)
    // Get all the information about an account, including balance and allowances.
    GET_ACCOUNT_DETAILS(83)
    // Perform an Ethereum (EVM) transaction.
    ETHEREUM_TRANSACTION(84)
    // Used to indicate when the network has updated the staking information at the end of a staking period and to
    // indicate a new staking period has started.
    NODE_STAKE_UPDATE(85)
    // Generate and return a pseudorandom number based on network state.
    UTIL_PRNG(86)
    // Get a record for a "recent" transaction.
    //
    // Deprecated by the protocol (`[deprecated = true]` in `basic_types.proto`); the network no longer offers it.
    @@deprecated TRANSACTION_GET_FAST_RECORD(87)
    // Update the metadata of one or more NFT's of a specific token type.
    TOKEN_UPDATE_NFTS(88)
    // Create a node
    NODE_CREATE(89)
    // Update a node
    NODE_UPDATE(90)
    // Delete a node
    NODE_DELETE(91)
    // Transfer one or more token balances held by the requesting account to the treasury for each token type.
    TOKEN_REJECT(92)
    // Airdrop one or more tokens to one or more accounts.
    TOKEN_AIRDROP(93)
    // Remove one or more pending airdrops from state on behalf of the sender(s) for each airdrop.
    TOKEN_CANCEL_AIRDROP(94)
    // Claim one or more pending airdrops
    TOKEN_CLAIM_AIRDROP(95)
    // Submit a signature of a state root hash gossiped to other nodes
    STATE_SIGNATURE_TRANSACTION(100)
    // Publish a hinTS key to the network.
    HINTS_KEY_PUBLICATION(101)
    // Vote for a particular preprocessing output of a hinTS construction.
    HINTS_PREPROCESSING_VOTE(102)
    // Sign a partial signature for the active hinTS construction.
    HINTS_PARTIAL_SIGNATURE(103)
    // Sign a particular history assembly.
    HISTORY_ASSEMBLY_SIGNATURE(104)
    // Publish a roster history proof key to the network.
    HISTORY_PROOF_KEY_PUBLICATION(105)
    // Vote for a particular history proof.
    HISTORY_PROOF_VOTE(106)
    // Publish a random CRS to the network.
    CRS_PUBLICATION(107)
    // Submit a batch of transactions to run atomically
    ATOMIC_BATCH(108)
    // (DEPRECATED) Remove once no production throttle assets reference it.
    LAMBDA_S_STORE(109)
    // (Internal-only) Dispatch a hook action.
    HOOK_DISPATCH(110)
    // Update one or more storage slots in an EVM hook.
    HOOK_STORE(111)
    // (Internal-only) Publish a new ledger id and chain-of-trust key.
    LEDGER_ID_PUBLICATION(112)
    // Create a registered node
    REGISTERED_NODE_CREATE(113)
    // Update a registered node
    REGISTERED_NODE_UPDATE(114)
    // Delete a registered node
    REGISTERED_NODE_DELETE(115)
    // Vote on wrapped record migration hash state.
    MIGRATION_ROOT_HASH_VOTE(116)
}
```

## Testing

```tests
// Source: HederaFunctionality of the HAPI (basic_types.proto).
@@testId("the-basic-request-types-have-the-codes-of-the-protocol")
test "the basic request types have the codes of the protocol" {
    // when / then
    assertEquals(expected: 0, actual: BasicRequestType.NONE.code)
    assertEquals(expected: 1, actual: BasicRequestType.CRYPTO_TRANSFER.code)
    assertEquals(expected: 23, actual: BasicRequestType.FILE_GET_CONTENTS.code)
    assertEquals(expected: 27, actual: BasicRequestType.CRYPTO_CREATE.code)
    assertEquals(expected: 50, actual: BasicRequestType.CONSENSUS_CREATE_TOPIC.code)
    assertEquals(expected: 56, actual: BasicRequestType.TOKEN_CREATE.code)
    assertEquals(expected: 108, actual: BasicRequestType.ATOMIC_BATCH.code)
    assertEquals(expected: 116, actual: BasicRequestType.MIGRATION_ROOT_HASH_VOTE.code)
}
```

## Questions & Comments

- **Internal request types.** `HederaFunctionality` also lists types no client sends (`CREATE_TRANSACTION_RECORD`,
  `STATE_SIGNATURE_TRANSACTION`, the hinTS and history proof types, ...). They are kept so the list stays a mechanical
  copy of the protocol; whether `BasicRequestType` should name only the request types a client can send is open.
- **A request type per `TransactionSupport`?** A `TransactionSupport` describes one transaction type, which is one
  request type of the protocol. It could name its `RequestType`, so that the request types of a custom service need no
  separate support. Queries have no support, though. — open

# Mirror Node Contract Query API

## Description

Query smart contracts and their executions from the Mirror Node. Use the `ContractRepository` (available as
`MirrorNodeClient.contracts`) to

- look up a single `Contract` by its `ContractId` or page through all contracts (`findAll`, `findById`),
- page through the results of contract executions, of one contract (`findResults`) or of all contracts
  (`findAllResults`), and look up a single `ContractResult` by the contract and its consensus time (`findResult`) or by
  the transaction id or Ethereum transaction hash of its transaction (`findResultByTransaction`),
- read the storage slots of a contract (`findState`), the log events it emitted (`findLogs`, `findAllLogs`), the
  actions (calls) of a transaction (`findActions`) and the opcode trace of a transaction (`findOpcodes`),
- and simulate a call of a contract without submitting a transaction or estimate its gas (`call`).

A `Contract` describes the contract entity: its keys, auto-renew settings, memo, EVM address, lifecycle timestamps
and, if available, its bytecode. Data of the EVM - bytecode, call data, results, hashes, storage slots, log topics -
is given as hexadecimal text with the prefix `0x`, as the Mirror Node returns it.

A transaction is identified either by its transaction id in the form of the Mirror Node, `shard.realm.num-seconds-nanos`
(e.g. `0.0.1001-1700000000-123456789`), or by the 32-byte hash of an Ethereum transaction in hexadecimal.

## Design Notes

Deviations from the mirror node REST API (see the Design Notes of [`mirror-node.md`](mirror-node.md)):

- **The contract of a path is a `ContractId`.** The REST API accepts a contract as `shard.realm.num`, as an EVM address
  or as `shard.realm.<EVM address>`; a `ContractId` holds either the number or the EVM address and is sent in one of
  these forms.
- **A transaction of a path is a `string`.** `transactionIdOrHash` accepts a transaction id or an Ethereum transaction
  hash, two different kinds of value; the text is sent as it is. The consensus time of a result in the path of
  `findResult` is a `zonedDateTime`, sent as `seconds.nanoseconds`.
- **The sender filter `from` is an `AccountId`.** It is sent as `shard.realm.num`, or as the EVM address for an
  `AccountId` that holds one.
- **The `ContractResponse` of `findById` is merged into `Contract`.** `bytecode` and `runtimeBytecode` are only set by
  `findById`; `findAll` leaves them `null`.
- **`ContractResultDetails` is merged into `ContractResult`.** `logs` and `stateChanges` are only filled by the lookups
  of a single result (`findResult`, `findResultByTransaction`) and are empty in the lists of `findResults` and
  `findAllResults`.
- **`ContractResultLog` is merged into `ContractLog`.** The attributes that only `ContractLog` has in the REST API
  (`blockHash`, `blockNumber`, `rootContractId`, `timestamp`, `transactionHash`, `transactionIndex`) are `@@nullable`
  and are `null` in the `logs` of a `ContractResult`.
- **`ContractResultStateChange` is `ContractStateChange`, `OpcodesResponse` is `OpcodeTrace`, the items of
  `access_list` and `authorization_list` are `AccessListEntry` and `AuthorizationListEntry`.** The REST names of these
  schemas describe a response or a list, not one value.
- **`call` returns the hexadecimal result.** `ContractCallResponse` has the single field `result`; the method returns it
  directly.
- **EVM addresses are `EvmAddress`, entity ids are typed.** The hexadecimal addresses of a contract, a sender, a
  recipient, a log, an access-list entry and an authorization are `EvmAddress`; entity ids are `ContractId` for
  contracts, `AccountId` for accounts and `Address` where both kinds occur (`Contract.obtainerId` - the remaining
  balance of a deleted contract goes to an account or a contract -, `ContractAction.caller` / `recipient`).
- **An entity id that identifies the value itself is not nullable.** The `EntityId` schema is nullable everywhere, but
  `Contract.contractId`, `ContractState.contractId` and `ContractStateChange.contractId` are always set.
- **Keys are `Authority`, times are `zonedDateTime`, durations `seconds`.** `admin_key` is an `Authority` (ADR-0004);
  `auto_renew_period` is `seconds`.
- **`Contract.timestamp` is a `TimestampRange`.** It replaces the former `fromTimestamp` / `toTimestamp`; the end of
  the range is `null` for the current state of a contract.
- **`ContractResult.amount` is an `int256`.** The REST API declares it as `int64`, but with `hbar = false` it is given
  in weibars (10^10 weibars per tinybar), which exceed `int64` from about 9.2 HBAR on.
- **Lists that the REST API returns as `null` are empty.** `ContractResult.createdContractIds`, `Opcode.memory`,
  `Opcode.stack` and `Opcode.storage` are empty instead of `null`.
- **The string enumerations are enums.** `call_operation_type`, `call_type`, `caller_type` / `recipient_type` and
  `result_data_type` of a `ContractAction` are `ContractCallOperationType`, `ContractActionType`, `ContractEntityType`
  and `ContractResultDataType`. `ContractResult.result` (the name of a transaction status) and `status` (`0x1` or
  `0x0`) stay `string`.
- **`ContractCallRequest.data` has no `@@maxLength`.** The REST API limits it to 262146 characters (131072 bytes); the
  limit is documented instead, because the generated Java contract test of a `@@maxLength` builds a string literal one
  character longer than the limit, which exceeds the limit of a Java string constant (65535).
- **`links` is the `Page`.** The pagination links of a list response are not an attribute; `Page.next()` follows
  them.

## API Schema

```
namespace mirrornode.contract

requires {AccountId, Address, ContractId, EvmAddress, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common
requires {Order, TimestampRange} from mirrornode.common

@@finalType
Contract {
    @@immutable contractId: ContractId
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable createdTimestamp: zonedDateTime
    @@immutable deleted: bool
    @@immutable @@nullable expirationTimestamp: zonedDateTime
    @@immutable @@nullable fileId: Address                             // the file that holds the bytecode of the contract, if any
    @@immutable @@nullable evmAddress: EvmAddress
    @@immutable @@nullable memo: string
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable nonce: int64
    @@immutable @@nullable obtainerId: Address                         // account or contract that received the remaining balance when the contract was deleted
    @@immutable @@nullable permanentRemoval: bool
    @@immutable @@nullable proxyAccountId: AccountId                   // proxy account of the contract, if any
    @@immutable timestamp: TimestampRange                              // the period this state of the contract is valid for
    @@immutable @@nullable bytecode: string                            // the bytecode in hex during deployment; only set by findById
    @@immutable @@nullable runtimeBytecode: string                     // the bytecode in hex after deployment; only set by findById
}

// A contract and the storage keys of it that a wrapped Ethereum transaction accesses (EIP-2930).
@@finalType
AccessListEntry {
    @@immutable address: EvmAddress                    // the accessed contract
    @@immutable @@default([]) storageKeys: list<string> // the accessed storage keys in hex
}

// An authorization of a wrapped EIP-7702 Ethereum transaction: the delegation of an account to the code of an address.
@@finalType
AuthorizationListEntry {
    @@immutable address: EvmAddress    // the address that is delegated to
    @@immutable chainId: string        // the chain id in hex
    @@immutable nonce: int64
    @@immutable r: string              // the signature value r in hex
    @@immutable s: string              // the signature value s in hex
    @@immutable yParity: string        // the parity of the y-coordinate of the signature point in hex
}

// An event that a contract emitted during an execution.
@@finalType
ContractLog {
    @@immutable address: EvmAddress                        // the EVM address of the contract
    @@immutable @@nullable bloom: string                   // the bloom filter of the log in hex
    @@immutable @@nullable contractId: ContractId
    @@immutable @@nullable data: string                    // the data of the log in hex
    @@immutable index: int32                               // the position of the log in the logs of the execution
    @@immutable @@default([]) topics: list<string>         // the topics of the event in hex
    @@immutable @@nullable blockHash: string               // the hash of the block in hex; null in the logs of a ContractResult
    @@immutable @@nullable blockNumber: int64              // the number of the block; null in the logs of a ContractResult
    @@immutable @@nullable rootContractId: ContractId      // the executed contract that created the log
    @@immutable @@nullable timestamp: zonedDateTime        // consensus time of the execution; null in the logs of a ContractResult
    @@immutable @@nullable transactionHash: string         // the hash of the transaction in hex; null in the logs of a ContractResult
    @@immutable @@nullable transactionIndex: int32         // the position of the transaction in the block
}

// A storage slot of a contract that an execution read or changed.
@@finalType
ContractStateChange {
    @@immutable address: EvmAddress            // the EVM address of the contract
    @@immutable contractId: ContractId
    @@immutable slot: string                   // the storage slot in hex
    @@immutable valueRead: string              // the value read from the slot in hex
    @@immutable @@nullable valueWritten: string // the value written to the slot in hex; null if nothing was written
}

// The result of the execution of a contract function, a contract creation or a wrapped Ethereum transaction.
@@finalType
ContractResult {
    @@immutable @@default([]) accessList: list<AccessListEntry>               // the access list of a wrapped Ethereum transaction
    @@immutable @@nullable address: EvmAddress                                // the EVM address of the contract
    @@immutable @@default([]) authorizationList: list<AuthorizationListEntry> // the authorizations of a wrapped EIP-7702 Ethereum transaction
    @@immutable @@nullable amount: int256          // the amount sent to the function; in tinybars, in weibars if requested with hbar = false
    @@immutable @@nullable blockGasUsed: int64     // the gas used by the whole block
    @@immutable @@nullable blockHash: string       // the hash of the block in hex
    @@immutable @@nullable blockNumber: int64
    @@immutable @@nullable bloom: string           // the bloom filter of the result in hex
    @@immutable @@nullable callResult: string      // the value returned by the function in hex
    @@immutable @@nullable chainId: string         // the chain id of a wrapped Ethereum transaction in hex
    @@immutable @@nullable contractId: ContractId
    @@immutable @@default([]) createdContractIds: list<ContractId>   // the contracts created by the execution
    @@immutable @@nullable errorMessage: string    // the message of an error during the execution
    @@immutable @@nullable failedInitcode: string  // the init code of a failed contract creation in hex
    @@immutable @@nullable from: EvmAddress        // the sender
    @@immutable @@nullable functionParameters: string   // the parameters passed to the function in hex
    @@immutable @@nullable gasConsumed: int64      // the gas consumed by the EVM to execute the contract
    @@immutable gasLimit: int64                    // the maximum gas allowed for the execution
    @@immutable @@nullable gasPrice: string        // the gas price of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
    @@immutable @@nullable gasUsed: int64          // the gas used to execute the contract
    @@immutable hash: string                       // the hash of the transaction in hex
    @@immutable @@nullable maxFeePerGas: string    // of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
    @@immutable @@nullable maxPriorityFeePerGas: string // of a wrapped Ethereum transaction in hex; tinybars, weibars with hbar = false
    @@immutable @@nullable nonce: int64            // the nonce of a wrapped Ethereum transaction
    @@immutable @@nullable r: string               // the signature value r of a wrapped Ethereum transaction in hex
    @@immutable result: string                     // the status of the transaction, e.g. SUCCESS
    @@immutable @@nullable s: string               // the signature value s of a wrapped Ethereum transaction in hex
    @@immutable status: string                     // 0x1 for a successful transaction, 0x0 otherwise
    @@immutable timestamp: zonedDateTime           // the consensus time of the execution
    @@immutable @@nullable to: EvmAddress          // the recipient
    @@immutable @@nullable transactionIndex: int64 // the position of the transaction in the block
    @@immutable @@nullable type: int32             // the type of a wrapped Ethereum transaction: 0 (before EIP-1559) or 2 (EIP-1559)
    @@immutable @@nullable v: int32                // the recovery id of a wrapped Ethereum transaction
    @@immutable @@default([]) logs: list<ContractLog>                  // only filled by the lookups of a single result
    @@immutable @@default([]) stateChanges: list<ContractStateChange>  // only filled by the lookups of a single result
}

// A storage slot of a contract and its value.
@@finalType
ContractState {
    @@immutable address: EvmAddress        // the EVM address of the contract
    @@immutable contractId: ContractId
    @@immutable timestamp: zonedDateTime   // the consensus time at which the value was written
    @@immutable slot: string               // the storage slot in hex
    @@immutable value: string              // the value of the slot in hex; "0x" if no value was written
}

// The operation of a call in the EVM.
enum ContractCallOperationType {
    CALL
    CALLCODE
    CREATE
    CREATE2
    DELEGATECALL
    STATICCALL
    UNKNOWN
}

// The kind of a contract action.
enum ContractActionType {
    NO_ACTION
    CALL
    CREATE
    PRECOMPILE
    SYSTEM
}

// The kind of entity that calls or receives a contract action.
enum ContractEntityType {
    ACCOUNT
    CONTRACT
}

// The kind of data a contract action returns.
enum ContractResultDataType {
    OUTPUT
    REVERT_REASON
    ERROR
}

// A call within the execution of a transaction, e.g. a call of one contract by another.
@@finalType
ContractAction {
    @@immutable callDepth: int32                               // the nesting depth of the call
    @@immutable callOperationType: ContractCallOperationType
    @@immutable callType: ContractActionType
    @@immutable @@nullable caller: Address                     // the calling account or contract
    @@immutable callerType: ContractEntityType
    @@immutable from: EvmAddress                               // the EVM address of the caller
    @@immutable gas: int64                                     // the gas provided
    @@immutable gasUsed: int64
    @@immutable index: int32                                   // the position of the action in the actions of the transaction
    @@immutable @@nullable input: string                       // the input data in hex
    @@immutable @@nullable recipient: Address                  // the receiving account or contract
    @@immutable @@nullable recipientType: ContractEntityType
    @@immutable @@nullable resultData: string                  // the result data in hex
    @@immutable resultDataType: ContractResultDataType
    @@immutable timestamp: zonedDateTime                       // the consensus time of the transaction
    @@immutable @@nullable to: EvmAddress                      // the EVM address of the recipient
    @@immutable value: int64                                   // the value transferred in tinybars
}

// One step of an opcode trace: an opcode and the state of the EVM when it is executed.
@@finalType
Opcode {
    @@immutable depth: int32                            // the current call depth
    @@immutable gas: int64                              // the remaining gas
    @@immutable gasCost: int64                          // the cost of the opcode
    @@immutable @@default([]) memory: list<string>      // the EVM memory in hex; empty unless requested
    @@immutable op: string                              // the opcode
    @@immutable pc: int32                               // the program counter
    @@immutable @@nullable reason: string               // the revert reason in hex
    @@immutable @@default([]) stack: list<string>       // the EVM stack in hex; empty unless requested
    @@immutable storage: map<string, string>            // the storage slots read and written by the contract, in hex; empty unless requested
}

// The opcode trace of the execution of a transaction.
@@finalType
OpcodeTrace {
    @@immutable address: EvmAddress                 // the recipient of the transaction; the zero address for a contract creation
    @@immutable @@nullable contractId: ContractId
    @@immutable failed: bool                        // whether the execution failed
    @@immutable gas: int64                          // the gas used
    @@immutable @@default([]) opcodes: list<Opcode>
    @@immutable returnValue: string                 // the data returned by the transaction in hex
}

// A call of a contract that the Mirror Node simulates without submitting a transaction.
@@finalType
ContractCallRequest {
    @@immutable @@nullable block: string                        // a block number in hex, or "earliest", "pending", "latest" (the default)
    @@immutable @@nullable data: string                         // the function selector and the encoded parameters in hex, at most 131072 bytes
    @@immutable @@nullable estimate: bool                       // whether the gas is estimated instead of the call executed; default false
    @@immutable @@nullable from: EvmAddress                     // the sender
    @@immutable @@nullable @@min(0) gas: int64                  // the gas provided; default 15000000
    @@immutable @@nullable @@min(0) gasPrice: int64             // the price of a unit of gas
    @@immutable to: EvmAddress                                  // the contract that is called
    @@immutable @@nullable @@min(0) value: int64                // the value sent with the call; default 0
}

ContractRepository {
    // The contracts of the network. Maps to GET /api/v1/contracts.
    @@async @@throws(mirror-node-error)
    Page<Contract> findAll()

    // The contracts of the network, filtered by their id. Maps to GET /api/v1/contracts.
    @@async @@throws(mirror-node-error)
    Page<Contract> findAll(contractId: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order)

    // The contract with the given id, with its bytecode. Maps to GET /api/v1/contracts/{contractIdOrAddress}.
    @@async @@throws(mirror-node-error)
    @@nullable Contract findById(contractId: ContractId)

    // The contract with the given id at the given consensus time, with its bytecode.
    // Maps to GET /api/v1/contracts/{contractIdOrAddress}.
    @@async @@throws(mirror-node-error)
    @@nullable Contract findById(contractId: ContractId, timestamp: list<string>)

    // The results of the executions of a contract. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results.
    @@async @@throws(mirror-node-error)
    Page<ContractResult> findResults(contractId: ContractId)

    // The results of the executions of a contract, filtered by block, sender, consensus time and position in the
    // block; `internal` includes the child transactions. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results.
    @@async @@throws(mirror-node-error)
    Page<ContractResult> findResults(contractId: ContractId, blockHash: list<string>, blockNumber: list<string>,
            @@nullable from: AccountId, @@nullable internal: bool, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>, @@nullable @@min(0) transactionIndex: int32)

    // The storage slots of a contract and their current values.
    // Maps to GET /api/v1/contracts/{contractIdOrAddress}/state.
    @@async @@throws(mirror-node-error)
    Page<ContractState> findState(contractId: ContractId)

    // The storage slots of a contract, filtered by slot, and their values at the given consensus time.
    // Maps to GET /api/v1/contracts/{contractIdOrAddress}/state.
    @@async @@throws(mirror-node-error)
    Page<ContractState> findState(contractId: ContractId, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, slot: list<string>, timestamp: list<string>)

    // The result of the execution of a contract at the given consensus time, with its logs and state changes.
    // Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/{timestamp}.
    @@async @@throws(mirror-node-error)
    @@nullable ContractResult findResult(contractId: ContractId, timestamp: zonedDateTime)

    // The result of the execution of a contract at the given consensus time; `hbar = false` gives the monetary values
    // in weibars instead of tinybars. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/{timestamp}.
    @@async @@throws(mirror-node-error)
    @@nullable ContractResult findResult(contractId: ContractId, timestamp: zonedDateTime, @@nullable hbar: bool)

    // The results of the executions of all contracts. Maps to GET /api/v1/contracts/results.
    @@async @@throws(mirror-node-error)
    Page<ContractResult> findAllResults()

    // The results of the executions of all contracts, filtered by sender, block, consensus time and position in the
    // block; `hbar = false` gives the monetary values in weibars, `internal` includes the child transactions.
    // Maps to GET /api/v1/contracts/results.
    @@async @@throws(mirror-node-error)
    Page<ContractResult> findAllResults(@@nullable from: AccountId, blockHash: list<string>, blockNumber: list<string>,
            @@nullable hbar: bool, @@nullable internal: bool, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>, @@nullable @@min(0) transactionIndex: int32)

    // The result of the contract execution of a transaction, given by its transaction id or Ethereum transaction hash,
    // with its logs and state changes. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}.
    @@async @@throws(mirror-node-error)
    @@nullable ContractResult findResultByTransaction(transactionIdOrHash: string)

    // The result of the contract execution of a transaction; `hbar = false` gives the monetary values in weibars,
    // `nonce` selects a child transaction. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}.
    @@async @@throws(mirror-node-error)
    @@nullable ContractResult findResultByTransaction(transactionIdOrHash: string, @@nullable hbar: bool,
            @@nullable @@min(0) nonce: int32)

    // The actions (calls) of the execution of a transaction.
    // Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/actions.
    @@async @@throws(mirror-node-error)
    Page<ContractAction> findActions(transactionIdOrHash: string)

    // The actions (calls) of the execution of a transaction, filtered by their position.
    // Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/actions.
    @@async @@throws(mirror-node-error)
    Page<ContractAction> findActions(transactionIdOrHash: string, index: list<string>,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order)

    // The opcode trace of the execution of a transaction, with the stack of each step.
    // Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/opcodes.
    @@async @@throws(mirror-node-error)
    @@nullable OpcodeTrace findOpcodes(transactionIdOrHash: string)

    // The opcode trace of the execution of a transaction, with the stack, the memory and the storage of each step as
    // requested. Maps to GET /api/v1/contracts/results/{transactionIdOrHash}/opcodes.
    @@async @@throws(mirror-node-error)
    @@nullable OpcodeTrace findOpcodes(transactionIdOrHash: string, @@nullable stack: bool, @@nullable memory: bool,
            @@nullable storage: bool)

    // The log events emitted by a contract. Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/logs.
    @@async @@throws(mirror-node-error)
    Page<ContractLog> findLogs(contractId: ContractId)

    // The log events emitted by a contract, filtered by position, consensus time and topics (each in hex).
    // Maps to GET /api/v1/contracts/{contractIdOrAddress}/results/logs.
    @@async @@throws(mirror-node-error)
    Page<ContractLog> findLogs(contractId: ContractId, index: list<string>,
            @@nullable @@min(1) @@max(100) limit: int32, @@nullable order: Order, timestamp: list<string>,
            topic0: list<string>, topic1: list<string>, topic2: list<string>, topic3: list<string>)

    // The log events emitted by all contracts. Maps to GET /api/v1/contracts/results/logs.
    @@async @@throws(mirror-node-error)
    Page<ContractLog> findAllLogs()

    // The log events emitted by all contracts, filtered by position, consensus time, topics (each in hex) and
    // transaction hash. Maps to GET /api/v1/contracts/results/logs.
    @@async @@throws(mirror-node-error)
    Page<ContractLog> findAllLogs(index: list<string>, @@nullable @@min(1) @@max(100) limit: int32,
            @@nullable order: Order, timestamp: list<string>, topic0: list<string>, topic1: list<string>,
            topic2: list<string>, topic3: list<string>, transactionHash: list<string>)

    // Simulates a call of a contract without submitting a transaction, or estimates its gas, and returns the result
    // in hex. Maps to POST /api/v1/contracts/call.
    @@async @@throws(mirror-node-error)
    string call(request: ContractCallRequest)
}

@@static ContractRepository createRepository(mirrorNode: MirrorNode)

```

## Testing

The queries of `ContractRepository` need a Mirror Node; only the value types are tested here.

```tests
// Source: the Hiero Enterprise Java test fixture contract.json (contract 0.0.10790540, auto-renew period 7776000
// seconds, created "1785753223.568147754", expiration "1793529223.568147754", timestamp range from the creation without
// an end, memo "0.0.10790537", nonce 1, max. automatic token associations 0); millisecond precision.
@@testId("a-contract-keeps-its-auto-renew-period-and-its-lifecycle-timestamps")
test "a contract keeps its auto-renew period and its lifecycle timestamps" {
    // given
    let created = toZonedDateTime(text: "2026-08-03T10:33:43.568Z")
    let expiration = toZonedDateTime(text: "2026-11-01T10:33:43.568Z")

    // when
    let contract = Contract{contractId: ContractId.fromString(value: "0.0.10790540"),
            autoRenewPeriod: toSeconds(value: 7776000), createdTimestamp: created, deleted: false,
            expirationTimestamp: expiration, memo: "0.0.10790537", maxAutomaticTokenAssociations: 0, nonce: 1,
            permanentRemoval: false, timestamp: TimestampRange{from: created}}

    // then
    assertEquals(expected: ContractId.fromString(value: "0.0.10790540"), actual: contract.contractId)
    assertEquals(expected: toSeconds(value: 7776000), actual: contract.autoRenewPeriod)
    assertEquals(expected: toZonedDateTime(text: "2026-08-03T10:33:43.568Z"), actual: contract.createdTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2026-11-01T10:33:43.568Z"), actual: contract.expirationTimestamp)
    assertEquals(expected: created, actual: contract.timestamp.from)
    assertNull(actual: contract.timestamp.to)
    assertEquals(expected: "0.0.10790537", actual: contract.memo)
    assertEquals(expected: 1, actual: contract.nonce)
    assertFalse(condition: contract.deleted)
}

// Source: this spec (the optional values of a contract are nullable).
@@testId("a-contract-without-optional-values-has-no-admin-authority-expiration-evm-address-and-bytecode")
test "a contract without optional values has no admin authority, expiration, EVM address and bytecode" {
    // when
    let contract = Contract{contractId: DEFAULT(ContractId), deleted: false,
            timestamp: TimestampRange{from: DEFAULT(zonedDateTime)}}

    // then
    assertNull(actual: contract.adminAuthority)
    assertNull(actual: contract.autoRenewAccount)
    assertNull(actual: contract.autoRenewPeriod)
    assertNull(actual: contract.createdTimestamp)
    assertNull(actual: contract.expirationTimestamp)
    assertNull(actual: contract.fileId)
    assertNull(actual: contract.evmAddress)
    assertNull(actual: contract.memo)
    assertNull(actual: contract.nonce)
    assertNull(actual: contract.obtainerId)
    assertNull(actual: contract.permanentRemoval)
    assertNull(actual: contract.proxyAccountId)
    assertNull(actual: contract.bytecode)
    assertNull(actual: contract.runtimeBytecode)
}

// Source: the examples of the schemas ContractState (slot, value), EvmAddress (address) and EntityId (contract id
// "0.0.2") in the mirror node OpenAPI definition.
@@testId("a-contract-state-keeps-its-slot-and-its-value")
test "a contract state keeps its slot and its value" {
    // when
    let state = ContractState{address: EvmAddress.fromString(value: "0x0000000000000000000000000000000000001f41"),
            contractId: ContractId.fromString(value: "0.0.2"), timestamp: DEFAULT(zonedDateTime),
            slot: "0x00000000000000000000000000000000000000000000000000000000000000fa",
            value: "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925"}

    // then
    assertEquals(expected: "0x00000000000000000000000000000000000000000000000000000000000000fa", actual: state.slot)
    assertEquals(expected: "0x8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925", actual: state.value)
    assertEquals(expected: ContractId.fromString(value: "0.0.2"), actual: state.contractId)
}

// Source: this spec (the attributes that only the logs of the log queries have are null in the logs of a result;
// the topics default to an empty list).
@@testId("a-contract-log-of-a-result-has-no-block-transaction-and-timestamp")
test "a contract log of a result has no block, transaction and timestamp" {
    // when
    let log = ContractLog{address: EvmAddress.fromString(value: "0x0000000000000000000000000000000000001f41"), index: 0}

    // then
    assertEquals(expected: 0, actual: log.index)
    assertEquals(expected: 0, actual: length(value: log.topics))
    assertNull(actual: log.blockHash)
    assertNull(actual: log.blockNumber)
    assertNull(actual: log.timestamp)
    assertNull(actual: log.transactionHash)
    assertNull(actual: log.transactionIndex)
}

// Source: this spec (logs and state changes are only filled by the lookups of a single result; lists default to
// empty); the example of ContractResult in the mirror node OpenAPI definition (gas limit 100000, result SUCCESS,
// status 0x1).
@@testId("a-contract-result-of-a-list-has-no-logs-and-no-state-changes")
test "a contract result of a list has no logs and no state changes" {
    // when
    let result = ContractResult{gasLimit: 100000,
            hash: "0xfebbaa29c513d124a6377246ea3506ad917d740c21a88f61a1c55ba338fc2bb1", result: "SUCCESS",
            status: "0x1", timestamp: DEFAULT(zonedDateTime)}

    // then
    assertEquals(expected: 100000, actual: result.gasLimit)
    assertEquals(expected: "SUCCESS", actual: result.result)
    assertEquals(expected: 0, actual: length(value: result.logs))
    assertEquals(expected: 0, actual: length(value: result.stateChanges))
    assertEquals(expected: 0, actual: length(value: result.createdContractIds))
    assertEquals(expected: 0, actual: length(value: result.accessList))
    assertNull(actual: result.amount)
    assertNull(actual: result.contractId)
}
```

## Questions & Comments

- **Hexadecimal EVM data is `string`.** Bytecode, call data, results, hashes, storage slots and values, log topics and
  data, bloom filters and the Ethereum signature values are kept as the hexadecimal text of the Mirror Node (with
  `0x`). `bytes` would be typed, but loses the distinction between `"0x"` and `null` that some fields make (e.g.
  `ContractState.value`), and hex is what EVM tooling works with. Should they become `bytes` (or a dedicated hex type)?
- **The filters `blockHash`, `blockNumber` and `transactionHash` are condition lists, but accept one value.** They
  allow only the operator `eq:`, and the Mirror Node uses only the last value. A `@@nullable string` would be more
  precise; they follow the general rule for parameters with an operator for now.
- **`ContractResult.result` and `status` are strings.** `result` is the name of a transaction status (e.g. `SUCCESS`)
  and could be typed with the status codes of the consensus node client, which the mirror node client cannot import;
  `status` is `0x1` or `0x0` and could be a `bool`.
- **An unknown enum value of the REST API fails the query.** The enums of `ContractAction` list the values of the
  pinned OpenAPI definition; a newer Mirror Node may return another value.
- **`ContractRepository` and `NetworkRepository` are concrete types.** The other five repositories are abstractions,
  but these two are declared without `abstraction`, so they can be instantiated directly — and a
  `new ContractRepository()` / `new NetworkRepository()` has no `MirrorNode` and cannot reach a mirror node. The Java
  implementation creates working instances only through `createRepository(...)` / `createMirrorNodeClient(...)`.
  Should both be abstractions like the other repositories?

# Contract service

The TCK methods of the smart contract service
([contract-service](https://github.com/hiero-ledger/hiero-sdk-tck/tree/main/docs/test-specifications/contract-service)):
creating, executing, updating and deleting contracts, and Ethereum transactions. Bytecode, constructor parameters,
call data and Ethereum data are hex strings (`hex`); amounts are tinybar.

## Gaps

- **`gas` is required in V3** (`ContractCreateTransaction.gas`, `ContractExecuteTransaction.gas`), optional in the
  TCK. The bindings send `0` when the TCK sends no gas, which is what the V2 SDKs send; the network then answers
  `INSUFFICIENT_GAS`.
- **Required ids.** `ContractExecuteTransaction`, `ContractUpdateTransaction` and `ContractDeleteTransaction` require a
  `contractId`; a test that leaves it out (and expects `INVALID_CONTRACT_ID` from the network) gets an error of the
  SDK instead.
- **Checked before sending:** a negative `gas` (`@@min(0)`), empty `ethereumData` (`@@minSize(1)`), bytecode and a
  bytecode file together or neither (`@@oneOf`), two obtainers of a deleted contract (`@@oneOf`) and staking to an
  account and a node (`@@oneOrNoneOf`) are rejected by the SDK, where the TCK may expect a status of the network.
- **`permanentRemoval`** of `deleteContract`: only the network itself may set it; `ContractDeleteTransaction` does not
  offer it. `false`, which a user transaction has anyway, is accepted.
- **Expiration times beyond the range of a Java `Instant`** (`int64` max / min seconds) cannot be converted by the
  Java runtime (`timestamp`); the TCK expects the status of the network.
- The specification of `createContract` names the parameter `maxAutomaticTokenAssociation`, its tests send
  `maxAutomaticTokenAssociations`; the specification of `executeContract` names the call data `parameters`, its tests
  send `functionParameters`. The bindings accept both.
- **The contract queries** (`contractCallQuery`, `getContractInfo`, `getContractBytecode`) are not bound: their API is
  not specified yet.

```bindings
requires {ContractCreateTransaction, ContractExecuteTransaction, ContractUpdateTransaction} from consensusnode.transactions.contracts
requires {ContractDeleteTransaction, EthereumTransaction} from consensusnode.transactions.contracts

// Creates a contract (ContractCreateTransaction.md).
binding createContract -> ContractCreateTransaction {
    bytecode                      = initcode : hex
    bytecodeFileId                = bytecodeFileId : address
    gas                           = gas : int64 | "0" : int64
    constructorParameters         = constructorParameters : hex
    initialBalance                = initialBalance : tinybar
    adminAuthority                = adminKey : key
    contractMemo                  = memo : string
    autoRenewPeriod               = autoRenewPeriod : seconds
    autoRenewAccount              = autoRenewAccountId : accountId
    maxAutomaticTokenAssociations = maxAutomaticTokenAssociations : int32 | maxAutomaticTokenAssociation : int32
    stakedAccountId               = stakedAccountId : accountId
    stakedNodeId                  = stakedNodeId : int64
    declineStakingReward          = declineStakingReward : bool
    result contractId = receipt.contractId : contractId
    result status     = receipt.status : status
}

// Calls a function of a contract (ContractExecuteTransaction.md).
binding executeContract -> ContractExecuteTransaction {
    contractId    = contractId : contractId
    gas           = gas : int64 | "0" : int64
    payableAmount = amount : tinybar
    callData      = functionParameters : hex | parameters : hex
    result status = receipt.status : status
}

// Updates a contract (ContractUpdateTransaction.md).
binding updateContract -> ContractUpdateTransaction {
    contractId                    = contractId : contractId
    adminAuthority                = adminKey : key
    contractMemo                  = memo : string
    expirationTime                = expirationTime : timestamp
    autoRenewPeriod               = autoRenewPeriod : seconds
    autoRenewAccount              = autoRenewAccountId : accountId
    maxAutomaticTokenAssociations = maxAutomaticTokenAssociations : int32
    stakedAccountId               = stakedAccountId : accountId
    stakedNodeId                  = stakedNodeId : int64
    declineStakingReward          = declineStakingReward : bool
    result status = receipt.status : status
}

// Deletes a contract (ContractDeleteTransaction.md).
binding deleteContract -> ContractDeleteTransaction {
    contractId         = contractId : contractId
    transferAccountId  = transferAccountId : accountId
    transferContractId = transferContractId : contractId
    unsupported permanentRemoval "only the network itself may set it; ContractDeleteTransaction does not offer it" unless false
    result status = receipt.status : status
}

// Submits an Ethereum transaction (EthereumTransaction.md).
binding createEthereumTransaction -> EthereumTransaction {
    ethereumData    = ethereumData : hex
    callDataFileId  = callDataFileId : address
    maxGasAllowance = maxGasAllowance : tinybar
    result contractId = receipt.contractId : contractId
    result status     = receipt.status : status
}
```

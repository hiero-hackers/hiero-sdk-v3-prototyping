# Mirror Node Contract Query API

## Description

Query smart contracts from the Mirror Node. Use the `ContractRepository` (available as `MirrorNodeClient.contracts`)
to look up a single `Contract` by its `ContractId` or to page through all contracts. A `Contract` describes the
contract entity: its keys, auto-renew settings, memo, EVM address, lifecycle timestamps and, if available, its
bytecode.

## API Schema

```
namespace mirrornode.contract

requires {AccountId, ContractId, EvmAddress, MirrorNode} from ledger
requires {Authority} from authority
requires {Page} from common

@@finalType
Contract {
    @@immutable contractId: ContractId
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable autoRenewAccount: AccountId
    @@immutable autoRenewPeriod: seconds
    @@immutable createdTimestamp: zonedDateTime
    @@immutable deleted: bool
    @@immutable @@nullable expirationTimestamp: zonedDateTime
    @@immutable @@nullable fileId: string                              // id of the file that holds the contract bytecode, if any
    @@immutable @@nullable evmAddress: EvmAddress
    @@immutable @@nullable memo: string
    @@immutable @@nullable maxAutomaticTokenAssociations: int32
    @@immutable @@nullable nonce: int64
    @@immutable @@nullable obtainerId: string                          // account that received the remaining balance when the contract was deleted
    @@immutable permanentRemoval: bool
    @@immutable @@nullable proxyAccountId: string                      // proxy account of the contract, if any
    @@immutable fromTimestamp: zonedDateTime
    @@immutable toTimestamp: zonedDateTime
    @@immutable @@nullable bytecode: string
    @@immutable @@nullable runtimeBytecode: string
}

ContractRepository {
    @@async @@throws(mirror-node-error)
    Page<Contract> findAll()

    @@async @@throws(mirror-node-error)
    @@nullable Contract findById(contractId: ContractId)
}

@@static ContractRepository createRepository(mirrorNode: MirrorNode)

```

## Questions & Comments

- TODO: `Contract.fileId` should be typed `Address` once the string-typed entity-id fields in this file are cleaned up
  (separate work).
- TODO: `Contract.obtainerId` should be typed `AccountId` once the string-typed entity-id fields are cleaned up.
- TODO: `Contract.proxyAccountId` should be typed `AccountId` once the string-typed entity-id fields are cleaned up.

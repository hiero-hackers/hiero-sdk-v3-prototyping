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

## Testing

The queries of `ContractRepository` need a Mirror Node; only the value type is tested here.

```tests
// Source: the Hiero Enterprise Java test fixture contract.json (contract 0.0.10790540, auto-renew period 7776000
// seconds, created "1785753223.568147754", expiration "1793529223.568147754", timestamp range from the creation,
// memo "0.0.10790537", nonce 1, max. automatic token associations 0); millisecond precision. The fixture has no end
// of the timestamp range, which this spec requires, so the expiration time stands in for it.
@@testId("a-contract-keeps-its-auto-renew-period-and-its-lifecycle-timestamps")
test "a contract keeps its auto-renew period and its lifecycle timestamps" {
    // given
    let created = toZonedDateTime(text: "2026-08-03T10:33:43.568Z")
    let expiration = toZonedDateTime(text: "2026-11-01T10:33:43.568Z")

    // when
    let contract = Contract{contractId: ContractId.fromString(value: "0.0.10790540"),
            autoRenewPeriod: toSeconds(value: 7776000), createdTimestamp: created, deleted: false,
            expirationTimestamp: expiration, memo: "0.0.10790537", maxAutomaticTokenAssociations: 0, nonce: 1,
            permanentRemoval: false, fromTimestamp: created, toTimestamp: expiration}

    // then
    assertEquals(expected: ContractId.fromString(value: "0.0.10790540"), actual: contract.contractId)
    assertEquals(expected: toSeconds(value: 7776000), actual: contract.autoRenewPeriod)
    assertEquals(expected: toZonedDateTime(text: "2026-08-03T10:33:43.568Z"), actual: contract.createdTimestamp)
    assertEquals(expected: toZonedDateTime(text: "2026-11-01T10:33:43.568Z"), actual: contract.expirationTimestamp)
    assertEquals(expected: created, actual: contract.fromTimestamp)
    assertEquals(expected: expiration, actual: contract.toTimestamp)
    assertEquals(expected: "0.0.10790537", actual: contract.memo)
    assertEquals(expected: 1, actual: contract.nonce)
    assertFalse(condition: contract.deleted)
}

// Source: this spec (the optional values of a contract are nullable).
@@testId("a-contract-without-optional-values-has-no-admin-authority-expiration-evm-address-and-bytecode")
test "a contract without optional values has no admin authority, expiration, EVM address and bytecode" {
    // given
    let time = DEFAULT(zonedDateTime)

    // when
    let contract = Contract{contractId: DEFAULT(ContractId), autoRenewPeriod: DEFAULT(seconds), createdTimestamp: time,
            deleted: false, permanentRemoval: false, fromTimestamp: time, toTimestamp: time}

    // then
    assertNull(actual: contract.adminAuthority)
    assertNull(actual: contract.autoRenewAccount)
    assertNull(actual: contract.expirationTimestamp)
    assertNull(actual: contract.evmAddress)
    assertNull(actual: contract.memo)
    assertNull(actual: contract.nonce)
    assertNull(actual: contract.obtainerId)
    assertNull(actual: contract.bytecode)
    assertNull(actual: contract.runtimeBytecode)
}
```

## Questions & Comments

- TODO: `Contract.fileId` should be typed `Address` once the string-typed entity-id fields in this file are cleaned up
  (separate work).
- TODO: `Contract.obtainerId` should be typed `AccountId` once the string-typed entity-id fields are cleaned up.
- TODO: `Contract.proxyAccountId` should be typed `AccountId` once the string-typed entity-id fields are cleaned up.
- **`Contract.toTimestamp` is not nullable, but the Mirror Node leaves it open.** The REST API returns the validity of
  the contract state as a range `timestamp: {from, to}`, and for the current state `to` is `null` (e.g. the Hiero
  Enterprise Java test fixture `contract.json`). A `Contract` of the current state therefore cannot be built without
  inventing an end time. Should `toTimestamp` be `@@nullable`? The same holds for `permanentRemoval`, which the REST
  API also returns as `null`. The Java implementation fills an open `to` with a sentinel end time and a `null`
  `permanentRemoval` with a fallback value (see the general item in [`mirror-node.md`](mirror-node.md)), so a caller
  cannot tell these values from real ones.

- **`ContractRepository` and `NetworkRepository` are concrete types.** The other five repositories are abstractions,
  but these two are declared without `abstraction`, so they can be instantiated directly — and a
  `new ContractRepository()` / `new NetworkRepository()` has no `MirrorNode` and cannot reach a mirror node. The Java
  implementation creates working instances only through `createRepository(...)` / `createMirrorNodeClient(...)`.
  Should both be abstractions like the other repositories?

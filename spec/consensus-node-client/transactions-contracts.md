# Contract Transactions API

## Description

The smart contract service runs contracts written for the Ethereum Virtual Machine (EVM) on the consensus node. A
contract is addressed by a `ContractId`, either by its number or by its 20-byte EVM address.

A contract's lifecycle is:

```
  create ── (execute | update)* ── delete
```

- `ContractCreateTransaction` deploys a contract. Its initcode (the compiled bytecode that runs the constructor and
  returns the code of the contract) is either given inline as `bytecode` or read from a file of the file service
  (`bytecodeFileId`), which is the way for initcode that does not fit into one transaction. The arguments of the
  constructor are passed as `constructorParameters`.
- `ContractExecuteTransaction` calls a function of a contract and can transfer native tokens to it. The function and
  its arguments are the `callData`.
- `ContractUpdateTransaction` changes the properties of a contract: its admin authority, memo, expiration, renewal
  and staking. It does not change the code of the contract.
- `ContractDeleteTransaction` deletes a contract and transfers its remaining balance to an account or another
  contract.
- `EthereumTransaction` submits a transaction that was signed by an Ethereum account (with its ECDSA secp256k1 key),
  in the encoding of the Ethereum network. The account of the client pays the transaction fee and, up to
  `maxGasAllowance`, the gas the Ethereum account does not pay itself.

### Arguments and results in the contract ABI

The call data, the constructor arguments, the result of a call and the logs of a contract are encoded in the
contract ABI. The types of the namespace `evm` produce and read them:

- `AbiFunction.encodeCall(arguments)` returns the `callData` of a `ContractExecuteTransaction`: the 4-byte selector
  of the function followed by its encoded arguments.
- `AbiTuple{values}.encode()` returns the `constructorParameters` of a `ContractCreateTransaction`: the encoded
  arguments without a selector, as a constructor has none.
- The record of a contract transaction (`Response.queryRecord(client)`) holds a `ContractFunctionResult`.
  `AbiFunction.decodeResult(result.result)` decodes what the function returned, and
  `AbiEvent.decodeLog(log.topics, log.data)` decodes each of its `logs`.

### Gas

Every contract transaction names the maximum amount of gas its execution may use (`gas`). The gas actually used is
charged in addition to the transaction fee; gas that is reserved but not used is refunded only in part, so `gas`
should be close to what the execution needs. An execution that runs out of gas fails with `INSUFFICIENT_GAS` and its
changes are reverted, but the gas is charged.

### Signing requirements (contract)

| Transaction          | Signers required                                                                                         |
|----------------------|----------------------------------------------------------------------------------------------------------|
| `ContractCreate`     | the *payer*; **and** the `adminAuthority` if it is set (anti-spoofing); **and** the `autoRenewAccount` if it is set |
| `ContractExecute`    | the *payer* only; what the contract allows the caller to do is decided by its code                       |
| `ContractUpdate`     | the *payer*; **and** the contract's *current* `adminAuthority`; **and** the *new* `adminAuthority` if it changes; **and** the new `autoRenewAccount` if it changes. Extending only `expirationTime` needs no admin signature. |
| `ContractDelete`     | the *payer*; **and** the contract's current `adminAuthority`                                             |
| `EthereumTransaction` | the *payer* (the relay); the Ethereum account has already signed `ethereumData`                         |

A contract without an `adminAuthority` is immutable: it can neither be updated (except for extending its expiration)
nor deleted. An `adminAuthority` can be a contract (`Authority.ofContract(...)`), so that a contract manages itself.

### Clearing optional values

`ContractUpdate` distinguishes "leave unchanged" from "clear":

- An optional field that is left unset means *leave unchanged*.
- The auto-renew account is removed by setting `autoRenewAccount` to `ZERO_ACCOUNT_ID`.

## Design Notes

- **Raw bytes, encoded with `evm`.** `callData`, `constructorParameters` and the result are `bytes`, not lists of
  `AbiValue`: the consensus node transfers bytes, a contract can be called with data that no ABI describes (a
  fallback function, a proxy), and the transaction must not depend on knowing the signature of the function. The ABI
  types of [`evm.md`](../base/evm.md) produce and read the bytes. Typed calls with the arguments as values of the
  programming language are part of `enterprise.service.contract`.
- **`callData`, not `functionParameters`.** HAPI and the V2 SDKs call the bytes `functionParameters`, although they
  start with the selector of the function; V3 uses the term of the ABI (`AbiFunction.encodeCall`,
  `AbiFunction.decodeCall(data)`).
- **The result is on the record, not the receipt.** HAPI returns the `ContractFunctionResult` in the
  `TransactionRecord` (`contractCallResult` / `contractCreateResult`), so it is read with
  `Response.queryRecord(client)`. It is an attribute of the base `Record` (see
  [`transactions.md`](transactions.md)) because the child records of a contract call can carry one too, and because
  the record is not typed per transaction.
- **Not exposed:** the deprecated `proxyAccountID` and `ContractUpdateTransactionBody.fileID`; the
  `shardID`, `realmID` and `newRealmAdminKey` of `ContractCreateTransactionBody`, which the network ignores;
  `ContractDeleteTransactionBody.permanent_removal`, which only the network itself may set; and the hooks of HIP-1195
  (`hook_creation_details`, `hook_ids_to_delete`), which are not specified for accounts either.
- `gas` is `@@min(0)` like in the V2 SDKs (`setGas(-1)` is rejected, `setGas(0)` is accepted); the network rejects a
  gas below the intrinsic gas of the transaction.

## API Schema

```
namespace consensusnode.transactions.contracts
requires {Address, AccountId, ContractId} from ledger
requires {Authority} from authority
requires {NativeToken} from nativeToken
requires {Receipt, Transaction} from consensusnode.transactions

// Deploys a contract. The initcode is either given as `bytecode` or read from the file `bytecodeFileId`.
// A contract is staked to another account or to a node, not to both.
@@finalType
@@oneOf(bytecode, bytecodeFileId)
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
ContractCreateTransaction extends Transaction<ContractCreateReceipt, ContractCreateTransaction> {
    @@immutable @@nullable bytecode: bytes                          // the initcode of the contract
    @@immutable @@nullable bytecodeFileId: Address                  // the file that holds the initcode, as hex text
    @@immutable @@min(0) gas: int64                                  // the maximum gas the constructor may use
    // The ABI-encoded arguments of the constructor, without a selector (see `AbiTuple.encode()`).
    @@immutable @@default([]) constructorParameters: bytes
    // Amount transferred from the payer to the new contract. The constructor must be payable if it is set.
    @@immutable @@nullable initialBalance: NativeToken<ANY, ANY>
    // Allows to update and delete the contract; without it the contract cannot be changed. MUST sign if set.
    @@immutable @@nullable adminAuthority: Authority
    @@immutable @@nullable @@maxBytes(100) contractMemo: string
    @@immutable @@nullable autoRenewPeriod: seconds                 // the renewal period; the network default if unset
    @@immutable @@nullable autoRenewAccount: AccountId              // the account that pays for the renewal; MUST sign if set
    @@immutable @@nullable maxAutomaticTokenAssociations: int32     // -1: unlimited
    @@immutable @@nullable stakedAccountId: AccountId
    @@immutable @@nullable stakedNodeId: int64
    @@immutable @@default(false) declineStakingReward: bool
}

@@finalType
ContractCreateReceipt extends Receipt {
    @@immutable contractId: ContractId                              // the id of the new contract
}

// Calls a function of a contract. The result, the gas used and the logs are part of the record of the transaction.
@@finalType
ContractExecuteTransaction extends Transaction<ContractExecuteReceipt, ContractExecuteTransaction> {
    @@immutable contractId: ContractId                              // the contract that is called
    @@immutable @@min(0) gas: int64                                  // the maximum gas the call may use
    // Amount transferred from the payer to the contract. The function must be payable if it is set.
    @@immutable @@nullable payableAmount: NativeToken<ANY, ANY>
    // The selector of the function followed by its ABI-encoded arguments (see `AbiFunction.encodeCall`). Empty
    // calls the receive or fallback function of the contract.
    @@immutable @@default([]) callData: bytes
}

@@finalType
ContractExecuteReceipt extends Receipt {
}

// Updates one or more properties of a contract. Every nullable field is "leave unchanged" when null.
// A contract is staked to another account or to a node, not to both.
@@finalType
@@oneOrNoneOf(stakedAccountId, stakedNodeId)
ContractUpdateTransaction extends Transaction<ContractUpdateReceipt, ContractUpdateTransaction> {
    @@immutable contractId: ContractId                              // the contract that is updated
    @@immutable @@nullable adminAuthority: Authority                // the new admin authority; the new one MUST sign as well
    @@immutable @@nullable @@maxBytes(100) contractMemo: string
    @@immutable @@nullable expirationTime: zonedDateTime            // a later expiration time; cannot be earlier than the current one
    @@immutable @@nullable autoRenewPeriod: seconds
    @@immutable @@nullable autoRenewAccount: AccountId              // the new account MUST sign; ZERO_ACCOUNT_ID removes it
    @@immutable @@nullable maxAutomaticTokenAssociations: int32     // -1: unlimited
    @@immutable @@nullable stakedAccountId: AccountId
    @@immutable @@nullable stakedNodeId: int64
    @@immutable @@nullable declineStakingReward: bool
}

@@finalType
ContractUpdateReceipt extends Receipt {
}

// Deletes a contract. Its remaining balance is transferred to an account or to another contract.
@@finalType
@@oneOf(transferAccountId, transferContractId)
ContractDeleteTransaction extends Transaction<ContractDeleteReceipt, ContractDeleteTransaction> {
    @@immutable contractId: ContractId                              // the contract that is deleted
    @@immutable @@nullable transferAccountId: AccountId             // the account that receives the remaining balance
    @@immutable @@nullable transferContractId: ContractId           // the contract that receives the remaining balance
}

@@finalType
ContractDeleteReceipt extends Receipt {
}

// Submits a transaction signed by an Ethereum account. The fee of this transaction is paid by the payer (the relay);
// the gas is paid by the Ethereum account and, up to `maxGasAllowance`, by the payer.
@@finalType
EthereumTransaction extends Transaction<EthereumTransactionReceipt, EthereumTransaction> {
    // The signed Ethereum transaction, RLP-encoded: a legacy transaction or a typed transaction of EIP-2930 or
    // EIP-1559. If `callDataFileId` is set, its call data must be empty.
    @@immutable @@minSize(1) ethereumData: bytes
    // The file that holds the call data, as hex text, for call data that does not fit into one transaction.
    @@immutable @@nullable callDataFileId: Address
    // The most the payer pays for the gas that the Ethereum account does not pay itself; nothing if unset.
    @@immutable @@nullable maxGasAllowance: NativeToken<ANY, ANY>
}

@@finalType
EthereumTransactionReceipt extends Receipt {
    @@immutable @@nullable contractId: ContractId                   // the contract that was created or called
}
```

## Default Instances for Testing

```
// The contract, file and accounts of the contract transaction tests of the Java SDK (ContractCreateTransactionTest,
// ContractExecuteTransactionTest, ContractUpdateTransactionTest, ContractDeleteTransactionTest).
values JAVA_SDK_CONTRACT: ContractId = ContractId.fromString(value: "0.0.5007")
values JAVA_SDK_BYTECODE_FILE: Address = Address.fromString(address: "0.0.3003")
values JAVA_SDK_AUTO_RENEW_ACCOUNT: AccountId = AccountId.fromString(value: "0.0.30")
```

## Examples

### Deploy a contract with a constructor argument

```
HieroClient client = ...;
bytes initcode = ...;                               // output of the Solidity compiler

Response<ContractCreateReceipt> response = new ContractCreateTransaction()
    .bytecode(initcode)
    .gas(200_000)
    .constructorParameters(new AbiTuple(List.of(new AbiString("hello"))).encode())
    .adminAuthority(Authority.of(client.operatorPublicKey))
    .signWithOperatorAndSubmit(client);

ContractId contractId = response.queryReceipt(client).contractId;
```

### Call a function and decode its result and logs

```
AbiFunction transfer = AbiFunction.parse("transfer(address,uint256) returns (bool)");
AbiEvent transferEvent = AbiEvent.parse("Transfer(address indexed,address indexed,uint256)");

Response<ContractExecuteReceipt> response = new ContractExecuteTransaction()
    .contractId(tokenContract)
    .gas(100_000)
    .callData(transfer.encodeCall(List.of(new AbiAddress(recipient), new AbiUint(1000))))
    .signWithOperatorAndSubmit(client);

ContractFunctionResult result = response.queryRecord(client).contractResult;
boolean ok = ((AbiBool) transfer.decodeResult(result.result).get(0)).value();
for (ContractLog log : result.logs) {
    List<AbiValue> values = transferEvent.decodeLog(log.topics, log.data);
}
```

### Build the function and the event without a signature

`AbiFunction` and `AbiEvent` are plain values: instead of parsing a signature, they can be built from their name and
the ABI types of their arguments, results and parameters. The result is equal to the parsed one.

```
AbiFunction transfer = new AbiFunction("transfer",
        List.of(new AbiAddressType(), new AbiUintType(256)),      // the arguments
        List.of(new AbiBoolType()));                              // the results

AbiEvent transferEvent = new AbiEvent("Transfer", List.of(
        new AbiEventParameter(new AbiAddressType(), true),        // indexed: a topic of the log
        new AbiEventParameter(new AbiAddressType(), true),
        new AbiEventParameter(new AbiUintType(256), false)),      // not indexed: in the data of the log
        false);                                                   // not anonymous

new ContractExecuteTransaction()
    .contractId(tokenContract)
    .gas(100_000)
    .callData(transfer.encodeCall(List.of(new AbiAddress(recipient), new AbiUint(1000))))
    .signWithOperatorAndSubmit(client);
```

### Relay an Ethereum transaction

```
bytes signedEthereumTransaction = ...;              // RLP-encoded and signed by the Ethereum account

new EthereumTransaction()
    .ethereumData(signedEthereumTransaction)
    .maxGasAllowance(new Hbar(2, HbarUnit.HBAR))
    .signWithOperatorAndSubmit(client);
```

### Delete a contract

```
new ContractDeleteTransaction()
    .contractId(contractId)
    .transferAccountId(client.operatorAccountId)
    .signWithOperatorAndSubmit(client);           // the operator holds the admin authority
```

## Testing

Offline tests of the contract transactions, written as given / when / then: the attributes a transaction keeps, the
defaults of the attributes it leaves out, the attributes it rejects, the packed form of each transaction, and the
encoding of call data and constructor arguments with the ABI types of `evm`. The execution of a contract is tested by
the TCK, not here.

```tests
// Source: this spec (@@default([]) of constructorParameters, @@default(false) of declineStakingReward, @@nullable).
@@testId("a-contract-create-transaction-from-a-file-has-defaults-for-everything-else")
test "a contract create transaction from a file has defaults for everything else" {
    // when
    let transaction = ContractCreateTransaction{bytecodeFileId: JAVA_SDK_BYTECODE_FILE, gas: 0}

    // then
    assertEquals(expected: JAVA_SDK_BYTECODE_FILE, actual: transaction.bytecodeFileId)
    assertNull(actual: transaction.bytecode)
    assertEquals(expected: 0, actual: transaction.gas)
    assertEquals(expected: 0, actual: length(value: transaction.constructorParameters))
    assertNull(actual: transaction.initialBalance)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.contractMemo)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
    assertNull(actual: transaction.maxAutomaticTokenAssociations)
    assertNull(actual: transaction.stakedAccountId)
    assertNull(actual: transaction.stakedNodeId)
    assertFalse(condition: transaction.declineStakingReward)
}

// Source: the Java SDK (ContractCreateTransactionTest.spawnTestTransaction: file 0.0.3003, 1000 tinybars, staked to
// 0.0.3, 101 automatic associations, an auto renew period of Duration.ofHours(10) = 36000 seconds, constructor
// parameters {10, 11, 12, 13, 25}, auto renew account 0.0.30).
@@testId("a-contract-create-transaction-keeps-its-attributes")
test "a contract create transaction keeps its attributes" {
    // given
    let initialBalance: NativeToken<ANY, ANY> = hedera.Hbar{amount: 1000, unit: hedera.HbarUnit.TINYBAR}
    let constructorParameters = hex(text: "0a0b0c0d19")

    // when
    let transaction = ContractCreateTransaction{bytecodeFileId: JAVA_SDK_BYTECODE_FILE, gas: 0,
            constructorParameters: constructorParameters, initialBalance: initialBalance,
            adminAuthority: DEFAULT(Authority), contractMemo: "contract memo", autoRenewPeriod: toSeconds(value: 36000),
            autoRenewAccount: JAVA_SDK_AUTO_RENEW_ACCOUNT, maxAutomaticTokenAssociations: 101,
            stakedAccountId: AccountId.fromString(value: "0.0.3"), declineStakingReward: true}

    // then
    assertEquals(expected: constructorParameters, actual: transaction.constructorParameters)
    assertEquals(expected: initialBalance, actual: transaction.initialBalance)
    assertEquals(expected: DEFAULT(Authority), actual: transaction.adminAuthority)
    assertEquals(expected: "contract memo", actual: transaction.contractMemo)
    assertEquals(expected: toSeconds(value: 36000), actual: transaction.autoRenewPeriod)
    assertEquals(expected: JAVA_SDK_AUTO_RENEW_ACCOUNT, actual: transaction.autoRenewAccount)
    assertEquals(expected: 101, actual: transaction.maxAutomaticTokenAssociations)
    assertEquals(expected: AccountId.fromString(value: "0.0.3"), actual: transaction.stakedAccountId)
    assertTrue(condition: transaction.declineStakingReward)
}

// Source: the Java SDK (ContractCreateTransactionTest.shouldBytes and shouldBytes2: from the file 0.0.3003 and staked
// to 0.0.3, from the bytecode deadbeef and staked to node 4; both with 1000 tinybars, 101 automatic associations, an
// auto renew period of 36000 seconds, constructor parameters {10, 11, 12, 13, 25} and auto renew account 0.0.30).
@@testId("a-contract-create-transaction-survives-tobytes-and-frombytes-from-a-file-or-from-bytecode")
test "a contract create transaction survives toBytes and fromBytes, from a file or from bytecode" {
    // given
    let payer = DEFAULT(consensusnode.client.Account)
    let nodes = [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")]
    let initialBalance: NativeToken<ANY, ANY> = hedera.Hbar{amount: 1000, unit: hedera.HbarUnit.TINYBAR}
    let fromFile = ContractCreateTransaction{bytecodeFileId: JAVA_SDK_BYTECODE_FILE, gas: 0,
            constructorParameters: hex(text: "0a0b0c0d19"), initialBalance: initialBalance,
            adminAuthority: DEFAULT(Authority), autoRenewPeriod: toSeconds(value: 36000),
            autoRenewAccount: JAVA_SDK_AUTO_RENEW_ACCOUNT, maxAutomaticTokenAssociations: 101,
            stakedAccountId: AccountId.fromString(value: "0.0.3")}
    let fromBytecode = ContractCreateTransaction{bytecode: hex(text: "deadbeef"), gas: 0,
            constructorParameters: hex(text: "0a0b0c0d19"), initialBalance: initialBalance,
            adminAuthority: DEFAULT(Authority), autoRenewPeriod: toSeconds(value: 36000),
            autoRenewAccount: JAVA_SDK_AUTO_RENEW_ACCOUNT, maxAutomaticTokenAssociations: 101, stakedNodeId: 4}

    for transaction in [fromFile, fromBytecode] {
        // given
        let packed = transaction.pack(payer: payer, nodes: nodes)

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}

// Source: this spec (@@oneOf(bytecode, bytecodeFileId)); HAPI (ContractCreateTransactionBody.initcodeSource is a
// oneof).
@@testId("a-contract-create-transaction-rejects-both-bytecode-and-a-file-and-neither")
test "a contract create transaction rejects both bytecode and a file, and neither" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractCreateTransaction{bytecode: hex(text: "deadbeef"), bytecodeFileId: JAVA_SDK_BYTECODE_FILE, gas: 0}
    @@expectThrows(invalid-argument-error)
    ContractCreateTransaction{gas: 0}
}

// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (ContractCreateTransactionBody.staked_id is
// a oneof).
@@testId("a-contract-create-transaction-rejects-being-staked-to-an-account-and-to-a-node")
test "a contract create transaction rejects being staked to an account and to a node" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractCreateTransaction{bytecodeFileId: JAVA_SDK_BYTECODE_FILE, gas: 0,
            stakedAccountId: AccountId.fromString(value: "0.0.3"), stakedNodeId: 4}
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (the arguments 69 and true of
// baz(uint32,bool), without the selector).
@@testId("constructor-parameters-are-the-abi-encoded-arguments-without-a-selector")
test "constructor parameters are the ABI-encoded arguments without a selector" {
    // when
    let transaction = ContractCreateTransaction{bytecode: hex(text: "deadbeef"), gas: 0,
            constructorParameters: evm.AbiTuple{values: [evm.AbiUint{value: 69, bits: 32},
                    evm.AbiBool{value: true}]}.encode()}

    // then
    assertEquals(expected: hex(text: "0000000000000000000000000000000000000000000000000000000000000045"
            + "0000000000000000000000000000000000000000000000000000000000000001"),
            actual: transaction.constructorParameters)
}

// Source: the Java SDK (ContractExecuteTransactionTest.spawnTestTransaction: contract 0.0.5007, gas 10, 1000 tinybars,
// function parameters {24, 43, 11}; shouldBytes).
@@testId("a-contract-execute-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
test "a contract execute transaction keeps its attributes and survives toBytes and fromBytes" {
    // given
    let payableAmount: NativeToken<ANY, ANY> = hedera.Hbar{amount: 1000, unit: hedera.HbarUnit.TINYBAR}
    let callData = hex(text: "182b0b")
    let transaction = ContractExecuteTransaction{contractId: JAVA_SDK_CONTRACT, gas: 10, payableAmount: payableAmount,
            callData: callData}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
            nodes: [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: JAVA_SDK_CONTRACT, actual: transaction.contractId)
    assertEquals(expected: 10, actual: transaction.gas)
    assertEquals(expected: payableAmount, actual: transaction.payableAmount)
    assertEquals(expected: callData, actual: transaction.callData)
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@default([]) of callData: empty call data calls the receive or fallback function, @@nullable of
// payableAmount).
@@testId("a-contract-execute-transaction-without-call-data-has-empty-call-data-and-no-amount")
test "a contract execute transaction without call data has empty call data and no amount" {
    // when
    let transaction = ContractExecuteTransaction{contractId: JAVA_SDK_CONTRACT, gas: 10}

    // then
    assertEquals(expected: 0, actual: length(value: transaction.callData))
    assertNull(actual: transaction.payableAmount)
}

// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with 69 and
// true).
@@testId("the-call-data-of-a-contract-execute-transaction-is-the-selector-and-the-abi-encoded-arguments")
test "the call data of a contract execute transaction is the selector and the ABI-encoded arguments" {
    // given
    let baz = evm.AbiFunction.parse(signature: "baz(uint32,bool) returns (bool)")

    // when
    let transaction = ContractExecuteTransaction{contractId: JAVA_SDK_CONTRACT, gas: 10,
            callData: baz.encodeCall(arguments: [evm.AbiUint{value: 69, bits: 32}, evm.AbiBool{value: true}])}

    // then
    assertEquals(expected: hex(text: "cdcd77c0"
            + "0000000000000000000000000000000000000000000000000000000000000045"
            + "0000000000000000000000000000000000000000000000000000000000000001"), actual: transaction.callData)
}

// Source: the Java SDK (ContractExecuteTransactionTest.setGasShouldRejectNegativeValues and
// setGasShouldAcceptZeroAndPositiveValues); this spec (@@min(0) of gas).
@@testId("a-contract-execute-transaction-accepts-zero-gas-and-rejects-negative-gas")
test "a contract execute transaction accepts zero gas and rejects negative gas" {
    // when
    let transaction = ContractExecuteTransaction{contractId: JAVA_SDK_CONTRACT, gas: 0}

    // then
    assertEquals(expected: 0, actual: transaction.gas)

    // when / then
    @@expectThrows(invalid-argument-error)
    ContractExecuteTransaction{contractId: JAVA_SDK_CONTRACT, gas: -1}
}

// Source: this spec (@@nullable: an update changes only what it names).
@@testId("a-contract-update-transaction-changes-nothing-but-what-it-names")
test "a contract update transaction changes nothing but what it names" {
    // when
    let transaction = ContractUpdateTransaction{contractId: JAVA_SDK_CONTRACT}

    // then
    assertEquals(expected: JAVA_SDK_CONTRACT, actual: transaction.contractId)
    assertNull(actual: transaction.adminAuthority)
    assertNull(actual: transaction.contractMemo)
    assertNull(actual: transaction.expirationTime)
    assertNull(actual: transaction.autoRenewPeriod)
    assertNull(actual: transaction.autoRenewAccount)
    assertNull(actual: transaction.maxAutomaticTokenAssociations)
    assertNull(actual: transaction.stakedAccountId)
    assertNull(actual: transaction.stakedNodeId)
    assertNull(actual: transaction.declineStakingReward)
}

// Source: the Java SDK (ContractUpdateTransactionTest.spawnTestTransaction and shouldBytes: contract 0.0.5007, 101
// automatic associations, an auto renew period of Duration.ofDays(1) = 86400 seconds, memo "3", staked to 0.0.3,
// expiration time Instant.ofEpochMilli(4) = 1970-01-01T00:00:00.004Z, auto renew account 0.0.30).
@@testId("a-contract-update-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
test "a contract update transaction keeps its attributes and survives toBytes and fromBytes" {
    // given
    let expirationTime = toZonedDateTime(text: "1970-01-01T00:00:00.004Z")
    let transaction = ContractUpdateTransaction{contractId: JAVA_SDK_CONTRACT, adminAuthority: DEFAULT(Authority),
            contractMemo: "3", expirationTime: expirationTime, autoRenewPeriod: toSeconds(value: 86400),
            autoRenewAccount: JAVA_SDK_AUTO_RENEW_ACCOUNT, maxAutomaticTokenAssociations: 101,
            stakedAccountId: AccountId.fromString(value: "0.0.3")}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
            nodes: [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: DEFAULT(Authority), actual: transaction.adminAuthority)
    assertEquals(expected: "3", actual: transaction.contractMemo)
    assertEquals(expected: expirationTime, actual: transaction.expirationTime)
    assertEquals(expected: toSeconds(value: 86400), actual: transaction.autoRenewPeriod)
    assertEquals(expected: JAVA_SDK_AUTO_RENEW_ACCOUNT, actual: transaction.autoRenewAccount)
    assertEquals(expected: 101, actual: transaction.maxAutomaticTokenAssociations)
    assertEquals(expected: AccountId.fromString(value: "0.0.3"), actual: transaction.stakedAccountId)
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (ContractUpdateTransactionBody.staked_id is
// a oneof).
@@testId("a-contract-update-transaction-rejects-being-staked-to-an-account-and-to-a-node")
test "a contract update transaction rejects being staked to an account and to a node" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractUpdateTransaction{contractId: JAVA_SDK_CONTRACT, stakedAccountId: AccountId.fromString(value: "0.0.3"),
            stakedNodeId: 4}
}

// Source: the Java SDK (ContractDeleteTransactionTest.spawnTestTransaction and shouldBytes: contract 0.0.5007,
// transfer account 0.0.9, transfer contract 0.0.5008), one obtainer at a time as HAPI's oneof obtainers allows.
@@testId("a-contract-delete-transaction-keeps-its-obtainer-and-survives-tobytes-and-frombytes")
test "a contract delete transaction keeps its obtainer and survives toBytes and fromBytes" {
    // given
    let toAccount = ContractDeleteTransaction{contractId: JAVA_SDK_CONTRACT,
            transferAccountId: AccountId.fromString(value: "0.0.9")}
    let toContract = ContractDeleteTransaction{contractId: JAVA_SDK_CONTRACT,
            transferContractId: ContractId.fromString(value: "0.0.5008")}

    // then
    assertEquals(expected: AccountId.fromString(value: "0.0.9"), actual: toAccount.transferAccountId)
    assertNull(actual: toAccount.transferContractId)
    assertEquals(expected: ContractId.fromString(value: "0.0.5008"), actual: toContract.transferContractId)
    assertNull(actual: toContract.transferAccountId)

    for transaction in [toAccount, toContract] {
        // given
        let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
                nodes: [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")])

        // when
        let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

        // then
        assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
        assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
    }
}

// Source: this spec (@@oneOf(transferAccountId, transferContractId)); HAPI (ContractDeleteTransactionBody.obtainers
// is a oneof; the network rejects a delete without one with OBTAINER_REQUIRED).
@@testId("a-contract-delete-transaction-rejects-two-obtainers-and-none")
test "a contract delete transaction rejects two obtainers and none" {
    // when / then
    @@expectThrows(invalid-argument-error)
    ContractDeleteTransaction{contractId: JAVA_SDK_CONTRACT, transferAccountId: AccountId.fromString(value: "0.0.9"),
            transferContractId: ContractId.fromString(value: "0.0.5008")}
    @@expectThrows(invalid-argument-error)
    ContractDeleteTransaction{contractId: JAVA_SDK_CONTRACT}
}

// Source: the Java SDK (EthereumTransactionTest.spawnTestTransaction and shouldBytes: Ethereum data deadbeef, call
// data file 4.5.6, a max gas allowance of Hbar.fromString("3") = 3 hbar).
@@testId("an-ethereum-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
test "an ethereum transaction keeps its attributes and survives toBytes and fromBytes" {
    // given
    let maxGasAllowance: NativeToken<ANY, ANY> = hedera.Hbar{amount: 3, unit: hedera.HbarUnit.HBAR}
    let transaction = EthereumTransaction{ethereumData: hex(text: "deadbeef"),
            callDataFileId: Address.fromString(address: "4.5.6"), maxGasAllowance: maxGasAllowance}

    // when
    let packed = transaction.pack(payer: DEFAULT(consensusnode.client.Account),
            nodes: [AccountId.fromString(value: "0.0.5005"), AccountId.fromString(value: "0.0.5006")])
    let restored = consensusnode.transactions.PackedTransaction.fromBytes(bytes: packed.toBytes())

    // then
    assertEquals(expected: hex(text: "deadbeef"), actual: transaction.ethereumData)
    assertEquals(expected: Address.fromString(address: "4.5.6"), actual: transaction.callDataFileId)
    assertEquals(expected: maxGasAllowance, actual: transaction.maxGasAllowance)
    assertEquals(expected: packed.toBytes(), actual: restored.toBytes())
    assertEquals(expected: packed.signableBodies(), actual: restored.signableBodies())
}

// Source: this spec (@@minSize(1) of ethereumData: there is no Ethereum transaction without bytes).
@@testId("an-ethereum-transaction-rejects-empty-ethereum-data")
test "an ethereum transaction rejects empty ethereum data" {
    // when / then
    @@expectThrows(invalid-argument-error)
    EthereumTransaction{ethereumData: hex(text: "")}
}
```

## Questions & Comments

- **Which contract does the receipt of an `EthereumTransaction` name?** HAPI's `TransactionReceipt.contractID` is
  documented for contract creation only. The consensus node also sets it for an Ethereum transaction that calls a
  contract, which is why `EthereumTransactionReceipt.contractId` is "created or called" and nullable (a transfer of
  native tokens to an account has none). This has to be confirmed against the consensus node and the TCK.

- **Record attributes of an `EthereumTransaction`.** The record carries the Keccak-256 hash of the Ethereum
  transaction (`TransactionRecord.ethereum_hash`), with which the transaction is found in Ethereum tooling (JSON-RPC
  relay, block explorers). It is not exposed yet: it is a record attribute of one transaction type, the case of the
  open question on typed records in `docs/missing-features.md` §3.5. The signer nonce is exposed through
  `ContractFunctionResult.signerNonce`.

- **No decoding of the Ethereum data.** `ethereumData` is passed through as bytes; the SDK neither builds nor decodes
  RLP-encoded Ethereum transactions; building and signing one is the job of an Ethereum library. Should the SDK at
  least validate `ethereumData` before packing (an RLP-encoded transaction of a known type, empty call data
  when `callDataFileId` is set)?

- **What does a failed or reverted execution return?** For a reverted call, HAPI puts the revert data into
  `ContractFunctionResult.errorMessage` as hex text (e.g. `0x08c379a0…` for `Error(string)`), while `result`
  may be empty. Whether `errorMessage` should be offered as bytes (to be decoded with
  `AbiFunction.parse("Error(string)").decodeCall(...)`) or stay text as in HAPI is open.

- **Bytecode that does not fit into one transaction.** `ContractCreateTransaction.bytecode` is limited by the size
  of a transaction (6 KiB, more with HIP-1300 jumbo transactions); larger initcode has to be uploaded with
  `FileCreateTransaction` and `FileAppendTransaction` first. The maximum size of `bytecode` is not stated as an annotation because it depends on the size of
  the rest of the transaction.

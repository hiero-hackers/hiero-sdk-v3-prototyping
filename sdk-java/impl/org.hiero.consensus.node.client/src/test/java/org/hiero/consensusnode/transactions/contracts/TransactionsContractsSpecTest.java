package org.hiero.consensusnode.transactions.contracts;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.transactions.PackedTransaction;
import org.hiero.evm.AbiBool;
import org.hiero.evm.AbiFunction;
import org.hiero.evm.AbiTuple;
import org.hiero.evm.AbiUint;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-contracts`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-contracts")
class TransactionsContractsSpecTest {

    /// The contract, file and accounts of the contract transaction tests of the Java SDK (ContractCreateTransactionTest,
    /// ContractExecuteTransactionTest, ContractUpdateTransactionTest, ContractDeleteTransactionTest).
    private static ContractId JAVA_SDK_CONTRACT() {
        return ContractId.fromString("0.0.5007");
    }

    private static Address JAVA_SDK_BYTECODE_FILE() {
        return Address.fromString("0.0.3003");
    }

    private static AccountId JAVA_SDK_AUTO_RENEW_ACCOUNT() {
        return AccountId.fromString("0.0.30");
    }

    /// `consensusnode.transactions.contracts/a-contract-create-transaction-from-a-file-has-defaults-for-everything-else`
    ///
    /// Source: this spec (@@default([]) of constructorParameters, @@default(false) of declineStakingReward, @@nullable).
    @Test
    @DisplayName("a contract create transaction from a file has defaults for everything else")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-create-transaction-from-a-file-has-defaults-for-everything-else")
    void aContractCreateTransactionFromAFileHasDefaultsForEverythingElse() {
        // when
        final ContractCreateTransaction transaction = new ContractCreateTransaction(null, JAVA_SDK_BYTECODE_FILE(), 0L, new byte[0], null, null, null, null, null, null, null, null, false);

        // then
        assertEquals(JAVA_SDK_BYTECODE_FILE(), transaction.bytecodeFileId());
        assertNull(transaction.bytecode());
        assertEquals(0L, transaction.gas());
        assertEquals(0L, length(transaction.constructorParameters()));
        assertNull(transaction.initialBalance());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.contractMemo());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
        assertNull(transaction.maxAutomaticTokenAssociations());
        assertNull(transaction.stakedAccountId());
        assertNull(transaction.stakedNodeId());
        assertFalse(transaction.declineStakingReward());
    }

    /// `consensusnode.transactions.contracts/a-contract-create-transaction-keeps-its-attributes`
    ///
    /// Source: the Java SDK (ContractCreateTransactionTest.spawnTestTransaction: file 0.0.3003, 1000 tinybars, staked to
    /// 0.0.3, 101 automatic associations, an auto renew period of Duration.ofHours(10) = 36000 seconds, constructor
    /// parameters {10, 11, 12, 13, 25}, auto renew account 0.0.30).
    @Test
    @DisplayName("a contract create transaction keeps its attributes")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-create-transaction-keeps-its-attributes")
    void aContractCreateTransactionKeepsItsAttributes() {
        // given
        final NativeToken<?, ?> initialBalance = new Hbar(1000L, HbarUnit.TINYBAR);
        final byte[] constructorParameters = hex("0a0b0c0d19");

        // when
        final ContractCreateTransaction transaction = new ContractCreateTransaction(null, JAVA_SDK_BYTECODE_FILE(), 0L, constructorParameters, initialBalance, defaultAuthority(), "contract memo", toSeconds(36000L), JAVA_SDK_AUTO_RENEW_ACCOUNT(), 101, AccountId.fromString("0.0.3"), null, true);

        // then
        assertEquals(constructorParameters, transaction.constructorParameters());
        assertEquals(initialBalance, transaction.initialBalance());
        assertEquals(defaultAuthority(), transaction.adminAuthority());
        assertEquals("contract memo", transaction.contractMemo());
        assertEquals(toSeconds(36000L), transaction.autoRenewPeriod());
        assertEquals(JAVA_SDK_AUTO_RENEW_ACCOUNT(), transaction.autoRenewAccount());
        assertEquals(101, transaction.maxAutomaticTokenAssociations());
        assertEquals(AccountId.fromString("0.0.3"), transaction.stakedAccountId());
        assertTrue(transaction.declineStakingReward());
    }

    /// `consensusnode.transactions.contracts/a-contract-create-transaction-survives-tobytes-and-frombytes-from-a-file-or-from-bytecode`
    ///
    /// Source: the Java SDK (ContractCreateTransactionTest.shouldBytes and shouldBytes2: from the file 0.0.3003 and staked
    /// to 0.0.3, from the bytecode deadbeef and staked to node 4; both with 1000 tinybars, 101 automatic associations, an
    /// auto renew period of 36000 seconds, constructor parameters {10, 11, 12, 13, 25} and auto renew account 0.0.30).
    @Test
    @DisplayName("a contract create transaction survives toBytes and fromBytes, from a file or from bytecode")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-create-transaction-survives-tobytes-and-frombytes-from-a-file-or-from-bytecode")
    void aContractCreateTransactionSurvivesToBytesAndFromBytesFromAFileOrFromBytecode() {
        // given
        final Account payer = defaultAccount();
        final List<AccountId> nodes = List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006"));
        final NativeToken<?, ?> initialBalance = new Hbar(1000L, HbarUnit.TINYBAR);
        final ContractCreateTransaction fromFile = new ContractCreateTransaction(null, JAVA_SDK_BYTECODE_FILE(), 0L, hex("0a0b0c0d19"), initialBalance, defaultAuthority(), null, toSeconds(36000L), JAVA_SDK_AUTO_RENEW_ACCOUNT(), 101, AccountId.fromString("0.0.3"), null, false);
        final ContractCreateTransaction fromBytecode = new ContractCreateTransaction(hex("deadbeef"), null, 0L, hex("0a0b0c0d19"), initialBalance, defaultAuthority(), null, toSeconds(36000L), JAVA_SDK_AUTO_RENEW_ACCOUNT(), 101, null, 4L, false);
        for (final ContractCreateTransaction transaction : List.of(fromFile, fromBytecode)) {
            // given
            final PackedTransaction<ContractCreateReceipt, ContractCreateTransaction> packed = transaction.pack(payer, nodes);

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    /// `consensusnode.transactions.contracts/a-contract-create-transaction-rejects-both-bytecode-and-a-file-and-neither`
    ///
    /// Source: this spec (@@oneOf(bytecode, bytecodeFileId)); HAPI (ContractCreateTransactionBody.initcodeSource is a
    /// oneof).
    @Test
    @DisplayName("a contract create transaction rejects both bytecode and a file, and neither")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-create-transaction-rejects-both-bytecode-and-a-file-and-neither")
    void aContractCreateTransactionRejectsBothBytecodeAndAFileAndNeither() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(hex("deadbeef"), JAVA_SDK_BYTECODE_FILE(), 0L, new byte[0], null, null, null, null, null, null, null, null, false));
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(null, null, 0L, new byte[0], null, null, null, null, null, null, null, null, false));
    }

    /// `consensusnode.transactions.contracts/a-contract-create-transaction-rejects-being-staked-to-an-account-and-to-a-node`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (ContractCreateTransactionBody.staked_id is
    /// a oneof).
    @Test
    @DisplayName("a contract create transaction rejects being staked to an account and to a node")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-create-transaction-rejects-being-staked-to-an-account-and-to-a-node")
    void aContractCreateTransactionRejectsBeingStakedToAnAccountAndToANode() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateTransaction(null, JAVA_SDK_BYTECODE_FILE(), 0L, new byte[0], null, null, null, null, null, null, AccountId.fromString("0.0.3"), 4L, false));
    }

    /// `consensusnode.transactions.contracts/constructor-parameters-are-the-abi-encoded-arguments-without-a-selector`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (the arguments 69 and true of
    /// baz(uint32,bool), without the selector).
    @Test
    @DisplayName("constructor parameters are the ABI-encoded arguments without a selector")
    @Tag("testId:consensusnode.transactions.contracts/constructor-parameters-are-the-abi-encoded-arguments-without-a-selector")
    void constructorParametersAreTheABIEncodedArgumentsWithoutASelector() {
        // when
        final ContractCreateTransaction transaction = new ContractCreateTransaction(hex("deadbeef"), null, 0L, new AbiTuple(List.of(new AbiUint(new BigInteger("69"), 32), new AbiBool(true))).encode(), null, null, null, null, null, null, null, null, false);

        // then
        assertEquals(hex("0000000000000000000000000000000000000000000000000000000000000045" + "0000000000000000000000000000000000000000000000000000000000000001"), transaction.constructorParameters());
    }

    /// `consensusnode.transactions.contracts/a-contract-execute-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (ContractExecuteTransactionTest.spawnTestTransaction: contract 0.0.5007, gas 10, 1000 tinybars,
    /// function parameters {24, 43, 11}; shouldBytes).
    @Test
    @DisplayName("a contract execute transaction keeps its attributes and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-execute-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
    void aContractExecuteTransactionKeepsItsAttributesAndSurvivesToBytesAndFromBytes() {
        // given
        final NativeToken<?, ?> payableAmount = new Hbar(1000L, HbarUnit.TINYBAR);
        final byte[] callData = hex("182b0b");
        final ContractExecuteTransaction transaction = new ContractExecuteTransaction(JAVA_SDK_CONTRACT(), 10L, payableAmount, callData);

        // when
        final PackedTransaction<ContractExecuteReceipt, ContractExecuteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(JAVA_SDK_CONTRACT(), transaction.contractId());
        assertEquals(10L, transaction.gas());
        assertEquals(payableAmount, transaction.payableAmount());
        assertEquals(callData, transaction.callData());
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.contracts/a-contract-execute-transaction-without-call-data-has-empty-call-data-and-no-amount`
    ///
    /// Source: this spec (@@default([]) of callData: empty call data calls the receive or fallback function, @@nullable of
    /// payableAmount).
    @Test
    @DisplayName("a contract execute transaction without call data has empty call data and no amount")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-execute-transaction-without-call-data-has-empty-call-data-and-no-amount")
    void aContractExecuteTransactionWithoutCallDataHasEmptyCallDataAndNoAmount() {
        // when
        final ContractExecuteTransaction transaction = new ContractExecuteTransaction(JAVA_SDK_CONTRACT(), 10L, null, new byte[0]);

        // then
        assertEquals(0L, length(transaction.callData()));
        assertNull(transaction.payableAmount());
    }

    /// `consensusnode.transactions.contracts/the-call-data-of-a-contract-execute-transaction-is-the-selector-and-the-abi-encoded-arguments`
    ///
    /// Source: the Solidity documentation, "Contract ABI Specification", section "Examples" (baz(uint32,bool) with 69 and
    /// true).
    @Test
    @DisplayName("the call data of a contract execute transaction is the selector and the ABI-encoded arguments")
    @Tag("testId:consensusnode.transactions.contracts/the-call-data-of-a-contract-execute-transaction-is-the-selector-and-the-abi-encoded-arguments")
    void theCallDataOfAContractExecuteTransactionIsTheSelectorAndTheABIEncodedArguments() {
        // given
        final AbiFunction baz = AbiFunction.parse("baz(uint32,bool) returns (bool)");

        // when
        final ContractExecuteTransaction transaction = new ContractExecuteTransaction(JAVA_SDK_CONTRACT(), 10L, null, baz.encodeCall(List.of(new AbiUint(new BigInteger("69"), 32), new AbiBool(true))));

        // then
        assertEquals(hex(("cdcd77c0" + "0000000000000000000000000000000000000000000000000000000000000045") + "0000000000000000000000000000000000000000000000000000000000000001"), transaction.callData());
    }

    /// `consensusnode.transactions.contracts/a-contract-execute-transaction-accepts-zero-gas-and-rejects-negative-gas`
    ///
    /// Source: the Java SDK (ContractExecuteTransactionTest.setGasShouldRejectNegativeValues and
    /// setGasShouldAcceptZeroAndPositiveValues); this spec (@@min(0) of gas).
    @Test
    @DisplayName("a contract execute transaction accepts zero gas and rejects negative gas")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-execute-transaction-accepts-zero-gas-and-rejects-negative-gas")
    void aContractExecuteTransactionAcceptsZeroGasAndRejectsNegativeGas() {
        // when
        final ContractExecuteTransaction transaction = new ContractExecuteTransaction(JAVA_SDK_CONTRACT(), 0L, null, new byte[0]);

        // then
        assertEquals(0L, transaction.gas());

        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractExecuteTransaction(JAVA_SDK_CONTRACT(), -1L, null, new byte[0]));
    }

    /// `consensusnode.transactions.contracts/a-contract-update-transaction-changes-nothing-but-what-it-names`
    ///
    /// Source: this spec (@@nullable: an update changes only what it names).
    @Test
    @DisplayName("a contract update transaction changes nothing but what it names")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-update-transaction-changes-nothing-but-what-it-names")
    void aContractUpdateTransactionChangesNothingButWhatItNames() {
        // when
        final ContractUpdateTransaction transaction = new ContractUpdateTransaction(JAVA_SDK_CONTRACT(), null, null, null, null, null, null, null, null, null);

        // then
        assertEquals(JAVA_SDK_CONTRACT(), transaction.contractId());
        assertNull(transaction.adminAuthority());
        assertNull(transaction.contractMemo());
        assertNull(transaction.expirationTime());
        assertNull(transaction.autoRenewPeriod());
        assertNull(transaction.autoRenewAccount());
        assertNull(transaction.maxAutomaticTokenAssociations());
        assertNull(transaction.stakedAccountId());
        assertNull(transaction.stakedNodeId());
        assertNull(transaction.declineStakingReward());
    }

    /// `consensusnode.transactions.contracts/a-contract-update-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (ContractUpdateTransactionTest.spawnTestTransaction and shouldBytes: contract 0.0.5007, 101
    /// automatic associations, an auto renew period of Duration.ofDays(1) = 86400 seconds, memo "3", staked to 0.0.3,
    /// expiration time Instant.ofEpochMilli(4) = 1970-01-01T00:00:00.004Z, auto renew account 0.0.30).
    @Test
    @DisplayName("a contract update transaction keeps its attributes and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-update-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
    void aContractUpdateTransactionKeepsItsAttributesAndSurvivesToBytesAndFromBytes() {
        // given
        final ZonedDateTime expirationTime = toZonedDateTime("1970-01-01T00:00:00.004Z");
        final ContractUpdateTransaction transaction = new ContractUpdateTransaction(JAVA_SDK_CONTRACT(), defaultAuthority(), "3", expirationTime, toSeconds(86400L), JAVA_SDK_AUTO_RENEW_ACCOUNT(), 101, AccountId.fromString("0.0.3"), null, null);

        // when
        final PackedTransaction<ContractUpdateReceipt, ContractUpdateTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(defaultAuthority(), transaction.adminAuthority());
        assertEquals("3", transaction.contractMemo());
        assertEquals(expirationTime, transaction.expirationTime());
        assertEquals(toSeconds(86400L), transaction.autoRenewPeriod());
        assertEquals(JAVA_SDK_AUTO_RENEW_ACCOUNT(), transaction.autoRenewAccount());
        assertEquals(101, transaction.maxAutomaticTokenAssociations());
        assertEquals(AccountId.fromString("0.0.3"), transaction.stakedAccountId());
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.contracts/a-contract-update-transaction-rejects-being-staked-to-an-account-and-to-a-node`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI (ContractUpdateTransactionBody.staked_id is
    /// a oneof).
    @Test
    @DisplayName("a contract update transaction rejects being staked to an account and to a node")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-update-transaction-rejects-being-staked-to-an-account-and-to-a-node")
    void aContractUpdateTransactionRejectsBeingStakedToAnAccountAndToANode() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractUpdateTransaction(JAVA_SDK_CONTRACT(), null, null, null, null, null, null, AccountId.fromString("0.0.3"), 4L, null));
    }

    /// `consensusnode.transactions.contracts/a-contract-delete-transaction-keeps-its-obtainer-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (ContractDeleteTransactionTest.spawnTestTransaction and shouldBytes: contract 0.0.5007,
    /// transfer account 0.0.9, transfer contract 0.0.5008), one obtainer at a time as HAPI's oneof obtainers allows.
    @Test
    @DisplayName("a contract delete transaction keeps its obtainer and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-delete-transaction-keeps-its-obtainer-and-survives-tobytes-and-frombytes")
    void aContractDeleteTransactionKeepsItsObtainerAndSurvivesToBytesAndFromBytes() {
        // given
        final ContractDeleteTransaction toAccount = new ContractDeleteTransaction(JAVA_SDK_CONTRACT(), AccountId.fromString("0.0.9"), null);
        final ContractDeleteTransaction toContract = new ContractDeleteTransaction(JAVA_SDK_CONTRACT(), null, ContractId.fromString("0.0.5008"));

        // then
        assertEquals(AccountId.fromString("0.0.9"), toAccount.transferAccountId());
        assertNull(toAccount.transferContractId());
        assertEquals(ContractId.fromString("0.0.5008"), toContract.transferContractId());
        assertNull(toContract.transferAccountId());
        for (final ContractDeleteTransaction transaction : List.of(toAccount, toContract)) {
            // given
            final PackedTransaction<ContractDeleteReceipt, ContractDeleteTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")));

            // when
            final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

            // then
            assertEquals(packed.toBytes(), restored.toBytes());
            assertEquals(packed.signableBodies(), restored.signableBodies());
        }
    }

    /// `consensusnode.transactions.contracts/a-contract-delete-transaction-rejects-two-obtainers-and-none`
    ///
    /// Source: this spec (@@oneOf(transferAccountId, transferContractId)); HAPI (ContractDeleteTransactionBody.obtainers
    /// is a oneof; the network rejects a delete without one with OBTAINER_REQUIRED).
    @Test
    @DisplayName("a contract delete transaction rejects two obtainers and none")
    @Tag("testId:consensusnode.transactions.contracts/a-contract-delete-transaction-rejects-two-obtainers-and-none")
    void aContractDeleteTransactionRejectsTwoObtainersAndNone() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractDeleteTransaction(JAVA_SDK_CONTRACT(), AccountId.fromString("0.0.9"), ContractId.fromString("0.0.5008")));
        assertThrows(IllegalArgumentException.class, () -> new ContractDeleteTransaction(JAVA_SDK_CONTRACT(), null, null));
    }

    /// `consensusnode.transactions.contracts/an-ethereum-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes`
    ///
    /// Source: the Java SDK (EthereumTransactionTest.spawnTestTransaction and shouldBytes: Ethereum data deadbeef, call
    /// data file 4.5.6, a max gas allowance of Hbar.fromString("3") = 3 hbar).
    @Test
    @DisplayName("an ethereum transaction keeps its attributes and survives toBytes and fromBytes")
    @Tag("testId:consensusnode.transactions.contracts/an-ethereum-transaction-keeps-its-attributes-and-survives-tobytes-and-frombytes")
    void anEthereumTransactionKeepsItsAttributesAndSurvivesToBytesAndFromBytes() {
        // given
        final NativeToken<?, ?> maxGasAllowance = new Hbar(3L, HbarUnit.HBAR);
        final EthereumTransaction transaction = new EthereumTransaction(hex("deadbeef"), Address.fromString("4.5.6"), maxGasAllowance);

        // when
        final PackedTransaction<EthereumTransactionReceipt, EthereumTransaction> packed = transaction.pack(defaultAccount(), List.of(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")));
        final PackedTransaction<?, ?> restored = PackedTransaction.fromBytes(packed.toBytes());

        // then
        assertEquals(hex("deadbeef"), transaction.ethereumData());
        assertEquals(Address.fromString("4.5.6"), transaction.callDataFileId());
        assertEquals(maxGasAllowance, transaction.maxGasAllowance());
        assertEquals(packed.toBytes(), restored.toBytes());
        assertEquals(packed.signableBodies(), restored.signableBodies());
    }

    /// `consensusnode.transactions.contracts/an-ethereum-transaction-rejects-empty-ethereum-data`
    ///
    /// Source: this spec (@@minSize(1) of ethereumData: there is no Ethereum transaction without bytes).
    @Test
    @DisplayName("an ethereum transaction rejects empty ethereum data")
    @Tag("testId:consensusnode.transactions.contracts/an-ethereum-transaction-rejects-empty-ethereum-data")
    void anEthereumTransactionRejectsEmptyEthereumData() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new EthereumTransaction(hex(""), null, null));
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static Account defaultAccount() {
        return new Account(defaultAccountId(), defaultPrivateKey());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}

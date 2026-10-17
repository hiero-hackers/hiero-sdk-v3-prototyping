package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.ContractId;
import org.hiero.ledger.EvmAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-contract`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-contract")
class ServiceContractSpecTest {

    /// `enterprise.service.contract/contract-parameters-with-bytecode-from-a-file-have-the-defaults-of-the-spec`
    ///
    /// Source: this spec (@@oneOf(fileId, bytecode), @@default and @@nullable of ContractCreateParams); the Python SDK
    /// (contract_create_transaction_test.py, test_constructor_default_values: no bytecode, gas, admin key, memo or staking
    /// target).
    @Test
    @DisplayName("contract parameters with bytecode from a file have the defaults of the spec")
    @Tag("testId:enterprise.service.contract/contract-parameters-with-bytecode-from-a-file-have-the-defaults-of-the-spec")
    void contractParametersWithBytecodeFromAFileHaveTheDefaultsOfTheSpec() {
        // when
        final ContractCreateParams params = defaultContractCreateParams();

        // then
        assertEquals(defaultAddress(), params.fileId());
        assertNull(params.bytecode());
        assertEquals(0L, length(params.constructorParams()));
        assertFalse(params.declineStakingReward());
        assertNull(params.gas());
        assertNull(params.initialBalance());
        assertNull(params.adminAuthority());
        assertNull(params.contractMemo());
        assertNull(params.autoRenewPeriod());
        assertNull(params.autoRenewAccount());
        assertNull(params.maxAutomaticTokenAssociations());
        assertNull(params.stakedAccountId());
        assertNull(params.stakedNodeId());
    }

    /// `enterprise.service.contract/contract-parameters-take-the-bytecode-directly-instead-of-from-a-file`
    ///
    /// Source: this spec (@@oneOf(fileId, bytecode)); the Python SDK (contract_create_transaction_test.py,
    /// test_build_transaction_body_with_bytecode and test_bytecode_setters_clear_each_other).
    @Test
    @DisplayName("contract parameters take the bytecode directly instead of from a file")
    @Tag("testId:enterprise.service.contract/contract-parameters-take-the-bytecode-directly-instead-of-from-a-file")
    void contractParametersTakeTheBytecodeDirectlyInsteadOfFromAFile() {
        // given
        final byte[] bytecode = hex("608060405234801561001057600080fd5b50");

        // when
        final ContractCreateParams params = new ContractCreateParams(null, bytecode, List.of(), null, null, null, null, null, null, null, null, null, false);

        // then
        assertEquals(bytecode, params.bytecode());
        assertNull(params.fileId());
    }

    /// `enterprise.service.contract/contract-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the ContractCreateTransaction unit tests of the Java (ContractCreateTransactionTest), Go
    /// (contract_create_transaction_unit_test.go), C++ (ContractCreateTransactionUnitTests), Swift
    /// (ContractCreateTransactionUnitTests) and Python (contract_create_transaction_test.py, test_constructor_with_parameters)
    /// SDKs; hiero-enterprise-java (ProtocolLayerDataCreationTests, testContractCreateRequestCreation: a file id, gas, an
    /// admin key and constructor parameters): every value that is set is read back unchanged. The auto-renew period is the
    /// one of the Java test (Duration.ofHours(10), which is exactly 36000 seconds).
    @Test
    @DisplayName("contract parameters keep the values they are created with")
    @Tag("testId:enterprise.service.contract/contract-parameters-keep-the-values-they-are-created-with")
    void contractParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final Duration autoRenewPeriod = toSeconds(36000L);
        final AccountId account = defaultAccountId();
        final Param<String, ?> constructorParam = ContractFactory.ofString("Hello from Hiero.");

        // when
        final ContractCreateParams params = new ContractCreateParams(defaultAddress(), null, List.of(constructorParam), 1_000_000L, null, authority, "memo", autoRenewPeriod, account, 10, null, 3L, true);

        // then
        assertEquals(1L, length(params.constructorParams()));
        assertEquals(1_000_000L, params.gas());
        assertEquals(authority, params.adminAuthority());
        assertEquals("memo", params.contractMemo());
        assertEquals(autoRenewPeriod, params.autoRenewPeriod());
        assertEquals(account, params.autoRenewAccount());
        assertEquals(10, params.maxAutomaticTokenAssociations());
        assertEquals(3L, params.stakedNodeId());
        assertTrue(params.declineStakingReward());
    }

    /// `enterprise.service.contract/contract-parameters-accept-a-gas-of-1-and-a-memo-of-100-characters`
    ///
    /// Source: this spec (@@min(1) of gas and @@maxBytes(100) of contractMemo); HAPI limits a memo to 100 bytes, which
    /// ASCII text of 100 characters is.
    @Test
    @DisplayName("contract parameters accept a gas of 1 and a memo of 100 characters")
    @Tag("testId:enterprise.service.contract/contract-parameters-accept-a-gas-of-1-and-a-memo-of-100-characters")
    void contractParametersAcceptAGasOf1AndAMemoOf100Characters() {
        // given
        final String memo = repeat("a", 100L);

        // when
        final ContractCreateParams params = new ContractCreateParams(defaultAddress(), null, List.of(), 1L, null, null, memo, null, null, null, null, null, false);

        // then
        assertEquals(1L, params.gas());
        assertEquals(memo, params.contractMemo());
    }

    /// `enterprise.service.contract/contract-parameters-reject-a-gas-of-0`
    ///
    /// Source: this spec (@@min(1) of gas); the Python SDK (contract_create_transaction_test.py,
    /// test_set_gas_rejects_negative_values and test_constructor_rejects_negative_gas reject a negative gas).
    @Test
    @DisplayName("contract parameters reject a gas of 0")
    @Tag("testId:enterprise.service.contract/contract-parameters-reject-a-gas-of-0")
    void contractParametersRejectAGasOf0() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(defaultAddress(), null, List.of(), 0L, null, null, null, null, null, null, null, null, false));
    }

    /// `enterprise.service.contract/contract-parameters-reject-a-memo-of-101-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of contractMemo); HAPI limits a memo to 100 bytes.
    @Test
    @DisplayName("contract parameters reject a memo of 101 characters")
    @Tag("testId:enterprise.service.contract/contract-parameters-reject-a-memo-of-101-characters")
    void contractParametersRejectAMemoOf101Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(defaultAddress(), null, List.of(), null, null, null, repeat("a", 101L), null, null, null, null, null, false));
    }

    /// `enterprise.service.contract/contract-parameters-with-bytecode-both-from-a-file-and-directly-are-rejected`
    ///
    /// Source: this spec (@@oneOf(fileId, bytecode)); HAPI contract_create.proto (initcodeSource is a oneof of fileID and
    /// initcode).
    @Test
    @DisplayName("contract parameters with bytecode both from a file and directly are rejected")
    @Tag("testId:enterprise.service.contract/contract-parameters-with-bytecode-both-from-a-file-and-directly-are-rejected")
    void contractParametersWithBytecodeBothFromAFileAndDirectlyAreRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(defaultAddress(), hex("608060405234801561001057600080fd5b50"), List.of(), null, null, null, null, null, null, null, null, null, false));
    }

    /// `enterprise.service.contract/contract-parameters-without-bytecode-are-rejected`
    ///
    /// Source: this spec (@@oneOf(fileId, bytecode): the bytecode is required).
    @Test
    @DisplayName("contract parameters without bytecode are rejected")
    @Tag("testId:enterprise.service.contract/contract-parameters-without-bytecode-are-rejected")
    void contractParametersWithoutBytecodeAreRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(null, null, List.of(), null, null, null, null, null, null, null, null, null, false));
    }

    /// `enterprise.service.contract/a-contract-is-staked-to-an-account-to-a-node-or-to-neither`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); the Python SDK (contract_create_transaction_test.py,
    /// test_staked_setters_clear_each_other).
    @Test
    @DisplayName("a contract is staked to an account, to a node or to neither")
    @Tag("testId:enterprise.service.contract/a-contract-is-staked-to-an-account-to-a-node-or-to-neither")
    void aContractIsStakedToAnAccountToANodeOrToNeither() {
        // given
        final AccountId stakedAccount = defaultAccountId();

        // when
        final ContractCreateParams toAccount = new ContractCreateParams(defaultAddress(), null, List.of(), null, null, null, null, null, null, null, stakedAccount, null, false);
        final ContractCreateParams toNode = new ContractCreateParams(defaultAddress(), null, List.of(), null, null, null, null, null, null, null, null, 1L, false);
        final ContractCreateParams toNeither = defaultContractCreateParams();

        // then
        assertEquals(stakedAccount, toAccount.stakedAccountId());
        assertNull(toAccount.stakedNodeId());
        assertEquals(1L, toNode.stakedNodeId());
        assertNull(toNode.stakedAccountId());
        assertNull(toNeither.stakedAccountId());
        assertNull(toNeither.stakedNodeId());
    }

    /// `enterprise.service.contract/contract-parameters-staked-to-both-an-account-and-a-node-are-rejected`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI contract_create.proto (staked_id is a oneof).
    @Test
    @DisplayName("contract parameters staked to both an account and a node are rejected")
    @Tag("testId:enterprise.service.contract/contract-parameters-staked-to-both-an-account-and-a-node-are-rejected")
    void contractParametersStakedToBothAnAccountAndANodeAreRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(defaultAddress(), null, List.of(), null, null, null, null, null, null, null, defaultAccountId(), 3L, false));
    }

    /// `enterprise.service.contract/a-parameter-keeps-the-value-it-wraps`
    ///
    /// Source: this spec (a Param wraps the value of the host language); hiero-enterprise-java (ContractParam: the value of
    /// a parameter is the given value).
    @Test
    @DisplayName("a parameter keeps the value it wraps")
    @Tag("testId:enterprise.service.contract/a-parameter-keeps-the-value-it-wraps")
    void aParameterKeepsTheValueItWraps() {
        // given
        final String text = "Hello from Hiero.";
        final byte[] data = hex("0a1b2c3d");
        final String hexAddress = "0x5aAeb6053F3E94C9b9A09f33669435E7Ef1BeAed";
        final EvmAddress evmAddress = EvmAddress.fromString(hexAddress);

        // when / then
        assertEquals(text, ContractFactory.ofString(text).value());
        assertEquals(text, ContractFactory.ofBytes(text).value());
        assertEquals(data, ContractFactory.ofBytes(data).value());
        assertEquals(hexAddress, ContractFactory.ofAddress(hexAddress).value());
        assertEquals(evmAddress, ContractFactory.ofAddress(evmAddress).value());
        assertTrue(ContractFactory.ofBool(true).value());
        assertFalse(ContractFactory.ofBool(false).value());
    }

    /// `enterprise.service.contract/integer-parameters-accept-the-smallest-and-the-largest-value-of-their-solidity-type`
    ///
    /// Source: hiero-enterprise-java (ContractParamTests: testUint8Range, testInt8Range, testUint256Range and
    /// testInt256Range accept the smallest and the largest value of each type); the Solidity documentation (an intN holds
    /// -2^(N-1) to 2^(N-1)-1, a uintN 0 to 2^N-1). The 256-bit bounds were computed with Python (2**256-1, -2**255,
    /// 2**255-1).
    @Test
    @DisplayName("integer parameters accept the smallest and the largest value of their Solidity type")
    @Tag("testId:enterprise.service.contract/integer-parameters-accept-the-smallest-and-the-largest-value-of-their-solidity-type")
    void integerParametersAcceptTheSmallestAndTheLargestValueOfTheirSolidityType() {
        // when / then
        assertEquals((short) 0, ContractFactory.uint8((short) 0).value());
        assertEquals((short) 255, ContractFactory.uint8((short) 255).value());
        assertEquals((byte) -128, ContractFactory.int8((byte) -128).value());
        assertEquals((byte) 127, ContractFactory.int8((byte) 127).value());
        assertEquals(new BigInteger("0"), ContractFactory.uint256(new BigInteger("0")).value());
        assertEquals(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935"), ContractFactory.uint256(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007913129639935")).value());
        assertEquals(new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968"), ContractFactory.int256(new BigInteger("-57896044618658097711785492504343953926634992332820282019728792003956564819968")).value());
        assertEquals(new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967"), ContractFactory.int256(new BigInteger("57896044618658097711785492504343953926634992332820282019728792003956564819967")).value());
    }

    /// `enterprise.service.contract/contracts-with-the-same-contract-id-are-equal`
    ///
    /// Source: this spec (Contract is a value of its contract id).
    @Test
    @DisplayName("contracts with the same contract id are equal")
    @Tag("testId:enterprise.service.contract/contracts-with-the-same-contract-id-are-equal")
    void contractsWithTheSameContractIdAreEqual() {
        // given
        final ContractId contractId = defaultContractId();

        // when
        final Contract contract = new Contract(contractId);

        // then
        assertEquals(contractId, contract.contractId());
        assertEquals(defaultContract(), contract);
        assertNotEquals(new Contract(new ContractId(0L, 0L, "", 1002L, null)), contract);
    }

    private static ContractCreateParams defaultContractCreateParams() {
        return new ContractCreateParams(defaultAddress(), null, List.of(), null, null, null, null, null, null, null, null, null, false);
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
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

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static ContractId defaultContractId() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static Contract defaultContract() {
        return new Contract(defaultContractId());
    }
}

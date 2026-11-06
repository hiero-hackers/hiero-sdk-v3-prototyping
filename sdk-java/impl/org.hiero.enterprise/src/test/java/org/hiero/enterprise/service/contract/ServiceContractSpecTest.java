package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.evm.AbiString;
import org.hiero.evm.AbiValue;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-contract`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-contract")
class ServiceContractSpecTest {

    /// `enterprise.service.contract/contract-parameters-without-values-have-the-defaults-of-the-spec`
    ///
    /// Source: this spec (@@default and @@nullable of ContractCreateParams); the Python SDK (contract_create_transaction_test.py,
    /// test_constructor_default_values: no gas, admin key or memo).
    @Test
    @DisplayName("contract parameters without values have the defaults of the spec")
    @Tag("testId:enterprise.service.contract/contract-parameters-without-values-have-the-defaults-of-the-spec")
    void contractParametersWithoutValuesHaveTheDefaultsOfTheSpec() {
        // when
        final ContractCreateParams params = defaultContractCreateParams();

        // then
        assertEquals(0L, length(params.constructorArguments()));
        assertNull(params.gas());
        assertNull(params.initialBalance());
        assertNull(params.adminAuthority());
        assertNull(params.contractMemo());
        assertNull(params.autoRenewPeriod());
        assertNull(params.autoRenewAccount());
        assertNull(params.maxAutomaticTokenAssociations());
    }

    /// `enterprise.service.contract/contract-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the ContractCreateTransaction unit tests of the Java (ContractCreateTransactionTest), Go
    /// (contract_create_transaction_unit_test.go), C++ (ContractCreateTransactionUnitTests), Swift
    /// (ContractCreateTransactionUnitTests) and Python (contract_create_transaction_test.py, test_constructor_with_parameters)
    /// SDKs; hiero-enterprise-java (ProtocolLayerDataCreationTests, testContractCreateRequestCreation: gas, an admin key and
    /// constructor parameters): every value that is set is read back unchanged. The auto-renew period is the
    /// one of the Java test (Duration.ofHours(10), which is exactly 36000 seconds).
    @Test
    @DisplayName("contract parameters keep the values they are created with")
    @Tag("testId:enterprise.service.contract/contract-parameters-keep-the-values-they-are-created-with")
    void contractParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final Duration autoRenewPeriod = toSeconds(36000L);
        final AccountId account = defaultAccountId();
        final AbiValue constructorArgument = new AbiString("Hello from Hiero.");

        // when
        final ContractCreateParams params = new ContractCreateParams(List.of(constructorArgument), 1_000_000L, null, authority, "memo", autoRenewPeriod, account, 10);

        // then
        assertEquals(List.of(constructorArgument), params.constructorArguments());
        assertEquals(1_000_000L, params.gas());
        assertEquals(authority, params.adminAuthority());
        assertEquals("memo", params.contractMemo());
        assertEquals(autoRenewPeriod, params.autoRenewPeriod());
        assertEquals(account, params.autoRenewAccount());
        assertEquals(10, params.maxAutomaticTokenAssociations());
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
        final ContractCreateParams params = new ContractCreateParams(List.of(), 1L, null, null, memo, null, null, null);

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
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(List.of(), 0L, null, null, null, null, null, null));
    }

    /// `enterprise.service.contract/contract-parameters-reject-a-memo-of-101-characters`
    ///
    /// Source: this spec (@@maxBytes(100) of contractMemo); HAPI limits a memo to 100 bytes.
    @Test
    @DisplayName("contract parameters reject a memo of 101 characters")
    @Tag("testId:enterprise.service.contract/contract-parameters-reject-a-memo-of-101-characters")
    void contractParametersRejectAMemoOf101Characters() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(List.of(), null, null, null, repeat("a", 101L), null, null, null));
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
        return new ContractCreateParams(List.of(), null, null, null, null, null, null, null);
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

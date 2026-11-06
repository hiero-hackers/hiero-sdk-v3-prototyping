package org.hiero.enterprise.service.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.evm.AbiUint;
import org.hiero.evm.AbiValue;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractCreateParams`, generated from the specs.
@DisplayName("ContractCreateParams")
class ContractCreateParamsTest {

    @Test
    @DisplayName("creates a ContractCreateParams and returns the values")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/create")
    void shouldCreateAndReturnTheValues() {
        final List<AbiValue> constructorArguments = constructorArgumentsValue();
        final @Nullable Long gas = gasValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final ContractCreateParams subject = new ContractCreateParams(constructorArguments, gas, initialBalance, adminAuthority, contractMemo, autoRenewPeriod, autoRenewAccount, maxAutomaticTokenAssociations);
        assertValue(constructorArguments, subject.constructorArguments());
        assertValue(gas, subject.gas());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("creates a ContractCreateParams with the default values")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractCreateParams subject = new ContractCreateParams(gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
        assertValue(List.of(), subject.constructorArguments());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final List<AbiValue> constructorArguments = constructorArgumentsValue();
        final @Nullable Long gas = gasValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final @Nullable String contractMemo = contractMemoValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final ContractCreateParams subject = ContractCreateParams.builder()
                .constructorArguments(constructorArguments)
                .gas(gas)
                .initialBalance(initialBalance)
                .adminAuthority(adminAuthority)
                .contractMemo(contractMemo)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .build();
        assertValue(constructorArguments, subject.constructorArguments());
        assertValue(gas, subject.gas());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractCreateParams subject = ContractCreateParams.builder()
                .build();
        assertValue(List.of(), subject.constructorArguments());
        assertNull(subject.gas());
        assertNull(subject.initialBalance());
        assertNull(subject.adminAuthority());
        assertNull(subject.contractMemo());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertNull(subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("rejects null for constructorArguments")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/constructorArguments/rejects-null")
    void shouldRejectNullConstructorArguments() {
        assertThrows(NullPointerException.class, () -> new ContractCreateParams((List<AbiValue>) null, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue()));
    }

    @Test
    @DisplayName("the constructor copies constructorArguments; the accessor returns an unmodifiable collection")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/constructorArguments/copies-in")
    void shouldCopyConstructorArgumentsIn() {
        final List<AbiValue> value = new ArrayList<>(constructorArgumentsValue());
        final List<AbiValue> expected = List.copyOf(value);
        final ContractCreateParams subject = new ContractCreateParams(value, gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
        value.clear();
        assertValue(expected, subject.constructorArguments());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.constructorArguments()).clear());
    }

    @Test
    @DisplayName("gas: accepts the minimum (1)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/accepts-at-minimum")
    void shouldAcceptGasAtMinimum() {
        final @Nullable Long value = 1L;
        final ContractCreateParams subject = new ContractCreateParams(constructorArgumentsValue(), value, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: accepts the maximum (9223372036854775807)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/accepts-at-maximum")
    void shouldAcceptGasAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractCreateParams subject = new ContractCreateParams(constructorArgumentsValue(), value, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
        assertValue(value, subject.gas());
    }

    @Test
    @DisplayName("gas: rejects a value below the minimum (0)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/gas/rejects-below-minimum")
    void shouldRejectGasBelowMinimum() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(constructorArgumentsValue(), 0L, initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue()));
    }

    @Test
    @DisplayName("contractMemo: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/contractMemo/accepts-at-maximum-bytes")
    void shouldAcceptContractMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final ContractCreateParams subject = new ContractCreateParams(constructorArgumentsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), value, autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
        assertValue(value, subject.contractMemo());
    }

    @Test
    @DisplayName("contractMemo: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/contractMemo/rejects-above-maximum-bytes")
    void shouldRejectContractMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new ContractCreateParams(constructorArgumentsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final ContractCreateParams subject = new ContractCreateParams(constructorArgumentsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value);
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:enterprise.service.contract.ContractCreateParams/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final ContractCreateParams subject = new ContractCreateParams(constructorArgumentsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value);
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    private static List<AbiValue> constructorArgumentsValue() {
        return List.of(new AbiUint(new BigInteger("1"), 256));
    }

    private static long gasValue() {
        return 1L;
    }

    private static NativeToken<?, ?> initialBalanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String contractMemoValue() {
        return "value";
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static ContractCreateParams create() {
        return new ContractCreateParams(constructorArgumentsValue(), gasValue(), initialBalanceValue(), adminAuthorityValue(), contractMemoValue(), autoRenewPeriodValue(), autoRenewAccountValue(), maxAutomaticTokenAssociationsValue());
    }

    private static ContractCreateParams defaultSubject() {
        return new ContractCreateParams(List.of(), null, null, null, null, null, null, null);
    }
}

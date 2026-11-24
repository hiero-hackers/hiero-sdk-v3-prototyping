package org.hiero.consensusnode.queries.contracts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.evm.EvmAddress;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ContractId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `ContractInfo`, generated from the specs.
@DisplayName("ContractInfo")
class ContractInfoTest {

    @Test
    @DisplayName("creates a ContractInfo and returns the values")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/create")
    void shouldCreateAndReturnTheValues() {
        final ContractId contractId = contractIdValue();
        final AccountId accountId = accountIdValue();
        final EvmAddress evmAddress = evmAddressValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final long storage = storageValue();
        final String contractMemo = contractMemoValue();
        final NativeToken<?, ?> balance = balanceValue();
        final boolean deleted = deletedValue();
        final int maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractInfo subject = new ContractInfo(contractId, accountId, evmAddress, adminAuthority, expirationTime, autoRenewPeriod, autoRenewAccount, storage, contractMemo, balance, deleted, maxAutomaticTokenAssociations, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(contractId, subject.contractId());
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(storage, subject.storage());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(balance, subject.balance());
        assertValue(deleted, subject.deleted());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("creates a ContractInfo with the default values")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), balanceValue(), stakedAccountIdValue(), (Long) null);
        assertValue("", subject.contractMemo());
        assertValue(false, subject.deleted());
        assertValue(0, subject.maxAutomaticTokenAssociations());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final ContractId contractId = contractIdValue();
        final AccountId accountId = accountIdValue();
        final EvmAddress evmAddress = evmAddressValue();
        final @Nullable Authority adminAuthority = adminAuthorityValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId autoRenewAccount = autoRenewAccountValue();
        final long storage = storageValue();
        final String contractMemo = contractMemoValue();
        final NativeToken<?, ?> balance = balanceValue();
        final boolean deleted = deletedValue();
        final int maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final ContractInfo subject = ContractInfo.builder()
                .contractId(contractId)
                .accountId(accountId)
                .evmAddress(evmAddress)
                .adminAuthority(adminAuthority)
                .expirationTime(expirationTime)
                .autoRenewPeriod(autoRenewPeriod)
                .autoRenewAccount(autoRenewAccount)
                .storage(storage)
                .contractMemo(contractMemo)
                .balance(balance)
                .deleted(deleted)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .build();
        assertValue(contractId, subject.contractId());
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(adminAuthority, subject.adminAuthority());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(autoRenewAccount, subject.autoRenewAccount());
        assertValue(storage, subject.storage());
        assertValue(contractMemo, subject.contractMemo());
        assertValue(balance, subject.balance());
        assertValue(deleted, subject.deleted());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractInfo subject = ContractInfo.builder()
                .contractId(contractIdValue())
                .accountId(accountIdValue())
                .evmAddress(evmAddressValue())
                .expirationTime(expirationTimeValue())
                .storage(storageValue())
                .balance(balanceValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.adminAuthority());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.autoRenewAccount());
        assertValue("", subject.contractMemo());
        assertValue(false, subject.deleted());
        assertValue(0, subject.maxAutomaticTokenAssociations());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for contractId")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/contractId/rejects-null")
    void shouldRejectNullContractId() {
        assertThrows(NullPointerException.class, () -> new ContractInfo((ContractId) null, accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new ContractInfo(contractIdValue(), (AccountId) null, evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for evmAddress")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/evmAddress/rejects-null")
    void shouldRejectNullEvmAddress() {
        assertThrows(NullPointerException.class, () -> new ContractInfo(contractIdValue(), accountIdValue(), (EvmAddress) null, adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), (ZonedDateTime) null, autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("storage: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/storage/accepts-at-minimum")
    void shouldAcceptStorageAtMinimum() {
        final long value = -9223372036854775808L;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.storage());
    }

    @Test
    @DisplayName("storage: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/storage/accepts-at-maximum")
    void shouldAcceptStorageAtMaximum() {
        final long value = 9223372036854775807L;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), value, contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.storage());
    }

    @Test
    @DisplayName("rejects null for contractMemo")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/contractMemo/rejects-null")
    void shouldRejectNullContractMemo() {
        assertThrows(NullPointerException.class, () -> new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), (String) null, balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for balance")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/balance/rejects-null")
    void shouldRejectNullBalance() {
        assertThrows(NullPointerException.class, () -> new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), (NativeToken<?, ?>) null, deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final int value = 2147483647;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), value, stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final ContractInfo subject = new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:consensusnode.queries.contracts.ContractInfo/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    private static ContractId contractIdValue() {
        return new ContractId(0L, 0L, "", 1001L, null);
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static Authority adminAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static AccountId autoRenewAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long storageValue() {
        return 1L;
    }

    private static String contractMemoValue() {
        return "value";
    }

    private static NativeToken<?, ?> balanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static boolean deletedValue() {
        return true;
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static AccountId stakedAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static long stakedNodeIdValue() {
        return 1L;
    }

    private static boolean declineStakingRewardValue() {
        return true;
    }

    private static ContractInfo create() {
        return new ContractInfo(contractIdValue(), accountIdValue(), evmAddressValue(), adminAuthorityValue(), expirationTimeValue(), autoRenewPeriodValue(), autoRenewAccountValue(), storageValue(), contractMemoValue(), balanceValue(), deletedValue(), maxAutomaticTokenAssociationsValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
    }
}

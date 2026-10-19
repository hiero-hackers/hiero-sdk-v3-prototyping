package org.hiero.consensusnode.queries.accounts;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.EvmAddress;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountInfo`, generated from the specs.
@DisplayName("AccountInfo")
class AccountInfoTest {

    @Test
    @DisplayName("creates an AccountInfo and returns the values")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final NativeToken<?, ?> balance = balanceValue();
        final @Nullable Authority authority = authorityValue();
        final String accountMemo = accountMemoValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final int maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final long ownedNfts = ownedNftsValue();
        final boolean deleted = deletedValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final AccountInfo subject = new AccountInfo(accountId, evmAddress, balance, authority, accountMemo, expirationTime, autoRenewPeriod, maxAutomaticTokenAssociations, receiverSignatureRequired, ownedNfts, deleted, stakedAccountId, stakedNodeId, declineStakingReward);
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(balance, subject.balance());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(ownedNfts, subject.ownedNfts());
        assertValue(deleted, subject.deleted());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("creates an AccountInfo with the default values")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), expirationTimeValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null);
        assertValue("", subject.accountMemo());
        assertValue(0, subject.maxAutomaticTokenAssociations());
        assertValue(false, subject.receiverSignatureRequired());
        assertValue(0L, subject.ownedNfts());
        assertValue(false, subject.deleted());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId accountId = accountIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final NativeToken<?, ?> balance = balanceValue();
        final @Nullable Authority authority = authorityValue();
        final String accountMemo = accountMemoValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final int maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final long ownedNfts = ownedNftsValue();
        final boolean deleted = deletedValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final AccountInfo subject = AccountInfo.builder()
                .accountId(accountId)
                .evmAddress(evmAddress)
                .balance(balance)
                .authority(authority)
                .accountMemo(accountMemo)
                .expirationTime(expirationTime)
                .autoRenewPeriod(autoRenewPeriod)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .receiverSignatureRequired(receiverSignatureRequired)
                .ownedNfts(ownedNfts)
                .deleted(deleted)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .build();
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(balance, subject.balance());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(expirationTime, subject.expirationTime());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(ownedNfts, subject.ownedNfts());
        assertValue(deleted, subject.deleted());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountInfo subject = AccountInfo.builder()
                .accountId(accountIdValue())
                .balance(balanceValue())
                .expirationTime(expirationTimeValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.evmAddress());
        assertNull(subject.authority());
        assertValue("", subject.accountMemo());
        assertNull(subject.autoRenewPeriod());
        assertValue(0, subject.maxAutomaticTokenAssociations());
        assertValue(false, subject.receiverSignatureRequired());
        assertValue(0L, subject.ownedNfts());
        assertValue(false, subject.deleted());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountInfo((AccountId) null, evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for balance")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/balance/rejects-null")
    void shouldRejectNullBalance() {
        assertThrows(NullPointerException.class, () -> new AccountInfo(accountIdValue(), evmAddressValue(), (NativeToken<?, ?>) null, authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for accountMemo")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/accountMemo/rejects-null")
    void shouldRejectNullAccountMemo() {
        assertThrows(NullPointerException.class, () -> new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), (String) null, expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), (ZonedDateTime) null, autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final int value = Integer.MIN_VALUE;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), value, receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final int value = 2147483647;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), value, receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("ownedNfts: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/ownedNfts/accepts-at-minimum")
    void shouldAcceptOwnedNftsAtMinimum() {
        final long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), value, deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.ownedNfts());
    }

    @Test
    @DisplayName("ownedNfts: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/ownedNfts/accepts-at-maximum")
    void shouldAcceptOwnedNftsAtMaximum() {
        final long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), value, deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
        assertValue(value, subject.ownedNfts());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), (AccountId) null, value, declineStakingRewardValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    @Tag("testId:consensusnode.queries.accounts.AccountInfo/stakedAccountId-stakedNodeId/rejects-two")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue()));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static NativeToken<?, ?> balanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String accountMemoValue() {
        return "value";
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static boolean receiverSignatureRequiredValue() {
        return true;
    }

    private static long ownedNftsValue() {
        return 1L;
    }

    private static boolean deletedValue() {
        return true;
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

    private static AccountInfo create() {
        return new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), authorityValue(), accountMemoValue(), expirationTimeValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), ownedNftsValue(), deletedValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue());
    }
}

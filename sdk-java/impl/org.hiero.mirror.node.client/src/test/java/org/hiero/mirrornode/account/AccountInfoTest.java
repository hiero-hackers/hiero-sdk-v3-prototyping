package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.EvmAddress;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountInfo`, generated from the specs.
@DisplayName("AccountInfo")
class AccountInfoTest {

    @Test
    @DisplayName("creates an AccountInfo and returns the values")
    @Tag("testId:mirrornode.account.AccountInfo/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final long balance = balanceValue();
        final @Nullable ZonedDateTime balanceTimestamp = balanceTimestampValue();
        final List<TokenBalance> tokenBalances = tokenBalancesValue();
        final long ethereumNonce = ethereumNonceValue();
        final long pendingReward = pendingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String accountMemo = accountMemoValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime expiryTimestamp = expiryTimestampValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final boolean declineReward = declineRewardValue();
        final boolean deleted = deletedValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = stakedNodeIdValue();
        final @Nullable ZonedDateTime stakePeriodStart = stakePeriodStartValue();
        final @Nullable EvmAddress delegationAddress = delegationAddressValue();
        final AccountInfo subject = new AccountInfo(accountId, evmAddress, balance, balanceTimestamp, tokenBalances, ethereumNonce, pendingReward, alias, authority, accountMemo, createdTimestamp, expiryTimestamp, autoRenewPeriod, maxAutomaticTokenAssociations, receiverSignatureRequired, declineReward, deleted, stakedAccountId, stakedNodeId, stakePeriodStart, delegationAddress);
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(balance, subject.balance());
        assertValue(balanceTimestamp, subject.balanceTimestamp());
        assertValue(tokenBalances, subject.tokenBalances());
        assertValue(ethereumNonce, subject.ethereumNonce());
        assertValue(pendingReward, subject.pendingReward());
        assertValue(alias, subject.alias());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(expiryTimestamp, subject.expiryTimestamp());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(declineReward, subject.declineReward());
        assertValue(deleted, subject.deleted());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(stakePeriodStart, subject.stakePeriodStart());
        assertValue(delegationAddress, subject.delegationAddress());
    }

    @Test
    @DisplayName("creates an AccountInfo with the default values")
    @Tag("testId:mirrornode.account.AccountInfo/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(List.of(), subject.tokenBalances());
        assertValue(false, subject.declineReward());
        assertValue(false, subject.deleted());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.account.AccountInfo/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final AccountId accountId = accountIdValue();
        final @Nullable EvmAddress evmAddress = evmAddressValue();
        final long balance = balanceValue();
        final @Nullable ZonedDateTime balanceTimestamp = balanceTimestampValue();
        final List<TokenBalance> tokenBalances = tokenBalancesValue();
        final long ethereumNonce = ethereumNonceValue();
        final long pendingReward = pendingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final @Nullable Authority authority = authorityValue();
        final @Nullable String accountMemo = accountMemoValue();
        final @Nullable ZonedDateTime createdTimestamp = createdTimestampValue();
        final @Nullable ZonedDateTime expiryTimestamp = expiryTimestampValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final boolean declineReward = declineRewardValue();
        final boolean deleted = deletedValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = stakedNodeIdValue();
        final @Nullable ZonedDateTime stakePeriodStart = stakePeriodStartValue();
        final @Nullable EvmAddress delegationAddress = delegationAddressValue();
        final AccountInfo subject = AccountInfo.builder()
                .accountId(accountId)
                .evmAddress(evmAddress)
                .balance(balance)
                .balanceTimestamp(balanceTimestamp)
                .tokenBalances(tokenBalances)
                .ethereumNonce(ethereumNonce)
                .pendingReward(pendingReward)
                .alias(alias)
                .authority(authority)
                .accountMemo(accountMemo)
                .createdTimestamp(createdTimestamp)
                .expiryTimestamp(expiryTimestamp)
                .autoRenewPeriod(autoRenewPeriod)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .receiverSignatureRequired(receiverSignatureRequired)
                .declineReward(declineReward)
                .deleted(deleted)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .stakePeriodStart(stakePeriodStart)
                .delegationAddress(delegationAddress)
                .build();
        assertValue(accountId, subject.accountId());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(balance, subject.balance());
        assertValue(balanceTimestamp, subject.balanceTimestamp());
        assertValue(tokenBalances, subject.tokenBalances());
        assertValue(ethereumNonce, subject.ethereumNonce());
        assertValue(pendingReward, subject.pendingReward());
        assertValue(alias, subject.alias());
        assertValue(authority, subject.authority());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(createdTimestamp, subject.createdTimestamp());
        assertValue(expiryTimestamp, subject.expiryTimestamp());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(declineReward, subject.declineReward());
        assertValue(deleted, subject.deleted());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(stakePeriodStart, subject.stakePeriodStart());
        assertValue(delegationAddress, subject.delegationAddress());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.account.AccountInfo/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountInfo subject = AccountInfo.builder()
                .accountId(accountIdValue())
                .balance(balanceValue())
                .ethereumNonce(ethereumNonceValue())
                .pendingReward(pendingRewardValue())
                .build();
        assertNull(subject.evmAddress());
        assertNull(subject.balanceTimestamp());
        assertValue(List.of(), subject.tokenBalances());
        assertNull(subject.alias());
        assertNull(subject.authority());
        assertNull(subject.accountMemo());
        assertNull(subject.createdTimestamp());
        assertNull(subject.expiryTimestamp());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.receiverSignatureRequired());
        assertValue(false, subject.declineReward());
        assertValue(false, subject.deleted());
        assertNull(subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertNull(subject.stakePeriodStart());
        assertNull(subject.delegationAddress());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.account.AccountInfo/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountInfo.builder().build());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:mirrornode.account.AccountInfo/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountInfo((AccountId) null, evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue()));
    }

    @Test
    @DisplayName("balance: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.AccountInfo/balance/accepts-at-minimum")
    void shouldAcceptBalanceAtMinimum() {
        final long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), value, balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("balance: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.AccountInfo/balance/accepts-at-maximum")
    void shouldAcceptBalanceAtMaximum() {
        final long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), value, balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.balance());
    }

    @Test
    @DisplayName("rejects null for tokenBalances")
    @Tag("testId:mirrornode.account.AccountInfo/tokenBalances/rejects-null")
    void shouldRejectNullTokenBalances() {
        assertThrows(NullPointerException.class, () -> new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), (List<TokenBalance>) null, ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue()));
    }

    @Test
    @DisplayName("the constructor copies tokenBalances; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.account.AccountInfo/tokenBalances/copies-in")
    void shouldCopyTokenBalancesIn() {
        final List<TokenBalance> value = new ArrayList<>(tokenBalancesValue());
        final List<TokenBalance> expected = List.copyOf(value);
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), value, ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        value.clear();
        assertValue(expected, subject.tokenBalances());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.tokenBalances()).clear());
    }

    @Test
    @DisplayName("ethereumNonce: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.AccountInfo/ethereumNonce/accepts-at-minimum")
    void shouldAcceptEthereumNonceAtMinimum() {
        final long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), value, pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.ethereumNonce());
    }

    @Test
    @DisplayName("ethereumNonce: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.AccountInfo/ethereumNonce/accepts-at-maximum")
    void shouldAcceptEthereumNonceAtMaximum() {
        final long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), value, pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.ethereumNonce());
    }

    @Test
    @DisplayName("pendingReward: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.AccountInfo/pendingReward/accepts-at-minimum")
    void shouldAcceptPendingRewardAtMinimum() {
        final long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), value, aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.pendingReward());
    }

    @Test
    @DisplayName("pendingReward: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.AccountInfo/pendingReward/accepts-at-maximum")
    void shouldAcceptPendingRewardAtMaximum() {
        final long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), value, aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.pendingReward());
    }

    @Test
    @DisplayName("the constructor copies alias; the accessor returns a copy")
    @Tag("testId:mirrornode.account.AccountInfo/alias/copies-in")
    void shouldCopyAliasIn() {
        final byte[] value = aliasValue();
        final byte[] expected = value.clone();
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), value, authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.alias());
        final byte[] returned = subject.alias();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.alias());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:mirrornode.account.AccountInfo/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), value, receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:mirrornode.account.AccountInfo/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), value, receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.account.AccountInfo/stakedNodeId/accepts-at-minimum")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), value, stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.account.AccountInfo/stakedNodeId/accepts-at-maximum")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final AccountInfo subject = new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), value, stakePeriodStartValue(), delegationAddressValue());
        assertValue(value, subject.stakedNodeId());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static long balanceValue() {
        return 1L;
    }

    private static ZonedDateTime balanceTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static List<TokenBalance> tokenBalancesValue() {
        return List.of(new TokenBalance(new Address(0L, 0L, "", 1001L), 1L));
    }

    private static long ethereumNonceValue() {
        return 1L;
    }

    private static long pendingRewardValue() {
        return 1L;
    }

    private static byte[] aliasValue() {
        return new byte[] {1, 2, 3};
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static String accountMemoValue() {
        return "value";
    }

    private static ZonedDateTime createdTimestampValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static ZonedDateTime expiryTimestampValue() {
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

    private static boolean declineRewardValue() {
        return true;
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

    private static ZonedDateTime stakePeriodStartValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static EvmAddress delegationAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static AccountInfo create() {
        return new AccountInfo(accountIdValue(), evmAddressValue(), balanceValue(), balanceTimestampValue(), tokenBalancesValue(), ethereumNonceValue(), pendingRewardValue(), aliasValue(), authorityValue(), accountMemoValue(), createdTimestampValue(), expiryTimestampValue(), autoRenewPeriodValue(), maxAutomaticTokenAssociationsValue(), receiverSignatureRequiredValue(), declineRewardValue(), deletedValue(), stakedAccountIdValue(), stakedNodeIdValue(), stakePeriodStartValue(), delegationAddressValue());
    }
}

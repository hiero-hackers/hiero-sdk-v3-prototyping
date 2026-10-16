package org.hiero.enterprise.service.account;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `AccountDef`, generated from the specs.
@DisplayName("AccountDef")
class AccountDefTest {

    @Test
    @DisplayName("creates an AccountDef and returns the values")
    void shouldCreateAndReturnTheValues() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountDef subject = new AccountDef(authority, initialBalance, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, stakedAccountId, stakedNodeId, declineStakingReward, alias);
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("creates an AccountDef with the default values")
    void shouldCreateWithTheDefaultValues() {
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, aliasValue());
        assertValue(false, subject.receiverSignatureRequired());
        assertValue(false, subject.declineStakingReward());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final @Nullable AccountId stakedAccountId = stakedAccountIdValue();
        final @Nullable Long stakedNodeId = (Long) null;
        final boolean declineStakingReward = declineStakingRewardValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountDef subject = AccountDef.builder()
                .authority(authority)
                .initialBalance(initialBalance)
                .accountMemo(accountMemo)
                .receiverSignatureRequired(receiverSignatureRequired)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .autoRenewPeriod(autoRenewPeriod)
                .stakedAccountId(stakedAccountId)
                .stakedNodeId(stakedNodeId)
                .declineStakingReward(declineStakingReward)
                .alias(alias)
                .build();
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(stakedAccountId, subject.stakedAccountId());
        assertValue(stakedNodeId, subject.stakedNodeId());
        assertValue(declineStakingReward, subject.declineStakingReward());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountDef subject = AccountDef.builder()
                .authority(authorityValue())
                .stakedAccountId(stakedAccountIdValue())
                .build();
        assertNull(subject.initialBalance());
        assertNull(subject.accountMemo());
        assertValue(false, subject.receiverSignatureRequired());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.autoRenewPeriod());
        assertValue(stakedAccountIdValue(), subject.stakedAccountId());
        assertNull(subject.stakedNodeId());
        assertValue(false, subject.declineStakingReward());
        assertNull(subject.alias());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountDef.builder().build());
    }

    @Test
    @DisplayName("rejects null for authority")
    void shouldRejectNullAuthority() {
        assertThrows(NullPointerException.class, () -> new AccountDef((Authority) null, initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the minimum (-9223372036854775808)")
    void shouldAcceptStakedNodeIdAtMinimum() {
        final @Nullable Long value = -9223372036854775808L;
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), (AccountId) null, value, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("stakedNodeId: accepts the maximum (9223372036854775807)")
    void shouldAcceptStakedNodeIdAtMaximum() {
        final @Nullable Long value = 9223372036854775807L;
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), (AccountId) null, value, declineStakingRewardValue(), aliasValue());
        assertValue(value, subject.stakedNodeId());
    }

    @Test
    @DisplayName("the constructor copies alias; the accessor returns a copy")
    void shouldCopyAliasIn() {
        final byte[] value = aliasValue();
        final byte[] expected = value.clone();
        final AccountDef subject = new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.alias());
        final byte[] returned = subject.alias();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.alias());
    }

    @Test
    @DisplayName("rejects stakedAccountId and stakedNodeId together (@@oneOrNoneOf)")
    void shouldRejectTwoOfStakedAccountIdOrStakedNodeId() {
        assertThrows(IllegalArgumentException.class, () -> new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), stakedNodeIdValue(), declineStakingRewardValue(), aliasValue()));
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static NativeToken<?, ?> initialBalanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static String accountMemoValue() {
        return "value";
    }

    private static boolean receiverSignatureRequiredValue() {
        return true;
    }

    private static int maxAutomaticTokenAssociationsValue() {
        return 1;
    }

    private static Duration autoRenewPeriodValue() {
        return Duration.ofSeconds(1L);
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

    private static byte[] aliasValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountDef create() {
        return new AccountDef(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), stakedAccountIdValue(), (Long) null, declineStakingRewardValue(), aliasValue());
    }
}

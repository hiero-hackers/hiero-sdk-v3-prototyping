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
import org.hiero.nativeToken.NativeToken;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountCreateParams`, generated from the specs.
@DisplayName("AccountCreateParams")
class AccountCreateParamsTest {

    @Test
    @DisplayName("creates an AccountCreateParams and returns the values")
    @Tag("testId:enterprise.service.account.AccountCreateParams/create")
    void shouldCreateAndReturnTheValues() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountCreateParams subject = new AccountCreateParams(authority, initialBalance, accountMemo, receiverSignatureRequired, maxAutomaticTokenAssociations, autoRenewPeriod, alias);
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("creates an AccountCreateParams with the default values")
    @Tag("testId:enterprise.service.account.AccountCreateParams/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccountCreateParams subject = new AccountCreateParams(authorityValue(), initialBalanceValue(), accountMemoValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), aliasValue());
        assertValue(false, subject.receiverSignatureRequired());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:enterprise.service.account.AccountCreateParams/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final Authority authority = authorityValue();
        final @Nullable NativeToken<?, ?> initialBalance = initialBalanceValue();
        final @Nullable String accountMemo = accountMemoValue();
        final boolean receiverSignatureRequired = receiverSignatureRequiredValue();
        final @Nullable Integer maxAutomaticTokenAssociations = maxAutomaticTokenAssociationsValue();
        final @Nullable Duration autoRenewPeriod = autoRenewPeriodValue();
        final byte @Nullable [] alias = aliasValue();
        final AccountCreateParams subject = AccountCreateParams.builder()
                .authority(authority)
                .initialBalance(initialBalance)
                .accountMemo(accountMemo)
                .receiverSignatureRequired(receiverSignatureRequired)
                .maxAutomaticTokenAssociations(maxAutomaticTokenAssociations)
                .autoRenewPeriod(autoRenewPeriod)
                .alias(alias)
                .build();
        assertValue(authority, subject.authority());
        assertValue(initialBalance, subject.initialBalance());
        assertValue(accountMemo, subject.accountMemo());
        assertValue(receiverSignatureRequired, subject.receiverSignatureRequired());
        assertValue(maxAutomaticTokenAssociations, subject.maxAutomaticTokenAssociations());
        assertValue(autoRenewPeriod, subject.autoRenewPeriod());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:enterprise.service.account.AccountCreateParams/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountCreateParams subject = AccountCreateParams.builder()
                .authority(authorityValue())
                .build();
        assertNull(subject.initialBalance());
        assertNull(subject.accountMemo());
        assertValue(false, subject.receiverSignatureRequired());
        assertNull(subject.maxAutomaticTokenAssociations());
        assertNull(subject.autoRenewPeriod());
        assertNull(subject.alias());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:enterprise.service.account.AccountCreateParams/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountCreateParams.builder().build());
    }

    @Test
    @DisplayName("rejects null for authority")
    @Tag("testId:enterprise.service.account.AccountCreateParams/authority/rejects-null")
    void shouldRejectNullAuthority() {
        assertThrows(NullPointerException.class, () -> new AccountCreateParams((Authority) null, initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), aliasValue()));
    }

    @Test
    @DisplayName("accountMemo: accepts more elements than the maximum")
    @Tag("testId:enterprise.service.account.AccountCreateParams/accountMemo/accepts-at-maximum-bytes")
    void shouldAcceptAccountMemoAtMaximumBytes() {
        final @Nullable String value = "ääääääääääääääääääääääääääääääääääääääääääääääääää";
        final AccountCreateParams subject = new AccountCreateParams(authorityValue(), initialBalanceValue(), value, receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), aliasValue());
        assertValue(value, subject.accountMemo());
    }

    @Test
    @DisplayName("accountMemo: rejects more elements than the maximum")
    @Tag("testId:enterprise.service.account.AccountCreateParams/accountMemo/rejects-above-maximum-bytes")
    void shouldRejectAccountMemoAboveMaximumBytes() {
        assertThrows(IllegalArgumentException.class, () -> new AccountCreateParams(authorityValue(), initialBalanceValue(), "ääääääääääääääääääääääääääääääääääääääääääääääääääa", receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), aliasValue()));
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the minimum (-2147483648)")
    @Tag("testId:enterprise.service.account.AccountCreateParams/maxAutomaticTokenAssociations/accepts-at-minimum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMinimum() {
        final @Nullable Integer value = Integer.MIN_VALUE;
        final AccountCreateParams subject = new AccountCreateParams(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("maxAutomaticTokenAssociations: accepts the maximum (2147483647)")
    @Tag("testId:enterprise.service.account.AccountCreateParams/maxAutomaticTokenAssociations/accepts-at-maximum")
    void shouldAcceptMaxAutomaticTokenAssociationsAtMaximum() {
        final @Nullable Integer value = 2147483647;
        final AccountCreateParams subject = new AccountCreateParams(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), value, autoRenewPeriodValue(), aliasValue());
        assertValue(value, subject.maxAutomaticTokenAssociations());
    }

    @Test
    @DisplayName("the constructor copies alias; the accessor returns a copy")
    @Tag("testId:enterprise.service.account.AccountCreateParams/alias/copies-in")
    void shouldCopyAliasIn() {
        final byte[] value = aliasValue();
        final byte[] expected = value.clone();
        final AccountCreateParams subject = new AccountCreateParams(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.alias());
        final byte[] returned = subject.alias();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.alias());
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

    private static byte[] aliasValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountCreateParams create() {
        return new AccountCreateParams(authorityValue(), initialBalanceValue(), accountMemoValue(), receiverSignatureRequiredValue(), maxAutomaticTokenAssociationsValue(), autoRenewPeriodValue(), aliasValue());
    }

    private static AccountCreateParams defaultSubject() {
        return new AccountCreateParams(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey()), null, null, false, null, null, null);
    }
}

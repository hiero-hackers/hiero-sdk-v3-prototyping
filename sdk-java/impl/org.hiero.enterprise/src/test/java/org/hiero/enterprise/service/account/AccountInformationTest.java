package org.hiero.enterprise.service.account;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
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

/// Tests of `AccountInformation`, generated from the specs.
@DisplayName("AccountInformation")
class AccountInformationTest {

    @Test
    @DisplayName("creates an AccountInformation and returns the values")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final NativeToken<?, ?> balance = balanceValue();
        final Authority authority = authorityValue();
        final AccountInformation subject = new AccountInformation(accountId, balance, authority);
        assertValue(accountId, subject.accountId());
        assertValue(balance, subject.balance());
        assertValue(authority, subject.authority());
    }

    @Test
    @DisplayName("rejects null for accountId")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new AccountInformation((AccountId) null, balanceValue(), authorityValue()));
    }

    @Test
    @DisplayName("rejects null for balance")
    void shouldRejectNullBalance() {
        assertThrows(NullPointerException.class, () -> new AccountInformation(accountIdValue(), (NativeToken<?, ?>) null, authorityValue()));
    }

    @Test
    @DisplayName("rejects null for authority")
    void shouldRejectNullAuthority() {
        assertThrows(NullPointerException.class, () -> new AccountInformation(accountIdValue(), balanceValue(), (Authority) null));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NativeToken<?, ?> balanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountInformation create() {
        return new AccountInformation(accountIdValue(), balanceValue(), authorityValue());
    }

    /// Asserts that two values are equal; arrays by their content.
    private static void assertValue(final @Nullable Object expected, final @Nullable Object actual) {
        if (expected instanceof byte[] bytes && actual instanceof byte[] other) {
            assertArrayEquals(bytes, other, () -> "expected " + Arrays.toString(bytes) + " but was " + Arrays.toString(other));
        } else {
            assertEquals(expected, actual);
        }
    }
}

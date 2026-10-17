package org.hiero.consensusnode.client;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Account`, generated from the specs.
@DisplayName("Account")
class AccountTest {

    @Test
    @DisplayName("creates an Account and returns the values")
    @Tag("testId:consensusnode.client.Account/create")
    void shouldCreateAndReturnTheValues() {
        final AccountId accountId = accountIdValue();
        final PrivateKey privateKey = privateKeyValue();
        final Account subject = new Account(accountId, privateKey);
        assertValue(accountId, subject.accountId());
        assertValue(privateKey, subject.privateKey());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.client.Account/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new Account((AccountId) null, privateKeyValue()));
    }

    @Test
    @DisplayName("rejects null for privateKey")
    @Tag("testId:consensusnode.client.Account/privateKey/rejects-null")
    void shouldRejectNullPrivateKey() {
        assertThrows(NullPointerException.class, () -> new Account(accountIdValue(), (PrivateKey) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:consensusnode.client.Account/value-semantics")
    void shouldCompareByValue() {
        final Account subject = create();
        final Account other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static Account create() {
        return new Account(accountIdValue(), privateKeyValue());
    }

    private static Account defaultSubject() {
        return new Account(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }
}

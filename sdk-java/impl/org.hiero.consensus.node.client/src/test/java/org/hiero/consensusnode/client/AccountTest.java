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
        final TransactionSigner signer = signerValue();
        final Account subject = new Account(accountId, signer);
        assertValue(accountId, subject.accountId());
        assertValue(signer, subject.signer());
    }

    @Test
    @DisplayName("rejects null for accountId")
    @Tag("testId:consensusnode.client.Account/accountId/rejects-null")
    void shouldRejectNullAccountId() {
        assertThrows(NullPointerException.class, () -> new Account((AccountId) null, signerValue()));
    }

    @Test
    @DisplayName("rejects null for signer")
    @Tag("testId:consensusnode.client.Account/signer/rejects-null")
    void shouldRejectNullSigner() {
        assertThrows(NullPointerException.class, () -> new Account(accountIdValue(), (TransactionSigner) null));
    }

    @Test
    @DisplayName("of(accountId, privateKey) can be called and returns a value")
    @Tag("testId:consensusnode.client.Account/of/call")
    void shouldCallOf() {
        assertNotNull(Account.of(accountIdValue(), privateKeyValue()));
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionSigner signerValue() {
        return TransactionSigner.of(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static Account create() {
        return new Account(accountIdValue(), signerValue());
    }

    private static Account defaultSubject() {
        return Account.of(new AccountId(0L, 0L, "", 1001L, null, null), KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c"));
    }

    private static PrivateKey privateKeyValue() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }
}

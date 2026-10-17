package org.hiero.enterprise.service.account;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountService`, generated from the specs.
@DisplayName("AccountService")
class AccountServiceTest {

    @Test
    @DisplayName("creates an AccountService and returns the values")
    @Tag("testId:enterprise.service.account.AccountService/create")
    void shouldCreateAndReturnTheValues() {
        final AccountService subject = new AccountService();
    }

    @Test
    @DisplayName("createNewAccount(authority) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/createNewAccount/call")
    void shouldCallCreateNewAccount() throws Exception {
        try {
            assertNotNull(create().createNewAccount(authorityValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createNewAccount(authority, initialBalance) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/createNewAccount/call-2")
    void shouldCallCreateNewAccount2() throws Exception {
        try {
            assertNotNull(create().createNewAccount(authorityValue(), initialBalanceValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createNewAccount(params) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/createNewAccount/call-3")
    void shouldCallCreateNewAccount3() throws Exception {
        try {
            assertNotNull(create().createNewAccount(paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("updateAccountAuthority(accountId, newAuthority, oldAuthority) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/updateAccountAuthority/call")
    void shouldCallUpdateAccountAuthority() throws Exception {
        try {
            create().updateAccountAuthority(accountIdValue(), newAuthorityValue(), oldAuthorityValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("deleteAccount(account) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/deleteAccount/call")
    void shouldCallDeleteAccount() throws Exception {
        try {
            create().deleteAccount(accountValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findById(account) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/findById/call")
    void shouldCallFindById() throws Exception {
        try {
            assertNotNull(create().findById(accountValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findAll() can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.account.AccountService/findAll/call")
    void shouldCallFindAll() throws Exception {
        try {
            assertNotNull(create().findAll());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    private static AccountService create() {
        return new AccountService();
    }

    private static Authority authorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static NativeToken<?, ?> initialBalanceValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static AccountCreateParams paramsValue() {
        return new AccountCreateParams(new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey()), null, null, false, null, null, null, null, false, null);
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Authority newAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static Authority oldAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId accountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}

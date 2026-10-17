package org.hiero.enterprise.service.token;

import static org.junit.jupiter.api.Assertions.*;

import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.enterprise.service.ServiceException;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.token.TokenSupplyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FungibleTokenService`, generated from the specs.
@DisplayName("FungibleTokenService")
class FungibleTokenServiceTest {

    @Test
    @DisplayName("creates a FungibleTokenService and returns the values")
    @Tag("testId:enterprise.service.token.FungibleTokenService/create")
    void shouldCreateAndReturnTheValues() {
        final FungibleTokenService subject = new FungibleTokenService();
    }

    @Test
    @DisplayName("createToken(name, symbol) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/createToken/call")
    void shouldCallCreateToken() throws Exception {
        try {
            assertNotNull(create().createToken(nameValue(), symbolValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createToken(name, symbol, supplyAuthority) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/createToken/call-2")
    void shouldCallCreateToken2() throws Exception {
        try {
            assertNotNull(create().createToken(nameValue(), symbolValue(), supplyAuthorityValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createToken(name, symbol, treasuryAccount, supplyAuthority) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/createToken/call-3")
    void shouldCallCreateToken3() throws Exception {
        try {
            assertNotNull(create().createToken(nameValue(), symbolValue(), treasuryAccountValue(), supplyAuthorityValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("createToken(params) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/createToken/call-4")
    void shouldCallCreateToken4() throws Exception {
        try {
            assertNotNull(create().createToken(paramsValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("associateToken(accountId, tokenIds) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/associateToken/call")
    void shouldCallAssociateToken() throws Exception {
        try {
            create().associateToken(accountIdValue(), tokenIdsValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("dissociateToken(accountId, tokenIds) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/dissociateToken/call")
    void shouldCallDissociateToken() throws Exception {
        try {
            create().dissociateToken(accountIdValue(), tokenIdsValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("mintToken(tokenId, amount) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/mintToken/call")
    void shouldCallMintToken() throws Exception {
        try {
            create().mintToken(tokenIdValue(), amountValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("burnToken(tokenId, amount) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/burnToken/call")
    void shouldCallBurnToken() throws Exception {
        try {
            create().burnToken(tokenIdValue(), amountValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("transferToken(tokenId, toAccountId, amount) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/transferToken/call")
    void shouldCallTransferToken() throws Exception {
        try {
            create().transferToken(tokenIdValue(), toAccountIdValue(), amountValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("transferToken(tokenId, fromAccountId, toAccountId, amount) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/transferToken/call-2")
    void shouldCallTransferToken2() throws Exception {
        try {
            create().transferToken(tokenIdValue(), fromAccountIdValue(), toAccountIdValue(), amountValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findById(tokenId) can be called (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/findById/call")
    void shouldCallFindById() throws Exception {
        try {
            create().findById(tokenIdValue());
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("findByAccount(accountId) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/findByAccount/call")
    void shouldCallFindByAccount() throws Exception {
        try {
            assertNotNull(create().findByAccount(accountIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getBalances(tokenId) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/getBalances/call")
    void shouldCallGetBalances() throws Exception {
        try {
            assertNotNull(create().getBalances(tokenIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("getBalancesForAccount(tokenId, accountId) can be called and returns a value (only ServiceException may occur)")
    @Tag("testId:enterprise.service.token.FungibleTokenService/getBalancesForAccount/call")
    void shouldCallGetBalancesForAccount() throws Exception {
        try {
            assertNotNull(create().getBalancesForAccount(tokenIdValue(), accountIdValue()));
        } catch (final Exception e) {
            if (!(e instanceof ServiceException)) {
                throw e;
            }
        }
    }

    private static FungibleTokenService create() {
        return new FungibleTokenService();
    }

    private static String nameValue() {
        return "value";
    }

    private static String symbolValue() {
        return "value";
    }

    private static Authority supplyAuthorityValue() {
        return new PublicKeyAuthority(KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c").createPublicKey());
    }

    private static AccountId treasuryAccountValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TokenCreateParams paramsValue() {
        return new TokenCreateParams("Token Name", "TKN", null, 0, 0L, TokenSupplyType.INFINITE, null, false, null, new byte[0], null, null, null, null, null, null, null, null, null, null, null);
    }

    private static AccountId accountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Address tokenIdsValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static long amountValue() {
        return 1L;
    }

    private static AccountId toAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static AccountId fromAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}

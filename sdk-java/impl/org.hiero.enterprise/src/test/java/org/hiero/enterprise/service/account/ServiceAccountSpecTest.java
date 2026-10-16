package org.hiero.enterprise.service.account;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.hiero.authority.Authority;
import org.hiero.authority.PublicKeyAuthority;
import org.hiero.keys.ByteImportEncoding;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `service-account`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("service-account")
class ServiceAccountSpecTest {

    /// `enterprise.service.account/account-parameters-with-only-an-authority-have-the-defaults-of-the-spec`
    ///
    /// Source: this spec (@@default and @@nullable of AccountCreateParams).
    @Test
    @DisplayName("account parameters with only an authority have the defaults of the spec")
    void accountParametersWithOnlyAnAuthorityHaveTheDefaultsOfTheSpec() {
        // when
        final AccountCreateParams params = defaultAccountCreateParams();

        // then
        assertEquals(defaultAuthority(), params.authority());
        assertFalse(params.receiverSignatureRequired());
        assertFalse(params.declineStakingReward());
        assertNull(params.initialBalance());
        assertNull(params.accountMemo());
        assertNull(params.maxAutomaticTokenAssociations());
        assertNull(params.autoRenewPeriod());
        assertNull(params.stakedAccountId());
        assertNull(params.stakedNodeId());
        assertNull(params.alias());
    }

    /// `enterprise.service.account/account-parameters-keep-the-values-they-are-created-with`
    ///
    /// Source: the AccountCreateTransaction unit tests of the Java (AccountCreateTransactionTest), Go
    /// (account_create_transaction_unit_test.go), C++ (AccountCreateTransactionUnitTests), Swift
    /// (AccountCreateTransactionUnitTests) and Python (account_create_transaction_test.py) SDKs: every value that is set is
    /// read back unchanged. The auto-renew period is the one of the Java test (Duration.ofHours(10), which is exactly 36000
    /// seconds).
    @Test
    @DisplayName("account parameters keep the values they are created with")
    void accountParametersKeepTheValuesTheyAreCreatedWith() {
        // given
        final Authority authority = defaultAuthority();
        final Duration autoRenewPeriod = toSeconds(36000L);

        // when
        final AccountCreateParams params = new AccountCreateParams(authority, null, "memo", true, 10, autoRenewPeriod, null, 3L, true, null);

        // then
        assertEquals(authority, params.authority());
        assertEquals("memo", params.accountMemo());
        assertTrue(params.receiverSignatureRequired());
        assertEquals(10, params.maxAutomaticTokenAssociations());
        assertEquals(autoRenewPeriod, params.autoRenewPeriod());
        assertEquals(3L, params.stakedNodeId());
        assertTrue(params.declineStakingReward());
    }

    /// `enterprise.service.account/an-account-is-staked-to-an-account-to-a-node-or-to-neither`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); the Python SDK (account_create_transaction_test.py,
    /// test_set_stake_account_id_reset_stake_node_id and test_set_stack_node_id_reset_stake_account_id: one target clears
    /// the other).
    @Test
    @DisplayName("an account is staked to an account, to a node or to neither")
    void anAccountIsStakedToAnAccountToANodeOrToNeither() {
        // given
        final AccountId stakedAccount = defaultAccountId();

        // when
        final AccountCreateParams toAccount = new AccountCreateParams(defaultAuthority(), null, null, false, null, null, stakedAccount, null, false, null);
        final AccountCreateParams toNode = new AccountCreateParams(defaultAuthority(), null, null, false, null, null, null, 1L, false, null);
        final AccountCreateParams toNeither = defaultAccountCreateParams();

        // then
        assertEquals(stakedAccount, toAccount.stakedAccountId());
        assertNull(toAccount.stakedNodeId());
        assertEquals(1L, toNode.stakedNodeId());
        assertNull(toNode.stakedAccountId());
        assertNull(toNeither.stakedAccountId());
        assertNull(toNeither.stakedNodeId());
    }

    /// `enterprise.service.account/account-parameters-staked-to-both-an-account-and-a-node-are-rejected`
    ///
    /// Source: this spec (@@oneOrNoneOf(stakedAccountId, stakedNodeId)); HAPI crypto_create.proto (staked_id is a oneof:
    /// staked_account_id or staked_node_id).
    @Test
    @DisplayName("account parameters staked to both an account and a node are rejected")
    void accountParametersStakedToBothAnAccountAndANodeAreRejected() {
        // when / then
        assertThrows(IllegalArgumentException.class, () -> new AccountCreateParams(defaultAuthority(), null, null, false, null, null, defaultAccountId(), 3L, false, null));
    }

    private static AccountCreateParams defaultAccountCreateParams() {
        return new AccountCreateParams(defaultAuthority(), null, null, false, null, null, null, null, false, null);
    }

    private static Authority defaultAuthority() {
        return new PublicKeyAuthority(defaultPublicKey());
    }

    private static PublicKey defaultPublicKey() {
        return defaultPrivateKey().createPublicKey();
    }

    private static PrivateKey defaultPrivateKey() {
        return KeysFactory.createPrivateKey(KeyAlgorithm.ED25519, ByteImportEncoding.HEX, "d3671a1e98bb22f011c0e4bcf5125590e15d8f21a7017309bb558852039bc75c");
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}

package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-account`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-account")
class MirrorNodeAccountSpecTest {

    /// `mirrornode.account/an-account-info-without-optional-values-does-not-decline-rewards-and-is-not-deleted`
    ///
    /// Source: this spec (`declineReward` and `deleted` default to false; the other optional values are nullable).
    @Test
    @DisplayName("an account info without optional values does not decline rewards and is not deleted")
    void anAccountInfoWithoutOptionalValuesDoesNotDeclineRewardsAndIsNotDeleted() {
        // given
        final AccountId account = defaultAccountId();

        // when
        final AccountInfo info = new AccountInfo(account, null, 100L, null, 0L, 0L, null, null, null, null, null, null, null, null, false, false, null, null, null);

        // then
        assertEquals(account, info.accountId());
        assertEquals(100L, info.balance());
        assertFalse(info.declineReward());
        assertFalse(info.deleted());
        assertNull(info.evmAddress());
        assertNull(info.alias());
        assertNull(info.stakedAccountId());
        assertNull(info.stakedNodeId());
    }

    /// `mirrornode.account/an-account-balance-without-token-balances-has-an-empty-token-list`
    ///
    /// Source: this spec (`tokens` defaults to an empty list).
    @Test
    @DisplayName("an account balance without token balances has an empty token list")
    void anAccountBalanceWithoutTokenBalancesHasAnEmptyTokenList() {
        // when
        final AccountBalance balance = new AccountBalance(defaultAccountId(), 100L, List.of());

        // then
        assertEquals(100L, balance.balance());
        assertEquals(0L, length(balance.tokens()));
    }

    /// `mirrornode.account/an-account-balance-keeps-its-token-balances`
    ///
    /// Source: this spec (an account balance lists its token balances in the token's smallest unit).
    @Test
    @DisplayName("an account balance keeps its token balances")
    void anAccountBalanceKeepsItsTokenBalances() {
        // given
        final List<TokenBalance> tokens = List.of(new TokenBalance(Address.fromString("0.0.5005"), 10L), new TokenBalance(Address.fromString("0.0.5006"), 0L));

        // when
        final AccountBalance balance = new AccountBalance(defaultAccountId(), 100L, tokens);

        // then
        assertEquals(tokens, balance.tokens());
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }
}

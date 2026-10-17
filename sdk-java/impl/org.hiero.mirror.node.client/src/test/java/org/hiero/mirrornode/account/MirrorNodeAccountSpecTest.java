package org.hiero.mirrornode.account;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-account`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-account")
class MirrorNodeAccountSpecTest {

    /// The creation, expiry and balance timestamps of the Hiero Enterprise Java test fixture account-info.json
    /// ("1605733260.898372007", "1612733260.898372007", "1742896287.333154756"), in millisecond precision.
    private static List<ZonedDateTime> TIMES() {
        return List.of(toZonedDateTime("2020-11-18T21:01:00.898Z"), toZonedDateTime("2021-02-07T21:27:40.898Z"), toZonedDateTime("2025-03-25T09:51:27.333Z"));
    }

    /// `mirrornode.account/an-account-info-without-optional-values-does-not-decline-rewards-and-is-not-deleted`
    ///
    /// Source: this spec (`declineReward` and `deleted` default to false; the other optional values are nullable).
    @Test
    @DisplayName("an account info without optional values does not decline rewards and is not deleted")
    @Tag("testId:mirrornode.account/an-account-info-without-optional-values-does-not-decline-rewards-and-is-not-deleted")
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
        assertNull(info.balanceTimestamp());
        assertNull(info.createdTimestamp());
        assertNull(info.expiryTimestamp());
        assertNull(info.autoRenewPeriod());
        assertNull(info.stakePeriodStart());
    }

    /// `mirrornode.account/an-account-info-keeps-its-timestamps-and-its-auto-renew-period`
    ///
    /// Source: the Hiero Enterprise Java test fixture account-info.json (created "1605733260.898372007", expiry
    /// "1612733260.898372007", balance timestamp "1742896287.333154756", auto-renew period 7000000 seconds, no stake period
    /// start), the values of TIMES; millisecond precision.
    @Test
    @DisplayName("an account info keeps its timestamps and its auto-renew period")
    @Tag("testId:mirrornode.account/an-account-info-keeps-its-timestamps-and-its-auto-renew-period")
    void anAccountInfoKeepsItsTimestampsAndItsAutoRenewPeriod() {
        // given
        final ZonedDateTime created = get(TIMES(), 0L);
        final ZonedDateTime expiry = get(TIMES(), 1L);
        final ZonedDateTime balanceTime = get(TIMES(), 2L);

        // when
        final AccountInfo info = new AccountInfo(defaultAccountId(), null, 1663012637744658L, balanceTime, 0L, 0L, null, null, null, created, expiry, toSeconds(7000000L), null, null, false, false, null, null, null);

        // then
        assertEquals(toZonedDateTime("2020-11-18T21:01:00.898Z"), info.createdTimestamp());
        assertEquals(toZonedDateTime("2021-02-07T21:27:40.898Z"), info.expiryTimestamp());
        assertEquals(toZonedDateTime("2025-03-25T09:51:27.333Z"), info.balanceTimestamp());
        assertEquals(toSeconds(7000000L), info.autoRenewPeriod());
        assertNull(info.stakePeriodStart());
    }

    /// `mirrornode.account/a-staking-reward-keeps-its-account-amount-and-payout-time`
    ///
    /// Source: this spec (a staking reward is a payout of tinybars to the account at a consensus time); the values of
    /// TIMES.
    @Test
    @DisplayName("a staking reward keeps its account, amount and payout time")
    @Tag("testId:mirrornode.account/a-staking-reward-keeps-its-account-amount-and-payout-time")
    void aStakingRewardKeepsItsAccountAmountAndPayoutTime() {
        // given
        final AccountId account = defaultAccountId();

        // when
        final StakingReward reward = new StakingReward(account, 100L, get(TIMES(), 0L));
        final StakingReward later = new StakingReward(account, 100L, get(TIMES(), 1L));

        // then
        assertEquals(account, reward.accountId());
        assertEquals(100L, reward.amount());
        assertEquals(get(TIMES(), 0L), reward.timestamp());
        assertEquals(new StakingReward(account, 100L, get(TIMES(), 0L)), reward);
        assertNotEquals(reward, later);
    }

    /// `mirrornode.account/a-crypto-allowance-keeps-its-owner-spender-remaining-and-granted-amounts-and-its-time`
    ///
    /// Source: this spec (an HBAR allowance has a remaining and an originally approved amount and the time it was granted
    /// or last modified).
    @Test
    @DisplayName("a crypto allowance keeps its owner, spender, remaining and granted amounts and its time")
    @Tag("testId:mirrornode.account/a-crypto-allowance-keeps-its-owner-spender-remaining-and-granted-amounts-and-its-time")
    void aCryptoAllowanceKeepsItsOwnerSpenderRemainingAndGrantedAmountsAndItsTime() {
        // given
        final AccountId owner = AccountId.fromString("0.0.1001");
        final AccountId spender = AccountId.fromString("0.0.1002");
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final CryptoAllowance allowance = new CryptoAllowance(owner, spender, 25L, 100L, time);

        // then
        assertEquals(owner, allowance.owner());
        assertEquals(spender, allowance.spender());
        assertEquals(25L, allowance.amount());
        assertEquals(100L, allowance.amountGranted());
        assertEquals(time, allowance.timestamp());
    }

    /// `mirrornode.account/a-token-allowance-keeps-its-token-remaining-and-granted-amounts-and-its-time`
    ///
    /// Source: this spec (a fungible-token allowance has the token, a remaining and an originally approved amount and the
    /// time it was granted or last modified).
    @Test
    @DisplayName("a token allowance keeps its token, remaining and granted amounts and its time")
    @Tag("testId:mirrornode.account/a-token-allowance-keeps-its-token-remaining-and-granted-amounts-and-its-time")
    void aTokenAllowanceKeepsItsTokenRemainingAndGrantedAmountsAndItsTime() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final TokenAllowance allowance = new TokenAllowance(AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), token, 0L, 10L, time);

        // then
        assertEquals(token, allowance.tokenId());
        assertEquals(0L, allowance.amount());
        assertEquals(10L, allowance.amountGranted());
        assertEquals(time, allowance.timestamp());
    }

    /// `mirrornode.account/an-nft-allowance-keeps-whether-it-is-approved-for-the-whole-collection`
    ///
    /// Source: this spec (an NFT allowance is approved for all serials of the collection or not).
    @Test
    @DisplayName("an NFT allowance keeps whether it is approved for the whole collection")
    @Tag("testId:mirrornode.account/an-nft-allowance-keeps-whether-it-is-approved-for-the-whole-collection")
    void anNFTAllowanceKeepsWhetherItIsApprovedForTheWholeCollection() {
        // given
        final Address token = Address.fromString("0.0.5005");
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        for (final boolean approved : List.of(false, true)) {
            // when
            final NftAllowance allowance = new NftAllowance(AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), token, approved, time);

            // then
            assertEquals(approved, allowance.approvedForAll());
            assertEquals(token, allowance.tokenId());
            assertEquals(time, allowance.timestamp());
        }
    }

    /// `mirrornode.account/a-fungible-airdrop-has-an-amount-and-no-serial-number`
    ///
    /// Source: this spec (exactly one of `amount` and `serialNumber` is set; `serialNumber` is null for a fungible
    /// airdrop); HIP-904.
    @Test
    @DisplayName("a fungible airdrop has an amount and no serial number")
    @Tag("testId:mirrornode.account/a-fungible-airdrop-has-an-amount-and-no-serial-number")
    void aFungibleAirdropHasAnAmountAndNoSerialNumber() {
        // given
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final TokenAirdrop airdrop = new TokenAirdrop(AccountId.fromString("0.0.1001"), AccountId.fromString("0.0.1002"), Address.fromString("0.0.5005"), 10L, null, time);

        // then
        assertEquals(10L, airdrop.amount());
        assertNull(airdrop.serialNumber());
        assertEquals(time, airdrop.timestamp());
    }

    /// `mirrornode.account/an-nft-airdrop-has-a-serial-number-and-no-amount`
    ///
    /// Source: this spec (exactly one of `amount` and `serialNumber` is set; `amount` is null for an NFT airdrop);
    /// HIP-904.
    @Test
    @DisplayName("an NFT airdrop has a serial number and no amount")
    @Tag("testId:mirrornode.account/an-nft-airdrop-has-a-serial-number-and-no-amount")
    void anNFTAirdropHasASerialNumberAndNoAmount() {
        // given
        final AccountId sender = AccountId.fromString("0.0.1001");
        final AccountId receiver = AccountId.fromString("0.0.1002");

        // when
        final TokenAirdrop airdrop = new TokenAirdrop(sender, receiver, Address.fromString("0.0.5005"), null, 1L, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));

        // then
        assertEquals(sender, airdrop.senderId());
        assertEquals(receiver, airdrop.receiverId());
        assertEquals(1L, airdrop.serialNumber());
        assertNull(airdrop.amount());
    }

    /// `mirrornode.account/an-account-balance-without-token-balances-has-an-empty-token-list`
    ///
    /// Source: this spec (`tokens` defaults to an empty list).
    @Test
    @DisplayName("an account balance without token balances has an empty token list")
    @Tag("testId:mirrornode.account/an-account-balance-without-token-balances-has-an-empty-token-list")
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
    @Tag("testId:mirrornode.account/an-account-balance-keeps-its-token-balances")
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

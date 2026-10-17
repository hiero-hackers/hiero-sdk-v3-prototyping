package org.hiero.mirrornode.network;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-network`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-network")
class MirrorNodeNetworkSpecTest {

    /// `mirrornode.network/a-service-endpoint-keeps-either-its-ipv4-address-or-its-domain-name-and-its-port`
    ///
    /// Source: the Java SDK (MirrorNodeServiceEndpointTest: IP address 1.2.3.4, domain name "test.mirror.com", port 443);
    /// HIP-1046 (an endpoint is published either with an IP address or with a domain name).
    @Test
    @DisplayName("a service endpoint keeps either its IPv4 address or its domain name and its port")
    @Tag("testId:mirrornode.network/a-service-endpoint-keeps-either-its-ipv4-address-or-its-domain-name-and-its-port")
    void aServiceEndpointKeepsEitherItsIPv4AddressOrItsDomainNameAndItsPort() {
        // when
        final ServiceEndpoint byAddress = new ServiceEndpoint("1.2.3.4", 443, null);
        final ServiceEndpoint byName = new ServiceEndpoint(null, 443, "test.mirror.com");

        // then
        assertEquals("1.2.3.4", byAddress.ipAddressV4());
        assertNull(byAddress.domainName());
        assertEquals(443, byAddress.port());
        assertEquals("test.mirror.com", byName.domainName());
        assertNull(byName.ipAddressV4());
        assertEquals(443, byName.port());
    }

    /// `mirrornode.network/a-network-node-without-service-endpoints-and-admin-key-has-an-empty-endpoint-list-and-no-admin-key`
    ///
    /// Source: this spec (`serviceEndpoints` defaults to an empty list; `adminKey` is null on entries from before HIP-869).
    @Test
    @DisplayName("a network node without service endpoints and admin key has an empty endpoint list and no admin key")
    @Tag("testId:mirrornode.network/a-network-node-without-service-endpoints-and-admin-key-has-an-empty-endpoint-list-and-no-admin-key")
    void aNetworkNodeWithoutServiceEndpointsAndAdminKeyHasAnEmptyEndpointListAndNoAdminKey() {
        // given
        final AccountId account = AccountId.fromString("0.0.3");

        // when
        final NetworkNode node = new NetworkNode(0L, account, null, null, null, List.of(), null, 0L, null);

        // then
        assertEquals(0L, node.nodeId());
        assertEquals(account, node.accountId());
        assertEquals(0L, length(node.serviceEndpoints()));
        assertNull(node.adminKey());
        assertNull(node.timestamp());
    }

    /// `mirrornode.network/a-network-node-keeps-its-service-endpoints-in-order`
    ///
    /// Source: the Java SDK (MirrorNodeServiceEndpointTest); this spec (the address book entry lists its endpoints).
    @Test
    @DisplayName("a network node keeps its service endpoints in order")
    @Tag("testId:mirrornode.network/a-network-node-keeps-its-service-endpoints-in-order")
    void aNetworkNodeKeepsItsServiceEndpointsInOrder() {
        // given
        final List<ServiceEndpoint> endpoints = List.of(new ServiceEndpoint("1.2.3.4", 443, null), new ServiceEndpoint(null, 443, "test.mirror.com"));

        // when
        final NetworkNode node = new NetworkNode(0L, AccountId.fromString("0.0.3"), null, null, null, endpoints, null, 0L, null);

        // then
        assertEquals(endpoints, node.serviceEndpoints());
    }

    /// `mirrornode.network/a-network-node-keeps-the-consensus-time-of-its-address-book-entry`
    ///
    /// Source: this spec (`timestamp` is the consensus time of the address-book entry).
    @Test
    @DisplayName("a network node keeps the consensus time of its address-book entry")
    @Tag("testId:mirrornode.network/a-network-node-keeps-the-consensus-time-of-its-address-book-entry")
    void aNetworkNodeKeepsTheConsensusTimeOfItsAddressBookEntry() {
        // given
        final ZonedDateTime time = ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

        // when
        final NetworkNode node = new NetworkNode(0L, AccountId.fromString("0.0.3"), null, null, null, List.of(), null, 0L, time);

        // then
        assertEquals(time, node.timestamp());
    }

    /// `mirrornode.network/exchange-rates-keep-the-current-and-the-next-rate-with-their-expiration-times`
    ///
    /// Source: the Hiero Enterprise Java test fixture exchange-rate.json (current rate 212848 cents for 30000 HBAR expiring
    /// at 1785852000, next rate 211949 cents for 30000 HBAR expiring at 1785855600; the epoch seconds written as ISO 8601).
    @Test
    @DisplayName("exchange rates keep the current and the next rate with their expiration times")
    @Tag("testId:mirrornode.network/exchange-rates-keep-the-current-and-the-next-rate-with-their-expiration-times")
    void exchangeRatesKeepTheCurrentAndTheNextRateWithTheirExpirationTimes() {
        // given
        final ExchangeRate current = new ExchangeRate(212848, 30000, toZonedDateTime("2026-08-04T14:00:00Z"));
        final ExchangeRate next = new ExchangeRate(211949, 30000, toZonedDateTime("2026-08-04T15:00:00Z"));

        // when
        final ExchangeRates rates = new ExchangeRates(current, next);

        // then
        assertEquals(current, rates.currentRate());
        assertEquals(next, rates.nextRate());
        assertEquals(212848, rates.currentRate().centEquivalent());
        assertEquals(30000, rates.currentRate().hbarEquivalent());
        assertEquals(toZonedDateTime("2026-08-04T14:00:00Z"), rates.currentRate().expirationTime());
        assertEquals(toZonedDateTime("2026-08-04T15:00:00Z"), rates.nextRate().expirationTime());
        assertNotEquals(rates.currentRate(), rates.nextRate());
    }

    /// `mirrornode.network/exchange-rates-that-differ-only-in-their-expiration-time-are-different`
    ///
    /// Source: this spec (exchange rates are values: rates that differ only in their expiration time are different).
    @Test
    @DisplayName("exchange rates that differ only in their expiration time are different")
    @Tag("testId:mirrornode.network/exchange-rates-that-differ-only-in-their-expiration-time-are-different")
    void exchangeRatesThatDifferOnlyInTheirExpirationTimeAreDifferent() {
        // when
        final ExchangeRate first = new ExchangeRate(212848, 30000, toZonedDateTime("2026-08-04T14:00:00Z"));
        final ExchangeRate second = new ExchangeRate(212848, 30000, toZonedDateTime("2026-08-04T15:00:00Z"));

        // then
        assertNotEquals(first, second);
        assertEquals(new ExchangeRate(212848, 30000, toZonedDateTime("2026-08-04T14:00:00Z")), first);
    }

    /// `mirrornode.network/network-stake-keeps-its-staking-parameters-and-its-staking-period`
    ///
    /// Source: the Hiero Enterprise Java test fixture network-stake.json (its values; `staking_period_duration` 1440
    /// minutes, i.e. one day, is the staking period of 86400 seconds).
    @Test
    @DisplayName("network stake keeps its staking parameters and its staking period")
    @Tag("testId:mirrornode.network/network-stake-keeps-its-staking-parameters-and-its-staking-period")
    void networkStakeKeepsItsStakingParametersAndItsStakingPeriod() {
        // when
        final NetworkStake stake = new NetworkStake(650000000000000000L, 5287L, 44512288670430L, 0.1, 2781375493493491L, 8500000000000000L, 1465970599400000000L, toSeconds(86400L), 365L, 0.1, 44512288670430L, 25000000000000000L, 14652305964403937L);

        // then
        assertEquals(toSeconds(86400L), stake.stakingPeriod());
        assertEquals(365L, stake.stakingPeriodsStored());
        assertEquals(1465970599400000000L, stake.stakeTotal());
        assertEquals(650000000000000000L, stake.maxStakeReward());
        assertEquals(5287L, stake.maxStakeRewardPerHbar());
        assertEquals(0.1, stake.nodeRewardFeeFraction());
        assertEquals(0.1, stake.stakingRewardFeeFraction());
        assertEquals(14652305964403937L, stake.unreservedStakingRewardBalance());
    }
}

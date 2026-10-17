package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.HbarUnit;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccountId`, generated from the specs.
@DisplayName("AccountId")
class AccountIdTest {

    @Test
    @DisplayName("creates an AccountId and returns the values")
    @Tag("testId:ledger.AccountId/create")
    void shouldCreateAndReturnTheValues() {
        final long shard = shardValue();
        final long realm = realmValue();
        final String checksum = checksumValue();
        final @Nullable Long num = numValue();
        final @Nullable EvmAddress evmAddress = (EvmAddress) null;
        final byte @Nullable [] alias = (byte[]) null;
        final AccountId subject = new AccountId(shard, realm, checksum, num, evmAddress, alias);
        assertValue(shard, subject.shard());
        assertValue(realm, subject.realm());
        assertValue(checksum, subject.checksum());
        assertValue(num, subject.num());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:ledger.AccountId/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long shard = shardValue();
        final long realm = realmValue();
        final String checksum = checksumValue();
        final @Nullable Long num = numValue();
        final @Nullable EvmAddress evmAddress = (EvmAddress) null;
        final byte @Nullable [] alias = (byte[]) null;
        final AccountId subject = AccountId.builder()
                .shard(shard)
                .realm(realm)
                .checksum(checksum)
                .num(num)
                .evmAddress(evmAddress)
                .alias(alias)
                .build();
        assertValue(shard, subject.shard());
        assertValue(realm, subject.realm());
        assertValue(checksum, subject.checksum());
        assertValue(num, subject.num());
        assertValue(evmAddress, subject.evmAddress());
        assertValue(alias, subject.alias());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:ledger.AccountId/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AccountId subject = AccountId.builder()
                .shard(shardValue())
                .realm(realmValue())
                .checksum(checksumValue())
                .num(numValue())
                .build();
        assertValue(numValue(), subject.num());
        assertNull(subject.evmAddress());
        assertNull(subject.alias());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:ledger.AccountId/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AccountId.builder().build());
    }

    @Test
    @DisplayName("shard: accepts the minimum (0)")
    @Tag("testId:ledger.AccountId/shard/accepts-at-minimum")
    void shouldAcceptShardAtMinimum() {
        final long value = 0L;
        final AccountId subject = new AccountId(value, realmValue(), checksumValue(), numValue(), (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("shard: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.AccountId/shard/accepts-at-maximum")
    void shouldAcceptShardAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final AccountId subject = new AccountId(value, realmValue(), checksumValue(), numValue(), (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("realm: accepts the minimum (0)")
    @Tag("testId:ledger.AccountId/realm/accepts-at-minimum")
    void shouldAcceptRealmAtMinimum() {
        final long value = 0L;
        final AccountId subject = new AccountId(shardValue(), value, checksumValue(), numValue(), (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("realm: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.AccountId/realm/accepts-at-maximum")
    void shouldAcceptRealmAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final AccountId subject = new AccountId(shardValue(), value, checksumValue(), numValue(), (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("rejects null for checksum")
    @Tag("testId:ledger.AccountId/checksum/rejects-null")
    void shouldRejectNullChecksum() {
        assertThrows(NullPointerException.class, () -> new AccountId(shardValue(), realmValue(), (String) null, numValue(), (EvmAddress) null, (byte[]) null));
    }

    @Test
    @DisplayName("num: accepts the minimum (0)")
    @Tag("testId:ledger.AccountId/num/accepts-at-minimum")
    void shouldAcceptNumAtMinimum() {
        final @Nullable Long value = 0L;
        final AccountId subject = new AccountId(shardValue(), realmValue(), checksumValue(), value, (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("num: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.AccountId/num/accepts-at-maximum")
    void shouldAcceptNumAtMaximum() {
        final @Nullable Long value = Long.parseUnsignedLong("18446744073709551615");
        final AccountId subject = new AccountId(shardValue(), realmValue(), checksumValue(), value, (EvmAddress) null, (byte[]) null);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("the constructor copies alias; the accessor returns a copy")
    @Tag("testId:ledger.AccountId/alias/copies-in")
    void shouldCopyAliasIn() {
        final byte[] value = aliasValue();
        final byte[] expected = value.clone();
        final AccountId subject = new AccountId(shardValue(), realmValue(), checksumValue(), (Long) null, (EvmAddress) null, value);
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.alias());
        final byte[] returned = subject.alias();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.alias());
    }

    @Test
    @DisplayName("rejects num and evmAddress together (@@oneOf)")
    @Tag("testId:ledger.AccountId/num-evmAddress-alias/rejects-two")
    void shouldRejectTwoOfNumOrEvmAddressOrAlias() {
        assertThrows(IllegalArgumentException.class, () -> new AccountId(shardValue(), realmValue(), checksumValue(), numValue(), evmAddressValue(), (byte[]) null));
    }

    @Test
    @DisplayName("rejects none of num, evmAddress, alias (@@oneOf)")
    @Tag("testId:ledger.AccountId/num-evmAddress-alias/rejects-none")
    void shouldRejectNoneOfNumOrEvmAddressOrAlias() {
        assertThrows(IllegalArgumentException.class, () -> new AccountId(shardValue(), realmValue(), checksumValue(), (Long) null, (EvmAddress) null, (byte[]) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:ledger.AccountId/value-semantics")
    void shouldCompareByValue() {
        final AccountId subject = create();
        final AccountId other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("validateChecksum(network) can be called")
    @Tag("testId:ledger.AccountId/validateChecksum/call")
    void shouldCallValidateChecksum() {
        defaultSubject().validateChecksum(networkValue());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:ledger.AccountId/toString/call")
    void shouldCallToString() {
        assertNotNull(defaultSubject().toString());
    }

    @Test
    @DisplayName("toStringWithChecksum() can be called and returns a value")
    @Tag("testId:ledger.AccountId/toStringWithChecksum/call")
    void shouldCallToStringWithChecksum() {
        assertNotNull(defaultSubject().toStringWithChecksum());
    }

    @Test
    @DisplayName("fromString(value) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:ledger.AccountId/fromString/call")
    void shouldCallFromString() throws Exception {
        try {
            assertNotNull(AccountId.fromString(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("fromEvmAddress(shard, realm, address) can be called and returns a value")
    @Tag("testId:ledger.AccountId/fromEvmAddress/call")
    void shouldCallFromEvmAddress() {
        assertNotNull(AccountId.fromEvmAddress(shardValue(), realmValue(), addressValue()));
    }

    private static long shardValue() {
        return 1L;
    }

    private static long realmValue() {
        return 1L;
    }

    private static String checksumValue() {
        return "value";
    }

    private static long numValue() {
        return 1L;
    }

    private static EvmAddress evmAddressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static byte[] aliasValue() {
        return new byte[] {1, 2, 3};
    }

    private static AccountId create() {
        return new AccountId(shardValue(), realmValue(), checksumValue(), numValue(), (EvmAddress) null, (byte[]) null);
    }

    private static AccountId defaultSubject() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static Network<?> networkValue() {
        return new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
    }

    private static String valueValue() {
        return "value";
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }
}

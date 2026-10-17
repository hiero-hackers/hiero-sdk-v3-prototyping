package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.HbarUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Address`, generated from the specs.
@DisplayName("Address")
class AddressTest {

    @Test
    @DisplayName("creates an Address and returns the values")
    @Tag("testId:ledger.Address/create")
    void shouldCreateAndReturnTheValues() {
        final long shard = shardValue();
        final long realm = realmValue();
        final String checksum = checksumValue();
        final Long num = numValue();
        final Address subject = new Address(shard, realm, checksum, num);
        assertValue(shard, subject.shard());
        assertValue(realm, subject.realm());
        assertValue(checksum, subject.checksum());
        assertValue(num, subject.num());
    }

    @Test
    @DisplayName("shard: accepts the minimum (0)")
    @Tag("testId:ledger.Address/shard/accepts-at-minimum")
    void shouldAcceptShardAtMinimum() {
        final long value = 0L;
        final Address subject = new Address(value, realmValue(), checksumValue(), numValue());
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("shard: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.Address/shard/accepts-at-maximum")
    void shouldAcceptShardAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final Address subject = new Address(value, realmValue(), checksumValue(), numValue());
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("realm: accepts the minimum (0)")
    @Tag("testId:ledger.Address/realm/accepts-at-minimum")
    void shouldAcceptRealmAtMinimum() {
        final long value = 0L;
        final Address subject = new Address(shardValue(), value, checksumValue(), numValue());
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("realm: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.Address/realm/accepts-at-maximum")
    void shouldAcceptRealmAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final Address subject = new Address(shardValue(), value, checksumValue(), numValue());
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("rejects null for checksum")
    @Tag("testId:ledger.Address/checksum/rejects-null")
    void shouldRejectNullChecksum() {
        assertThrows(NullPointerException.class, () -> new Address(shardValue(), realmValue(), (String) null, numValue()));
    }

    @Test
    @DisplayName("rejects null for num")
    @Tag("testId:ledger.Address/num/rejects-null")
    void shouldRejectNullNum() {
        assertThrows(NullPointerException.class, () -> new Address(shardValue(), realmValue(), checksumValue(), (Long) null));
    }

    @Test
    @DisplayName("num: accepts the minimum (0)")
    @Tag("testId:ledger.Address/num/accepts-at-minimum")
    void shouldAcceptNumAtMinimum() {
        final Long value = 0L;
        final Address subject = new Address(shardValue(), realmValue(), checksumValue(), value);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("num: accepts the maximum (18446744073709551615)")
    @Tag("testId:ledger.Address/num/accepts-at-maximum")
    void shouldAcceptNumAtMaximum() {
        final Long value = Long.parseUnsignedLong("18446744073709551615");
        final Address subject = new Address(shardValue(), realmValue(), checksumValue(), value);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:ledger.Address/value-semantics")
    void shouldCompareByValue() {
        final Address subject = create();
        final Address other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("validateChecksum(network) can be called")
    @Tag("testId:ledger.Address/validateChecksum/call")
    void shouldCallValidateChecksum() {
        defaultSubject().validateChecksum(networkValue());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    @Tag("testId:ledger.Address/toString/call")
    void shouldCallToString() {
        assertNotNull(defaultSubject().toString());
    }

    @Test
    @DisplayName("toStringWithChecksum() can be called and returns a value")
    @Tag("testId:ledger.Address/toStringWithChecksum/call")
    void shouldCallToStringWithChecksum() {
        assertNotNull(defaultSubject().toStringWithChecksum());
    }

    @Test
    @DisplayName("fromString(address) can be called and returns a value (only IllegalArgumentException may occur)")
    @Tag("testId:ledger.Address/fromString/call")
    void shouldCallFromString() throws Exception {
        try {
            assertNotNull(Address.fromString(addressValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
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

    private static Address create() {
        return new Address(shardValue(), realmValue(), checksumValue(), numValue());
    }

    private static Address defaultSubject() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static Network<?> networkValue() {
        return new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
    }

    private static String addressValue() {
        return "value";
    }
}

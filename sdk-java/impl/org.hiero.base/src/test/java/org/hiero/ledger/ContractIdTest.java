package org.hiero.ledger;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.HbarUnit;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `ContractId`, generated from the specs.
@DisplayName("ContractId")
class ContractIdTest {

    @Test
    @DisplayName("creates a ContractId and returns the values")
    void shouldCreateAndReturnTheValues() {
        final long shard = shardValue();
        final long realm = realmValue();
        final String checksum = checksumValue();
        final @Nullable Long num = numValue();
        final @Nullable EvmAddress evmAddress = (EvmAddress) null;
        final ContractId subject = new ContractId(shard, realm, checksum, num, evmAddress);
        assertValue(shard, subject.shard());
        assertValue(realm, subject.realm());
        assertValue(checksum, subject.checksum());
        assertValue(num, subject.num());
        assertValue(evmAddress, subject.evmAddress());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    void shouldBuildTheValuesOfTheConstructor() {
        final long shard = shardValue();
        final long realm = realmValue();
        final String checksum = checksumValue();
        final @Nullable Long num = numValue();
        final @Nullable EvmAddress evmAddress = (EvmAddress) null;
        final ContractId subject = ContractId.builder()
                .shard(shard)
                .realm(realm)
                .checksum(checksum)
                .num(num)
                .evmAddress(evmAddress)
                .build();
        assertValue(shard, subject.shard());
        assertValue(realm, subject.realm());
        assertValue(checksum, subject.checksum());
        assertValue(num, subject.num());
        assertValue(evmAddress, subject.evmAddress());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    void shouldBuildWithoutTheOptionalValues() {
        final ContractId subject = ContractId.builder()
                .shard(shardValue())
                .realm(realmValue())
                .checksum(checksumValue())
                .num(numValue())
                .build();
        assertValue(numValue(), subject.num());
        assertNull(subject.evmAddress());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> ContractId.builder().build());
    }

    @Test
    @DisplayName("shard: accepts the minimum (0)")
    void shouldAcceptShardAtMinimum() {
        final long value = 0L;
        final ContractId subject = new ContractId(value, realmValue(), checksumValue(), numValue(), (EvmAddress) null);
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("shard: accepts the maximum (18446744073709551615)")
    void shouldAcceptShardAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ContractId subject = new ContractId(value, realmValue(), checksumValue(), numValue(), (EvmAddress) null);
        assertValue(value, subject.shard());
    }

    @Test
    @DisplayName("realm: accepts the minimum (0)")
    void shouldAcceptRealmAtMinimum() {
        final long value = 0L;
        final ContractId subject = new ContractId(shardValue(), value, checksumValue(), numValue(), (EvmAddress) null);
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("realm: accepts the maximum (18446744073709551615)")
    void shouldAcceptRealmAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final ContractId subject = new ContractId(shardValue(), value, checksumValue(), numValue(), (EvmAddress) null);
        assertValue(value, subject.realm());
    }

    @Test
    @DisplayName("rejects null for checksum")
    void shouldRejectNullChecksum() {
        assertThrows(NullPointerException.class, () -> new ContractId(shardValue(), realmValue(), (String) null, numValue(), (EvmAddress) null));
    }

    @Test
    @DisplayName("num: accepts the minimum (0)")
    void shouldAcceptNumAtMinimum() {
        final @Nullable Long value = 0L;
        final ContractId subject = new ContractId(shardValue(), realmValue(), checksumValue(), value, (EvmAddress) null);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("num: accepts the maximum (18446744073709551615)")
    void shouldAcceptNumAtMaximum() {
        final @Nullable Long value = Long.parseUnsignedLong("18446744073709551615");
        final ContractId subject = new ContractId(shardValue(), realmValue(), checksumValue(), value, (EvmAddress) null);
        assertValue(value, subject.num());
    }

    @Test
    @DisplayName("rejects num and evmAddress together (@@oneOf)")
    void shouldRejectTwoOfNumOrEvmAddress() {
        assertThrows(IllegalArgumentException.class, () -> new ContractId(shardValue(), realmValue(), checksumValue(), numValue(), evmAddressValue()));
    }

    @Test
    @DisplayName("rejects none of num, evmAddress (@@oneOf)")
    void shouldRejectNoneOfNumOrEvmAddress() {
        assertThrows(IllegalArgumentException.class, () -> new ContractId(shardValue(), realmValue(), checksumValue(), (Long) null, (EvmAddress) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final ContractId subject = create();
        final ContractId other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    @Test
    @DisplayName("validateChecksum(network) can be called")
    void shouldCallValidateChecksum() {
        defaultSubject().validateChecksum(networkValue());
    }

    @Test
    @DisplayName("toString() can be called and returns a value")
    void shouldCallToString() {
        assertNotNull(defaultSubject().toString());
    }

    @Test
    @DisplayName("toStringWithChecksum() can be called and returns a value")
    void shouldCallToStringWithChecksum() {
        assertNotNull(defaultSubject().toStringWithChecksum());
    }

    @Test
    @DisplayName("fromString(value) can be called and returns a value (only IllegalArgumentException may occur)")
    void shouldCallFromString() throws Exception {
        try {
            assertNotNull(ContractId.fromString(valueValue()));
        } catch (final Exception e) {
            if (!(e instanceof IllegalArgumentException)) {
                throw e;
            }
        }
    }

    @Test
    @DisplayName("fromEvmAddress(shard, realm, address) can be called and returns a value")
    void shouldCallFromEvmAddress() {
        assertNotNull(ContractId.fromEvmAddress(shardValue(), realmValue(), addressValue()));
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

    private static ContractId create() {
        return new ContractId(shardValue(), realmValue(), checksumValue(), numValue(), (EvmAddress) null);
    }

    private static ContractId defaultSubject() {
        return new ContractId(0L, 0L, "", 1001L, null);
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

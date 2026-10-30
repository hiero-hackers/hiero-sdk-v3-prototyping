package org.hiero.mirrornode.contract;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.EvmAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AccessListEntry`, generated from the specs.
@DisplayName("AccessListEntry")
class AccessListEntryTest {

    @Test
    @DisplayName("creates an AccessListEntry and returns the values")
    @Tag("testId:mirrornode.contract.AccessListEntry/create")
    void shouldCreateAndReturnTheValues() {
        final EvmAddress address = addressValue();
        final List<String> storageKeys = storageKeysValue();
        final AccessListEntry subject = new AccessListEntry(address, storageKeys);
        assertValue(address, subject.address());
        assertValue(storageKeys, subject.storageKeys());
    }

    @Test
    @DisplayName("creates an AccessListEntry with the default values")
    @Tag("testId:mirrornode.contract.AccessListEntry/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AccessListEntry subject = new AccessListEntry(addressValue());
        assertValue(List.of(), subject.storageKeys());
    }

    @Test
    @DisplayName("rejects null for address")
    @Tag("testId:mirrornode.contract.AccessListEntry/address/rejects-null")
    void shouldRejectNullAddress() {
        assertThrows(NullPointerException.class, () -> new AccessListEntry((EvmAddress) null, storageKeysValue()));
    }

    @Test
    @DisplayName("rejects null for storageKeys")
    @Tag("testId:mirrornode.contract.AccessListEntry/storageKeys/rejects-null")
    void shouldRejectNullStorageKeys() {
        assertThrows(NullPointerException.class, () -> new AccessListEntry(addressValue(), (List<String>) null));
    }

    @Test
    @DisplayName("the constructor copies storageKeys; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.contract.AccessListEntry/storageKeys/copies-in")
    void shouldCopyStorageKeysIn() {
        final List<String> value = new ArrayList<>(storageKeysValue());
        final List<String> expected = List.copyOf(value);
        final AccessListEntry subject = new AccessListEntry(addressValue(), value);
        value.clear();
        assertValue(expected, subject.storageKeys());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.storageKeys()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.contract.AccessListEntry/value-semantics")
    void shouldCompareByValue() {
        final AccessListEntry subject = create();
        final AccessListEntry other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static EvmAddress addressValue() {
        return new EvmAddress(new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
    }

    private static List<String> storageKeysValue() {
        return List.of("value");
    }

    private static AccessListEntry create() {
        return new AccessListEntry(addressValue(), storageKeysValue());
    }
}

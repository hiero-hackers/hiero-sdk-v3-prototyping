package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AddressBookRange`, generated from the specs.
@DisplayName("AddressBookRange")
class AddressBookRangeTest {

    @Test
    @DisplayName("creates an AddressBookRange and returns the values")
    @Tag("testId:blocknode.status.AddressBookRange/create")
    void shouldCreateAndReturnTheValues() {
        final byte[] addressBook = addressBookValue();
        final long startBlock = startBlockValue();
        final @Nullable Long endBlock = endBlockValue();
        final AddressBookRange subject = new AddressBookRange(addressBook, startBlock, endBlock);
        assertValue(addressBook, subject.addressBook());
        assertValue(startBlock, subject.startBlock());
        assertValue(endBlock, subject.endBlock());
    }

    @Test
    @DisplayName("rejects null for addressBook")
    @Tag("testId:blocknode.status.AddressBookRange/addressBook/rejects-null")
    void shouldRejectNullAddressBook() {
        assertThrows(NullPointerException.class, () -> new AddressBookRange((byte[]) null, startBlockValue(), endBlockValue()));
    }

    @Test
    @DisplayName("the constructor copies addressBook; the accessor returns a copy")
    @Tag("testId:blocknode.status.AddressBookRange/addressBook/copies-in")
    void shouldCopyAddressBookIn() {
        final byte[] value = addressBookValue();
        final byte[] expected = value.clone();
        final AddressBookRange subject = new AddressBookRange(value, startBlockValue(), endBlockValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.addressBook());
        final byte[] returned = subject.addressBook();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.addressBook());
    }

    @Test
    @DisplayName("startBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.AddressBookRange/startBlock/accepts-at-minimum")
    void shouldAcceptStartBlockAtMinimum() {
        final long value = 0L;
        final AddressBookRange subject = new AddressBookRange(addressBookValue(), value, endBlockValue());
        assertValue(value, subject.startBlock());
    }

    @Test
    @DisplayName("startBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.AddressBookRange/startBlock/accepts-at-maximum")
    void shouldAcceptStartBlockAtMaximum() {
        final long value = Long.parseUnsignedLong("18446744073709551615");
        final AddressBookRange subject = new AddressBookRange(addressBookValue(), value, endBlockValue());
        assertValue(value, subject.startBlock());
    }

    @Test
    @DisplayName("endBlock: accepts the minimum (0)")
    @Tag("testId:blocknode.status.AddressBookRange/endBlock/accepts-at-minimum")
    void shouldAcceptEndBlockAtMinimum() {
        final @Nullable Long value = 0L;
        final AddressBookRange subject = new AddressBookRange(addressBookValue(), startBlockValue(), value);
        assertValue(value, subject.endBlock());
    }

    @Test
    @DisplayName("endBlock: accepts the maximum (18446744073709551615)")
    @Tag("testId:blocknode.status.AddressBookRange/endBlock/accepts-at-maximum")
    void shouldAcceptEndBlockAtMaximum() {
        final @Nullable Long value = Long.parseUnsignedLong("18446744073709551615");
        final AddressBookRange subject = new AddressBookRange(addressBookValue(), startBlockValue(), value);
        assertValue(value, subject.endBlock());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.AddressBookRange/value-semantics")
    void shouldCompareByValue() {
        final AddressBookRange subject = create();
        final AddressBookRange other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static byte[] addressBookValue() {
        return new byte[] {1, 2, 3};
    }

    private static long startBlockValue() {
        return 1L;
    }

    private static long endBlockValue() {
        return 1L;
    }

    private static AddressBookRange create() {
        return new AddressBookRange(addressBookValue(), startBlockValue(), endBlockValue());
    }
}

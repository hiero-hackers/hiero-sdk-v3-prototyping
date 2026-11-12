package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `BlockNodeStatusDetail`, generated from the specs.
@DisplayName("BlockNodeStatusDetail")
class BlockNodeStatusDetailTest {

    @Test
    @DisplayName("creates a BlockNodeStatusDetail and returns the values")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/create")
    void shouldCreateAndReturnTheValues() {
        final BlockNodeVersions versions = versionsValue();
        final List<BlockRange> availableRanges = availableRangesValue();
        final List<BlockRange> storedRanges = storedRangesValue();
        final @Nullable TssData tssData = tssDataValue();
        final byte[] nodeAddressBook = nodeAddressBookValue();
        final List<AddressBookRange> addressBookHistory = addressBookHistoryValue();
        final BlockNodeStatusDetail subject = new BlockNodeStatusDetail(versions, availableRanges, storedRanges, tssData, nodeAddressBook, addressBookHistory);
        assertValue(versions, subject.versions());
        assertValue(availableRanges, subject.availableRanges());
        assertValue(storedRanges, subject.storedRanges());
        assertValue(tssData, subject.tssData());
        assertValue(nodeAddressBook, subject.nodeAddressBook());
        assertValue(addressBookHistory, subject.addressBookHistory());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final BlockNodeVersions versions = versionsValue();
        final List<BlockRange> availableRanges = availableRangesValue();
        final List<BlockRange> storedRanges = storedRangesValue();
        final @Nullable TssData tssData = tssDataValue();
        final byte[] nodeAddressBook = nodeAddressBookValue();
        final List<AddressBookRange> addressBookHistory = addressBookHistoryValue();
        final BlockNodeStatusDetail subject = BlockNodeStatusDetail.builder()
                .versions(versions)
                .availableRanges(availableRanges)
                .storedRanges(storedRanges)
                .tssData(tssData)
                .nodeAddressBook(nodeAddressBook)
                .addressBookHistory(addressBookHistory)
                .build();
        assertValue(versions, subject.versions());
        assertValue(availableRanges, subject.availableRanges());
        assertValue(storedRanges, subject.storedRanges());
        assertValue(tssData, subject.tssData());
        assertValue(nodeAddressBook, subject.nodeAddressBook());
        assertValue(addressBookHistory, subject.addressBookHistory());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final BlockNodeStatusDetail subject = BlockNodeStatusDetail.builder()
                .versions(versionsValue())
                .availableRanges(availableRangesValue())
                .storedRanges(storedRangesValue())
                .nodeAddressBook(nodeAddressBookValue())
                .addressBookHistory(addressBookHistoryValue())
                .build();
        assertNull(subject.tssData());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> BlockNodeStatusDetail.builder().build());
    }

    @Test
    @DisplayName("rejects null for versions")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/versions/rejects-null")
    void shouldRejectNullVersions() {
        assertThrows(NullPointerException.class, () -> new BlockNodeStatusDetail((BlockNodeVersions) null, availableRangesValue(), storedRangesValue(), tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue()));
    }

    @Test
    @DisplayName("rejects null for availableRanges")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/availableRanges/rejects-null")
    void shouldRejectNullAvailableRanges() {
        assertThrows(NullPointerException.class, () -> new BlockNodeStatusDetail(versionsValue(), (List<BlockRange>) null, storedRangesValue(), tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue()));
    }

    @Test
    @DisplayName("the constructor copies availableRanges; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/availableRanges/copies-in")
    void shouldCopyAvailableRangesIn() {
        final List<BlockRange> value = new ArrayList<>(availableRangesValue());
        final List<BlockRange> expected = List.copyOf(value);
        final BlockNodeStatusDetail subject = new BlockNodeStatusDetail(versionsValue(), value, storedRangesValue(), tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue());
        value.clear();
        assertValue(expected, subject.availableRanges());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.availableRanges()).clear());
    }

    @Test
    @DisplayName("rejects null for storedRanges")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/storedRanges/rejects-null")
    void shouldRejectNullStoredRanges() {
        assertThrows(NullPointerException.class, () -> new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), (List<BlockRange>) null, tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue()));
    }

    @Test
    @DisplayName("the constructor copies storedRanges; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/storedRanges/copies-in")
    void shouldCopyStoredRangesIn() {
        final List<BlockRange> value = new ArrayList<>(storedRangesValue());
        final List<BlockRange> expected = List.copyOf(value);
        final BlockNodeStatusDetail subject = new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), value, tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue());
        value.clear();
        assertValue(expected, subject.storedRanges());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.storedRanges()).clear());
    }

    @Test
    @DisplayName("rejects null for nodeAddressBook")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/nodeAddressBook/rejects-null")
    void shouldRejectNullNodeAddressBook() {
        assertThrows(NullPointerException.class, () -> new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), storedRangesValue(), tssDataValue(), (byte[]) null, addressBookHistoryValue()));
    }

    @Test
    @DisplayName("the constructor copies nodeAddressBook; the accessor returns a copy")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/nodeAddressBook/copies-in")
    void shouldCopyNodeAddressBookIn() {
        final byte[] value = nodeAddressBookValue();
        final byte[] expected = value.clone();
        final BlockNodeStatusDetail subject = new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), storedRangesValue(), tssDataValue(), value, addressBookHistoryValue());
        value[0] = (byte) (value[0] + 1);
        assertValue(expected, subject.nodeAddressBook());
        final byte[] returned = subject.nodeAddressBook();
        assertNotNull(returned);
        returned[0] = (byte) (returned[0] + 1);
        assertValue(expected, subject.nodeAddressBook());
    }

    @Test
    @DisplayName("rejects null for addressBookHistory")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/addressBookHistory/rejects-null")
    void shouldRejectNullAddressBookHistory() {
        assertThrows(NullPointerException.class, () -> new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), storedRangesValue(), tssDataValue(), nodeAddressBookValue(), (List<AddressBookRange>) null));
    }

    @Test
    @DisplayName("the constructor copies addressBookHistory; the accessor returns an unmodifiable collection")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/addressBookHistory/copies-in")
    void shouldCopyAddressBookHistoryIn() {
        final List<AddressBookRange> value = new ArrayList<>(addressBookHistoryValue());
        final List<AddressBookRange> expected = List.copyOf(value);
        final BlockNodeStatusDetail subject = new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), storedRangesValue(), tssDataValue(), nodeAddressBookValue(), value);
        value.clear();
        assertValue(expected, subject.addressBookHistory());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.addressBookHistory()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:blocknode.status.BlockNodeStatusDetail/value-semantics")
    void shouldCompareByValue() {
        final BlockNodeStatusDetail subject = create();
        final BlockNodeStatusDetail other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static BlockNodeVersions versionsValue() {
        return new BlockNodeVersions(new SemanticVersion(0, 44, 2, "", ""), new SemanticVersion(0, 44, 2, "", ""), List.of());
    }

    private static List<BlockRange> availableRangesValue() {
        return List.of(new BlockRange(0L, 100L));
    }

    private static List<BlockRange> storedRangesValue() {
        return List.of(new BlockRange(0L, 100L));
    }

    private static TssData tssDataValue() {
        return new TssData(new byte[] {1, 2, 3}, new byte[] {1, 2, 3}, List.of(new TssRosterEntry(1L, 1L, new byte[] {1, 2, 3})), 1L);
    }

    private static byte[] nodeAddressBookValue() {
        return new byte[] {1, 2, 3};
    }

    private static List<AddressBookRange> addressBookHistoryValue() {
        return List.of(new AddressBookRange(new byte[] {1, 2, 3}, 1L, null));
    }

    private static BlockNodeStatusDetail create() {
        return new BlockNodeStatusDetail(versionsValue(), availableRangesValue(), storedRangesValue(), tssDataValue(), nodeAddressBookValue(), addressBookHistoryValue());
    }

    private static BlockNodeStatusDetail defaultSubject() {
        return new BlockNodeStatusDetail(new BlockNodeVersions(new SemanticVersion(0, 44, 2, "", ""), new SemanticVersion(0, 44, 2, "", ""), List.of()), List.of(new BlockRange(0L, 100L)), List.of(new BlockRange(0L, 100L)), null, new byte[0], List.of());
    }
}

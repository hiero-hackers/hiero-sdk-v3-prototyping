package org.hiero.blocknode.status;

import static org.hiero.sdk.testing.Testing.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `block-node-status`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("block-node-status")
class BlockNodeStatusSpecTest {

    /// `blocknode.status/a-status-without-a-known-last-block-has-no-last-and-no-expected-block`
    ///
    /// Source: node_service.proto of the block node (ServerStatusResponse: `last_available_block` and
    /// `next_expected_block` may be uint64_max, then the block node accepts any block next); this spec (both are null then).
    @Test
    @DisplayName("a status without a known last block has no last and no expected block")
    @Tag("testId:blocknode.status/a-status-without-a-known-last-block-has-no-last-and-no-expected-block")
    void aStatusWithoutAKnownLastBlockHasNoLastAndNoExpectedBlock() {
        // when
        final BlockNodeStatus status = new BlockNodeStatus(0L, null, false, null);

        // then
        assertEquals(0L, status.firstAvailableBlock());
        assertNull(status.lastAvailableBlock());
        assertNull(status.nextExpectedBlock());
    }

    /// `blocknode.status/an-open-ended-address-book-range-has-no-end-block`
    ///
    /// Source: node_service.proto of the block node (RangedNodeAddressBook: an `end_block` of uint64_max is open-ended);
    /// this spec (an open end is null).
    @Test
    @DisplayName("an open-ended address book range has no end block")
    @Tag("testId:blocknode.status/an-open-ended-address-book-range-has-no-end-block")
    void anOpenEndedAddressBookRangeHasNoEndBlock() {
        // when
        final AddressBookRange range = new AddressBookRange(hex("0a00"), 1000L, null);

        // then
        assertEquals(1000L, range.startBlock());
        assertEquals(hex("0a00"), range.addressBook());
        assertNull(range.endBlock());
    }

    /// `blocknode.status/a-detailed-status-without-tss-data-has-none`
    ///
    /// Source: node_service.proto of the block node (TssData is set completely or not at all).
    @Test
    @DisplayName("a detailed status without TSS data has none")
    @Tag("testId:blocknode.status/a-detailed-status-without-tss-data-has-none")
    void aDetailedStatusWithoutTSSDataHasNone() {
        // when
        final BlockNodeStatusDetail detail = defaultBlockNodeStatusDetail();

        // then
        assertNull(detail.tssData());
        assertEquals(List.of(defaultBlockRange()), detail.availableRanges());
    }

    private static BlockNodeStatusDetail defaultBlockNodeStatusDetail() {
        return new BlockNodeStatusDetail(defaultBlockNodeVersions(), List.of(defaultBlockRange()), List.of(defaultBlockRange()), null, new byte[0], List.of());
    }

    private static BlockNodeVersions defaultBlockNodeVersions() {
        return new BlockNodeVersions(defaultSemanticVersion(), defaultSemanticVersion(), List.of());
    }

    private static SemanticVersion defaultSemanticVersion() {
        return new SemanticVersion(0, 44, 2, "", "");
    }

    private static BlockRange defaultBlockRange() {
        return new BlockRange(0L, 100L);
    }
}

package org.hiero.mirrornode.block;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.hiero.mirrornode.common.TimestampRange;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `mirror-node-block`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("mirror-node-block")
class MirrorNodeBlockSpecTest {

    /// `mirrornode.block/a-block-keeps-its-number-hashes-and-consensus-time-range`
    ///
    /// Source: the example of the Block schema of the mirror node OpenAPI definition (block 77 with 3 transactions, gas used
    /// 300000, HAPI version "0.11.0", record file "2022-05-03T06_46_26.060890949Z.rcd", size 8192, timestamp range from
    /// "1651560386.060890949" to "1651560386.661997287"); millisecond precision.
    @Test
    @DisplayName("a block keeps its number, hashes and consensus time range")
    @Tag("testId:mirrornode.block/a-block-keeps-its-number-hashes-and-consensus-time-range")
    void aBlockKeepsItsNumberHashesAndConsensusTimeRange() {
        // given
        final byte[] hash = hex("3c08bbbee74d287b1dcd3f0ca6d1d2cb92c90883c4acf9747de9f3f3162ad25b999fc7e86699f60f2a3fb3ed9a646c6b");
        final byte[] previousHash = hex("f7d6481f659c866c35391ee230c374f163642ebf13a5e604e04a95a9ca48a298dc2dfa10f51bcbaab8ae23bc6d662a0b");
        final TimestampRange range = new TimestampRange(toZonedDateTime("2022-05-03T06:46:26.060Z"), toZonedDateTime("2022-05-03T06:46:26.661Z"));

        // when
        final Block block = new Block(3L, 300000L, "0.11.0", hash, null, "2022-05-03T06_46_26.060890949Z.rcd", 77L, previousHash, 8192L, range);

        // then
        assertEquals(77L, block.number());
        assertEquals(3L, block.count());
        assertEquals(300000L, block.gasUsed());
        assertEquals("0.11.0", block.hapiVersion());
        assertEquals(hash, block.hash());
        assertEquals(48L, length(block.hash()));
        assertEquals(previousHash, block.previousHash());
        assertEquals("2022-05-03T06_46_26.060890949Z.rcd", block.name());
        assertEquals(8192L, block.size());
        assertEquals(range, block.timestamp());
        assertNull(block.logsBloom());
    }

    /// `mirrornode.block/a-block-without-evm-data-has-no-gas-used-and-no-logs-bloom`
    ///
    /// Source: the Block schema of the mirror node OpenAPI definition (`gas_used`, `hapi_version`, `logs_bloom` and `size`
    /// may be null).
    @Test
    @DisplayName("a block without EVM data has no gas used and no logs bloom")
    @Tag("testId:mirrornode.block/a-block-without-evm-data-has-no-gas-used-and-no-logs-bloom")
    void aBlockWithoutEVMDataHasNoGasUsedAndNoLogsBloom() {
        // when
        final Block block = new Block(0L, null, null, hex("00"), null, "block", 0L, hex("00"), null, new TimestampRange(ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), null));

        // then
        assertNull(block.gasUsed());
        assertNull(block.hapiVersion());
        assertNull(block.logsBloom());
        assertNull(block.size());
    }
}

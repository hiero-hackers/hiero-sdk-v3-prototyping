package org.hiero.consensusnode.admin.freeze;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.ledger.Address;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `transactions-freeze`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("transactions-freeze")
class TransactionsFreezeSpecTest {

    /// The id of the system file of the example: an upgrade file.
    private static Address UPGRADE_FILE() {
        return new Address(0L, 0L, "", 150L);
    }

    /// A SHA-384 hash has 48 bytes; the content of the hash does not matter to the transaction.
    private static byte[] UPGRADE_FILE_HASH() {
        return repeat(hex("ab"), 48L);
    }

    /// `consensusnode.admin.freeze/a-freeze-upgrade-keeps-its-type-start-time-update-file-and-file-hash`
    ///
    /// Source: the Java (getSetFreezeType, getSetStartTime, getSetFileId, getSetFileHash), Python
    /// (test_constructor_with_parameters), C++, Go and Swift FreezeTransaction unit tests. The start time is the one of the
    /// Java test (Instant.ofEpochSecond(1554158542), converted exactly to 2019-04-01T22:42:22Z).
    @Test
    @DisplayName("a freeze upgrade keeps its type, start time, update file and file hash")
    void aFreezeUpgradeKeepsItsTypeStartTimeUpdateFileAndFileHash() {
        // given
        final ZonedDateTime startTime = toZonedDateTime("2019-04-01T22:42:22Z");

        // when
        final FreezeTransaction transaction = new FreezeTransaction(FreezeType.FREEZE_UPGRADE, startTime, UPGRADE_FILE(), UPGRADE_FILE_HASH());

        // then
        assertEquals(FreezeType.FREEZE_UPGRADE, transaction.freezeType());
        assertEquals(startTime, transaction.startTime());
        assertEquals(UPGRADE_FILE(), transaction.updateFile());
        assertEquals(UPGRADE_FILE_HASH(), transaction.fileHash());
    }

    /// `consensusnode.admin.freeze/a-prepared-upgrade-needs-no-start-time`
    ///
    /// Source: the Python (test_build_transaction_body_with_none_values) and Java (shouldBytesNoSetters) FreezeTransaction
    /// unit tests; the Description of this spec (PREPARE_UPGRADE ignores startTime).
    @Test
    @DisplayName("a prepared upgrade needs no start time")
    void aPreparedUpgradeNeedsNoStartTime() {
        // when
        final FreezeTransaction transaction = new FreezeTransaction(FreezeType.PREPARE_UPGRADE, null, UPGRADE_FILE(), UPGRADE_FILE_HASH());

        // then
        assertEquals(FreezeType.PREPARE_UPGRADE, transaction.freezeType());
        assertNull(transaction.startTime());
        assertEquals(UPGRADE_FILE(), transaction.updateFile());
        assertEquals(UPGRADE_FILE_HASH(), transaction.fileHash());
    }

    /// `consensusnode.admin.freeze/a-freeze-without-upgrade-needs-no-update-file-and-no-file-hash`
    ///
    /// Source: the Python FreezeTransaction unit test (test_build_proto_body_with_none_fields, with the FREEZE_ONLY type and
    /// the start time Timestamp(1640995200, 0) of its freeze_params fixture, converted exactly to 2022-01-01T00:00:00Z); the
    /// Description of this spec (FREEZE_ONLY needs no upgrade payload).
    @Test
    @DisplayName("a freeze without upgrade needs no update file and no file hash")
    void aFreezeWithoutUpgradeNeedsNoUpdateFileAndNoFileHash() {
        // given
        final ZonedDateTime startTime = toZonedDateTime("2022-01-01T00:00:00Z");

        // when
        final FreezeTransaction transaction = new FreezeTransaction(FreezeType.FREEZE_ONLY, startTime, null, null);

        // then
        assertEquals(FreezeType.FREEZE_ONLY, transaction.freezeType());
        assertEquals(startTime, transaction.startTime());
        assertNull(transaction.updateFile());
        assertNull(transaction.fileHash());
    }

    /// `consensusnode.admin.freeze/a-telemetry-upgrade-keeps-its-start-time-update-file-and-file-hash`
    ///
    /// Source: the Description of this spec (TELEMETRY_UPGRADE requires a start time, updateFile and fileHash like
    /// FREEZE_UPGRADE); the start time is the one of the JavaScript FreezeTransaction unit test ("should correctly
    /// deserialize from protobuf with all fields": Timestamp(1624430225, 0), converted exactly to 2021-06-23T06:37:05Z).
    @Test
    @DisplayName("a telemetry upgrade keeps its start time, update file and file hash")
    void aTelemetryUpgradeKeepsItsStartTimeUpdateFileAndFileHash() {
        // given
        final ZonedDateTime startTime = toZonedDateTime("2021-06-23T06:37:05Z");

        // when
        final FreezeTransaction transaction = new FreezeTransaction(FreezeType.TELEMETRY_UPGRADE, startTime, UPGRADE_FILE(), UPGRADE_FILE_HASH());

        // then
        assertEquals(FreezeType.TELEMETRY_UPGRADE, transaction.freezeType());
        assertEquals(startTime, transaction.startTime());
        assertEquals(UPGRADE_FILE(), transaction.updateFile());
        assertEquals(UPGRADE_FILE_HASH(), transaction.fileHash());
    }

    /// `consensusnode.admin.freeze/a-freeze-transaction-keeps-every-freeze-type`
    ///
    /// Source: the Go (TestUnitFreezeTypeString) and Python (test_freeze_type_values, test_round_trip_conversion)
    /// FreezeType unit tests: every freeze type is kept as given.
    @Test
    @DisplayName("a freeze transaction keeps every freeze type")
    void aFreezeTransactionKeepsEveryFreezeType() {
        for (final FreezeType freezeType : List.of(FreezeType.FREEZE_ONLY, FreezeType.PREPARE_UPGRADE, FreezeType.FREEZE_UPGRADE, FreezeType.FREEZE_ABORT, FreezeType.TELEMETRY_UPGRADE)) {
            // when
            final FreezeTransaction transaction = new FreezeTransaction(freezeType, null, null, null);

            // then
            assertEquals(freezeType, transaction.freezeType());
        }
    }
}

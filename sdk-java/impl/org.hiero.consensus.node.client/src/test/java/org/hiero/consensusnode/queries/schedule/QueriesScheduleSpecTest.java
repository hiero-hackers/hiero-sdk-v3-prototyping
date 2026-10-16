package org.hiero.consensusnode.queries.schedule;

import static org.hiero.sdk.testing.Testing.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import org.hiero.consensusnode.transactions.utility.PrngTransaction;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.hiero.ledger.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `queries-schedule`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("queries-schedule")
class QueriesScheduleSpecTest {

    /// The point in time of the schedule fixtures of the Java SDK (ScheduleInfoTest, validStart):
    /// Instant.ofEpochSecond(1554158542), i.e. 2019-04-01T22:42:22Z.
    private static ZonedDateTime SCHEDULE_TEST_TIME() {
        return toZonedDateTime("2019-04-01T22:42:22Z");
    }

    /// `consensusnode.queries.schedule/a-schedule-info-query-keeps-the-id-of-its-schedule`
    ///
    /// Source: the Python SDK (schedule_info_query_test.py, test_constructor and test_set_schedule_id) and the Swift SDK
    /// (ScheduleInfoQueryUnitTests, the get / set test).
    @Test
    @DisplayName("a schedule info query keeps the id of its schedule")
    void aScheduleInfoQueryKeepsTheIdOfItsSchedule() {
        // given
        final Address schedule = defaultAddress();

        // when
        final ScheduleInfoQuery query = new ScheduleInfoQuery(schedule);

        // then
        assertEquals(schedule, query.scheduleId());
    }

    /// `consensusnode.queries.schedule/a-pending-schedule-info-has-these-defaults`
    ///
    /// Source: this spec (the defaults of ScheduleInfo; neither time is set while the schedule is pending); the Python SDK
    /// (schedule_info_test.py, test_schedule_info_default_initialization).
    @Test
    @DisplayName("a pending schedule info has these defaults")
    void aPendingScheduleInfoHasTheseDefaults() {
        // when
        final ScheduleInfo info = new ScheduleInfo(defaultAddress(), defaultAccountId(), defaultAccountId(), new PrngTransaction(null), defaultTransactionId(), List.of(), null, null, ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC), false, null, null);

        // then
        assertEquals(0L, length(info.signers()));
        assertFalse(info.waitForExpiry());
        assertNull(info.adminKey());
        assertNull(info.scheduleMemo());
        assertNull(info.executionTime());
        assertNull(info.deletionTime());
    }

    /// `consensusnode.queries.schedule/an-executed-schedule-info-keeps-its-execution-time-and-has-no-deletion-time`
    ///
    /// Source: the Java SDK (ScheduleInfoTest.shouldSerialize, spawnScheduleInfoExample): expiration and execution time
    /// Instant.ofEpochSecond(1554158542), i.e. 2019-04-01T22:42:22Z, no deletion time; this spec (an executed schedule
    /// has its executionTime set). Second precision.
    @Test
    @DisplayName("an executed schedule info keeps its execution time and has no deletion time")
    void anExecutedScheduleInfoKeepsItsExecutionTimeAndHasNoDeletionTime() {
        // when
        final ScheduleInfo info = new ScheduleInfo(defaultAddress(), defaultAccountId(), defaultAccountId(), new PrngTransaction(null), defaultTransactionId(), List.of(), null, null, SCHEDULE_TEST_TIME(), true, SCHEDULE_TEST_TIME(), null);

        // then
        assertEquals(toZonedDateTime("2019-04-01T22:42:22Z"), info.expirationTime());
        assertEquals(toZonedDateTime("2019-04-01T22:42:22Z"), info.executionTime());
        assertNull(info.deletionTime());
        assertTrue(info.waitForExpiry());
    }

    /// `consensusnode.queries.schedule/a-deleted-schedule-info-keeps-its-deletion-time-and-has-no-execution-time`
    ///
    /// Source: the Java SDK (ScheduleInfoTest.shouldSerializeDeleted, spawnScheduleInfoDeletedExample): expiration and
    /// deletion time Instant.ofEpochSecond(1554158542), i.e. 2019-04-01T22:42:22Z, no execution time; this spec (a deleted
    /// schedule has its deletionTime set). Second precision.
    @Test
    @DisplayName("a deleted schedule info keeps its deletion time and has no execution time")
    void aDeletedScheduleInfoKeepsItsDeletionTimeAndHasNoExecutionTime() {
        // when
        final ScheduleInfo info = new ScheduleInfo(defaultAddress(), defaultAccountId(), defaultAccountId(), new PrngTransaction(null), defaultTransactionId(), List.of(), null, null, SCHEDULE_TEST_TIME(), false, null, SCHEDULE_TEST_TIME());

        // then
        assertEquals(toZonedDateTime("2019-04-01T22:42:22Z"), info.expirationTime());
        assertEquals(toZonedDateTime("2019-04-01T22:42:22Z"), info.deletionTime());
        assertNull(info.executionTime());
    }

    private static Address defaultAddress() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AccountId defaultAccountId() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static TransactionId defaultTransactionId() {
        return TransactionId.generateTransactionId(defaultAddress());
    }
}

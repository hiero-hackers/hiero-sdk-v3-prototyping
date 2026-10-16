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

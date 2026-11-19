package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Set;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeSchedules`, generated from the specs.
@DisplayName("FeeSchedules")
class FeeSchedulesTest {

    @Test
    @DisplayName("creates a FeeSchedules and returns the values")
    @Tag("testId:consensusnode.queries.fees.FeeSchedules/create")
    void shouldCreateAndReturnTheValues() {
        final FeeSchedule currentSchedule = currentScheduleValue();
        final FeeSchedule nextSchedule = nextScheduleValue();
        final FeeSchedules subject = new FeeSchedules(currentSchedule, nextSchedule);
        assertValue(currentSchedule, subject.currentSchedule());
        assertValue(nextSchedule, subject.nextSchedule());
    }

    @Test
    @DisplayName("rejects null for currentSchedule")
    @Tag("testId:consensusnode.queries.fees.FeeSchedules/currentSchedule/rejects-null")
    void shouldRejectNullCurrentSchedule() {
        assertThrows(NullPointerException.class, () -> new FeeSchedules((FeeSchedule) null, nextScheduleValue()));
    }

    @Test
    @DisplayName("rejects null for nextSchedule")
    @Tag("testId:consensusnode.queries.fees.FeeSchedules/nextSchedule/rejects-null")
    void shouldRejectNullNextSchedule() {
        assertThrows(NullPointerException.class, () -> new FeeSchedules(currentScheduleValue(), (FeeSchedule) null));
    }

    private static FeeSchedule currentScheduleValue() {
        return new FeeSchedule(Set.of(new TransactionFeeSchedule(BasicRequestType.NONE, Set.of(new FeeData(new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), BasicFeeSubType.DEFAULT)))), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static FeeSchedule nextScheduleValue() {
        return new FeeSchedule(Set.of(new TransactionFeeSchedule(BasicRequestType.NONE, Set.of(new FeeData(new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), BasicFeeSubType.DEFAULT)))), ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC));
    }

    private static FeeSchedules create() {
        return new FeeSchedules(currentScheduleValue(), nextScheduleValue());
    }
}

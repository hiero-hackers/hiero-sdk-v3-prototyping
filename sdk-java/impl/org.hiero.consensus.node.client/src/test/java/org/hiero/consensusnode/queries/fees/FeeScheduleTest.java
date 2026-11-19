package org.hiero.consensusnode.queries.fees;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.hiero.consensusnode.requestTypes.BasicRequestType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `FeeSchedule`, generated from the specs.
@DisplayName("FeeSchedule")
class FeeScheduleTest {

    @Test
    @DisplayName("creates a FeeSchedule and returns the values")
    @Tag("testId:consensusnode.queries.fees.FeeSchedule/create")
    void shouldCreateAndReturnTheValues() {
        final Set<TransactionFeeSchedule> transactionFeeSchedules = transactionFeeSchedulesValue();
        final ZonedDateTime expirationTime = expirationTimeValue();
        final FeeSchedule subject = new FeeSchedule(transactionFeeSchedules, expirationTime);
        assertValue(transactionFeeSchedules, subject.transactionFeeSchedules());
        assertValue(expirationTime, subject.expirationTime());
    }

    @Test
    @DisplayName("rejects null for transactionFeeSchedules")
    @Tag("testId:consensusnode.queries.fees.FeeSchedule/transactionFeeSchedules/rejects-null")
    void shouldRejectNullTransactionFeeSchedules() {
        assertThrows(NullPointerException.class, () -> new FeeSchedule((Set<TransactionFeeSchedule>) null, expirationTimeValue()));
    }

    @Test
    @DisplayName("the constructor copies transactionFeeSchedules; the accessor returns an unmodifiable collection")
    @Tag("testId:consensusnode.queries.fees.FeeSchedule/transactionFeeSchedules/copies-in")
    void shouldCopyTransactionFeeSchedulesIn() {
        final Set<TransactionFeeSchedule> value = new LinkedHashSet<>(transactionFeeSchedulesValue());
        final Set<TransactionFeeSchedule> expected = Set.copyOf(value);
        final FeeSchedule subject = new FeeSchedule(value, expirationTimeValue());
        value.clear();
        assertValue(expected, subject.transactionFeeSchedules());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.transactionFeeSchedules()).clear());
    }

    @Test
    @DisplayName("rejects null for expirationTime")
    @Tag("testId:consensusnode.queries.fees.FeeSchedule/expirationTime/rejects-null")
    void shouldRejectNullExpirationTime() {
        assertThrows(NullPointerException.class, () -> new FeeSchedule(transactionFeeSchedulesValue(), (ZonedDateTime) null));
    }

    private static Set<TransactionFeeSchedule> transactionFeeSchedulesValue() {
        return Set.of(new TransactionFeeSchedule(BasicRequestType.NONE, Set.of(new FeeData(new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), new FeeComponents(1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L), BasicFeeSubType.DEFAULT))));
    }

    private static ZonedDateTime expirationTimeValue() {
        return ZonedDateTime.of(2024, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);
    }

    private static FeeSchedule create() {
        return new FeeSchedule(transactionFeeSchedulesValue(), expirationTimeValue());
    }
}

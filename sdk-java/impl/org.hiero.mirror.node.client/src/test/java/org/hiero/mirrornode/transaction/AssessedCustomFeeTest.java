package org.hiero.mirrornode.transaction;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.Address;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `AssessedCustomFee`, generated from the specs.
@DisplayName("AssessedCustomFee")
class AssessedCustomFeeTest {

    @Test
    @DisplayName("creates an AssessedCustomFee and returns the values")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/create")
    void shouldCreateAndReturnTheValues() {
        final long amount = amountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final List<AccountId> effectivePayerAccountIds = effectivePayerAccountIdsValue();
        final @Nullable Address tokenId = tokenIdValue();
        final AssessedCustomFee subject = new AssessedCustomFee(amount, collectorAccountId, effectivePayerAccountIds, tokenId);
        assertValue(amount, subject.amount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(effectivePayerAccountIds, subject.effectivePayerAccountIds());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("creates an AssessedCustomFee with the default values")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/create-with-defaults")
    void shouldCreateWithTheDefaultValues() {
        final AssessedCustomFee subject = new AssessedCustomFee(amountValue(), collectorAccountIdValue(), tokenIdValue());
        assertValue(List.of(), subject.effectivePayerAccountIds());
    }

    @Test
    @DisplayName("the builder builds the values of the constructor")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/builder/builds")
    void shouldBuildTheValuesOfTheConstructor() {
        final long amount = amountValue();
        final @Nullable AccountId collectorAccountId = collectorAccountIdValue();
        final List<AccountId> effectivePayerAccountIds = effectivePayerAccountIdsValue();
        final @Nullable Address tokenId = tokenIdValue();
        final AssessedCustomFee subject = AssessedCustomFee.builder()
                .amount(amount)
                .collectorAccountId(collectorAccountId)
                .effectivePayerAccountIds(effectivePayerAccountIds)
                .tokenId(tokenId)
                .build();
        assertValue(amount, subject.amount());
        assertValue(collectorAccountId, subject.collectorAccountId());
        assertValue(effectivePayerAccountIds, subject.effectivePayerAccountIds());
        assertValue(tokenId, subject.tokenId());
    }

    @Test
    @DisplayName("a value that is not set is null or its default value")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/builder/optional-values")
    void shouldBuildWithoutTheOptionalValues() {
        final AssessedCustomFee subject = AssessedCustomFee.builder()
                .amount(amountValue())
                .build();
        assertNull(subject.collectorAccountId());
        assertValue(List.of(), subject.effectivePayerAccountIds());
        assertNull(subject.tokenId());
    }

    @Test
    @DisplayName("the builder rejects a missing required value")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/builder/rejects-missing-value")
    void shouldRejectAMissingRequiredValue() {
        assertThrows(NullPointerException.class, () -> AssessedCustomFee.builder().build());
    }

    @Test
    @DisplayName("amount: accepts the minimum (-9223372036854775808)")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/amount/accepts-at-minimum")
    void shouldAcceptAmountAtMinimum() {
        final long value = -9223372036854775808L;
        final AssessedCustomFee subject = new AssessedCustomFee(value, collectorAccountIdValue(), effectivePayerAccountIdsValue(), tokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("amount: accepts the maximum (9223372036854775807)")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/amount/accepts-at-maximum")
    void shouldAcceptAmountAtMaximum() {
        final long value = 9223372036854775807L;
        final AssessedCustomFee subject = new AssessedCustomFee(value, collectorAccountIdValue(), effectivePayerAccountIdsValue(), tokenIdValue());
        assertValue(value, subject.amount());
    }

    @Test
    @DisplayName("rejects null for effectivePayerAccountIds")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/effectivePayerAccountIds/rejects-null")
    void shouldRejectNullEffectivePayerAccountIds() {
        assertThrows(NullPointerException.class, () -> new AssessedCustomFee(amountValue(), collectorAccountIdValue(), (List<AccountId>) null, tokenIdValue()));
    }

    @Test
    @DisplayName("the constructor copies effectivePayerAccountIds; the accessor returns an unmodifiable collection")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/effectivePayerAccountIds/copies-in")
    void shouldCopyEffectivePayerAccountIdsIn() {
        final List<AccountId> value = new ArrayList<>(effectivePayerAccountIdsValue());
        final List<AccountId> expected = List.copyOf(value);
        final AssessedCustomFee subject = new AssessedCustomFee(amountValue(), collectorAccountIdValue(), value, tokenIdValue());
        value.clear();
        assertValue(expected, subject.effectivePayerAccountIds());
        assertThrows(UnsupportedOperationException.class, () -> Objects.requireNonNull(subject.effectivePayerAccountIds()).clear());
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    @Tag("testId:mirrornode.transaction.AssessedCustomFee/value-semantics")
    void shouldCompareByValue() {
        final AssessedCustomFee subject = create();
        final AssessedCustomFee other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static long amountValue() {
        return 1L;
    }

    private static AccountId collectorAccountIdValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static List<AccountId> effectivePayerAccountIdsValue() {
        return List.of(new AccountId(0L, 0L, "", 1001L, null, null));
    }

    private static Address tokenIdValue() {
        return new Address(0L, 0L, "", 1001L);
    }

    private static AssessedCustomFee create() {
        return new AssessedCustomFee(amountValue(), collectorAccountIdValue(), effectivePayerAccountIdsValue(), tokenIdValue());
    }
}

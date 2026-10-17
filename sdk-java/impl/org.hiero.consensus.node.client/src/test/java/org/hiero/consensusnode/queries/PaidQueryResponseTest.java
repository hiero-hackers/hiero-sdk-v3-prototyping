package org.hiero.consensusnode.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.hedera.Hbar;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.nativeToken.NativeToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `PaidQueryResponse`, generated from the specs.
@DisplayName("PaidQueryResponse")
class PaidQueryResponseTest {

    @Test
    @DisplayName("creates a PaidQueryResponse and returns the values")
    @Tag("testId:consensusnode.queries.PaidQueryResponse/create")
    void shouldCreateAndReturnTheValues() {
        final String valueArgument = valueValue();
        final AccountId answeredBy = answeredByValue();
        final NativeToken<?, ?> cost = costValue();
        final PaidQueryResponse<String> subject = new PaidQueryResponse<>(valueArgument, answeredBy, cost);
        assertValue(valueArgument, subject.value());
        assertValue(answeredBy, subject.answeredBy());
        assertValue(cost, subject.cost());
    }

    @Test
    @DisplayName("rejects null for value")
    @Tag("testId:consensusnode.queries.PaidQueryResponse/value/rejects-null")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new PaidQueryResponse<>((String) null, answeredByValue(), costValue()));
    }

    @Test
    @DisplayName("rejects null for answeredBy")
    @Tag("testId:consensusnode.queries.PaidQueryResponse/answeredBy/rejects-null")
    void shouldRejectNullAnsweredBy() {
        assertThrows(NullPointerException.class, () -> new PaidQueryResponse<>(valueValue(), (AccountId) null, costValue()));
    }

    @Test
    @DisplayName("rejects null for cost")
    @Tag("testId:consensusnode.queries.PaidQueryResponse/cost/rejects-null")
    void shouldRejectNullCost() {
        assertThrows(NullPointerException.class, () -> new PaidQueryResponse<>(valueValue(), answeredByValue(), (NativeToken<?, ?>) null));
    }

    private static String valueValue() {
        return "value";
    }

    private static AccountId answeredByValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static NativeToken<?, ?> costValue() {
        return new Hbar(1L, HbarUnit.TINYBAR);
    }

    private static PaidQueryResponse<String> create() {
        return new PaidQueryResponse<>(valueValue(), answeredByValue(), costValue());
    }
}

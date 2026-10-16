package org.hiero.consensusnode.queries;

import static org.hiero.sdk.testing.Testing.assertValue;
import static org.junit.jupiter.api.Assertions.*;

import org.hiero.ledger.AccountId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of `QueryResponse`, generated from the specs.
@DisplayName("QueryResponse")
class QueryResponseTest {

    @Test
    @DisplayName("creates a QueryResponse and returns the values")
    void shouldCreateAndReturnTheValues() {
        final String valueArgument = valueValue();
        final AccountId answeredBy = answeredByValue();
        final QueryResponse<String> subject = new QueryResponse<>(valueArgument, answeredBy);
        assertValue(valueArgument, subject.value());
        assertValue(answeredBy, subject.answeredBy());
    }

    @Test
    @DisplayName("rejects null for value")
    void shouldRejectNullValue() {
        assertThrows(NullPointerException.class, () -> new QueryResponse<>((String) null, answeredByValue()));
    }

    @Test
    @DisplayName("rejects null for answeredBy")
    void shouldRejectNullAnsweredBy() {
        assertThrows(NullPointerException.class, () -> new QueryResponse<>(valueValue(), (AccountId) null));
    }

    @Test
    @DisplayName("equal values give equal objects and hash codes")
    void shouldCompareByValue() {
        final QueryResponse<String> subject = create();
        final QueryResponse<String> other = create();
        assertEquals(subject, other);
        assertEquals(subject.hashCode(), other.hashCode());
    }

    private static String valueValue() {
        return "value";
    }

    private static AccountId answeredByValue() {
        return new AccountId(0L, 0L, "", 1001L, null, null);
    }

    private static QueryResponse<String> create() {
        return new QueryResponse<>(valueValue(), answeredByValue());
    }
}

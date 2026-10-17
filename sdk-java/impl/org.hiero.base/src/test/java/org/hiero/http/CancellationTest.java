package org.hiero.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `Cancellation`, generated from the specs.
@DisplayName("Cancellation")
class CancellationTest {

    @Test
    @DisplayName("creates a Cancellation and returns the values")
    @Tag("testId:http.Cancellation/create")
    void shouldCreateAndReturnTheValues() {
        final Cancellation subject = new Cancellation();
    }

    @Test
    @DisplayName("isCancelled() can be called")
    @Tag("testId:http.Cancellation/isCancelled/call")
    void shouldCallIsCancelled() {
        create().isCancelled();
    }

    @Test
    @DisplayName("onCancel(callback) can be called and returns a value")
    @Tag("testId:http.Cancellation/onCancel/call")
    void shouldCallOnCancel() {
        assertNotNull(create().onCancel(callbackValue()));
    }

    @Test
    @DisplayName("none() can be called and returns a value")
    @Tag("testId:http.Cancellation/none/call")
    void shouldCallNone() {
        assertNotNull(Cancellation.none());
    }

    private static Cancellation create() {
        return new Cancellation();
    }

    private static Runnable callbackValue() {
        return () -> { };
    }
}

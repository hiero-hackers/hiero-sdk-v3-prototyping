package org.hiero.http;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `CancellationRegistration`, generated from the specs.
@DisplayName("CancellationRegistration")
class CancellationRegistrationTest {

    @Test
    @DisplayName("creates a CancellationRegistration and returns the values")
    @Tag("testId:http.CancellationRegistration/create")
    void shouldCreateAndReturnTheValues() {
        final CancellationRegistration subject = new CancellationRegistration();
    }

    @Test
    @DisplayName("release() can be called")
    @Tag("testId:http.CancellationRegistration/release/call")
    void shouldCallRelease() {
        create().release();
    }

    private static CancellationRegistration create() {
        return new CancellationRegistration();
    }
}

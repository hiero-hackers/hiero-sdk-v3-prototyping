package org.hiero.consensusnode.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `GiveUp`, generated from the specs.
@DisplayName("GiveUp")
class GiveUpTest {

    @Test
    @DisplayName("creates a GiveUp and returns the values")
    @Tag("testId:consensusnode.client.GiveUp/create")
    void shouldCreateAndReturnTheValues() {
        final GiveUp subject = new GiveUp();
    }

    private static GiveUp create() {
        return new GiveUp();
    }
}

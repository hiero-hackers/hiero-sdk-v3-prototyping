package org.hiero.keys;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of `KeyContainer`, generated from the specs.
@DisplayName("KeyContainer")
class KeyContainerTest {

    @Test
    @DisplayName("supportsType(type) can be called")
    @Tag("testId:keys.KeyContainer/supportsType/call")
    void shouldCallSupportsType() {
        KeyContainer.PKCS8.supportsType(typeValue());
    }

    private static KeyType typeValue() {
        return KeyType.PUBLIC;
    }
}

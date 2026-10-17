package org.hiero.solo;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.config.ConfigFactory;
import org.hiero.ledger.config.NetworkSetting;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/// Tests of the spec `solo`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("solo")
class SoloSpecTest {

    /// `solo/the-solo-identifier-looks-up-a-solo-network-setting`
    ///
    /// Source: this spec (SOLO_IDENTIFIER is "solo" and looks up the network setting of a Solo network); the Go SDK (the
    /// "localhost" name of ClientForName targets the local Solo network).
    @Test
    @DisplayName("the Solo identifier looks up a Solo network setting")
    @Tag("testId:solo/the-solo-identifier-looks-up-a-solo-network-setting")
    void theSoloIdentifierLooksUpASoloNetworkSetting() {
        // when
        final NetworkSetting setting = ConfigFactory.getNetworkSetting(SoloConstants.SOLO_IDENTIFIER);

        // then
        assertEquals("solo", SoloConstants.SOLO_IDENTIFIER);
        assertInstanceOf(SoloNetworkSetting.class, setting);
    }
}

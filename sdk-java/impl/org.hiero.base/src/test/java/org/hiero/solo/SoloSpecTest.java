package org.hiero.solo;

import static org.hiero.sdk.testing.Testing.*;

import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
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

    /// `solo/the-solo-setting-has-the-local-network-its-mirror-node-and-its-consensus-node`
    ///
    /// Source: this spec (`rule:solo-setting`).
    @Test
    @DisplayName("the Solo setting has the local network, its mirror node and its consensus node")
    @Tag("testId:solo/the-solo-setting-has-the-local-network-its-mirror-node-and-its-consensus-node")
    @Tag("rule:solo/solo-setting")
    void theSoloSettingHasTheLocalNetworkItsMirrorNodeAndItsConsensusNode() {
        // when
        final NetworkSetting setting = ConfigFactory.getNetworkSetting(SoloConstants.SOLO_IDENTIFIER);

        // then
        assertEquals(hex("03"), setting.network().id());
        assertEquals("local", setting.network().name());
        assertEquals(1L, length(setting.getMirrorNodes()));
        assertTrue(contains(setting.getMirrorNodes(), new MirrorNode("http://127.0.0.1:38081/api/v1")));
        assertEquals(1L, length(setting.getConsensusNodes()));
        assertTrue(contains(setting.getConsensusNodes(), new ConsensusNode(IpAddress.fromString("127.0.0.1"), 35211, AccountId.fromString("0.0.3"))));
    }
}

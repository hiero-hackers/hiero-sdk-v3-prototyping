package org.hiero.ledger.config;

import static org.hiero.sdk.testing.Testing.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import java.util.Set;
import org.hiero.hedera.HbarUnit;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/// Tests of the spec `ledger-config`, generated from the `tests` blocks of its `## Testing` section.
@DisplayName("ledger-config")
class LedgerConfigSpecTest {

    /// `ledger.config/a-registered-network-setting-is-returned-for-its-identifier`
    ///
    /// Source: this spec (getNetworkSetting returns the setting registered for the identifier); the Java and Go SDKs (a
    /// client is created for the network of a name: Client.forName, ClientForName).
    @Test
    @DisplayName("a registered network setting is returned for its identifier")
    void aRegisteredNetworkSettingIsReturnedForItsIdentifier() {
        // given
        final NetworkSetting setting = defaultNetworkSetting();
        ConfigFactory.registerNetworkSetting("ledger-config-test-registered", setting);

        // when
        final NetworkSetting found = ConfigFactory.getNetworkSetting("ledger-config-test-registered");

        // then
        assertEquals(setting, found);
    }

    /// `ledger.config/an-identifier-without-a-registered-network-setting-is-rejected`
    ///
    /// Source: this spec (getNetworkSetting throws if no setting exists for the identifier); the Java SDK (Client.forName
    /// rejects an unknown name).
    @Test
    @DisplayName("an identifier without a registered network setting is rejected")
    void anIdentifierWithoutARegisteredNetworkSettingIsRejected() {
        // when / then
        assertThrows(NoSuchElementException.class, () -> ConfigFactory.getNetworkSetting("ledger-config-test-unknown"));
    }

    private static NetworkSetting defaultNetworkSetting() {
        return new NetworkSetting(defaultNetwork(), Set.of(defaultConsensusNode()), Set.of(defaultMirrorNode()));
    }

    private static Network<?> defaultNetwork() {
        return new Network<>(new byte[] {(byte) 3}, "local", HbarUnit.HBAR);
    }

    private static ConsensusNode defaultConsensusNode() {
        return new ConsensusNode(defaultIpAddress(), 35211, new AccountId(0L, 0L, "", 3L, null, null));
    }

    private static IpAddress defaultIpAddress() {
        return new IpAddress(new byte[] {(byte) 127, (byte) 0, (byte) 0, (byte) 1});
    }

    private static MirrorNode defaultMirrorNode() {
        return new MirrorNode("http://127.0.0.1:38081");
    }
}

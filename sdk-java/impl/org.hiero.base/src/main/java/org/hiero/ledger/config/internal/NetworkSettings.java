package org.hiero.ledger.config.internal;

import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.hiero.hedera.HbarUnit;
import org.hiero.hedera.HederaConstants;
import org.hiero.hedera.HederaNetworkSetting;
import org.hiero.ledger.AccountId;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.solo.SoloConstants;
import org.hiero.solo.SoloNetworkSetting;

/// The process-wide registry of network settings behind `ConfigFactory`, preloaded with the settings of the Hedera
/// mainnet, the Hedera testnet and a local Solo network.
///
/// A second registration of an identifier replaces the first one (the spec leaves this open, see the
/// `## Questions & Comments` of `ledger-config.md`).
public final class NetworkSettings {

    /// The plaintext gRPC port of the consensus nodes of the Hedera networks.
    private static final int HEDERA_NODE_PORT = 50211;

    /// The node addresses of the Hedera mainnet as "ip account-number" pairs, taken from the Hiero JavaScript SDK
    /// (`ClientConstants.ALL_NETWORK_IPS`).
    private static final String MAINNET_NODES = "34.239.82.6 3;35.237.200.180 3;3.130.52.236 4;35.186.191.247 4;"
            + "3.18.18.254 5;35.192.2.25 5;74.50.117.35 5;23.111.186.250 5;107.155.64.98 5;3.114.54.4 7;"
            + "35.203.82.240 7;35.236.5.219 8;35.183.66.150 8;35.181.158.250 9;35.197.192.225 9;177.154.62.234 10;"
            + "3.248.27.48 10;35.242.233.154 10;35.204.86.32 12;35.177.162.180 12;34.215.192.104 13;35.234.132.107 13;"
            + "52.8.21.141 14;35.236.2.27 14;35.228.11.53 15;3.121.238.26 15;141.94.175.187 18;34.82.78.255 20;"
            + "52.39.162.216 20;34.76.140.109 21;13.36.123.209 21;52.78.202.34 22;34.64.141.166 22;3.18.91.176 23;"
            + "35.232.244.145 23;69.167.169.208 23;34.89.103.38 24;18.135.7.211 24;34.93.112.7 25;13.232.240.207 25;"
            + "35.198.220.75 28;18.139.47.5 28;54.74.60.120 29;34.142.71.129 29;80.85.70.197 29;217.76.57.165 31;"
            + "3.77.94.254 31;34.107.78.179 31;18.136.65.22 33;34.142.172.228 33;34.16.139.248 34;35.155.212.90 34";

    /// The node addresses of the Hedera testnet, from the same source.
    private static final String TESTNET_NODES = "34.94.106.61 3;50.18.132.211 3;3.212.6.13 4;35.237.119.55 4;"
            + "35.245.27.193 5;52.20.18.86 5;34.83.112.116 6;54.70.192.33 6;34.94.160.4 7;54.176.199.109 7;"
            + "35.155.49.147 8;34.106.102.218 8;34.133.197.230 9;52.14.252.207 9";

    private static final Map<String, NetworkSetting> REGISTRY = new ConcurrentHashMap<>();

    static {
        REGISTRY.put(HederaConstants.HEDERA_MAINNET_IDENTIFIER, new HederaNetworkSetting(
                new Network<>(new byte[] {0}, "mainnet", HbarUnit.HBAR),
                hederaNodes(MAINNET_NODES),
                Set.of(new MirrorNode("https://mainnet-public.mirrornode.hedera.com/api/v1"))));
        REGISTRY.put(HederaConstants.HEDERA_TESTNET_IDENTIFIER, new HederaNetworkSetting(
                new Network<>(new byte[] {1}, "testnet", HbarUnit.HBAR),
                hederaNodes(TESTNET_NODES),
                Set.of(new MirrorNode("https://testnet.mirrornode.hedera.com/api/v1"))));
        // the network of `solo one-shot single deploy`: ledger id 3, one consensus node, the mirror node REST API
        REGISTRY.put(SoloConstants.SOLO_IDENTIFIER, new SoloNetworkSetting(
                new Network<>(new byte[] {3}, "local", HbarUnit.HBAR),
                Set.of(new ConsensusNode(IpAddress.fromString("127.0.0.1"), 35211, account(3))),
                Set.of(new MirrorNode("http://127.0.0.1:38081/api/v1"))));
    }

    private NetworkSettings() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /// Registers `setting` under `identifier`, replacing a setting registered before.
    ///
    /// @param identifier the identifier
    /// @param setting the setting
    /// @throws NullPointerException if a value is `null`
    public static void register(final String identifier, final NetworkSetting setting) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        Objects.requireNonNull(setting, "setting must not be null");
        REGISTRY.put(identifier, setting);
    }

    /// Returns the setting registered under `identifier`.
    ///
    /// @param identifier the identifier
    /// @return the setting
    /// @throws NullPointerException if `identifier` is `null`
    /// @throws NoSuchElementException if no setting is registered under `identifier`
    public static NetworkSetting get(final String identifier) {
        Objects.requireNonNull(identifier, "identifier must not be null");
        final NetworkSetting setting = REGISTRY.get(identifier);
        if (setting == null) {
            throw new NoSuchElementException("no network setting is registered for '" + identifier + "'");
        }
        return setting;
    }

    private static Set<ConsensusNode> hederaNodes(final String nodes) {
        final Set<ConsensusNode> result = new HashSet<>();
        for (final String node : nodes.split(";")) {
            final String[] parts = node.split(" ");
            result.add(new ConsensusNode(IpAddress.fromString(parts[0]), HEDERA_NODE_PORT,
                    account(Long.parseLong(parts[1]))));
        }
        return result;
    }

    private static AccountId account(final long num) {
        return new AccountId(0L, 0L, "", num, null, null);
    }
}

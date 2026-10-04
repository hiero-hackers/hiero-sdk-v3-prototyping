package org.hiero.tck.runtime;

import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.hedera.HbarUnit;
import org.hiero.hedera.HederaConstants;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.ConfigFactory;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.tck.contract.Session;
import org.hiero.tck.contract.Source;

/**
 * The runtime methods of the TCK that are not bound to a type of the API: {@code setup}, {@code reset} and
 * {@code generateKey}.
 */
final class Utilities {

    /** The ledger ID of a local network (Hiero local node / Solo). */
    private static final byte[] LOCAL_LEDGER_ID = {3};

    private final JavaTckRuntime runtime;
    private final JavaConverters converters;

    /**
     * Creates the runtime methods.
     *
     * @param runtime    the runtime (JSON access)
     * @param converters the converters
     */
    Utilities(final JavaTckRuntime runtime, final JavaConverters converters) {
        this.runtime = runtime;
        this.converters = converters;
    }

    /**
     * Creates the client of a session: the operator plus either the custom network of the parameters
     * ({@code nodeIp}, {@code nodeAccountId}, {@code mirrorNetworkIp}) or the Hedera testnet.
     */
    Object setup(final Map<String, Object> params, final Session session) {
        final Account operator = new Account(
                runtime.required(runtime.value(params, List.of(new Source<>("operatorAccountId",
                        converters::accountId))), "operatorAccountId"),
                runtime.required(runtime.value(params, List.of(new Source<>("operatorPrivateKey",
                        converters::privateKey))), "operatorPrivateKey"));
        final NetworkSetting setting;
        if (params.get("nodeIp") != null) {
            final String[] node = converters.string(params.get("nodeIp")).split(":");
            final ConsensusNode consensusNode = new ConsensusNode(IpAddress.fromString(node[0]),
                    node.length > 1 ? Integer.parseInt(node[1]) : 50211,
                    runtime.required(runtime.value(params, List.of(new Source<>("nodeAccountId",
                            converters::accountId))), "nodeAccountId"));
            final Set<MirrorNode> mirrorNodes = params.get("mirrorNetworkIp") == null ? Set.of()
                    : Set.of(new MirrorNode("http://" + converters.string(params.get("mirrorNetworkIp"))));
            setting = new NetworkSetting(new Network<>(LOCAL_LEDGER_ID, "local", HbarUnit.TINYBAR),
                    Set.of(consensusNode), mirrorNodes);
        } else {
            setting = ConfigFactory.getNetworkSetting(HederaConstants.HEDERA_TESTNET_IDENTIFIER);
        }
        RuntimeSession.of(session).client(ClientFactory.createClient(setting, operator));
        return Map.of("message", "Successfully setup client", "status", "SUCCESS");
    }

    /** Drops the client of a session. */
    Object reset(final Map<String, Object> params, final Session session) {
        RuntimeSession.of(session).client(null);
        return Map.of("status", "SUCCESS");
    }

    /**
     * Generates a key in the TCK representation (DER hex). Key lists and threshold keys need a byte encoding of
     * {@code Authority}, EVM addresses a derivation from the public key; the API has neither yet.
     */
    Object generateKey(final Map<String, Object> params, final Session session) {
        final String type = converters.string(runtime.required(params.get("type"), "type"));
        final Map<String, Object> result = new LinkedHashMap<>();
        switch (type) {
            case "ed25519PrivateKey", "ecdsaSecp256k1PrivateKey" -> {
                final PrivateKey key = KeysFactory.generatePrivateKey(algorithm(type));
                result.put("key", hex(key.toBytes(KeyFormat.PKCS8_WITH_DER)));
                result.put("privateKeys", List.of(result.get("key")));
            }
            case "ed25519PublicKey", "ecdsaSecp256k1PublicKey" -> {
                final PrivateKey key = KeysFactory.generatePrivateKey(algorithm(type));
                result.put("key", hex(key.createPublicKey().toBytes(KeyFormat.SPKI_WITH_DER)));
                result.put("privateKeys", List.of(hex(key.toBytes(KeyFormat.PKCS8_WITH_DER))));
            }
            case "evmAddress" -> throw RpcError.gap("generateKey of type 'evmAddress': PublicKey has no EVM address");
            default -> throw RpcError.gap("generateKey of type '" + type + "': Authority has no toBytes");
        }
        return result;
    }

    private static KeyAlgorithm algorithm(final String type) {
        return type.startsWith("ed25519") ? KeyAlgorithm.ED25519 : KeyAlgorithm.ECDSA;
    }

    private static String hex(final byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}

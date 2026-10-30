package org.hiero.tck.runtime;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.crypto.digests.KeccakDigest;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.math.ec.ECPoint;
import org.hiero.authority.Authority;
import org.hiero.authority.AuthorityFactory;
import org.hiero.consensusnode.client.Account;
import org.hiero.consensusnode.client.ClientFactory;
import org.hiero.hedera.HbarUnit;
import org.hiero.keys.KeyAlgorithm;
import org.hiero.keys.KeyFormat;
import org.hiero.keys.KeysFactory;
import org.hiero.keys.PrivateKey;
import org.hiero.keys.PublicKey;
import org.hiero.ledger.ConsensusNode;
import org.hiero.ledger.IpAddress;
import org.hiero.ledger.MirrorNode;
import org.hiero.mirrornode.MirrornodeFactory;
import org.hiero.ledger.Network;
import org.hiero.ledger.config.NetworkSetting;
import org.hiero.tck.contract.Session;
import org.hiero.tck.contract.Source;
import org.jspecify.annotations.Nullable;

/**
 * The runtime methods of the TCK that are not bound to a type of the API: {@code setup}, {@code reset} and
 * {@code generateKey}.
 */
final class Utilities {

    /** The consensus node of a local Solo network (Solo 0.63+, {@code solo one-shot single deploy}). */
    static final String SOLO_NODE_IP = "127.0.0.1:35211";
    /** The account of the consensus node of a local Solo network. */
    static final String SOLO_NODE_ACCOUNT_ID = "0.0.3";
    /** The mirror node REST API of a local Solo network. */
    static final String SOLO_MIRROR_NODE_REST_URL = "http://127.0.0.1:38081";

    /** The ledger ID of a local network (Solo). */
    private static final byte[] LOCAL_LEDGER_ID = {3};

    private static final SecureRandom RANDOM = new SecureRandom();

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
     * Creates the client of a session: the operator plus the network of the parameters ({@code nodeIp},
     * {@code nodeAccountId}) and the mirror node REST API of the environment variable {@code MIRROR_NODE_REST_URL}
     * (the TCK's variable); every value that is not given is the one of a local Solo network.
     */
    Object setup(final Map<String, Object> params, final Session session) {
        final Account operator = new Account(
                runtime.required(runtime.value(params, List.of(new Source<>("operatorAccountId",
                        converters::accountId))), "operatorAccountId"),
                runtime.required(runtime.value(params, List.of(new Source<>("operatorPrivateKey",
                        converters::privateKey))), "operatorPrivateKey"));
        final String[] node = converters.string(orSolo(params.get("nodeIp"), SOLO_NODE_IP)).split(":");
        final ConsensusNode consensusNode = new ConsensusNode(IpAddress.fromString(node[0]),
                node.length > 1 ? Integer.parseInt(node[1]) : 50211,
                converters.accountId(orSolo(params.get("nodeAccountId"), SOLO_NODE_ACCOUNT_ID)));
        // mirrorNetworkIp is the gRPC endpoint of the mirror node, the API uses its REST API: MIRROR_NODE_REST_URL
        final MirrorNode mirrorNode = new MirrorNode(restBaseUrl((String) orSolo(System.getenv("MIRROR_NODE_REST_URL"),
                SOLO_MIRROR_NODE_REST_URL)));
        final NetworkSetting setting = new NetworkSetting(new Network<>(LOCAL_LEDGER_ID, "local", HbarUnit.TINYBAR),
                Set.of(consensusNode), Set.of(mirrorNode));
        RuntimeSession.of(session).client(ClientFactory.createClient(setting, operator));
        RuntimeSession.of(session).mirrorNode(MirrornodeFactory.createMirrorNodeClient(mirrorNode));
        return Map.of("message", "Successfully setup client", "status", "SUCCESS");
    }

    /**
     * The REST base URL of the API ({@code .../api/v1}) for the URL of the TCK, which is the host of the mirror node
     * without the path of the API.
     */
    static String restBaseUrl(final String url) {
        final String base = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return base.endsWith("/api/v1") ? base : base + "/api/v1";
    }

    /** A parameter, or the value of a local Solo network if it is not sent. */
    private static Object orSolo(final Object value, final String solo) {
        return value == null ? solo : value;
    }

    /** Drops the client of a session. */
    Object reset(final Map<String, Object> params, final Session session) {
        RuntimeSession.of(session).client(null);
        RuntimeSession.of(session).mirrorNode(null);
        return Map.of("status", "SUCCESS");
    }

    /**
     * Generates a key in the TCK representation: a DER-encoded private or public key, the hex of the protobuf
     * {@code Key} of a key list or threshold key (see {@link KeyProto}), or an EVM address. {@code privateKeys} lists the
     * private keys of every generated key, in the order of the request, also of nested lists.
     */
    Object generateKey(final Map<String, Object> params, final Session session) {
        final List<String> privateKeys = new ArrayList<>();
        final Generated generated = generate(params, privateKeys);
        final Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", generated.json());
        if (!privateKeys.isEmpty()) {
            result.put("privateKeys", privateKeys);
        }
        return result;
    }

    /** A generated key: its JSON form and, unless it is an EVM address, its authority (for lists). */
    private record Generated(String json, @Nullable Authority authority) {
    }

    private Generated generate(final Map<String, Object> params, final List<String> privateKeys) {
        final String type = converters.string(runtime.required(params.get("type"), "type"));
        final Object fromKey = params.get("fromKey");
        if (fromKey != null && !type.endsWith("PublicKey") && !type.equals("evmAddress")) {
            throw RpcError.internal("fromKey is only allowed for public keys and EVM addresses");
        }
        if ((params.get("threshold") != null && !type.equals("thresholdKey"))
                || (params.get("keys") != null && !type.equals("keyList") && !type.equals("thresholdKey"))) {
            throw RpcError.internal("threshold and keys are only allowed for key lists and threshold keys");
        }
        return switch (type) {
            case "ed25519PrivateKey", "ecdsaSecp256k1PrivateKey" -> {
                final PrivateKey key = KeysFactory.generatePrivateKey(algorithm(type));
                final String der = hex(key.toBytes(KeyFormat.PKCS8_WITH_DER));
                privateKeys.add(der);
                yield new Generated(der, AuthorityFactory.of(key.createPublicKey()));
            }
            case "ed25519PublicKey", "ecdsaSecp256k1PublicKey" -> {
                final PrivateKey key;
                if (fromKey != null) {
                    key = converters.privateKey(fromKey);
                    if (key.algorithm() != algorithm(type)) {
                        throw RpcError.internal("fromKey is no " + algorithm(type) + " private key");
                    }
                } else {
                    key = KeysFactory.generatePrivateKey(algorithm(type));
                    privateKeys.add(hex(key.toBytes(KeyFormat.PKCS8_WITH_DER)));
                }
                final PublicKey publicKey = key.createPublicKey();
                yield new Generated(hex(publicKey.toBytes(KeyFormat.SPKI_WITH_DER)), AuthorityFactory.of(publicKey));
            }
            case "keyList", "thresholdKey" -> {
                final List<Map<String, Object>> keys = runtime.objects(params, "keys");
                if (keys.isEmpty()) {
                    throw RpcError.internal(type + " needs keys");
                }
                final List<Authority> children = new ArrayList<>();
                for (final Map<String, Object> child : keys) {
                    final Authority authority = generate(child, privateKeys).authority();
                    if (authority == null) {
                        throw RpcError.internal("an EVM address cannot be part of a " + type);
                    }
                    children.add(authority);
                }
                final int threshold = type.equals("keyList") ? children.size()
                        : converters.int32(runtime.required(params.get("threshold"), "threshold"));
                final Authority list = AuthorityFactory.of(threshold, children.toArray(Authority[]::new));
                yield new Generated(hex(KeyProto.encode(list)), list);
            }
            case "evmAddress" -> new Generated(HexFormat.of().formatHex(fromKey == null ? randomBytes(20)
                    : evmAddress(ecdsaPublicKey(fromKey))), null);
            default -> throw RpcError.internal("unknown key type '" + type + "'");
        };
    }

    /** The ECDSA public key of a DER-encoded ECDSA private or public key. */
    private PublicKey ecdsaPublicKey(final Object json) {
        final byte[] bytes = converters.hex(json);
        PublicKey key;
        try {
            key = KeysFactory.createPrivateKey(KeyFormat.PKCS8_WITH_DER, bytes).createPublicKey();
        } catch (final IllegalArgumentException notPrivate) {
            key = KeysFactory.createPublicKey(KeyFormat.SPKI_WITH_DER, bytes);
        }
        if (key.algorithm() != KeyAlgorithm.ECDSA) {
            throw RpcError.internal("an EVM address needs an ECDSA secp256k1 key");
        }
        return key;
    }

    /**
     * The EVM address of an ECDSA secp256k1 public key: the last 20 bytes of the Keccak-256 hash of the uncompressed
     * point without its prefix. The API has no such derivation yet (see the {@code ## Questions & Comments} of
     * {@code keys.md}).
     */
    private static byte[] evmAddress(final PublicKey key) {
        final ECPoint point = CustomNamedCurves.getByName("secp256k1").getCurve().decodePoint(key.toRawBytes());
        final byte[] uncompressed = point.getEncoded(false);
        final KeccakDigest keccak = new KeccakDigest(256);
        keccak.update(uncompressed, 1, uncompressed.length - 1);
        final byte[] hash = new byte[32];
        keccak.doFinal(hash, 0);
        return Arrays.copyOfRange(hash, 12, 32);
    }

    private static byte[] randomBytes(final int length) {
        final byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    private static KeyAlgorithm algorithm(final String type) {
        return type.startsWith("ed25519") ? KeyAlgorithm.ED25519 : KeyAlgorithm.ECDSA;
    }

    private static String hex(final byte[] bytes) {
        return HexFormat.of().formatHex(bytes);
    }
}

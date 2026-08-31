package org.hiero.sdk.v3.fixture;

import org.hiero.sdk.v3.keys.ByteImportEncoding;
import org.hiero.sdk.v3.keys.KeyAlgorithm;
import org.hiero.sdk.v3.keys.KeyFactory;
import org.hiero.sdk.v3.keys.KeyFormat;
import org.hiero.sdk.v3.keys.KeyFormatOperations;
import org.hiero.sdk.v3.keys.PrivateKey;
import org.hiero.sdk.v3.keys.PublicKey;

public final class KeyContractsConsumer {
    PrivateKey privateKey(KeyFactory factory) { return factory.generatePrivateKey(KeyAlgorithm.ED25519); }
    PublicKey publicKey(KeyFactory factory) { return factory.createPublicKey(KeyFormat.SPKI_WITH_PEM, "value"); }
    byte[] decode(KeyFormatOperations operations, String value) {
        return operations.decode(ByteImportEncoding.HEX, value);
    }
}

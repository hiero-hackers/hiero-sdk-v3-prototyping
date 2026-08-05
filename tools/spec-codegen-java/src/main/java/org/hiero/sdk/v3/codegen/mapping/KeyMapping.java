package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.manifest.DeferredEnforcement;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class KeyMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("keys", "KeyType", "/** Key material classification. */\npublic enum KeyType { PUBLIC, PRIVATE }\n"),
                MappingSources.type("keys", "KeyAlgorithm", "/** Supported key algorithms. */\npublic enum KeyAlgorithm { ED25519, ECDSA }\n"),
                MappingSources.type("keys", "Key", """
                        /** Provider-owned key contract. Implementations must copy raw bytes and redact diagnostics. */
                        public interface Key {
                            /** Returns a defensive copy of raw key bytes. */ byte[] bytes();
                            /** Returns the key algorithm. */ KeyAlgorithm algorithm();
                            /** Returns the public/private classification. */ KeyType type();
                            /** Exports raw bytes. */ byte[] toRawBytes();
                            /** Exports bytes using a container format. */ byte[] toBytes(KeyFormat container);
                            /** Exports text using a container format. */ String toString(KeyFormat container);
                        }
                        """),
                MappingSources.type("keys", "KeyPair", """
                        /** Immutable public/private key pair. */
                        public record KeyPair(PublicKey publicKey, PrivateKey privateKey) {
                            /** Validates both pair components. */
                            public KeyPair { Objects.requireNonNull(publicKey, "publicKey"); Objects.requireNonNull(privateKey, "privateKey"); }
                        }
                        """, "java.util.Objects"),
                MappingSources.type("keys", "PublicKey", """
                        /** Public-key verification contract. */
                        public interface PublicKey extends Key {
                            /** Verifies a signature over a message. */ boolean verify(byte[] message, byte[] signature);
                        }
                        """),
                MappingSources.type("keys", "PrivateKey", """
                        /** Private-key signing contract. Implementations must not expose key material in diagnostics. */
                        public interface PrivateKey extends Key {
                            /** Signs a message. */ byte[] sign(byte[] message);
                            /** Derives a new public-key instance. */ PublicKey createPublicKey();
                        }
                        """),
                MappingSources.type("keys", "RawFormat", "/** Raw import/export representation. */\npublic enum RawFormat { STRING, BYTES }\n"),
                MappingSources.type("keys", "KeyEncoding", """
                        /** Key encodings and their raw representations. */
                        public enum KeyEncoding {
                            DER(RawFormat.BYTES), PEM(RawFormat.STRING);
                            private final RawFormat rawFormat;
                            KeyEncoding(RawFormat rawFormat) { this.rawFormat = rawFormat; }
                            /** Returns the raw representation. */ public RawFormat rawFormat() { return rawFormat; }
                        }
                        """),
                MappingSources.type("keys", "KeyContainer", "/** Key container formats. */\npublic enum KeyContainer { PKCS8, SPKI }\n"),
                MappingSources.type("keys", "ByteImportEncoding", "/** Text-to-byte import encodings. */\npublic enum ByteImportEncoding { HEX, BASE64 }\n"),
                MappingSources.type("keys", "KeyFormat", """
                        /** Combined key container and encoding. */
                        public enum KeyFormat {
                            PKCS8_WITH_DER(KeyContainer.PKCS8, KeyEncoding.DER),
                            SPKI_WITH_DER(KeyContainer.SPKI, KeyEncoding.DER),
                            PKCS8_WITH_PEM(KeyContainer.PKCS8, KeyEncoding.PEM),
                            SPKI_WITH_PEM(KeyContainer.SPKI, KeyEncoding.PEM);
                            private final KeyContainer container;
                            private final KeyEncoding encoding;
                            KeyFormat(KeyContainer container, KeyEncoding encoding) { this.container = container; this.encoding = encoding; }
                            /** Returns the container. */ public KeyContainer container() { return container; }
                            /** Returns the encoding. */ public KeyEncoding encoding() { return encoding; }
                        }
                        """),
                MappingSources.type("keys", "KeyFactory", """
                        /** Body-free key generation and import contract. */
                        public interface KeyFactory {
                            PrivateKey generatePrivateKey(KeyAlgorithm algorithm);
                            PublicKey generatePublicKey(KeyAlgorithm algorithm);
                            PrivateKey createPrivateKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
                            PublicKey createPublicKey(KeyAlgorithm algorithm, ByteImportEncoding encoding, String value);
                            PrivateKey createPrivateKey(KeyAlgorithm algorithm, byte[] rawBytes);
                            PublicKey createPublicKey(KeyAlgorithm algorithm, byte[] rawBytes);
                            PrivateKey createPrivateKey(KeyFormat container, String value);
                            PublicKey createPublicKey(KeyFormat container, String value);
                            PrivateKey createPrivateKey(KeyFormat container, byte[] value);
                            PublicKey createPublicKey(KeyFormat container, byte[] value);
                            PrivateKey createPrivateKey(String value);
                            PublicKey createPublicKey(String value);
                        }
                        """),
                MappingSources.type("keys", "KeyFormatOperations", """
                        /** Body-free decoding and format-support contract. */
                        public interface KeyFormatOperations {
                            byte[] decode(KeyEncoding encoding, KeyType keyType, String value);
                            boolean supportsType(KeyContainer container, KeyType type);
                            byte[] decode(ByteImportEncoding encoding, String value);
                            boolean supportsType(KeyFormat format, KeyType type);
                            byte[] decode(KeyFormat format, KeyType keyType, String value);
                        }
                        """));
    }

    public List<DeferredEnforcement> deferredEnforcement() {
        return List.of(
                new DeferredEnforcement("keys#Key.bytes", "DE-003", "interface byte ownership", "provider TCK"),
                new DeferredEnforcement("keys#Key", "DE-003", "provider diagnostic redaction", "security TCK"),
                new DeferredEnforcement("keys#namespace.generatePrivateKey(KeyAlgorithm):PrivateKey", "DE-005", "operational cryptography excluded", "provider implementation tests"));
    }
}

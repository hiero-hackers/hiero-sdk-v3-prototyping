// Source: spec/base/keys.md (keys#KeyFormatOperations).

package org.hiero.sdk.v3.keys;

/** Body-free decoding and format-support contract. */
public interface KeyFormatOperations {
    /** Decodes key text using an encoding and key type. */
    byte[] decode(KeyEncoding encoding, KeyType keyType, String value);
    /** Reports whether a container supports a key type. */
    boolean supportsType(KeyContainer container, KeyType type);
    /** Decodes raw text with the requested byte import encoding. */
    byte[] decode(ByteImportEncoding encoding, String value);
    /** Reports whether a combined format supports a key type. */
    boolean supportsType(KeyFormat format, KeyType type);
    /** Decodes key text using a combined format and key type. */
    byte[] decode(KeyFormat format, KeyType keyType, String value);
}

// Generated from spec/base/keys.md (keys#KeyFormatOperations); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Body-free decoding and format-support contract. */
public interface KeyFormatOperations {
    byte[] decode(KeyEncoding encoding, KeyType keyType, String value);
    boolean supportsType(KeyContainer container, KeyType type);
    byte[] decode(ByteImportEncoding encoding, String value);
    boolean supportsType(KeyFormat format, KeyType type);
    byte[] decode(KeyFormat format, KeyType keyType, String value);
}

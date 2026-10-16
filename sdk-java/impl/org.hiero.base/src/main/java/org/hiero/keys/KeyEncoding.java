package org.hiero.keys;

import java.util.Objects;
import org.hiero.keys.internal.Pem;

/// all supported encodings that can be used to import/export a container format
/// rawFormat: the raw format of the import / export
public enum KeyEncoding {

    /// Distinguished Encoding Rules (binary)
    DER(RawFormat.BYTES),
    /// Privacy Enhanced Mail (Base64 text)
    PEM(RawFormat.STRING);

    private final RawFormat rawFormat;

    KeyEncoding(final RawFormat rawFormat) {
        this.rawFormat = Objects.requireNonNull(rawFormat, "rawFormat must not be null");
    }

    /// Returns the `rawFormat` of this value.
    public RawFormat rawFormat() {
        return rawFormat;
    }

    /// decodes the given text into the DER bytes of a key of the given type; fails for a binary encoding and for text
    /// that is not in this encoding
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public byte[] decode(final KeyType keyType, final String value) {
        return switch (this) {
            case PEM -> Pem.fromPem(keyType, value);
            case DER -> throw new IllegalArgumentException("DER is a binary encoding, it cannot be read from text");
        };
    }
}

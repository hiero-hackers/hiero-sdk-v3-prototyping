package org.hiero.keys;

import java.util.Base64;
import java.util.HexFormat;


/// encoding information for import / export
public enum ByteImportEncoding {

    /// hex string representation of the bytes
    HEX,
    /// base64 string representation of the bytes
    BASE64;

    /// fails for text that is not in this encoding
    ///
    /// @throws IllegalArgumentException if an illegal format error occurs
    public byte[] decode(final String value) {
        final String text = value.strip();
        try {
            return switch (this) {
                case HEX -> HexFormat.of().parseHex(text.startsWith("0x") || text.startsWith("0X") ? text.substring(2) : text);
                case BASE64 -> Base64.getDecoder().decode(text);
            };
        } catch (final IllegalArgumentException invalid) {
            throw new IllegalArgumentException("The value is not " + this + "-encoded", invalid);
        }
    }
}

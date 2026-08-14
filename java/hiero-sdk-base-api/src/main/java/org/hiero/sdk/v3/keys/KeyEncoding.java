// Source: spec/base/keys.md (keys#KeyEncoding).

package org.hiero.sdk.v3.keys;

/** Key encodings and their raw representations. */
public enum KeyEncoding {
    /** Distinguished Encoding Rules bytes. */
    DER(RawFormat.BYTES),
    /** Privacy Enhanced Mail text. */
    PEM(RawFormat.STRING);
    private final RawFormat rawFormat;
    KeyEncoding(RawFormat rawFormat) { this.rawFormat = rawFormat; }
    /** Returns the raw representation. */ public RawFormat rawFormat() { return rawFormat; }
}

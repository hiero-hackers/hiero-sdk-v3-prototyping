// Generated from spec/base/keys.md (keys#KeyEncoding); DO NOT EDIT.

package org.hiero.sdk.v3.keys;

/** Key encodings and their raw representations. */
public enum KeyEncoding {
    DER(RawFormat.BYTES), PEM(RawFormat.STRING);
    private final RawFormat rawFormat;
    KeyEncoding(RawFormat rawFormat) { this.rawFormat = rawFormat; }
    /** Returns the raw representation. */ public RawFormat rawFormat() { return rawFormat; }
}

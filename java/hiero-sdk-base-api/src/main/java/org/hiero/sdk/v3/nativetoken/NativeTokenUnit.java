// Source: spec/base/native-token.md (nativeToken#NativeTokenUnit).

package org.hiero.sdk.v3.nativetoken;

/** A display unit for a network native token. */
public interface NativeTokenUnit {
    /** Returns the display symbol. */
    String symbol();
    /** Returns the number of smallest units represented by one unit. */
    long baseUnitFactor();
}

// Source: spec/base/hedera.md (hedera#HbarUnit).

package org.hiero.sdk.v3.hedera;

import org.hiero.sdk.v3.nativetoken.NativeTokenUnit;

/** HBAR units with authoritative symbols and tinybar factors. */
public enum HbarUnit implements NativeTokenUnit {
    /** Tinybar. */ TINYBAR("tℏ", 1L),
    /** Microbar. */ MICROBAR("μℏ", 100L),
    /** Millibar. */ MILLIBAR("mℏ", 100_000L),
    /** HBAR. */ HBAR("ℏ", 100_000_000L),
    /** Kilobar. */ KILOBAR("kℏ", 100_000_000_000L),
    /** Megabar. */ MEGABAR("Mℏ", 100_000_000_000_000L),
    /** Gigabar. */ GIGABAR("Gℏ", 100_000_000_000_000_000L);
    private final String symbol;
    private final long baseUnitFactor;
    HbarUnit(String symbol, long baseUnitFactor) {
        this.symbol = symbol;
        this.baseUnitFactor = baseUnitFactor;
    }
    @Override public String symbol() { return symbol; }
    @Override public long baseUnitFactor() { return baseUnitFactor; }
}

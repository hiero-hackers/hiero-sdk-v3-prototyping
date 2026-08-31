package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class NetworkProfileMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("hedera", "HederaNetworkSetting", """
                        /** Marker for a Hedera network setting. */
                        public interface HederaNetworkSetting extends NetworkSetting {}
                        """, "org.hiero.sdk.v3.ledger.config.NetworkSetting"),
                MappingSources.type("hedera", "HbarUnit", """
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
                        """, "org.hiero.sdk.v3.nativetoken.NativeTokenUnit"),
                MappingSources.type("hedera", "Hbar", """
                        /** An HBAR amount. */
                        public interface Hbar extends NativeToken<Hbar, HbarUnit> {
                            /** Returns this amount in tinybars. */
                            long toTinybars();
                        }
                        """, "org.hiero.sdk.v3.nativetoken.NativeToken"),
                MappingSources.type("hedera", "HederaConstants", """
                        /** Hedera network identifiers. */
                        public final class HederaConstants {
                            /** Hedera mainnet identifier. */
                            public static final String HEDERA_MAINNET_IDENTIFIER = "hedera-mainnet";
                            /** Hedera testnet identifier. */
                            public static final String HEDERA_TESTNET_IDENTIFIER = "hedera-testnet";
                            private HederaConstants() {}
                        }
                        """),
                MappingSources.type("solo", "SoloNetworkSetting", """
                        /** Marker for a Solo network setting. */
                        public interface SoloNetworkSetting extends NetworkSetting {}
                        """, "org.hiero.sdk.v3.ledger.config.NetworkSetting"),
                MappingSources.type("solo", "SoloConstants", """
                        /** Solo network identifiers. */
                        public final class SoloConstants {
                            /** Solo one-shot network identifier. */
                            public static final String SOLO_IDENTIFIER = "solo";
                            private SoloConstants() {}
                        }
                        """),
                MappingSources.type("token", "TokenType", """
                        /** Token classification. */
                        public enum TokenType { FUNGIBLE_COMMON, NON_FUNGIBLE_UNIQUE }
                        """),
                MappingSources.type("token", "TokenSupplyType", """
                        /** Token supply policy. */
                        public enum TokenSupplyType { INFINITE, FINITE }
                        """));
    }
}

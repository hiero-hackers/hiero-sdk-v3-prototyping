package org.hiero.sdk.v3.codegen.mapping;

import java.util.List;
import org.hiero.sdk.v3.codegen.render.JavaSourceFile;

public final class NativeTokenMapping {
    public List<JavaSourceFile> sourceFiles() {
        return List.of(
                MappingSources.type("nativeToken", "NativeTokenUnit", """
                        /** A display unit for a network native token. */
                        public interface NativeTokenUnit {
                            /** Returns the display symbol. */
                            String symbol();
                            /** Returns the number of smallest units represented by one unit. */
                            long baseUnitFactor();
                        }
                        """),
                MappingSources.type("nativeToken", "NativeToken", """
                        /** A native-token amount preserving its concrete token and unit types. */
                        public interface NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
                            /** Returns the amount expressed in {@link #unit()}. */
                            long amount();
                            /** Returns the amount unit. */
                            Unit unit();
                            /** Converts this amount to another unit. */
                            Self to(Unit targetUnit);
                            /** Returns the total number of smallest units. */
                            long toBaseUnits();
                        }
                        """),
                MappingSources.type("nativeToken", "ExchangeRate", """
                        /** A native-token exchange rate in US dollar cents. */
                        public interface ExchangeRate {
                            /** Returns the expiration instant with zone information. */
                            ZonedDateTime expirationTime();
                            /** Returns the rate in US dollar cents. */
                            double exchangeRateInUsdCents();
                            /** Observes whether this rate has expired. */
                            boolean isExpired();
                        }
                        """, "java.time.ZonedDateTime"),
                MappingSources.type("nativeToken", "NativeTokenOperations", """
                        /** Body-free native-token conversion and time-observation contracts. */
                        public interface NativeTokenOperations {
                            /** Converts a token to another unit of its own unit family. */
                            <S extends NativeToken<S, U>, U extends NativeTokenUnit> S convert(S token, U targetUnit);
                            /** Returns a token amount in smallest units. */
                            long toBaseUnits(NativeToken<?, ?> token);
                            /** Observes exchange-rate expiration without defining clock policy. */
                            boolean isExpired(ExchangeRate exchangeRate);
                        }
                        """));
    }
}

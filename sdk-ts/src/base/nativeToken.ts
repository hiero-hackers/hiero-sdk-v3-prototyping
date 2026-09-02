import type { ZonedDateTime } from './ledger.js';

/**
 * A unit of a native token, either the smallest indivisible unit or a named multiple of it.
 */
export interface NativeTokenUnit {
    /** Display symbol of the unit (e.g. "ℏ") */
    readonly symbol: string;
    /** Number of base (smallest) units contained in one of this unit */
    readonly baseUnitFactor: bigint;
}

/**
 * An amount of a network's native token, expressed in a given unit.
 */
export interface NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
    /** Amount expressed in `unit` */
    readonly amount: bigint;
    /** The unit `amount` is expressed in */
    readonly unit: Unit;

    /**
     * Convert this amount to a different unit of the same token.
     * @param targetUnit - The unit to convert to.
     * @returns A new instance representing the same total value in the target unit.
     */
    to(targetUnit: Unit): Self;

    /**
     * Total amount expressed in base (smallest) units.
     * @returns The total number of base units as a bigint.
     */
    toBaseUnits(): bigint;
}

/**
 * Represents the exchange rate of a native token in USD cents.
 */
export interface ExchangeRate {
    /** Expiration time of the exchange rate */
    readonly expirationTime: ZonedDateTime;
    /** Exchange rate of the native token in USD cents */
    readonly exchangeRateInUsdCents: number;

    /**
     * Check if this exchange rate has expired.
     * @returns true if current time is past expirationTime.
     */
    isExpired(): boolean;
}

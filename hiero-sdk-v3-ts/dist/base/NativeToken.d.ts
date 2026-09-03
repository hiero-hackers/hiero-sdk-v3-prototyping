export interface NativeTokenUnit {
    readonly symbol: string;
    readonly baseUnitFactor: bigint;
}
export interface NativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> {
    readonly amount: bigint;
    readonly unit: Unit;
    to(targetUnit: Unit): Self;
    toBaseUnits(): bigint;
}
export interface ExchangeRate {
    readonly expirationTime: Date;
    readonly exchangeRateInUsdCents: number;
    isExpired(): boolean;
}

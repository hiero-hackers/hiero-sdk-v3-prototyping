import type { NativeTokenUnit, NativeToken, ExchangeRate } from '../nativeToken.js';
import type { ZonedDateTime } from '../ledger.js';

export abstract class BaseNativeTokenUnit implements NativeTokenUnit {
    constructor(
        public readonly symbol: string,
        public readonly baseUnitFactor: bigint
    ) {
        Object.freeze(this);
    }
}

export abstract class BaseNativeToken<Self extends NativeToken<Self, Unit>, Unit extends NativeTokenUnit> implements NativeToken<Self, Unit> {
    constructor(
        public readonly amount: bigint,
        public readonly unit: Unit
    ) {}

    abstract to(targetUnit: Unit): Self;

    toBaseUnits(): bigint {
        return this.amount * this.unit.baseUnitFactor;
    }
}

export class ExchangeRateImpl implements ExchangeRate {
    constructor(
        public readonly expirationTime: ZonedDateTime,
        public readonly exchangeRateInUsdCents: number
    ) {
        Object.freeze(this);
    }

    isExpired(): boolean {
        return new Date().getTime() > this.expirationTime.getTime();
    }
}

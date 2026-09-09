import { describe, it, expect } from 'vitest';
import type { NativeTokenUnit, NativeToken } from '../src/base/nativeToken.js';
import { BaseNativeTokenUnit, BaseNativeToken, ExchangeRateImpl } from '../src/base/internal/nativeToken-impl.js';

// Dummy implementation for testing abstractions
class TestTokenUnit extends BaseNativeTokenUnit {
    static readonly BASE = new TestTokenUnit('tb', 1n);
    static readonly KILO = new TestTokenUnit('tk', 1000n);
}

class TestToken extends BaseNativeToken<TestToken, TestTokenUnit> {
    constructor(amount: bigint, unit: TestTokenUnit) {
        super(amount, unit);
        Object.freeze(this);
    }

    to(targetUnit: TestTokenUnit): TestToken {
        // Convert to base, then divide by target
        const baseAmount = this.toBaseUnits();
        return new TestToken(baseAmount / targetUnit.baseUnitFactor, targetUnit);
    }
}

describe('NativeToken API', () => {
    describe('NativeTokenUnit & NativeToken', () => {
        it('should calculate base units correctly', () => {
            const token = new TestToken(5n, TestTokenUnit.KILO);
            expect(token.toBaseUnits()).toBe(5000n);
        });

        it('should convert to different units', () => {
            const token = new TestToken(5000n, TestTokenUnit.BASE);
            const kiloToken = token.to(TestTokenUnit.KILO);
            
            expect(kiloToken.amount).toBe(5n);
            expect(kiloToken.unit).toBe(TestTokenUnit.KILO);
            expect(kiloToken.toBaseUnits()).toBe(5000n);
        });
    });

    describe('ExchangeRate', () => {
        it('should correctly identify non-expired rates', () => {
            const futureDate = new Date(new Date().getTime() + 10000);
            const rate = new ExchangeRateImpl(futureDate, 12.5);
            expect(rate.exchangeRateInUsdCents).toBe(12.5);
            expect(rate.isExpired()).toBe(false);
        });

        it('should correctly identify expired rates', () => {
            const pastDate = new Date(new Date().getTime() - 10000);
            const rate = new ExchangeRateImpl(pastDate, 12.5);
            expect(rate.isExpired()).toBe(true);
        });
    });
});

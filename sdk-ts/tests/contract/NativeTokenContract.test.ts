import { describe, it, expect } from 'vitest';
import { NativeToken } from '../../src/index.js';
import type { NativeTokenUnit } from '../../src/index.js';

describe('NativeTokenContract', () => {
    interface CurrencyUnit extends NativeTokenUnit {
        readonly symbol: string;
        readonly baseUnitFactor: bigint;
    }

    const TINYBAR: CurrencyUnit = Object.freeze({
        symbol: 'tℏ',
        baseUnitFactor: 1n
    });

    const MICROBAR: CurrencyUnit = Object.freeze({
        symbol: 'μℏ',
        baseUnitFactor: 100n
    });

    const HBAR: CurrencyUnit = Object.freeze({
        symbol: 'ℏ',
        baseUnitFactor: 100_000_000n
    });

    const KILOBAR: CurrencyUnit = Object.freeze({
        symbol: 'kℏ',
        baseUnitFactor: 100_000_000_000n
    });

    class CurrencyToken extends NativeToken<CurrencyToken, CurrencyUnit> {
        constructor(amount: bigint | number, unit: CurrencyUnit) {
            super(amount, unit);
        }

        protected create(amount: bigint, unit: CurrencyUnit): CurrencyToken {
            return new CurrencyToken(amount, unit);
        }
    }

    describe('Scenario: Unit state is observed', () => {
        it('returns immutable symbol and baseUnitFactor', () => {
            expect(HBAR.symbol).toBe('ℏ');
            expect(HBAR.baseUnitFactor).toBe(100_000_000n);
            expect(TINYBAR.symbol).toBe('tℏ');
            expect(TINYBAR.baseUnitFactor).toBe(1n);
        });
    });

    describe('Scenario: Token amount is observed', () => {
        it('returns immutable amount as bigint and its associated unit', () => {
            const token = new CurrencyToken(10, HBAR);

            expect(token.amount).toBe(10n);
            expect(token.unit).toBe(HBAR);
            expect(Object.isFrozen(token)).toBe(true);

            // Attempting to modify properties is ignored or throws in strict mode
            // @ts-expect-error read-only property
            expect(() => { (token as any).amount = 20n; }).toThrow();
        });
    });

    describe('Scenario: Token amount is converted to a different unit', () => {
        it('converts between units accurately and returns concrete subclass instance', () => {
            const fiveHbar = new CurrencyToken(5, HBAR);
            const inTinybars = fiveHbar.to(TINYBAR);

            expect(inTinybars).toBeInstanceOf(CurrencyToken);
            expect(inTinybars.amount).toBe(500_000_000n);
            expect(inTinybars.unit).toBe(TINYBAR);

            const inMicrobars = fiveHbar.to(MICROBAR);
            expect(inMicrobars.amount).toBe(5_000_000n);
            expect(inMicrobars.unit).toBe(MICROBAR);

            const inKilobars = new CurrencyToken(2000, HBAR).to(KILOBAR);
            expect(inKilobars.amount).toBe(2n);
            expect(inKilobars.unit).toBe(KILOBAR);
        });

        it('performs integer division when converting to a larger unit', () => {
            const smallAmount = new CurrencyToken(250, TINYBAR);
            const inMicrobars = smallAmount.to(MICROBAR);

            // 250 / 100 = 2 (integer division)
            expect(inMicrobars.amount).toBe(2n);
            expect(inMicrobars.unit).toBe(MICROBAR);
        });
    });

    describe('Scenario: Base units are calculated', () => {
        it('calculates total base units for positive amounts', () => {
            const token = new CurrencyToken(7, HBAR);
            expect(token.toBaseUnits()).toBe(700_000_000n);
        });

        it('calculates total base units for zero amount', () => {
            const zeroToken = new CurrencyToken(0, HBAR);
            expect(zeroToken.toBaseUnits()).toBe(0n);
        });

        it('calculates total base units for negative amounts', () => {
            const negativeToken = new CurrencyToken(-3, HBAR);
            expect(negativeToken.toBaseUnits()).toBe(-300_000_000n);
        });

        it('handles large 64-bit amounts exceeding 2^53 without precision loss', () => {
            // 50 billion HBAR in tinybars = 50_000_000_000 * 100_000_000 = 5 * 10^18 (well above 2^53 ≈ 9 * 10^15)
            const fiftyBillion = new CurrencyToken(50_000_000_000n, HBAR);
            expect(fiftyBillion.toBaseUnits()).toBe(5_000_000_000_000_000_000n);
        });
    });

    describe('Validation checks', () => {
        it('rejects null or undefined amount', () => {
            // @ts-expect-error null amount
            expect(() => new CurrencyToken(null as any, HBAR)).toThrow(TypeError);
            // @ts-expect-error undefined amount
            expect(() => new CurrencyToken(undefined as any, HBAR)).toThrow(TypeError);
        });

        it('rejects non-integer number amount', () => {
            expect(() => new CurrencyToken(3.14, HBAR)).toThrow(TypeError);
        });

        it('rejects unsafe integer numbers', () => {
            expect(() => new CurrencyToken(Number.MAX_SAFE_INTEGER + 10, HBAR)).toThrow(RangeError);
        });

        it('rejects null or undefined unit', () => {
            // @ts-expect-error null unit
            expect(() => new CurrencyToken(10, null as any)).toThrow(TypeError);
            // @ts-expect-error undefined unit
            expect(() => new CurrencyToken(10, undefined as any)).toThrow(TypeError);
        });

        it('rejects non-positive baseUnitFactor', () => {
            const invalidUnit: CurrencyUnit = { symbol: 'inv', baseUnitFactor: 0n };
            expect(() => new CurrencyToken(10, invalidUnit)).toThrow(RangeError);

            const negativeUnit: CurrencyUnit = { symbol: 'neg', baseUnitFactor: -5n };
            expect(() => new CurrencyToken(10, negativeUnit)).toThrow(RangeError);
        });

        it('rejects null or undefined targetUnit in to()', () => {
            const token = new CurrencyToken(10, HBAR);
            // @ts-expect-error null targetUnit
            expect(() => token.to(null as any)).toThrow(TypeError);
            // @ts-expect-error undefined targetUnit
            expect(() => token.to(undefined as any)).toThrow(TypeError);
        });

        it('rejects non-positive baseUnitFactor in targetUnit in to()', () => {
            const token = new CurrencyToken(10, HBAR);
            const zeroUnit: CurrencyUnit = { symbol: 'zero', baseUnitFactor: 0n };
            expect(() => token.to(zeroUnit)).toThrow(RangeError);
        });
    });
});
